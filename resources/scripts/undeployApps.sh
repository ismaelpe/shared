#!/bin/sh

if [  $# -eq 0 ]
then
	echo "comand use: ./app_without_load_v1.0.sh [NS] [ENV] [PERIOD] [ACTION]"
	echo " NS: AB3APP|AB3COR"
	echo " ENV: DEV|TST|PRE|PRO"
	echo " PERIOD: +/-val[y|m|w|d|H|M|S] -> +1d, -1w ,..."
	echo " ACTION: START|STOP|UNDEPLOY"
	echo "Example:"
	echo " ./launch_query.sh AB3APP DEV -2d START"
	exit 1	
fi

# Script Params
NAMESPACE=$1
ENVIRONMENT=$2
PERIOD=$3
ACTION=$4


# Set Vars
ID_COMPONENT=1766
if [ $NAMESPACE = "AB3COR" ]
then
	ID_COMPONENT=1765
fi 

KIBANA_USER=U0184320
KIBANA_PASS=Zephyr8.
PROXY=http://$KIBANA_USER:$KIBANA_PASS@proxynexe.svb.lacaixa.es:8080
KIBANA_URL=http://$KIBANA_USER:$KIBANA_PASS@gelogasp29.lacaixa.es:9200/logs_absis3_generic/_search
ICP_URL=https://publisher-ssp-cldalm.pro.ap.intranet.cloud.lacaixa.es
ICP_GET_PODS=$ICP_URL/api/publisher/v1/api/application/PCLD/$NAMESPACE/environment/$ENVIRONMENT/az/ALL?object=POD
ICP_GET_STATUS=$ICP_URL/api/publisher/v1/application/$ID_COMPONENT/component
ICP_GET_DEPLOYMEMT_PATTERN=$ICP_URL/api/publisher/v1/application/PCLD/$NAMESPACE/component/#ID#/deploy/current/environment/$ENVIRONMENT/az/ALL
CATMSV_URL=https://catmsv-micro-server-1.pro.int.srv.caixabank.com
CARMSV_TOKEN=eyJhbGciOiJQQkVTMi1IUzI1NitBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjoidjEiLCJjdHkiOiJKV1QiLCJ0dHkiOiJBVCIsInRpZCI6IkFUMSIsImF1ZCI6IlthcGkucHJvLmludGVybmFsLmNhaXhhYmFuay5jb20sIGFwaWNvbGxlY3Rvci5wcm8uaW50ZXJuYWwuY2FpeGFiYW5rLmNvbV0iLCJqdGkiOiJxTlVUb2Ria3ZVWTZLN01tc0dnM05BIiwiZXhwIjoiTnVtZXJpY0RhdGV7MTkxNDU1NjQwMyAtPiBTZXAgMiwgMjAzMCA3OjEzOjIzIEFNIENFU1R9IiwicDJjIjo4MTkyLCJwMnMiOiI5QTg0RWR2Vl9YcXdDbVY2In0
START_DATE=$(date -v$PERIOD +%F)
END_DATE=$(date +%F)

PATH_LOCAL=$(pwd)
PATH_LOCAL="${PATH_LOCAL}@tmp"
LOG_FILE="${PATH_LOCAL}/logs/output.log"

# Perform start or stop
# if start action launched, we will query to catalog to get previous replicas number
startOrStop()
{
	ID=$(jq --arg APP_NAME $1 '.[] | select(.name | ascii_downcase | .== $APP_NAME) | .id' ./icp-status.json)
	ICP_GET_DEPLOYMEMT=$(echo $ICP_GET_DEPLOYMEMT_PATTERN | sed -e "s/#ID#/$ID/g")
	
	curl --proxy $PROXY -k -s -X GET $ICP_GET_DEPLOYMEMT -H accept:*/* -H application_active:$NAMESPACE -H Content-Type:application/json --cert absis3-2023.pem:EstoEsUnPasswordRobusto > $PATH_LOCAL/deployment.json

	# PREPARE DATA TO CATALOG REQUEST DATA
	APP_TYPE=$(cat ./deployment.json | sed -En "s/.*name: ABSIS_APP_TYPE.*value: ([A-Z]{2,3}\.[A-Z]{2,3}).*/\1/p")
	APP_NAME_NO_VERSION=$(echo $1 | sed -En "s/(.*)([0-9]{1})/\1/p")
	APP_VERSION=$(echo $1 | sed -En "s/(.*)([0-9]{1})/\2/p")
	
	# QUERY TO CATALOG TO INSTALL_DATE AND REPLICAS
	FULL_CATMSV_URL=$CATMSV_URL/app/$APP_TYPE/$APP_NAME_NO_VERSION/version/$APP_VERSION/environment/$ENVIRONMENT
	
	curl --proxy $PROXY -k -s -X GET $FULL_CATMSV_URL -H accept:*/* -H Content-Type:application/json -H "Authorization: Bearer $CARMSV_TOKEN" > $PATH_LOCAL/catmsv_app_deploy_info.json
	
	REPLICAS=$(jq '.traceInstallation.deploy.replicas' $PATH_LOCAL/catmsv_app_deploy_info.json)
	INSTALLATION_DATE=$(jq -r '.traceInstallation.installationDate' $PATH_LOCAL/catmsv_app_deploy_info.json | sed -En "s/([0-9]{4}-[0-9]{2}-[0-9]{2})(.*)/\1/p")

	rm -rf ./catmsv_app_deploy_info.json

	if [ $2 = "STOP" ]
	then
		# IF STOP ALWAYS SET REPLICAS TO 0
		sed -r "s/replicas: [0-9]{1}/replicas: 0/g" $PATH_LOCAL/deployment.json > $PATH_LOCAL/deployment_update_replicas.json
		REPLICAS=0
	fi 

	if [ $2 = "START" ]
	then
		if [ -z "$REPLICAS" ] || [ $REPLICAS = "null" ]
		then			
      		echo "QUERY CATALOG FAILED: $APP_NAME_NO_VERSION, VERSION: $APP_VERSION, SET REPLICAS: '$REPLICAS'"
      		REPLICAS=1
		fi
		sed -r "s/replicas: [0-9]{1}/replicas: $REPLICAS/g" $PATH_LOCAL/deployment.json > $PATH_LOCAL/deployment_update_replicas.json
	fi
	
	# Check if ID is not null or empty to launch update replicas
	if [ -z "$ID" ] || [ "$ID" = "null" ]
	then
		# IF ID IF NULL WILL NOT CALL TO ICP, BECAUSE IT'S NEEDED TO PERFORM ACTION
		echo "[WARNING][UNK.UNK_UNK] $1 CHECK MANUALLY"
	else
		# CONSTRUCT URL TO CALL ICP API TO SET REPLICAS 
		ICP_UPDATE_DEPLOYMEMT_URL=$ICP_URL/api/publisher/v1/application/PCLD/$NAMESPACE/component/$ID/deploy

		# ###################################################################################################
		if [ -z "$INSTALLATION_DATE" ] || [ "$INSTALLATION_DATE" = "null" ]
		then
			echo $1 >> out_warnings.txt
			echo "[WARNING][$APP_TYPE.$APP_NAME_NO_VERSION$APP_VERSION] $1 - NO_INSTALL_DATE - CHECK MANUALLY"
		else
			if [[ $INSTALLATION_DATE > $END_DATE ]]
			then
				echo $1 >> $PATH_LOCAL/out_no_candidates.txt
				echo "[WARNING][$APP_TYPE.$APP_NAME_NO_VERSION$APP_VERSION] $1 - $INSTALLATION_DATE > $END_DATE"
			else
				METHOD=POST
				if [ $2 = "UNDEPLOY" ]
				then
					cat $PATH_LOCAL/deployment.json > $PATH_LOCAL/deployment_update_replicas.json
					METHOD=DELETE
				fi
				
				jq 'del(.id) | del(.statusProcess) | del(.component) | del(.type) | del(.clusters) | del(.pipeline)' $PATH_LOCAL/deployment_update_replicas.json > $PATH_LOCAL/new_deployment.json
				#curl --proxy $PROXY -k -s -X DELETE $ICP_UPDATE_DEPLOYMEMT_URL -H accept:*/* -H application_active:$NAMESPACE -H Content-Type:application/json --cert absis3-2023.pem:EstoEsUnPasswordRobusto -d @'$PATH_LOCAL/new_deployment.json' > $PATH_LOCAL/result.json

				echo $1,$INSTALLATION_DATE >> $PATH_LOCAL/out_candidates.txt
				echo $APP_TYPE"."$(echo $APP_NAME_NO_VERSION | tr '[:lower:]' '[:upper:]')"_"$APP_VERSION,$1,$INSTALLATION_DATE >> $PATH_LOCAL/out_candidates_to_pipeline.txt
				echo "[$2][$APP_TYPE.$APP_NAME_NO_VERSION$APP_VERSION] $1 REPLICAS $REPLICAS - $INSTALLATION_DATE < $END_DATE"
			
				#cat ./new_deployment.json
				#cat ./result.json
				#rm -rf ./deployment_update_replicas.json
				#rm -rf ./new_deployment.json
			fi
		fi
	fi
	rm -rf $PATH_LOCAL/deployment.json
} 

echo "Dates: from $START_DATE to $END_DATE"
echo "--< Quering kibana >--"
ENVIRONMENT_LOWER=$(echo "$ENVIRONMENT" | awk '{print tolower($0)}')
cat $PATH_LOCAL/query_pod_no_requests.template | sed -e "s/#ENVIRONMENT#/$ENVIRONMENT_LOWER/g" | sed -e "s/#START_DATE#/$START_DATE/g" | sed -e "s/#END_DATE#/$END_DATE/g" > $PATH_LOCAL/query_pod_no_requests.query
curl --proxy $PROXY -k -s -X POST $KIBANA_URL -H "Content-Type: application/json" -H "accept:*/*" -d @'$PATH_LOCAL/query_pod_no_requests.query' -o $PATH_LOCAL/query_results.json
echo "Done!"

# ##############################################################################################################################################
# GET PODS FROM ICP
# ##############################################################################################################################################
echo "--< Quering PODS from PCLD: $NAMESPACE, ENVIRONMENT: $ENVIRONMENT >--"
curl --proxy $PROXY -k -s -X GET $ICP_GET_PODS -H "Content-Type:application/json" -H "accept:*/*" -H "application_active:$NAMESPACE" --cert absis3-2023.pem:EstoEsUnPasswordRobusto -o $PATH_LOCAL/json.json
echo "Done!"

# FIND PODS AND DELETE FROM RESULTS TO OBTAINS PODS WITHOUT REQUESTS
echo "--< Filtering POD List >--"
jq -r '.aggregations."0".buckets | .[].key' $PATH_LOCAL/query_results.json > $PATH_LOCAL/pod_list.txt

input="$PATH_LOCAL/pod_list.txt"
while IFS= read -r line
do
	POD_NAME=$(echo "$line" | awk '{print tolower($0)}')
	jq --arg POD_NAME $POD_NAME 'del(.[].resources.[] | select(.name | match($POD_NAME)))' $PATH_LOCAL/json.json > $PATH_LOCAL/temp.json
	#ECHO "Get $POD_NAME: OK!"
	cp -rf $PATH_LOCAL/temp.json $PATH_LOCAL/json.json
done < "$input"

echo "Done!"

# SANITIZE OUTS
echo "--< Sanitizing results and delete duplicates >--"
jq -r '.[].resources.[] | .name | capture ("(?<id>[0-9a-zA-Z]*)(-.*)").id | { name: .}' $PATH_LOCAL/json.json | jq -s > $PATH_LOCAL/pod_list_filter_all.json
jq -n '[ inputs[] ] | unique_by(.name)' $PATH_LOCAL/pod_list_filter_all.json > $PATH_LOCAL/pod_list_filter_no_dups.json
jq -r '.[].name' $PATH_LOCAL/pod_list_filter_all.json > $PATH_LOCAL/pod_list_filter_all.list
jq -r '.[].name' $PATH_LOCAL/pod_list_filter_no_dups.json > $PATH_LOCAL/pod_list_filter_no_dups.list

echo "Done!"

# ##############################################################################################################################################
# GET ICP STATUS
# ##############################################################################################################################################
echo "--< Get ICP status PCLD: $NAMESPACE, ENVIRONMENT: $ENVIRONMENT >--"
curl --proxy $PROXY -k -s -X GET $ICP_GET_STATUS -H "accept:*/*" -H "application_active:$NAMESPACE" -H "Content-Type:application/json" --cert absis3-2023.pem:EstoEsUnPasswordRobusto -o $PATH_LOCAL/icp-status.json

echo "Done!"

echo "--< Perform action: $4 >--"

rm -rf $PATH_LOCAL/out_candidates.txt
rm -rf $PATH_LOCAL/out_no_candidates.txt
rm -rf $PATH_LOCAL/out_warnings.txt
rm -rf $PATH_LOCAL/out_candidates_to_pipeline.txt

echo "PIPELINE_PARAM,APP_NAME,INSTALLATION_DATE" >> $PATH_LOCAL/out_candidates_to_pipeline.txt
input="$PATH_LOCAL/pod_list_filter_no_dups.list"
while IFS= read -r line
do  
	# ACTION TO DO!
	startOrStop $line $4
done < "$input"	
echo "Done!"

echo "--< Deleting Temporal Files >--"

rm -rf $PATH_LOCAL/json.json
rm -rf $PATH_LOCAL/temp.json
rm -rf $PATH_LOCAL/pod_list.txt
rm -rf $PATH_LOCAL/query_pod_no_requests.query
rm -rf $PATH_LOCAL/query_results.json
rm -rf $PATH_LOCAL/pod_list_filter_all.json 
rm -rf $PATH_LOCAL/pod_list_filter_no_dups.json
rm -rf $PATH_LOCAL/pod_list_filter_all.list 
rm -rf $PATH_LOCAL/icp-status.json
rm -rf $PATH_LOCAL/pod_list_filter_no_dups.list

echo "Done!"

