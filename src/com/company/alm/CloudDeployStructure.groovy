package com.project.alm

import com.project.alm.AncientVersionInfo
import com.project.alm.GlobalVars
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import java.util.List
import java.util.ArrayList
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK


class CloudDeployStructure extends DeployStructure{
	
	String url_int
	String url_ext
	String url_cdp
	String env=""
	String envCloud=""
	String org
	String space	
	String console_Admin
	String idCenter
	String memory
	String suffixedComponentName = GlobalVars.NEW_COMPONENT_PREFIX
	AncientVersionInfo ancientVersion
	List secrets
	List volumeSecrets
	String cannaryType
	String microType
	Map drivers
	
	String probeEspecial=null
	String periodReadiness=null
	String timeoutReadiness=null
	String periodLiveness=null
	String timeoutLiveness=null
	String failureThresholdLiveness=null
	String failureThresholdReadiness=null
	String initialDelaySecondsLiveness = null
	String initialDelaySecondsReadiness=null
	
	boolean hasIngress=false
	
	String ingressMaxSize=null
	String ingressConnectTimeout=null
	String ingressReadTimeout=null
	String ingressWriteTimeout=null
	//.Values.local.app.enableNonMtls
	String onlyMtls=null
	boolean withoutAnnotations=false
	
	
	boolean hasMtls=false
	boolean needsSystemRoute=true
	String nonSystemRoute=""
	boolean needsSpecialVerifyDepth=false
	String verifyDepth="2"
	String deploymentArea = "alm"
	
	
	boolean isDb2=false
	boolean isMq=false
	boolean isKafka=false

	
	public String getProbesValues() {
		if (probeEspecial!=null) {
			return "deployment:\n"+
                    "  readinessProbe:\n"+
					"    initialDelaySeconds: "+initialDelaySecondsReadiness+"\n"+
					"    periodSeconds: "+periodReadiness+"\n"+
					"    timeoutSeconds: "+timeoutReadiness+"\n"+
					"    failureThreshold: "+failureThresholdReadiness+"\n"+
					"  livenessProbe:\n"+
					"    initialDelaySeconds: "+initialDelaySecondsLiveness+"\n"+
					"    periodSeconds: "+periodLiveness+"\n"+
					"    timeoutSeconds: "+timeoutLiveness+"\n"+
					"    failureThreshold: "+failureThresholdLiveness+"\n"
		}else {
			return ""
		}
	}
	
	public String getUrlPrefixTesting(boolean affinityUrlUp = true) {
		if ("pro".equals(env.toLowerCase()) && affinityUrlUp) {
			 return "https://api.pro.internal.project.com"
		} else {
			 return "https://k8sgateway"
		}
	}
	
	public String getUrlSuffixIntegrationTesting(String center, boolean affinityUrlUp = true) {
		if ("pro".equals(env.toLowerCase()) && affinityUrlUp) {
			return "/tech/alm-alm"
		}else {
			return getUrlSuffixTesting(center)
		}		
	}
	
	public String getUrlActuatorPrefixTesting() {
		return "https://k8sgateway"
	}
	
	public String getUrlSuffixTesting(String center = "ALL") {
		//new-democonnecta2-micro-2.dev.ap.intranet.cloud.lacaixa.es
		if (center=="ALL") {
			return "."+envCloud.toLowerCase()+".int.srv.project.com"
		}else {
			if (center=="AZ1") {
				return "."+envCloud.toLowerCase()+".cloud-1.alm.cloud.lacaixa.es"
			} else {
				return "."+envCloud.toLowerCase()+".cloud-2.alm.cloud.lacaixa.es"
			}
		}
	}
	
	
	public String getUrlPrefixApiGateway(String center = "ALL") {
		if (center=="ALL") {
			return "https://api-gateway-1." + envCloud.toLowerCase() +".int.srv.project.com"
		}else {
			if (center=="AZ1") {
				return "https://api-gateway-1." + envCloud.toLowerCase() +".cloud-1.alm.cloud.lacaixa.es"
			} else {
				return "https://api-gateway-1." + envCloud.toLowerCase() +".cloud-2.alm.cloud.lacaixa.es"
			}
		}
	}
	
	public String getUrlSuffixTestingPerCenter() {
		return getUrlSuffixTesting()
	}
	
	
	public String getSuffixedComponentName() {
		if ("pro".equals(env.toLowerCase())) return GlobalVars.BETA_COMPONENT_SUFFIX
		else return GlobalVars.NEW_COMPONENT_PREFIX
	}	
	
	
	public CloudDeployStructure(String url_int,String url_ext,String environment) {
		this.env=environment
		
		if (this.env.equalsIgnoreCase("eden")) {
			 this.envCloud="DEV"
		}else{
			this.envCloud=environment
		}
		this.url_int=url_int+'-'+this.envCloud.toLowerCase()
		this.url_ext=url_ext+'-'+this.envCloud.toLowerCase()
		
		this.secrets=new ArrayList()
		this.volumeSecrets=new ArrayList()
	}
		
