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
  echo "getLastDeployment.sh [-u ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-I ICP_APP_ID] [-V version ] "
  echo "getLastDeployment.sh [-u ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-I ICP_APP_ID] [-V version ] " > "$LOG_ERROR_FILE"
  exit 1
}

LOGGER_URL='NA'
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
LOG_ERROR_FILE="${PATH_LOCAL}/logs/error.log"
OUTPUT_DATA_FILE="${PATH_LOCAL}/data/result.rslt"

ICP_APP='NA'
VERSION='NA'

COMPONENT_TYPE='NA'
COMPONENT='NA'
PROXY='false'
STATUS_CODE='NA'
if [ -f "$OUTPUT_DATA_FILE" ]; then   
    rm "$OUTPUT_DATA_FILE"
fi

while getopts ":l:U:u:v:i:A:I:V:T:C:E:B:t:S:p:h:" options; do
    case "${options}" in          
         u) ICP_URL=${OPTARG}
            ;;
         l) LOGGER_URL=${OPTARG}
            ;;
         A) ICP_APP=${OPTARG}
            ;;
         V) VERSION=${OPTARG}
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

if [ "$VERSION" = 'NA' ] || [ "$ICP_APP" = 'NA' ] || [ "$ICP_URL" = 'NA' ] || 
[ "$LOGGER_URL" = 'NA' ]; then
    abort_and_help
fi

#RESULT=$("$PATH_LOCAL"/curlUtils.sh -u \'"${ICP_URL}"\' -v "${VERB}" -i "${BODY}" \
    #  -t 90 -f 2 -p true -b 2 -w 15 \
    #  -p "${PROXY}" \
    #  -h application_active:"${ICP_APP}" \
    #  -c 'Content-Type: application/json' \
    #  -m "${ICP_CERT}" \
    #  -n "${ICP_PASS}"
    #  )

CURL_ARGS=("$PATH_LOCAL"/sendCurlToICP.sh)
CURL_ARGS+=(-u "${ICP_URL}")
CURL_ARGS+=(-l "${LOGGER_URL}")

if [ "$STATUS_CODE" != 'NA' ]; then
   CURL_ARGS+=(-s "${STATUS_CODE}")
fi

#Usa del proxy
CURL_ARGS+=(-p "${PROXY}")
#Aplicacion activa
CURL_ARGS+=(-A "${ICP_APP}")

CURL_ARGS+=(-T "${COMPONENT_TYPE}")
CURL_ARGS+=(-C "${COMPONENT}")
CURL_ARGS+=(-E "${ENVIRONMENT}")
CURL_ARGS+=(-B "${BUILD}")
CURL_ARGS+=(-V "${VERSION}")
CURL_ARGS+=(-t "${TAG}")
CURL_ARGS+=(-s "200:300")

RESULT=$("${CURL_ARGS[@]}")
RC_STATUS=$?

echo $RC_STATUS >> "$LOG_FILE"
MESSAGE_TO_OUTPUT=''

if [ "$RC_STATUS" -eq 0 ]; then
##Es error tenemos que enviar el KPI con el error
   MESSAGE_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message'  )
   ZONE=$(echo "$MESSAGE_TO_OUTPUT" | jq -r '.[0].az' )
   MESSAGE_TO_OUTPUT=$(echo "$MESSAGE_TO_OUTPUT" | jq -r '.[0].values' )
   MESSAGE_TO_OUTPUT="$ZONE$MESSAGE_TO_OUTPUT"
   echo "${MESSAGE_TO_OUTPUT}" > "$OUTPUT_DATA_FILE"
else
   MESSAGE_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
   echo "${MESSAGE_TO_OUTPUT}" > "$LOG_ERROR_FILE"
fi

echo "$MESSAGE_TO_OUTPUT"
exit "$RC_STATUS"
