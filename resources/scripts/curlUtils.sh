#!/bin/bash

#Parametros de entrada
##-m Certificado
##-n Password certificado 
##-a Cabecera con token autorizacion
##-h cabecera
##-f Reintentar
##-u URL destino
##-p Debe usar proxy
##-w Delay entre reintentos
##-b Numero reintentos
##-r Host del proxy
##-v Verbo de la peticion
##-t Timeout
##-s Rango de status code permitidos
##-x Verbose
##-c Content type
##-i Input Data

validateReturnCode(){
    STATUSC=$1
    if [ $STATUSC -eq 58 ] || [ $STATUSC -eq 28 ] || [ $STATUSC -eq 16 ] || [ $STATUSC -eq 52 ] || [ $STATUSC -eq 56 ] || [ $STATUSC -eq 7 ] || [ $STATUSC -eq 2 ]; then
        return 1
    else 
        return 0
    fi
}

abort_and_help(){
  echo "curlUtils.sh [-s MinStatusCode:MaxStatusCode] [-t Timeout] [-r Proxy Host] [-v HTTP Verb] [-p true/false Tenemos que usar proxy] [-u curl URL] [-i input data] [-v http verb] [-r proxy value] [-x Verbose]"
  exit 1
}

URL="Void"
PROXY="false"
VERB="GET"
PROXY_HOST="10.113.10.13:8080"
TIMEOUT=30
MINSTATUSCODE=200
MAXSTATUSCODE=300
CONTENT_TYPE='Content-type:application/json'
VERBOSE_FLAG=false
INPUT_DATA=null
INPUT_DATA_FLAG=false
RESULT_SCRIPT=0
HEADER_AUTH=''
HEADER1=''
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
RETRY_DELAY=10
RETRY_NUM=0

RETRY_ON_CURL_ERROR=0
CERT='NA'
CERT_PASS='NA'

DONT_ADD=0

while getopts ":u:m:n:p:v:t:r:x:v:i:s:c:a:h:w:b:f:d:" options; do
    case "${options}" in
        m)  CERT=${OPTARG}
            ;;
        n)  CERT_PASS=${OPTARG}
            ;;
        a)  HEADER_AUTH="Authorization: Basic ${OPTARG}"  
            echo  "$HEADER_AUTH" >> "$LOG_FILE"
            ;;
        h)  HEADER1=${OPTARG}
            ;;
        f) 
            RETRY_ON_CURL_ERROR=${OPTARG}
            ;;
        u) 
            URL=${OPTARG}
            ;;
        p) 
            if [ "${OPTARG}" = "true" ]; then
              PROXY="true"
            fi
            ;;
        w)  RETRY_DELAY=${OPTARG}
            ;;
        b)  RETRY_NUM=${OPTARG}
            ;;
        v) 
            VERB=${OPTARG}
            ;;
        r) 
            PROXY_HOST=${OPTARG}
            ;;
        t) 
            TIMEOUT=${OPTARG}
            ;;
        s)  MINSTATUSCODE=$(echo "${OPTARG}" | awk -F ':' '{ print $1 }')
            MAXSTATUSCODE=$(echo "${OPTARG}" | awk -F ':' '{ print $2 }')
            echo "Min status ${MINSTATUSCODE} ${MAXSTATUSCODE}" >> "$LOG_FILE"
            ;;
        c) if [ "${OPTARG}" = "json" ]; then
              CONTENT_TYPE='Content-type:application/json'
           fi
           ;;
        x) VERBOSE_FLAG='true'
           ;;
        i) INPUT_DATA_FLAG=true
           INPUT_DATA="${OPTARG}"
           ;;
        d) DONT_ADD=1
           ;;
        *) echo "No sabemos opcion"
    esac
done

if [ "$URL" = "Void" ]; then
    abort_and_help
fi


FILE_OUTPUT="${PATH_LOCAL}/data/output.txt"
#Executing el log
echo "" > "$FILE_OUTPUT"

MESSAGE_REQUEST=''

