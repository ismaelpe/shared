#!/bin/bash

#Parametros de entrada

abort_and_help(){
  echo "buildArtifactICP.sh [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-V version ] [-i BODY ]"
  exit 1
}

LOGGER_URL='NA'
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
LOG_ERROR_FILE="${PATH_LOCAL}/logs/error.log"
OUTPUT_DATA_FILE="${PATH_LOCAL}/data/result.rslt"
BODY='NA'

ICP_APP='NA'
VERSION='NA'
MAJOR=1

COMPONENT_TYPE='NA'
COMPONENT='NA'
PROXY='false'

K8S='icp'

GROUP_ID='NA'
VERSION_ARTIFACT='NA'
ARTIFACT_ID='NA'
VERSION_IMAGE=''
ADDITIONAL_BUILD_PARAMS='NA'


if [ -f "$LOG_FILE" ]; then   
    rm "$LOG_FILE"
fi
if [ -f "$OUTPUT_DATA_FILE" ]; then   
    rm "$OUTPUT_DATA_FILE"
fi

MICRO='NA'

startTime=$(date +%s)
while getopts ":l:U:u:v:i:A:a:I:V:T:C:E:M:B:t:s:S:p:h:k:e:" options; do
        
    case "${options}" in          
         h) ICP_URL=${OPTARG}
            ;;
         l) LOGGER_URL=${OPTARG}
            ;;
         i) BODY=${OPTARG}
            ;;
         A) ICP_APP=${OPTARG}
            ;;          
         V) VERSION=${OPTARG}
            ;;
         M) MICRO=${OPTARG}
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
         *) ;;
    esac
done

if [ "$ICP_APP" = 'NA' ] || [ "$ICP_URL" = 'NA' ] || 
[ "$LOGGER_URL" = 'NA' ] || [ "$BODY" = 'NA' ]; then
    abort_and_help
fi

if [ "$BODY" != 'NA' ]; then
    GROUP_ID=$(echo "${BODY}" | awk -F ':' '{ print $1 }')  
    VERSION_ARTIFACT=$(echo "${BODY}" | awk -F ':' '{ print $2 }') 
    ARTIFACT_ID=$(echo "${BODY}" | awk -F ':' '{ print $3 }') 
    ADDITIONAL_BUILD_PARAMS=$(echo "${BODY}" | awk -F ':' '{ print $4 }')
    VERSION_IMAGE=$(echo "${BODY}" | awk -F ':' '{ print $5 }')
    if [ "${ADDITIONAL_BUILD_PARAMS}" = 'NA' ]; then
        ADDITIONAL_BUILD_PARAMS=''
    fi
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

EXTRA_ARGS="GROUP_ID=$GROUP_ID,VERSION_ARTIFACT=$VERSION_ARTIFACT,ARTIFACT_ID=$ARTIFACT_ID$ADDITIONAL_BUILD_PARAMS"
#Tenemos que dar de alta los recursos
JSON_OUTPUT_STRING=$( jq -n \
            --arg extraArgs "$EXTRA_ARGS" \
            --arg versionImage "$VERSION_IMAGE" \
            '{"extraArgs": $extraArgs,"version":$versionImage }')

PATH_ICP="/api/publisher/v1/api/application/${APLICATION_TYPE}/${ICP_APP}/component/${COMPONENT_ICP}/build"
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
CURL_ARGS+=(-i "${JSON_OUTPUT_STRING}")
CURL_ARGS+=(-v POST)

#Debe Reintentar
CURL_ARGS+=(-f 2)
#Numero de reintentos
CURL_ARGS+=(-b 2)
#Delay entre peticiones
CURL_ARGS+=(-w 15)

RESULT=$("${CURL_ARGS[@]}")
RC_STATUS=$?
CONTENT_TO_OUTPUT=''
RESULT_FINAL=0
MESSAGE_TO_OUTPUT=''

