#!/bin/bash

#Parametros de entrada
#h URL de ICP
#l URL de KPIS
#v VERB de la accion
#i BODY de la peticion
#A Aplicacion parent de ICP
#V Version con el formato major:minor:fix
#T Tipo de aplicacion openservices
#C Componente de open services
#E Entorno
#B Build
#t Tag de jenkins
#p Debe usar proxy
#s http status permitidos

abort_and_help(){
  echo "sendCurlToICP.sh [-u ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-I ICP_APP_ID] [-V version ] "
  exit 1
}

LOGGER_URL='NA'
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
VERB='GET'
BODY=''
INIT_TIME=$(date +%s%N | cut -b1-13)
ICP_APP='NA'
VERSION='NA'
MAJOR=1
MINOR=0
FIX=0
COMPONENT_TYPE='NA'
COMPONENT='NA'
PROXY='false'
STATUS_CODE='NA'

DONT_ADD='NA'

while getopts ":l:U:u:v:i:A:I:V:T:C:E:B:t:s:S:p:h:d:M:" options; do
    case "${options}" in          
         u) ICP_URL=${OPTARG}
            ;;
         l) LOGGER_URL=${OPTARG}
            ;;
         v) VERB=${OPTARG}
            ;;
         i) BODY=${OPTARG}
            ;;
         A) ICP_APP=${OPTARG}
            ;;
         V) VERSION=${OPTARG}
            ;;
         d) DONT_ADD=${OPTARG}
            echo "Nos ha llegado el -d ${OPTARG}"  >> "$LOG_FILE"
            ;;
         T) COMPONENT_TYPE=${OPTARG}
            ;;
         C) COMPONENT=${OPTARG}
            ;;
         E) ENVIRONMENT=${OPTARG}
            ;;
         B) BUILD=${OPTARG}  
            ;;
         t) TAG=${OPTARG}
            ;;
         p) PROXY=${OPTARG}
            ;;
         s) STATUS_CODE=${OPTARG}
            ;;
         *) ;;
    esac
done
if [ "$VERSION" != 'NA' ]; then
   MAJOR=$(echo "${VERSION}" | awk -F ':' '{ print $1 }')  
   MINOR=$(echo "${VERSION}" | awk -F ':' '{ print $2 }')
   FIX=$(echo "${VERSION}" | awk -F ':' '{ print $3 }')
fi

if [ "$VERSION" = 'NA' ] || [ "$ICP_APP" = 'NA' ] || [ "$ICP_URL" = 'NA' ] || 
[ "$LOGGER_URL" = 'NA' ]; then
    abort_and_help
fi
END_TIME=$(date +%s%N | cut -b1-13)

DURATION_TIME=$((END_TIME-INIT_TIME))
STATUS=""
STATUS_TYPE=""
ERROR_CODE=""

#RESULT=$("$PATH_LOCAL"/curlUtils.sh -u \'"${ICP_URL}"\' -v "${VERB}" -i "${BODY}" \
    #  -t 90 -f 2 -p true -b 2 -w 15 \
    #  -p "${PROXY}" \
    #  -h application_active:"${ICP_APP}" \
    #  -c 'Content-Type: application/json' \
    #  -m "${ICP_CERT}" \
    #  -n "${ICP_PASS}"
    #  )

CURL_ARGS=("$PATH_LOCAL"/curlUtils.sh)
CURL_ARGS+=(-u "${ICP_URL}")
CURL_ARGS+=(-v "${VERB}")

if [ "$DONT_ADD" != 'NA' ]; then
   CURL_ARGS+=(-d 1)
fi
if [ "$BODY" != '' ]; then
   CURL_ARGS+=(-i "${BODY}")
fi
if [ "$STATUS_CODE" != 'NA' ]; then
   CURL_ARGS+=(-s "${STATUS_CODE}")
