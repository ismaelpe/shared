import com.project.alm.*
import groovy.json.JsonSlurperClassic

def call(String typeAppParam, String nameAppParam, String gitAppURLParam) {
  withCredentials([
    usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASSWORD'),
    string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'GITLAB_API_TOKEN')
  ]) {

    def emailTo = idecuaRoutingUtils.getResponsiblesAppEmailList(nameAppParam, typeAppParam)
    printOpen( "to recovered: ${emailTo}", EchoLevel.DEBUG)

    // obtener la ruta del repositorio como urlencoded para llamar a la API de
    // gitlab
    def projectId = GlobalVars.EMAIL_REPOSITORY_ID
    def gitLabDomain = GlobalVars.gitlabApiDomain
    def gitLabEndpoint = GlobalVars.GIT_API_REPO_FILE_ENDPOINT
    def emailPath = GlobalVars.EMAIL_RELATIVE_URL_WELCOME
    def emailPathEncoded = URLEncoder.encode(emailPath, "UTF-8")
    def logoPath = GlobalVars.EMAIL_LOGO_RELATIVE_URL
    def logoPathEncoded = URLEncoder.encode(logoPath, "UTF-8")
    def gitlabFileRawMode = GlobalVars.GIT_API_REPO_FILE_RAW
    def standardTimeout = GlobalVars.HTTP_STD_TIMEOUT
   
    def logoContent = sh returnStdout: true, script: "curl \
    -k -s -X GET \
    --header \"Private-Token:${GITLAB_API_TOKEN}\" \
    --connect-timeout ${standardTimeout} \
    ${gitLabDomain}${projectId}${gitLabEndpoint}${logoPathEncoded}?ref=master"
    
    def logoJson = new JsonSlurperClassic().parseText(logoContent)
    printOpen( "json: ${logoJson}", EchoLevel.DEBUG)

    def logoBase64 = "${logoJson.content}"
    printOpen( "image content: ${logoBase64}", EchoLevel.DEBUG)

    def emailContent = sh returnStdout: true, script: "curl \
    -k -s -X GET \
    --header \"Private-Token:${GITLAB_API_TOKEN}\" \
    --connect-timeout ${standardTimeout} \
    ${gitLabDomain}${projectId}${gitLabEndpoint}${emailPathEncoded}${gitlabFileRawMode}?ref=master"
    printOpen( "email content: ${emailContent}", EchoLevel.DEBUG)

    //Por alguna razón que aún no he podido descubrir, no funciona la interpolazión del string logoBase64 
    //Hasta que no identifique el problema, lo solucionamos con el replaceAll como patch

    emailContent = emailContent.replaceAll('\\$\\{logoBase64\\}',logoBase64)
    emailContent = emailContent.replaceAll('\\$\\{typeAppParam\\}',typeAppParam)
    emailContent = emailContent.replaceAll('\\$\\{nameAppParam\\}',nameAppParam)
    emailContent = emailContent.replaceAll('\\$\\{gitAppURLParam\\}',gitAppURLParam)


    emailext(
          mimeType: 'text/html',
          replyTo: 'alm.microservices.support@project.com',
          body: emailContent,
          subject: "Welcome to OpenServices ${typeAppParam} ${nameAppParam}", 
          from: 'noreply@alm.project.com', 
          to: "${emailTo}, onboarding.opennow@projecttech.com, v.escalzo.rubio@accenture.com, a.fernandez.cristobo@accenture.com, jasaizt@projecttech.com"
      )

    
  }
}