CURL_ARGS=(curl)
CURL_ARGS+=(-s)
CURL_ARGS+=(-k --write-out '%{http_code}')
CURL_ARGS+=(--output "$FILE_OUTPUT")
CURL_ARGS+=(-X "$VERB")
if [ "$RETRY_NUM" -gt 0 ]; then
    CURL_ARGS+=(--retry "$RETRY_NUM" --retry-delay "$RETRY_DELAY")
fi
if [ $PROXY = "true" ]; then
    CURL_ARGS+=(--proxy "$PROXY_HOST")
else
    CURL_ARGS+=(--noproxy '*')
fi 
if [ $VERBOSE_FLAG = "true" ]; then
    CURL_ARGS+=(-vvv)
fi
if [ "$INPUT_DATA_FLAG" = "true" ]; then
    CURL_ARGS+=(-d "$INPUT_DATA")    
fi
if [ "$CERT_PASS" != 'NA' ] || [ "$CERT" != 'NA' ]; then
    CURL_ARGS+=(--cert "$CERT":"$CERT_PASS")   
fi

CURL_ARGS+=(--connect-timeout "$TIMEOUT")
CURL_ARGS+=(--header "$CONTENT_TYPE")
CURL_ARGS+=(--header accept:*/*)
if [ "$HEADER_AUTH" != "" ]; then
    CURL_ARGS+=(--header "$HEADER_AUTH")
fi
if [ "$HEADER1" != "" ]; then
    CURL_ARGS+=(--header "$HEADER1")
fi
CURL_ARGS+=("$URL")

echo "${CURL_ARGS[@]}" >> "$LOG_FILE"

REQUEST_NUM=0

while :; do
    echo "Executing " >> "$LOG_FILE"
    RESULT=$("${CURL_ARGS[@]}")
    RESULT_CURL=$?

    echo "Result CUrl ${RESULT_CURL}"  >> "$LOG_FILE"
    if [ $RETRY_ON_CURL_ERROR -gt 0 ] && [ $REQUEST_NUM -lt $RETRY_ON_CURL_ERROR ]; then
        validateReturnCode $RESULT_CURL
        SHOULD_WE_RETRY=$?
        if [ $SHOULD_WE_RETRY -eq 0 ]; then
          break
        else
          echo "Retry number  $REQUEST_NUM Will wait 20s"  >> "$LOG_FILE"
          sleep 20s
        fi
    else   
       break
    fi
    (( REQUEST_NUM++ ))
done

echo "EL resultado de salida del curl es de $RESULT"  >> "$LOG_FILE"
JSON_OUTPUT_STRING=""

if [ $RESULT_CURL = 0 ]; then
    echo "Curl exitoso" >> "$LOG_FILE"
else
    echo "Fallo en la ejecucion del curl" >> "$LOG_FILE"
    RESULT_SCRIPT=1
    RESULT=$RESULT_CURL
fi

if [ -f "$FILE_OUTPUT" ]; then
    MESSAGE_REQUEST=$( cat $FILE_OUTPUT )
    echo "Resultado: $MESSAGE_REQUEST" >> "$LOG_FILE"
    rm "$FILE_OUTPUT"
fi

if [ $RESULT_SCRIPT = 0 ] && [ $RESULT -ge $MINSTATUSCODE ] && [ $RESULT -le $MAXSTATUSCODE ]; then
    echo "Correcto" >> "$LOG_FILE"
else
    echo "ERROR en la peticion el resultado es de $RESULT"  >> "$LOG_FILE"
    RESULT_SCRIPT=1
fi

if [ $DONT_ADD = 0 ]; then
    JSON_OUTPUT_STRING=$( jq -n \
                        --arg statusCode "$RESULT" \
                        --arg content "" \
                        --arg message "$MESSAGE_REQUEST" \
                        '{statusCode: $statusCode, content: $content, message: $message}')
else    
    echo "Resultado para los logs ${MESSAGE_REQUEST} "  >> "$LOG_FILE"
    JSON_OUTPUT_STRING=$( jq -n \
                        --arg statusCode "$RESULT" \
                        --arg content "" \
                        --arg message "Body no recuperado" \
                        '{statusCode: $statusCode, content: $content, message: $message}')

fi

echo "$JSON_OUTPUT_STRING"
exit "$RESULT_SCRIPT"

