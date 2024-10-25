#!/bin/bash

#Parametros de entrada

abort_and_help(){
  echo "getArtifactICP.sh [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-e ENVIRONMENT] [-V version ] "
  echo "getArtifactICP.sh [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-e ENVIRONMENT] [-V version ] " > "$LOG_ERROR_FILE"
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
MAJOR=1

COMPONENT_TYPE='NA'
COMPONENT='NA'
PROXY='false'
ENVIRONMENT='ALL'

K8S='icp'

if [ -f "$LOG_FILE" ]; then   
    rm "$LOG_FILE"
fi
if [ -f "$OUTPUT_DATA_FILE" ]; then   
    rm "$OUTPUT_DATA_FILE"
fi

while getopts ":l:U:u:v:i:A:a:I:V:T:C:E:B:t:s:S:p:h:k:e:M:" options; do
        
    case "${options}" in          
         h) ICP_URL=${OPTARG}
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
         k) K8S=${OPTARG}
            ;;
         e) ENVIRONMENT=${OPTARG}
            ;;
         M) MICRO=${OPTARG}
            ;;
         *) ;;
    esac
done

if [ "$ENVIRONMENT" = 'ALL' ] || [ "$ICP_APP" = 'NA' ] || [ "$ICP_URL" = 'NA' ] || 
[ "$LOGGER_URL" = 'NA' ]; then
    abort_and_help
fi

if [ "$VERSION" != 'NA' ]; then
   MAJOR=$(echo "${VERSION}" | awk -F ':' '{ print $1 }')  
fi
COMPONENT_ICP=${MICRO^^}${MAJOR}
if [ "$K8S" != 'ocp' ]; then
    APLICATION_TYPE="PCLD"
else
    #Deberia ser de ocp
    APLICATION_TYPE="PCLD_MIGRATED"
fi
PATH_ICP="/api/publisher/v2/api/application/${APLICATION_TYPE}/${ICP_APP}/component/${COMPONENT_ICP}"
#/api/publisher/v2/api/application/{applicationType}/{applicationName}/component/{componentName}
#Comprovaremos si existe el componente

BASE_CURL_ARGS=("$PATH_LOCAL"/sendCurlToICP.sh)
BASE_CURL_ARGS+=(-p "${PROXY}")
BASE_CURL_ARGS+=(-A "${ICP_APP}")
BASE_CURL_ARGS+=(-V "${VERSION}")
BASE_CURL_ARGS+=(-T "${COMPONENT_TYPE}")
BASE_CURL_ARGS+=(-C "${COMPONENT}")
BASE_CURL_ARGS+=(-E "${ENVIRONMENT}")
BASE_CURL_ARGS+=(-B "${BUILD}")
BASE_CURL_ARGS+=(-l "${LOGGER_URL}")
BASE_CURL_ARGS+=(-t "${TAG}")

CURL_ARGS=("${BASE_CURL_ARGS[@]}")
CURL_ARGS+=(-u "${ICP_URL}${PATH_ICP}")
CURL_ARGS+=(-v GET)
CURL_ARGS+=(-s '200:500')

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
    if [ "$RESULT_TO_OUTPUT" -eq 404 ] || [ "$RESULT_TO_OUTPUT" -eq 500 ]; then
        #El elemento no existe. Tenemos recuperar que dar de alta el elemento
        echo "El componente ${COMPONENT} ${MAJOR} no existe en el entorno" >> "$LOG_FILE"
        RESULT_FINAL_SCRIPT=2
        CONTENT_TO_OUTPUT="Componente no existente en el sistema"
    else      
        if [ "$RESULT_TO_OUTPUT" -eq 201 ] || [ "$RESULT_TO_OUTPUT" -eq 200 ]; then
           ID_COMPONENT=$(echo "$CONTENT_TO_OUTPUT" | jq -r '.id' )
           echo "El componentes si que existe ${COMPONENT} ${MAJOR} existe en el entorno. El ID es de ${ID_COMPONENT}" >> "$LOG_FILE"
           RESULT_FINAL_SCRIPT=0
        else
           echo "Error en la consulta del componente no podemos continuar " >> "$LOG_FILE"
           RESULT_FINAL=$RESULT_TO_OUTPUT      
           RESULT_FINAL_SCRIPT=1     
        fi
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
else 
   if [ "$RESULT_FINAL_SCRIPT" -eq 2 ]; then
      RESULT_FINAL_SCRIPT=0
   fi
   echo "El resultado no es buen ${RESULT_FINAL}"
   echo "${JSON_OUTPUT_STRING}" > "$LOG_ERROR_FILE"
fi

echo "$JSON_OUTPUT_STRING"
exit "$RESULT_FINAL_SCRIPT"
