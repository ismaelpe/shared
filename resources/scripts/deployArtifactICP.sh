#!/bin/bash

#Parametros de entrada

validateRestartsAndDelete(){
    COLOUR_=$2
    DEPLOYMENT_=$1 
    FECHA_INICIO_SCRIPT=$3

    echo "Los pods son de ${DEPLOYMENT_} " >> "$LOG_FILE"
    EXISTEN_PODS=$(echo "${DEPLOYMENT_}" | jq -e '.items.pods')


    if [ "$EXISTEN_PODS" == null ]; then
        echo "El  resultado de la comprobacion de los restarts NO es correcto. NO TENEMOS PODS " >> "$LOG_FILE"
        return 1
    else

        echo "${DEPLOYMENT_}" | jq -c '.items.pods[]' | while read -r deploymentUnity; do
            
            nameDeployment=$(echo "$deploymentUnity" | jq '.controlledByName' )
            colourDeployment=$(echo "${nameDeployment}" | awk -F '-' '{ print $2 }' | awk -F '"' '{ print $1 }')  
            colourDeployment=${colourDeployment^^}
            echo "Analizando la info de los pods ${deploymentUnity} ${COLOUR_} ${colourDeployment} para nameDeployment" >> "$LOG_FILE"
        
            if [ "$colourDeployment" = "${COLOUR_}" ]; then
                numRestarts=$(echo "$deploymentUnity" | jq '.restarts' )
                fechaCreationTimeStamp=$(echo "$deploymentUnity" | jq -r '.creationTimestamp' )
                dateCreationTimeStamp=$(date --date="$fechaCreationTimeStamp" +%s)

                echo "Analizando la info de los pods ${deploymentUnity} ${COLOUR_} ${colourDeployment} para nameDeployment. Fecha creacion ${dateCreationTimeStamp} formato string ${fechaCreationTimeStamp}. Fecha inicio ${FECHA_INICIO_SCRIPT}" >> "$LOG_FILE"
        
                #REINICIOS MAXIMOS EN DEV
                if [ "$numRestarts" -gt 6 ] && [ "$dateCreationTimeStamp" -gt "$FECHA_INICIO_SCRIPT" ]; then
                    echo "Tenemos demasiados ${numRestarts}  para el micro" >> "$LOG_FILE"
                    return 1
                fi
            fi
        done

        restart=$?
        if [ $restart -eq 0 ]; then
            echo "El  resultado de la comprobacion de los restarts es correcto " >> "$LOG_FILE"
            return 0
        else 
            echo "El  resultado de la comprobacion de los restarts NO es correcto " >> "$LOG_FILE"
            return 1
        fi
    fi


}
validateReplicasDeployment(){
    COLOUR_=$3
    DEPLOYMENT_=$1
    ENVIRONMENT_=$2    
    #ENVIRONMENT_ 
    # DEV Si tenemos mas de N reinicios tenemos que DELETE
    # TST Solo nos interesa uno de los deployments, nos aplica el COLOUR_
    # PRE, PRO Tenemos que validar los dos colores, no nos importa ninguno de los colores todo tiene que arrancar
    echo "Validaremos el deployment ${DEPLOYMENT_} environment ${ENVIRONMENT_}" >> "$LOG_FILE" 
    contador_=0    
    EXISTEN_PODS=$(echo "${DEPLOYMENT_}" | jq -e '.items.deployments')


    if [ "$EXISTEN_PODS" == null ]; then
        echo "El  resultado de la comprobacion de las replicas NO es correcto. NO TENEMOS PODS " >> "$LOG_FILE"
        return 1
    else
        echo "${DEPLOYMENT_}" | jq -c '.items.deployments[]' | while read -r deploymentUnity; do

            nameDeployment=$(echo "$deploymentUnity" | jq '.name' )
            colourDeployment=$(echo "${nameDeployment}" | awk -F '-' '{ print $2 }' | awk -F '"' '{ print $1 }')  
            colourDeployment=${colourDeployment^^}
            echo "Validaremos el nameDeployment ${nameDeployment} del color ${colourDeployment} ${COLOUR_}" >> "$LOG_FILE" 
            (( contador_++ ))

            if [[ ( "$ENVIRONMENT_" = 'TST' ||  "$ENVIRONMENT_" = 'DEV' ) ]] && [ "$colourDeployment" = "${COLOUR_}" ]; then
            replicasInfo=$(echo "$deploymentUnity" | jq '.replicas' )
            #Tenemos que validar el color
            #Nos interesa saber las replicas del que desplegamos el otro no nos importa
            replicasAvailable=$(echo "$replicasInfo" | jq '.available')
            replicasDesired=$(echo "$replicasInfo" | jq '.desired')
            replicasUnavailable=$(echo "$replicasInfo" | jq '.unavailable')
            echo "Las replicasAvailable ${replicasAvailable} las replicasDesired ${replicasDesired} para el entorno ${ENVIRONMENT}" >> "$LOG_FILE" 

            if [ "$replicasAvailable" -eq "$replicasDesired" ]; then
            #Fin ya tenemos todas las instancias OK
                    echo "Todas las replicas OK" >> "$LOG_FILE" 
            else 
                    return 1
            fi
                if [ "$replicasUnavailable" -gt 0 ]; then
            #Fin ya tenemos todas las instancias OK
                    echo "Tenemos replicas que no han arrancado" >> "$LOG_FILE"
                    return 1
                fi 
            fi
            if [ "$ENVIRONMENT_" = 'PRE' ] || [ "$ENVIRONMENT_" = 'PRO' ]; then
            replicasInfo=$(echo "$deploymentUnity" | jq '.replicas' )
            #Tenemos que validar el color
            #Nos interesa saber las replicas del que desplegamos el otro no nos importa
            replicasAvailable=$(echo "$replicasInfo" | jq '.available')
            replicasDesired=$(echo "$replicasInfo" | jq '.desired')
            replicasUnavailable=$(echo "$replicasInfo" | jq '.unavailable')
            echo "Las replicasAvailable ${replicasAvailable} las replicasDesired ${replicasDesired} para el entorno ${ENVIRONMENT}" >> "$LOG_FILE" 
            if [ "$replicasAvailable" -ne "$replicasDesired" ]; then
            #Fin ya tenemos todas las instancias OK
                    echo "Tenemos replicas que no han arrancado" >> "$LOG_FILE"
                    return 1
            fi 
            if [ "$replicasUnavailable" -gt 0 ]; then
            #Fin ya tenemos todas las instancias OK
                    echo "Tenemos replicas que no han arrancado" >> "$LOG_FILE"
                    return 1
            fi                      
            fi
        done
    fi
    valorReplicas=$?  

    return "$valorReplicas"
}