fi
#Timeout
CURL_ARGS+=(-t 90)
#Debe Reintentar
CURL_ARGS+=(-f 2)
#Numero de reintentos
CURL_ARGS+=(-b 2)
#Delay entre peticiones
CURL_ARGS+=(-w 15)
#Usa del proxy
CURL_ARGS+=(-p "${PROXY}")
#Aplicacion activa
CURL_ARGS+=(-h application_active:"${ICP_APP}")
CURL_ARGS+=(-c 'Content-Type:application/json')
#Certificados
CURL_ARGS+=(-m "${ICP_CERT}")
CURL_ARGS+=(-n "${ICP_PASS}")

echo "${CURL_ARGS[@]}"  >> "$LOG_FILE"

RESULT=$("${CURL_ARGS[@]}")
RC_STATUS=$?

echo $RC_STATUS >> "$LOG_FILE"
MESSAGE_TO_OUTPUT=''
RESULT_TO_OUTPUT=''
CONTENT_TO_OUTPUT=''
if [ "$RC_STATUS" -eq 0 ]; then
##Es error tenemos que enviar el KPI con el error
   STATUS_TYPE="SUCCESS"  
   STATUS="OK"
   RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
   MESSAGE_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
   CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.content' )
else
##Es correcto, ha funcionado la peticiones
   STATUS_TYPE="FAIL"
   STATUS="KO" 
   ERROR_CODE=$(echo "$RESULT" | jq -r '.statusCode' )
   RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
   MESSAGE_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
   CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.content' )
fi

TIMESTAMP=$(date '+%Y-%m-%dT%H:%M:%S.000Z' )
KPI_EVENT=$(jq --null-input \
   --arg timestamp "${TIMESTAMP}" \
   --arg stage "${STAGE}" \
   --arg componentType "${COMPONENT_TYPE}" \
   --arg component "${COMPONENT}" \
   --arg major "${MAJOR}" \
   --arg minor "${MINOR}" \
   --arg patch "${FIX}" \
   --arg build "${BUILD}" \
   --arg buildTag "${TAG}" \
   --arg environment "${ENVIRONMENT}" \
   --arg duration "${DURATION_TIME}" \
   --arg statusType "${STATUS_TYPE}" \
   --arg errorCode "${ERROR_CODE}" \
   --arg status "${STATUS}" \
   '[{"space_name_str":"SRV_PRO","app":{"clase":"logs"},"loglevel":"INFO","timestamp": $timestamp, "aplicacion":"absis-alm", 
     "type":"LIFE_CYCLE","stage": $stage, "componentType":$componentType, "component":$component, 
     "major":$major,"minor":$minor,"patch":$patch,"build",$build,"buildTag":$buildTag, 
     "environment":$environment,"features":[],"lines":"0","qualityLevel":"","almStage":"UNDEFINED", 
     "operation":"ICPAPI_HTTP_CALL","subOperation":"CALL","statusType":$statusType, 
     "errorCode": $errorCode,"duration":$duration,"status":$status
   }]'
   )

CURL_ARGS=("$PATH_LOCAL"/openServicesKpi.sh)
CURL_ARGS+=(-p "${PROXY}")
CURL_ARGS+=(-u "${LOGGER_URL}")
CURL_ARGS+=(-i "${KPI_EVENT}")
RESULT=$("${CURL_ARGS[@]}")

RC_STATUS=$?
echo "Result KPI Event ${RC_STATUS}" >> "$LOG_FILE"

RESULT_SCRIPT=0
if [ "$STATUS" = "OK" ]; then
    echo "Correcto" >> "$LOG_FILE"
else
    echo "ERROR en la peticion el resultado es de $RESULT"  >> "$LOG_FILE"
    RESULT_SCRIPT=1
fi

JSON_OUTPUT_STRING=$( jq -n \
                        --arg statusCode "$RESULT_TO_OUTPUT" \
                        --arg content "$CONTENT_TO_OUTPUT" \
                        --arg message "$MESSAGE_TO_OUTPUT" \
                        '{statusCode: $statusCode, content: $content, message: $message}')

echo "$JSON_OUTPUT_STRING"
exit "$RESULT_SCRIPT"
