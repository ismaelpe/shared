#!/bin/bash

KPI_TIMEOUT=30
INPUT_DATA=""
KPI_URL=""
TOKEN_ALM_LOGCOLLECTOR="${ALM_LOGCOLLECTOR_PASSWORD}"
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"

while getopts ":u:i:p:" options; do
    case "${options}" in
        i) 
          INPUT_DATA=${OPTARG}
          ;;
        u) 
          KPI_URL=${OPTARG}
          ;;
        p) 
          PROXY=${OPTARG}
          ;;
        *) ;;
    esac

done

CURL_ARGS=("$PATH_LOCAL"/curlUtils.sh)
CURL_ARGS+=(-u "${KPI_URL}")
CURL_ARGS+=(-v POST)

#Timeout
CURL_ARGS+=(-t "${KPI_TIMEOUT}")
#Debe Reintentar
CURL_ARGS+=(-f 2)
#Numero de reintentos
CURL_ARGS+=(-b 2)
#Delay entre peticiones
CURL_ARGS+=(-w 15)
#Usa del proxy
CURL_ARGS+=(-p "${PROXY}")
CURL_ARGS+=(-i "${INPUT_DATA}")

CURL_ARGS+=(-a "${TOKEN_ALM_LOGCOLLECTOR}")
RESULT=$("${CURL_ARGS[@]}")
RC_STATUS=$?

if [ "$RC_STATUS" -eq 0 ]; then
    echo "Todo bien todo correcto" >> "$LOG_FILE"
else
    echo "ERROR $RESULT" >> "$LOG_FILE"
fi