abort_and_help(){
  echo "buildArtifactICP.sh [-h ICP_URL] [-l LOGGER_URL] [-A ICP_APP] [-V version ] [-i BODY ]"
  exit 1
}


TIMEOUT_SECONDS=480
TIMEOUT_ITERATIONS=15

LOGGER_URL='NA'
PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_ERROR_FILE="${PATH_LOCAL}/logs/error.log"
LOG_FILE="${PATH_LOCAL}/logs/output.log"
OUTPUT_DATA_FILE="${PATH_LOCAL}/data/result.rslt"
BODY='NA'

ICP_APP='NA'
VERSION='NA'
MAJOR=1

COMPONENT_TYPE='NA'
COMPONENT='NA'
COMPONENT_ID='NA'
PROXY='false'
COLOUR='G'
K8S='icp'

AZ='ALL'
if [ -f "$LOG_ERROR_FILE" ]; then   
    rm "$LOG_ERROR_FILE"
fi
if [ -f "$LOG_FILE" ]; then   
    rm "$LOG_FILE"
fi
if [ -f "$OUTPUT_DATA_FILE" ]; then   
    rm "$OUTPUT_DATA_FILE"
fi
INPUT_FILE=''

MICRO='NA'
IGNORE_START='false'
while getopts ":l:U:u:v:i:A:a:I:V:T:C:E:M:B:t:s:S:p:h:k:e:Z:c:f:w:" options; do
        
    case "${options}" in 
         Z) AZ=${OPTARG}
            ;; 
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
         I) COMPONENT_ID=${OPTARG}
            ;;
         c) COLOUR=${OPTARG^^}
            ;;
         f) INPUT_FILE=${OPTARG}
            ;;
         w) IGNORE_START=${OPTARG}
            ;;
         *) ;;
    esac