	public CloudDeployStructure(String url_int,String url_ext,String url_cdp,String environment,String org, String space, String console_Admin,String idCenter) {
		this.url_int=url_int
		this.url_ext=url_ext
		this.url_cdp=url_cdp
		this.env=environment
		this.org=org
		this.space=space
		this.console_Admin=console_Admin
		this.idCenter=idCenter
		this.secrets=new ArrayList()
		this.volumeSecrets=new ArrayList()
	}
	
	
	public String getEnvVariables(String garAppType, String appName,String appMajorVersion,String domain,String subDomain,String company) {
		String ALM_BlueGreen="'\n    ALM_BLUE_GREEN: G'"
		
		String additionalNonProxyHosts = "gelogasr01.lacaixa.es,"+this.url_int+","+this.url_ext+","+GlobalVars.HTTP_ADDITIONAL_NON_PROXY_HOSTS;
		if (this.env.equalsIgnoreCase("eden") || this.env.equalsIgnoreCase("dev")) {
		    additionalNonProxyHosts += ",cxb-ab3cor-tst"
		} else if (this.env.equalsIgnoreCase("tst")) {
            additionalNonProxyHosts += ",cxb-ab3app-dev"
        }
		String nonProxyHost = additionalNonProxyHosts.replaceAll(",","|*.")
		
		/**
		 * {{- if $.Values.local.app.ingress.defineBodySize }}
    ingress.kubernetes.io/proxy-body-size: {{ $.Values.local.app.ingress.maxBodySize }}

		 */
		String maxSize=""
		if (ingressMaxSize!=null) {
			maxSize="      defineBodySize: true\n"+
			        "      maxBodySize: "+ingressMaxSize+"m\n"
		}
		
		String timeouts=""
		if (ingressConnectTimeout!=null && ingressReadTimeout!=null && ingressWriteTimeout!=null) {
			timeouts="      defineTimeout: true\n"+
			         "      sendTimeout: "+ ingressWriteTimeout + "\n"+
					 "      readTimeout: "+ ingressReadTimeout +"\n"+ 
					 "      connectTimeout: "+ ingressConnectTimeout +"\n"
		}
		
		
		String onlyMtls=""
		if (onlyMtls!=null) {
			onlyMtls="    enableNonMtls: true\n"
		}
		
		String withoutAnnotationsS=""
		if (withoutAnnotations) {
			withoutAnnotationsS="    enableAnnotations: false\n"
		}
		
		String yamlLocal=getProbesValues()+
		       "local:\n"+
		       "  app:\n"+		
			   onlyMtls+
			   withoutAnnotationsS+
			   "    ingress:\n"+
			   "      enabled: "+hasIngress+"\n"+
			   "      deploymentArea: "+deploymentArea+"\n"+
			   maxSize +
			   timeouts +
			   "      alm:\n"+
			   "        enabled: true\n"+
			   "      mtls:\n"+
			   "        enabled: "+hasMtls+"\n"+
			   "        needsSystemRoute: "+needsSystemRoute+"\n"+
			   "        route: "+nonSystemRoute+"\n"+
			   "        needsSpecialVerifyDepth: "+needsSpecialVerifyDepth+ "\n"+
			   "        verifyDepth: "+verifyDepth+"\n"+
		       "    envVars:\n"+
			   "      - name: ALM_APP_ID\n"+
			   "        value: "+appName+"\n"+
			   "      - name: ALM_CENTER_ID\n"+
			   "        value: 1\n"+
			   "      - name: ALM_APP_TYPE\n"+
			   "        value: "+garAppType+"\n"+
			   "      - name: ALM_ENVIRONMENT\n"+
			   "        value: "+env.toLowerCase()+"\n"+
			   "      - name: ALM_APP_DOMAIN\n"+
			   "        value: "+domain+"\n"+
			   "      - name: ALM_APP_SUBDOMAIN\n"+
			   "        value: "+subDomain+"\n"+
			   "      - name: ALM_APP_COMPANY\n"+
			   "        value: "+company+"\n"+
			   "      - name: JAVA_OPTS\n"+
			   "        value: '-Dspring.cloud.config.failFast=true'\n"+
			   "      - name: nonProxyHosts\n"+
			   "        value: '"+nonProxyHost+"'\n"+
			   "      - name: http.additionalNonProxyHosts\n"+
			   "        value: '"+additionalNonProxyHosts+"'\n"+
			   "      - name: NO_PROXY\n"+
			   "        value: "+this.url_int+"\n"+
			   "      - name: CF_INSTANCE_INDEX\n"+//debe ser eliminado
			   "        value: 1\n"+
			   "      - name: spring.cloud.config.failFast\n"+
			   "        value: true\n"+
			   "      - name: SPRING_PROFILES_ACTIVE\n"+
			   "        value: "+springProfilesActive+"\n"+
			   "      - name: ALM_Cloud_ENVIRONMENT\n"+
			   "        value: "+this.envCloud.toLowerCase()+"\n"+
			   "      - name: "+GlobalVars.CANARY_TYPE_PROPERTY+"\n"+
			   "        value: "+this.cannaryType+"\n"
			   
		yamlLocal=yamlLocal+"    secrets:\n"
		//Lo añadimos siempre
		yamlLocal=yamlLocal+"      - name: alm-kafka-cloudbus\n"
	    if (secrets!=null && secrets.size()>0) {
			String secretName=""
			boolean isVolumeSecret=false
			secrets.each{
				secretName=it
				isVolumeSecret=false
				
				volumeSecrets.each{
					if (it.equals(secretName)) isVolumeSecret=true
				}
				
				if (!it.startsWith("eureka") && !isVolumeSecret) {
					//No se añade eureka lo tenemos pero no lo usamos
					yamlLocal=yamlLocal+"      - name: "+it+"\n"					
				}				
			}
		}
		
		if (volumeSecrets!=null && volumeSecrets.size()>0) {
			yamlLocal=yamlLocal+"    volumeSecrets:\n"
			volumeSecrets.each{
				//No se añade eureka lo tenemos pero no lo usamos
				yamlLocal=yamlLocal+"      - name: "+it+"\n"				
			}
		}
		
		return yamlLocal
	}

