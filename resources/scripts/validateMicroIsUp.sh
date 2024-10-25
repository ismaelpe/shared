#!/bin/bash

#Parametros de entrada

abort_and_help(){
  echo "validateMicroIsUp.sh [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-V version ] [-i BODY ]"
  exit 1
}


TIMEOUT_SECONDS=120
TIMEOUT_ITERATIONS=8

PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
OUTPUT_DATA_FILE="${PATH_LOCAL}/data/result.rslt"


PROXY='false'
MICRO_URL='NA'


if [ -f "$LOG_FILE" ]; then   
    rm "$LOG_FILE"
fi
if [ -f "$OUTPUT_DATA_FILE" ]; then   
    rm "$OUTPUT_DATA_FILE"
fi

while getopts ":l:U:u:v:i:A:a:I:V:T:C:E:M:B:t:s:S:p:h:k:e:Z:c:f:" options; do        
    case "${options}" in 
         h) MICRO_URL=${OPTARG}
            ;;       
         p) PROXY=${OPTARG}
            ;;
         *) ;;
    esac
done


if [ "$MICRO_URL" = 'NA' ]; then
    abort_and_help
fi


INIT_TOTAL_TIME=$(date +%s)


CURL_ARGS=("$PATH_LOCAL"/curlUtils.sh)
CURL_ARGS+=(-u "${MICRO_URL}")
CURL_ARGS+=(-v GET)
CURL_ARGS+=(-p "${PROXY}")

#Debe Reintentar
CURL_ARGS+=(-f 2)
#Numero de reintentos
CURL_ARGS+=(-b 2)
#Delay entre peticiones
CURL_ARGS+=(-w 15)


CONTENT_TO_OUTPUT=''
RESULT_FINAL=0
MESSAGE_TO_OUTPUT=''

contador=0
termina=$TIMEOUT_ITERATIONS
RESULT_FINAL_OUTPUT=2
INIT_TIME=$(date +%s)
DURATION=0



while [ $termina -ge $contador ] && [ $RESULT_FINAL_OUTPUT -eq 2 ] && [ $DURATION -lt $TIMEOUT_SECONDS ]
do
    RESULT=$("${CURL_ARGS[@]}")
    RC_STATUS=$?

    ACTUAL_TIME=$(date +%s)
    DURATION=$(( "$ACTUAL_TIME" - "$INIT_TIME" ))

    echo "Duracion:  ${DURATION} Contador: ${contador} Resultado Final: ${RESULT_FINAL_OUTPUT}" >> "$LOG_FILE"
    if [ "$RC_STATUS" -eq 0 ]; then
        RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
        CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
        if [ "$RESULT_FINAL" -eq 200 ]; then
            RESULT_FINAL_OUTPUT=1
            RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
        else
            RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
            sleep 45s
            (( contador++ ))
        fi        
    else
        RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
        sleep 45s
        (( contador++ ))
    fi
done

if [ "$RESULT_FINAL_OUTPUT" -eq 1 ]; then
    RESULT_FINAL=0
fi 

JSON_OUTPUT_STRING=$( jq -n \
                        --arg statusCode "$RESULT_FINAL" \
                        --arg content "$CONTENT_TO_OUTPUT" \
                        --arg message "$MESSAGE_TO_OUTPUT" \
                        '{statusCode: $statusCode, content: $content, message: $message}')

ACTUAL_TIME=$(date +%s)
DURATION=$(( "$ACTUAL_TIME" - "$INIT_TOTAL_TIME" ))
echo "DuracionTOTAL:  ${DURATION} Contador: ${contador} Resultado Final: ${RESULT_FINAL_OUTPUT}" >> "$LOG_FILE"

echo "$JSON_OUTPUT_STRING"

exit "$RESULT_FINAL"