if [ "$RC_STATUS" -eq 0 ]; then
    RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
    CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
    
    echo "Los valores son de ${RESULT_TO_OUTPUT} ${CONTENT_TO_OUTPUT}" >> "$LOG_FILE" 
    if [ "$RESULT_TO_OUTPUT" -ge 200 ] && [ "$RESULT_TO_OUTPUT" -lt 300 ]; then
        #El elemento no existe. Tenemos recuperar que dar de alta el elemento
        ID_BUILD=$(echo "$CONTENT_TO_OUTPUT" | jq -r '.id' )
        echo "Build generado ${COMPONENT} ${MAJOR} con el siguiente ID ${ID_BUILD}" >> "$LOG_FILE"
        #Tenemos que empezar a iterar y a hacer pulling hasta tener un OK o un NOK
        CURL_ARGS=("${BASE_CURL_ARGS[@]}")
        PATH_ICP="${PATH_ICP}/${ID_BUILD}"     
        CURL_ARGS+=(-v GET)
        CURL_ARGS+=(-u "${ICP_URL}${PATH_ICP}")
        #Debe Reintentar
        CURL_ARGS+=(-f 2)
        #Numero de reintentos
        CURL_ARGS+=(-b 2)
        #Delay entre peticiones
        CURL_ARGS+=(-w 15)
        contador=0
        termina=15
        while [ $termina -ge $contador ]
        do
            sleep 45s
            (( contador++ ))

            RESULT=$("${CURL_ARGS[@]}")            
            RC_STATUS=$?
            if [ "$RC_STATUS" -eq 0 ]; then
                RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
                if [ "$RESULT_FINAL" -ge 200 ] && [ "$RESULT_FINAL" -lt 300 ]; then
                    #Tenemos que evualuar la respuesta y segun ella proceder
                    CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
                    STATUS=$(echo "$CONTENT_TO_OUTPUT" | jq -r '.status' )
                    if [ "$STATUS" = 'OK' ]; then
                        echo "Peticion OK " >> "$LOG_FILE"
                        RESULT_FINAL=0
                        break
                    elif [ "$STATUS" = 'NOK' ]; then
                        RESULT_FINAL=1
                        MESSAGE_TO_OUTPUT="El BUILD NOK ${ID_BUILD} para el component ${COMPONENT_ICP}"
                        echo "Peticion KO. El status es de:  $STATUS" >> "$LOG_FILE"
                        #Provaremos a setter el MESSAGE con el LOG del build
                        PATH_ICP="/api/publisher/v1/api/application/${APLICATION_TYPE}/${ICP_APP}/component/${COMPONENT_ICP}/build/${ID_BUILD}/logs?initialByte=1"
                        CURL_ARGS=("${BASE_CURL_ARGS[@]}")  
                        CURL_ARGS+=(-v GET)
                        CURL_ARGS+=(-u "${ICP_URL}${PATH_ICP}")
                        #Debe Reintentar
                        CURL_ARGS+=(-d 1)

                        echo "${CURL_ARGS[@]}"  >> "$LOG_FILE"

                        RESULT=$("${CURL_ARGS[@]}")
                                 
                        RC_STATUS=$? >> "$LOG_FILE"
                        if [ "$RC_STATUS" -eq 0 ]; then
                            RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
                        fi
                        break
                    else 
                        endTime=$(date +%s)
                        durationTime=$((endTime-startTime))
                        RESULT_FINAL=1
                        CONTENT_TO_OUTPUT="El BUILD no ha finalizado ${STATUS}. Duration ${durationTime}. El build es ${ID_BUILD}. "
                        echo "Peticion WAITING ${STATUS}." >> "$LOG_FILE"
                    fi
                fi
            else
                RESULT_FINAL=$RC_STATUS
                CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
                break
            fi

        done
    else      
        echo "Error al enviar el build de ${COMPONENT} ${MAJOR}" >> "$LOG_FILE"    
        RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
        RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
        CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )         
    fi
else 
    RESULT_FINAL="$RC_STATUS"
    MESSAGE_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
fi


JSON_OUTPUT_STRING=$( jq -n \
                        --arg statusCode "$RESULT_FINAL" \
                        --arg content "$CONTENT_TO_OUTPUT" \
                        --arg message "$MESSAGE_TO_OUTPUT" \
                        '{statusCode: $statusCode, content: $content, message: $message}')


if [ "$RESULT_FINAL" -eq 0 ]; then
   echo "El resultado del build es correcto se procede a devolver el json con el componente creado o recuperado"  >> "$LOG_FILE"
   JSON_OUTPUT_STRING="${CONTENT_TO_OUTPUT}"
   echo "${JSON_OUTPUT_STRING}" > "$OUTPUT_DATA_FILE"
else 
   echo "${CONTENT_TO_OUTPUT}" > "$LOG_ERROR_FILE"
   echo "El resultado no es bueno ${RESULT_FINAL}" >> "$LOG_FILE" 
fi 
echo "$JSON_OUTPUT_STRING"

exit "$RESULT_FINAL"