done

if [ "$BODY" = 'NA' ] && [ "$INPUT_FILE" != '' ]; then
    if [ -f "$INPUT_FILE" ]; then  
        echo "El fichero con datos de entrada existe ${INPUT_FILE} y el BODY es vacio" >> "$LOG_FILE" 
        BODY=$(cat "$INPUT_FILE" )
    else
        echo "El fichero con datos de entrada NO existe ${INPUT_FILE} y el BODY es vacio" >> "$LOG_FILE" 
    fi
else
    echo "Los datos llegan por el parametro Body ${BODY}, el Input file no llega ${INPUT_FILE}" >> "$LOG_FILE"
fi

if [ "$ICP_APP" = 'NA' ] || [ "$ICP_URL" = 'NA' ] || 
[ "$LOGGER_URL" = 'NA' ] || [ "$BODY" = 'NA' ]; then
    abort_and_help
fi

if [ "$VERSION" != 'NA' ]; then
   MAJOR=$(echo "${VERSION}" | awk -F ':' '{ print $1 }')  
fi

COMPONENT_ICP=${MICRO^^}${MAJOR}

if [ "$K8S" != 'ocp' ]; then
    APLICATION_TYPE="PCLD"
    echo "El deploy es contra ICP ya que el param es de ${APLICATION_TYPE} ${K8S}" >> "$LOG_FILE"
else
    #Deberia ser de ocp
    APLICATION_TYPE="PCLD_MIGRATED"
    echo "El deploy es contra OCP ya que el param es de ${APLICATION_TYPE} ${K8S}" >> "$LOG_FILE"
fi

#Tenemos que dar de alta los recursos
JSON_OUTPUT_STRING=$( jq -n \
            --arg azArgs "$AZ" \
            --arg environmentArgs "$ENVIRONMENT" \
            --arg valuesArg "$BODY" \
            '{"az": $azArgs, "environment": $environmentArgs,"values":$valuesArg }')

PATH_ICP="/api/publisher/v1/api/application/${APLICATION_TYPE}/${ICP_APP}/component/${COMPONENT_ICP}/deploy"
#/api/publisher/v2/api/application/{applicationType}/{applicationName}/component/{componentName}
#Comprovaremos si existe el componente
INIT_TOTAL_TIME=$(date +%s)

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

echo "El IGNORE START ES DE ${IGNORE_START}" >> "$LOG_FILE"