    //FIXME: Remove this when cloud and cloud+env profiles are deprecated
    public String calculateSpringCloudActiveProfiles(boolean useHealthGroups) {
        String activeProfiles = "cloud,litmid,"+this.env+",cloud,cloud"+this.env
		activeProfiles += useHealthGroups ? ",healthgroups" : ""
        return activeProfiles.toLowerCase()
    }

    public String calculateSpringCloudActiveProfiles(String garAppType, String company, boolean useHealthGroups) {
        String activeProfiles = calculateSpringCloudActiveProfiles(useHealthGroups)
        boolean isBusinessApplication = GarAppType.MICRO_SERVICE.equalsName(garAppType) || GarAppType.DATA_SERVICE.equalsName(garAppType)
        def garAppTypeProfile = isBusinessApplication ? "app" : "arch"
        activeProfiles += ","+garAppTypeProfile
        activeProfiles += company ? ","+garAppTypeProfile+company.toLowerCase() : ""
        return activeProfiles.toLowerCase()
    }

	public String getVipaPerCenter() {
		return url_int.subSequence(0, 3)+idCenter+url_int.substring(3)
	}
	
	public def initSecretsFromYamlResources(String content) {
		def opts = new DumperOptions()
		opts.setDefaultFlowStyle(BLOCK)
		Yaml yaml= new Yaml(opts)
		
		Map valuesDeployed=(Map)yaml.load( content)
		if (valuesDeployed.get('resources')!=null) {
			Map resourcesApp=valuesDeployed.get('resources')
			if (resourcesApp.get('secrets')!=null) {
				List secretsTmp=resourcesApp.get('secrets')
				secretsTmp.each {
					if (!"configserver".equals(it) && !"alm-kafka-cloudbus".equals(it)) {
						volumeSecrets.add(it)
					}
				}
			}
		}		
	}
	
	public def initSecretsMemoryFromYaml(String content) {
		def opts = new DumperOptions()
		opts.setDefaultFlowStyle(BLOCK)
		Yaml yaml= new Yaml(opts)
		
		Map valuesDeployed=(Map)yaml.load( content)
		
		//[applications:[[memory:786M, env:[SPRING_PROFILES_ACTIVE:cloud,pro, JAVA_OPTS:-Dspring.cloud.config.failFast=true, ALM_APP_ID:demoConnectA2, ALM_APP_TYPE:SRV.MS, ALM_CENTER_ID:1, ALM_ENVIRONMENT:pro, ALM_SPACE:SRV_PRO, ALM_APP_DOMAIN:demoArquitectura, ALM_APP_SUBDOMAIN:NO_SUBDOMAIN, ALM_APP_COMPANY:CBK, http.additionalNonProxyHosts:pro.int.srv.project.com,pro.ext.srv.project.com, ALM_BLUE_GREEN:G, JBP_CONFIG_OPEN_JDK_JRE:{ jre: { version: 11.+ } }], services:[configserver]]]]
		Map application=valuesDeployed.get('applications').getAt(0)
	
		if (application.get('memory')!=null) {
			memory=application.get('memory').substring(0,application.get('memory').length()-1)	
		}		
		
		if (application.get('services')!=null) {
			List secretsTmp=application.get('services')
			secretsTmp.each {  
				if (!"configserver".equals(it) &&  !"alm-kafka-cloudbus".equals(it)) {
					secrets.add(it)
				}
			}
		}
		
		return valuesDeployed
	}
	
}
