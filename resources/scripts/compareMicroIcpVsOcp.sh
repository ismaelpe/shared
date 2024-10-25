#!/bin/bash

while getopts ":u:i:o:" options; do
    case "${options}" in
        i) 
          URL_ICP=${OPTARG}
          ;;
        o) 
          URL_OCP=${OPTARG}
          ;;
        *) ;;
    esac

done

#echo "ICP: ${URL_ICP}"
#echo "OCP: ${URL_OCP}"
MINSTATUSCODE=400

PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"

FILE_OUTPUT_ICP="${PATH_LOCAL}/data/output_icp.txt"
#Executing el log
echo "" > "$FILE_OUTPUT_ICP"


FILE_OUTPUT_OCP="${PATH_LOCAL}/data/output_ocp.txt"
#Executing el log
echo "" > "$FILE_OUTPUT_OCP"

#echo " curl -s -k --write-out '%{http_code}' ${URL_ICP} -o ${FILE_OUTPUT_ICP}"

statusCodeICP=$(curl -s -k --write-out '%{http_code}' ${URL_ICP} -o ${FILE_OUTPUT_ICP}) 
resultICP=$?

if [ $resultICP -gt 0 ]; then
  echo "Error al ejecutar la peticion contra ICP ${statusCodeICP}"  
  statusCodeICP=500
fi 

statusCodeOCP=$(curl -s -k --write-out '%{http_code}' ${URL_OCP} -o ${FILE_OUTPUT_OCP}) 
resultOCP=$?

echo "${statusCodeICP}"
echo "${statusCodeOCP}"

if [ $resultOCP -gt 0 ]; then
  echo "Error al ejecutar la peticion contra OCP ${statusCodeOCP}"  
  statusCodeOCP=500
fi 

if  [[ $statusCodeOCP -gt 200 || $statusCodeICP -gt 200 ]]; then
    echo "false"
else 
    versionOcp=$(cat ${FILE_OUTPUT_OCP} | jq -r '.build.version' )
    artifactOcp=$(cat ${FILE_OUTPUT_OCP} | jq -r '.build.artifact' )
    timeOcp=$(cat ${FILE_OUTPUT_OCP} | jq -r '.build.time' )

    versionIcp=$(cat ${FILE_OUTPUT_ICP} | jq -r '.build.version' )
    artifactIcp=$(cat ${FILE_OUTPUT_ICP} | jq -r '.build.artifact' )
    timeIcp=$(cat ${FILE_OUTPUT_ICP} | jq -r '.build.time' )
    #echo ""
    #echo "O${timeOcp}" 
    #echo "I${timeIcp}" 
    #echo "O${versionOcp}" 
    #echo "I${versionIcp}" 
    #echo "O${artifactOcp}" 
    #echo "I${artifactIcp}" 

    if [[ $versionOcp == $versionIcp && $artifactOcp == $artifactIcp ]]; then
       if [[ $versionIcp == *"SNAPSHOT"* ]]; then
          #Es snapshot tenemos que comprobar la fech
          fechaIcp=$(date -d "$timeIcp" +'%s')
          fechaOcp=$(date -d "$timeOcp" +'%s')

          if [ $fechaOcp -ge $fechaIcp ];
          then
             echo "true"
          else 
             echo "false"
          fi 
       else
          echo "true"	
       fi 
    else 
       echo "false"
    fi
fi

exit 0 
