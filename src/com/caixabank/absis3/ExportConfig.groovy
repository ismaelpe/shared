package com.caixabank.absis3

import hudson.util.Secret
import com.cloudbees.hudson.plugins.folder.*
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.SecretBytes
import io.jenkins.plugins.casc.ConfigurationAsCode
import jenkins.model.GlobalConfiguration
import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import com.cloudbees.groovy.cps.NonCPS
import javax.xml.transform.stream.*

import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl

class ExportConfig implements Serializable {
    private final def scriptContext
    private AbsisCipher absisCipher 
    
    String jenkinsHome
    String workSpace
    String jcascFullFilePath
   
    private Closure decryptCredential = { entry ->
        def credentialType  = entry.find().key
        def credential  = entry.find().value
        
        if (credentialType == "usernamePassword") {
            credential['password'] = absisCipher.decrypt(credential['password'])
        }
        
        if (credentialType == "string") {
            credential['secret'] = absisCipher.decrypt(credential['secret'])
        }
        
        if (credentialType == "file") {
            credential['secretBytes'] = SecretBytes.fromBytes(absisCipher.decryptBytes(credential['secretBytes'])).toString()
        }
        
        return entry	
    }

    private Closure encryptCredential = { credential ->
        def credentialType = "fileSystemServiceAccountCredential"

        def commonsMap = [
            "id": credential['id'],
            "description": credential['description'],
            "scope": credential['scope'].toString()
        ]
        
        if (credential instanceof com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl) {
            credentialType = "usernamePassword"
            commonsMap += [
                "username": credential['username'],
                "password": absisCipher.encrypt(credential['password'].toString())
            ]            
        }
        
        if (credential instanceof org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl) {
            credentialType = "string"
            commonsMap += [
                "secret": absisCipher.encrypt(credential['secret'].toString())
            ]
        }
        
        if (credential instanceof org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl) {
            credentialType = "file"
            commonsMap += [
                "fileName": credential['fileName'],
                "secretBytes": absisCipher.encrypt(new String(SecretBytes.fromString(credential['secretBytes'].toString()).getPlainData()))
            ]
        }  
        
        return [ (credentialType): commonsMap ]	
    }

    ExportConfig(scriptContext) {
        this.scriptContext = scriptContext
        this.jenkinsHome = scriptContext.env.JENKINS_HOME
        this.workSpace = scriptContext.env.WORKSPACE
        this.absisCipher = new AbsisCipher(this.scriptContext.env.CIPHER_PASSWORD.split("\\+"), this.scriptContext.env.CIPHER_IV)
        this.scriptContext.printOpen("Jenkins Home setted: $jenkinsHome", EchoLevel.INFO)

        this.jcascFullFilePath = "$jenkinsHome/casc_configs/jenkins.yaml"
    }

    @NonCPS
    def flushJCascConfigToDisk() {
        def outputStream = new ByteArrayOutputStream()
        ConfigurationAsCode.get().export(outputStream)
        
        def file = new File(this.jcascFullFilePath)
        file.withOutputStream { stream ->
            outputStream.writeTo(stream)
        }
        
        def dumperOptions = new DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        def yml = new Yaml(dumperOptions)

        // Sanitizamos
        def jcascMap = yml.load((this.jcascFullFilePath as File).text)
        yml.dump(sanitizeJCasc(jcascMap), new FileWriter(this.jcascFullFilePath))
    }

    @NonCPS
    def updateValuesYamlFromJCasc(valuesYamlPath) {  
        def dumperOptions = new DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        def yml = new Yaml(dumperOptions)

        def valuesMap = yml.load(("$workSpace/$valuesYamlPath" as File).text)
        
        valuesMap.get("controller").get("JCasC").get("configScripts").put("jenkins", (this.jcascFullFilePath as File).text)

        yml.dump(valuesMap, new FileWriter("$workSpace/$valuesYamlPath"))
    }

    @NonCPS
    def copyAndRencryptCredentialsFromJCasc(yamlFileName) {
        def dumperOptions = new DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        def yml = new Yaml(dumperOptions)

        def jcascMap = yml.load((this.jcascFullFilePath as File).text)
       
        def encryptedCredentials = SystemCredentialsProvider.getInstance().getCredentials().collect(encryptCredential)

        // Reencriptamos los secretos para exportarlos en git
        jcascMap.get("credentials").get("system").get("domainCredentials").get(0).put("credentials", encryptedCredentials)

        // Reencriptamos la password del LDAP
        def configurationLdap = jcascMap.get("jenkins").get("securityRealm").get('ldap').get('configurations').get(0)
        // Jenkins secret -> plainText
        def ldapSecretPlainText = Secret.fromString(configurationLdap.get('managerPasswordSecret')).toString()
        // plainText -> absisChiper to export to git
        configurationLdap.put('managerPasswordSecret', absisCipher.encrypt(ldapSecretPlainText))
     
        yml.dump(jcascMap, new FileWriter("$workSpace/$yamlFileName"))
    }

