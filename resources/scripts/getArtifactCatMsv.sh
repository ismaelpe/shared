#!/bin/bash

#Parametros de entrada

abort_and_help(){
  echo "getArtifactCatMsv.sh [-i MEM_REQ:MEM_LIM:CPU_REQ:CPU_LIM ] [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-e ENVIRONMENT] [-V version ] "
  echo "getArtifactCatMsv.sh [-i MEM_REQ:MEM_LIM:CPU_REQ:CPU_LIM ] [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-e ENVIRONMENT] [-V version ] " > "$LOG_ERROR_FILE"
  exit 1
}

LOGGER_URL='NA'
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
LOG_ERROR_FILE="${PATH_LOCAL}/logs/error.log"
OUTPUT_DATA_FILE="${PATH_LOCAL}/data/result.rslt"
CATMSV_URL='NA'


VERSION='NA'


COMPONENT_TYPE='NA'
COMPONENT='NA'
PROXY='false'
ENVIRONMENT='ALL'


if [ -f "$LOG_FILE" ]; then   
    rm "$LOG_FILE"
fi

while getopts ":l:U:u:v:i:A:a:I:V:T:C:E:B:t:s:S:p:h:k:e:M:" options; do
        
    case "${options}" in          
         h) CATMSV_URL=${OPTARG}
            ;;
         l) LOGGER_URL=${OPTARG}
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
         e) ENVIRONMENT=${OPTARG}
            ;;
         M) MICRO=${OPTARG}
            ;;            
         *) ;;
    esac
done

if [ "$VERSION" != 'NA' ]; then
   MAJOR=$(echo "${VERSION}" | awk -F ':' '{ print $1 }')  
   MINOR=$(echo "${VERSION}" | awk -F ':' '{ print $2 }')
   FIX=$(echo "${VERSION}" | awk -F ':' '{ print $3 }')
fi

if [ "$ENVIRONMENT" = 'ALL' ] || [ "$CATMSV_URL" = 'NA' ] || 
[ "$LOGGER_URL" = 'NA' ]; then
    abort_and_help
fi

PATH_CATMSV="/app/${COMPONENT_TYPE^^}/${MICRO^^}"
#Comprovaremos si existe el componente
INIT_TIME=$(date +%s%N | cut -b1-13)
CURL_ARGS=("$PATH_LOCAL"/curlUtils.sh)
if [ -z "${tokenAbsis3}" ]; then
    echo "No existe token definido" >> "$LOG_FILE"   
else
    CURL_ARGS+=(-a "${tokenAbsis3}")
fi
CURL_ARGS+=(-p "${PROXY}")
CURL_ARGS+=(-t 15)

CURL_ARGS+=(-u "${CATMSV_URL}${PATH_CATMSV}")
CURL_ARGS+=(-v GET)
CURL_ARGS+=(-s '200:404')


RESULT=$("${CURL_ARGS[@]}")

RC_STATUS=$?
CONTENT_TO_OUTPUT=''
RESULT_FINAL=0
MESSAGE_TO_OUTPUT=''
RESULT_FINAL_SCRIPT=0

if [ "$RC_STATUS" -eq 0 ]; then
    RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
    CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
    echo "Los valores son de ${RESULT_TO_OUTPUT} ${CONTENT_TO_OUTPUT}" >> "$LOG_FILE" 
    if [ "$RESULT_TO_OUTPUT" -eq 200 ]; then
        #El elemento no existe. Tenemos recuperar que dar de alta el elemento
        echo "El componente ${COMPONENT_TYPE^^}/${COMPONENT^^} existe en el entorno" >> "$LOG_FILE"
        RESULT_FINAL_SCRIPT=0        
    else      
       RESULT_FINAL_SCRIPT=1
       RESULT_FINAL="$RESULT_TO_OUTPUT"
       MESSAGE_TO_OUTPUT="El componente NO ${COMPONENT_TYPE^^}/${COMPONENT^^} existe"
    fi
else 
    RESULT_FINAL_SCRIPT=1
    RESULT_FINAL="$RC_STATUS"
    MESSAGE_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
    echo "${MESSAGE_TO_OUTPUT}" > "$LOG_ERROR_FILE"
fi


JSON_OUTPUT_STRING=$( jq -n \
                        --arg statusCode "$RESULT_FINAL" \
                        --arg content "$CONTENT_TO_OUTPUT" \
                        --arg message "$MESSAGE_TO_OUTPUT" \
                        '{statusCode: $statusCode, content: $content, message: $message}')

if [ "$RESULT_FINAL_SCRIPT" -eq 0 ]; then
   echo "El resultado del alta es correcta se procede a devolver el json con el componente creado o recuperado"  >> "$LOG_FILE"
   JSON_OUTPUT_STRING="${CONTENT_TO_OUTPUT}"
   echo "${JSON_OUTPUT_STRING}" > "$OUTPUT_DATA_FILE"
   STATUS_TYPE="SUCCESS"
   STATUS="OK"
   ERROR_CODE=""
else 
   echo "El resultado no es buen ${RESULT_FINAL}"
   STATUS_TYPE="FAIL"
   ERROR_CODE="${RESULT_FINAL}"
   STATUS="KO"
fi

END_TIME=$(date +%s%N | cut -b1-13)

DURATION_TIME=$((END_TIME-INIT_TIME))

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
     "operation":"CATMSV_HTTP_CALL","subOperation":"CALL","statusType":$statusType, 
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

if [ "$STATUS" = "OK" ]; then
    echo "Correcto" >> "$LOG_FILE"
else
    echo "$JSON_OUTPUT_STRING"  > "$LOG_ERROR_FILE"
fi

echo "$JSON_OUTPUT_STRING"
exit "$RESULT_FINAL_SCRIPT"