if [ "$RC_STATUS" -eq 0 ] && [ "${IGNORE_START}" = 'false' ]; then
    RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
    CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
    
    echo "Los valores son de ${RESULT_TO_OUTPUT} ${CONTENT_TO_OUTPUT}" >> "$LOG_FILE" 
    if [ "$RESULT_TO_OUTPUT" -ge 200 ] && [ "$RESULT_TO_OUTPUT" -lt 300 ]; then
        #El elemento no existe. Tenemos recuperar que dar de alta el elemento
        ID_DEPLOY=$(echo "$CONTENT_TO_OUTPUT" | jq -r '.id' )
        echo "Deploy generado ${COMPONENT} ${MAJOR} con el siguiente ID ${ID_DEPLOY}" >> "$LOG_FILE"
        #Tenemos que empezar a iterar y a hacer pulling hasta tener las instancias arrancadas
        PATH_ICP_STATUS="/api/publisher/v1/api/application/${APLICATION_TYPE}/${ICP_APP}/component/${COMPONENT_ICP}/environment/${ENVIRONMENT}/availabilityzone/$AZ/status"
        CURL_ARGS=("${BASE_CURL_ARGS[@]}")
        CURL_ARGS+=(-u "${ICP_URL}${PATH_ICP_STATUS}")        
        CURL_ARGS+=(-v GET)

        contador=0
        termina=$TIMEOUT_ITERATIONS
        RESULT_FINAL_OUTPUT=2
        INIT_TIME=$(date +%s)
        DURATION=0
        while [ $termina -ge $contador ] && [ $RESULT_FINAL_OUTPUT -eq 2 ] && [ $DURATION -lt $TIMEOUT_SECONDS ]
        do
            ACTUAL_TIME=$(date +%s)
            DURATION=$(( "$ACTUAL_TIME" - "$INIT_TIME" ))
        
            sleep 45s
            (( contador++ ))
            RESULT=$("${CURL_ARGS[@]}")
            RC_STATUS=$?
            echo "Duracion:  ${DURATION} Contador: ${contador} Resultado Final: ${RESULT_FINAL_OUTPUT}" >> "$LOG_FILE"

            if [ "$RC_STATUS" -eq 0 ]; then
                RESULT_FINAL=$(echo "$RESULT" | jq -r '.statusCode' )
                CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' )
                if [ "$RESULT_FINAL" -ge 200 ] && [ "$RESULT_FINAL" -lt 300 ]; then
                    #Tenemos que validar la respuesta a ver si es correcta
    
                    # jq '.[0].items.deployments[0].replicas.available'
                    # jq '.[0].items.deployments[0].name'
                    resultadoAnalisis=0
               
                    while IFS= read -r deployment 
                    do     
                                                  
                        echo "El micro deployment ${deployment} " >> "$LOG_FILE"
                                                                     
                        validateReplicasDeployment "$deployment" "$ENVIRONMENT" "$COLOUR"
                        validacionReplicas=$?
                       
                        echo "Resultado de validateReplicasDeployment ${validacionReplicas} " >> "$LOG_FILE"
                        if [ "$validacionReplicas" -eq 0 ]; then
                            echo "El micro ha arrancado correctamente al menos en este cluster" >> "$LOG_FILE"                          
                        else
                            echo "El micro no ha arrancado correctamente en este cluster. " >> "$LOG_FILE"                                                                   
                            #Validaremos si es DEV a ver que hacemos
                            if [ "$ENVIRONMENT" = 'DEV' ] || [ "$ENVIRONMENT" = 'TST' ]; then
                                #Validaremos si tiene restarts
                                echo "Validaremos los restarts de los micros " >> "$LOG_FILE"                            
                                                                                     
                                validateRestartsAndDelete "$deployment" "$COLOUR" "$INIT_TOTAL_TIME"
                                validacionReplicas=$?
                                                                                                        
                                echo "Resultado validateRestartsAndDelete ${validacionReplicas}" >> "$LOG_FILE"
                                if [ "$validacionReplicas" -ne 0 ] && [ "$ENVIRONMENT" = 'DEV' ]; then
                                    echo "El micro se ha reiniciado demasiado " >> "$LOG_FILE"
                                    CURL_ARGS=("${BASE_CURL_ARGS[@]}")
                                    CURL_ARGS+=(-u "${ICP_URL}${PATH_ICP}")
                                    CURL_ARGS+=(-i "${JSON_OUTPUT_STRING}")
                                    CURL_ARGS+=(-v DELETE)
                                    #Debe Reintentar
                                    CURL_ARGS+=(-f 2)
                                    #Numero de reintentos
                                    CURL_ARGS+=(-b 2)
                                    #Delay entre peticiones
                                    CURL_ARGS+=(-w 15)
                                    RESULT=$("${CURL_ARGS[@]}")
                                    RC_STATUS=$?
                                    echo "Resultado del Delete ${RC_STATUS} " >> "$LOG_FILE"

                                    if [ "$RC_STATUS" -eq 0 ]; then
                                        RESULT_DELETE=$(echo "$RESULT" | jq -r '.statusCode' )
                                        if [ "$RESULT_DELETE" -ge 200 ] && [ "$RESULT_DELETE" -lt 300 ]; then
                                            echo "El micro se ha BORRADO ${COMPONENT} ${MAJOR} en ${ENVIRONMENT}" >> "$LOG_FILE"
                                        fi
                                    fi
                                    echo "El micro se ha reiniciado ${COMPONENT} ${MAJOR} en ${ENVIRONMENT}" >> "$LOG_FILE"  
                                    MESSAGE_TO_OUTPUT="El micro se ha reiniciado demasiadas veces, lo hemos borrado de DEV"
                                    resultadoAnalisis=2
                                    break
                                else
                                    if [ "$validacionReplicas" -ne 0 ]; then
                                        resultadoAnalisis=2
                                        break
                                    else 
                                        resultadoAnalisis=1
                                    fi                                    
                                fi
                            else 
                                resultadoAnalisis=1
                            fi
                        fi
                                                                          
                    done < <( echo "${CONTENT_TO_OUTPUT}" | jq -c '.[]' )

                    if [ $resultadoAnalisis -eq 2 ]; then
                    #Hemos eliminado el micro. Se tiene que finalizar el bucle
                        echo "El micro se ha reiniciado demasiado en uno de los clusters ${COMPONENT} ${MAJOR} en ${ENVIRONMENT}" >> "$LOG_FILE"  
                        RESULT_FINAL_OUTPUT=1
                        MESSAGE_TO_OUTPUT="El micro se ha reiniciado demasiadas veces, lo hemos borrado de DEV"
                    fi
                    if [ $resultadoAnalisis -eq 0 ]; then
                        echo "El micro ha ARRANCADO OK ${COMPONENT} ${MAJOR} en ${ENVIRONMENT}" >> "$LOG_FILE"                         
                        RESULT_FINAL_OUTPUT=0
                    fi
                else
                    RESULT_FINAL_OUTPUT=1
                    RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
                    CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' ) 
                fi
            else
                RESULT_FINAL_OUTPUT=1
                RESULT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.statusCode' )
                CONTENT_TO_OUTPUT=$(echo "$RESULT" | jq -r '.message' ) 
            fi
        done
        RESULT_FINAL=$RESULT_FINAL_OUTPUT
    else      
        echo "Error al enviar el deploy de ${COMPONENT} ${MAJOR}" >> "$LOG_FILE"    
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
   echo "El resultado del deploy es correcto se procede a devolver el json del deploy"  >> "$LOG_FILE"
   JSON_OUTPUT_STRING="${CONTENT_TO_OUTPUT}"
   echo "${JSON_OUTPUT_STRING}" > "$OUTPUT_DATA_FILE"
else 
   echo "${CONTENT_TO_OUTPUT}" > "$LOG_ERROR_FILE"
   echo "El resultado no es bueno ${RESULT_FINAL}" >> "$LOG_FILE" 
fi 
ACTUAL_TIME=$(date +%s)
DURATION=$(( "$ACTUAL_TIME" - "$INIT_TOTAL_TIME" ))
echo "DuracionTOTAL:  ${DURATION} Contador: ${contador} Resultado Final: ${RESULT_FINAL_OUTPUT}" >> "$LOG_FILE"

echo "$JSON_OUTPUT_STRING"

exit "$RESULT_FINAL"