    @NonCPS
    def templarizeJCascFile(yamlFileName) 
    {
        def dumperOptions = new DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        def yml = new Yaml(dumperOptions)
        
        def jenkinsYml = yml.load(("$workSpace/$yamlFileName" as File).text)

        // Templarize config
        jenkinsYml.get("unclassified").get("location").put('url', 'https://jenkins-<%=component%>-<%=application%>.<%=environment%>.ap.intranet.cloud.lacaixa.es/') 
        jenkinsYml.get("jenkins").get('clouds').get(0).get('kubernetes').put('jenkinsTunnel', 'jaas-agent.cxb-<%=application%>-<%=environment%>:50000')
        jenkinsYml.get("jenkins").get('clouds').get(0).get('kubernetes').put('jenkinsUrl', 'http://jaas.cxb-<%=application%>-<%=environment%>:8080')
        jenkinsYml.get("jenkins").get('clouds').get(0).get('kubernetes').put('namespace', 'cxb-<%=application%>-<%=environment%>')
        jenkinsYml.get("jenkins").get('clouds').get(0).get('kubernetes').get('templates').each {
           it.get('containers').get(0).get('envVars').each { 
                def envVar = it.get('envVar')
                if (envVar.get('key') == 'JENKINS_URL') {
                    envVar.put('value', 'http://jaas.cxb-<%=application%>-<%=environment%>.svc.cluster.local:8080')
                }
            }
            it.get('volumes').get(0).get('persistentVolumeClaim').put('claimName','<%=application == "a3msje" && environment == "pre" ? "pvjenpre" : "pvjen"%>-cxb-<%=application%>-<%=component%>-<%=environment%>')
        }

        // Dump Modifications
        yml.dump(jenkinsYml, new FileWriter("$workSpace/$yamlFileName"))
    }

    @NonCPS
    def sanitizeJCasc(jenkinsYml) {
        jenkinsYml.get("jenkins").remove('views')
        jenkinsYml.get("jenkins").remove('viewsTabBar')
        jenkinsYml.get("jenkins").remove('labelAtoms')
        jenkinsYml.get("unclassified").remove('badgePlugin')
        jenkinsYml.get("unclassified").remove('bitbucketEndpointConfiguration')
        jenkinsYml.get("unclassified").remove('buildStepOperation')
        jenkinsYml.get("unclassified").remove('gitHubPluginConfig')
        jenkinsYml.get("unclassified").remove('gitHubConfiguration')
        jenkinsYml.get("tool").remove('pipelineMaven')

        return jenkinsYml
    }

    @NonCPS
    def restoreValues(pathRelative, pathValuesYaml) {
        def dumperOptions = new DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        def yml = new Yaml(dumperOptions)

        def templateEngine= new groovy.text.StreamingTemplateEngine()
  
        def jenkinsYml = yml.load(("$workSpace/$pathValuesYaml" as File).text)

        // Para restaurar los valores desde git hemos de desencriptar con nuestra semilla y encripytar con las semilla de jenkins
        // Secretos
        def decryptedCredentials = jenkinsYml.get("credentials").get("system").get("domainCredentials").get(0).get("credentials").collect(decryptCredential)

        jenkinsYml.get("credentials").get("system").get("domainCredentials").get(0).put("credentials", decryptedCredentials)

        // Ldap
        def configurationLdap = jenkinsYml.get("jenkins").get("securityRealm").get('ldap').get('configurations').get(0)
        // absisChiper -> plainText
        def ldapSecretPlainText = absisCipher.decrypt(configurationLdap.get('managerPasswordSecret'))
        // plainText -> Jenkins Secret
        configurationLdap.put('managerPasswordSecret', Secret.fromString(ldapSecretPlainText).getEncryptedValue())

        // simple $ scaping
        def jenkinsYmlTemplate = yml.dump(jenkinsYml).replaceAll('\\$', '|#|').replaceAll('\\\\', '¿')
        this.scriptContext.printOpen("================ BEFORE RENDER ================ ", EchoLevel.DEBUG)
        this.scriptContext.printOpen(jenkinsYmlTemplate, EchoLevel.DEBUG)        
        this.scriptContext.printOpen("============================= ================ ", EchoLevel.DEBUG)

        def jenkinsRendered = templateEngine.createTemplate(jenkinsYmlTemplate).make([
            environment: System.getProperty("jenkins.environment"),
            application: System.getProperty("jenkins.application"),
            component:   System.getProperty("jenkins.component")
        ])

        // revert simple $ scaping
        def jenkinsRenderedFinal = jenkinsRendered.toString().replaceAll('\\|\\#\\|', '\\$').replaceAll('¿', '\\\\')
        this.scriptContext.printOpen("================ AFTER RENDER ================ ", EchoLevel.DEBUG)
        this.scriptContext.printOpen(jenkinsRenderedFinal, EchoLevel.DEBUG)        
        this.scriptContext.printOpen("============================= ================ ", EchoLevel.DEBUG)

        // WriteOnDisk
        FileWriter writer = new FileWriter("$jenkinsHome/$pathRelative/$pathValuesYaml")
        writer.write(jenkinsRenderedFinal)
        writer.flush()
    }

    @NonCPS
    def restoreConfig(pathValuesYaml, pathRelative, name) {
        def dumperOptions = new DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        def yml = new Yaml(dumperOptions)

        def valuesYml = yml.load(("$workSpace/$pathValuesYaml" as File).text)

        def jcascConfig = valuesYml.get("controller").get("JCasC").get("configScripts").get("jenkins")

        File file = new File("$jenkinsHome/$pathRelative/$name")
        
        file.write jcascConfig
    }    

    def reloadConfig(pathRelative, pathValuesYaml) {
        ConfigurationAsCode.get().configure("$jenkinsHome/$pathRelative/$pathValuesYaml");
    }
}
