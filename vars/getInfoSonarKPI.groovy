import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.SonarData
import com.project.alm.SonarRequestStatus
import com.project.alm.SonarUtilities
import com.project.alm.PipelineData

import groovy.json.*

/** 
 * Método que recupera informacion de sonar via API 
 */
SonarData call(PomXmlStructure pomXml, PipelineData pipelineData) {
  def responseStatusCode = null
  def contentResponse = null
  String fileOut=CopyGlobalLibraryScript('',null,'responseSonarApi.json')
  SonarData sonarData = new SonarData()  
  SonarRequestStatus statusSonar = new SonarRequestStatus()
  def fecha

  withCredentials([string(credentialsId: 'sonartoken', variable: 'sonarToken')]) {

    
    timeout(GlobalVars.DEFAULT_SONAR_REQUEST_RETRIES_TIMEOUT) {
      waitUntil(initialRecurrencePeriod: 15000) {
        try {
  
          fecha = new Date()
          printOpen( "Iteration ${statusSonar.iteration} at date ${fecha}", EchoLevel.ALL)
    
          def sonar_url = "$GlobalVars.SONAR_URL/api/measures/component?metricKeys=coverage,duplicated_lines_density,sqale_index,blocker_violations,new_critical_violations,new_lines,sqale_debt_ratio,comment_lines_density&component=${pipelineData.garArtifactType.getGarName()}.${pomXml.getSpringAppName()}"
          printOpen("Retrieving sonar data from: ${sonar_url}", EchoLevel.ALL)
          
          responseStatusCode = sh(script: "curl -s --write-out '%{http_code}' -o ${fileOut} -u $sonarToken: '$sonar_url' --insecure --connect-timeout ${GlobalVars.SONAR_TIMEOUT}", returnStdout: true).trim()
          
          return SonarUtilities.evaluateResponse(responseStatusCode, statusSonar)
        } catch (e) {
          printOpen("[Sonar KPI] error : ${e}", EchoLevel.ERROR)
          return SonarUtilities.evaluateResponse(responseStatusCode, statusSonar)
        }
      }
    }
  } 

  						
          
  if (responseStatusCode == null) printOpen("Sonar error: No response from Sonar", EchoLevel.ALL)
  if (responseStatusCode == "200") {
    contentResponse= sh(script: "cat ${fileOut}", returnStdout:true )
	  
    def json = new JsonSlurperClassic().parseText(contentResponse)
    printOpen( "json: $json", EchoLevel.ALL)

    if (json.errors) {
      printOpen("Warning: Sonar error: We will continue without sonar information.")
      return sonarData
    }
    else sonarData.found = true;

    for (elem in json.component.measures) {
      switch (elem.metric) {
        case "blocker_violations":
          sonarData.blocker_violations=Float.parseFloat(elem.value)
          break
        case "duplicated_lines_density":
          sonarData.duplicated_lines_density=Float.parseFloat(elem.value)
          break
        case "new_critical_violations":
          for (item in elem.periods) {
            if(item.index == 1) {
              sonarData.new_critical_violations=Float.parseFloat(item.value)
            }
          }
          break
        case "comment_lines_density":
          sonarData.comment_lines_density=Float.parseFloat(elem.value)
          break
        case "sqale_index":
          sonarData.sqale_index=Float.parseFloat(elem.value)
          break
        case "sqale_debt_ratio":
          sonarData.sqale_debt_ratio=Float.parseFloat(elem.value)
          break
        case "coverage":
          sonarData.coverage=Float.parseFloat(elem.value)
          break
        case "new_lines":
          for (item in elem.periods) {
            if(item.index == 1) {
              sonarData.lines=Float.parseFloat(item.value)
            }
          }
        default:
          break
      }
    }
    printOpen("Sonar data: ${sonarData.toString()}", EchoLevel.ALL)
  } else printOpen("Sonar error: Unexpected response when sending request to Sonar (${responseStatusCode})!", EchoLevel.ALL)
  
  return sonarData
}

