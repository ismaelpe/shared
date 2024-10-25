import com.project.alm.*

class ServicesCatalogQueueHandler extends AbstractQueueHandler implements Serializable {
 
    static final String SCU_QUEUE_NAME = "services-catalog"
    static final String SCU_SERVICES_CATALOG_REPO_URL = "https://git.svb.lacaixa.es/cbk/alm/services/documentation/services-catalog.git"
    static final String SCU_GIT_REPO_PATH = "services-catalog-git-repo"
    //static final String SCU_JOB_URL = "http://buildasp07.lacaixa.es:8090/job/alm/job/services/job/arch/job/alm/job/job-services-catalog-updater/build?token=EDTLz6zHeUtyc5yUXZdAnR7BqtA7unuW"
 
    ServicesCatalogQueueHandler(scriptContext, gitRepoUrl, gitRepoPath) {
        super(scriptContext, gitRepoUrl, gitRepoPath)
    }
 
    static ServicesCatalogQueueHandler init(scriptContext) {
        ServicesCatalogQueueHandler instance = new ServicesCatalogQueueHandler(scriptContext, SCU_SERVICES_CATALOG_REPO_URL, SCU_GIT_REPO_PATH)
        instance.initQueuePaths()
        return instance
    }
 
    @Override
    String getGitRepoUrl() {
        return SCU_SERVICES_CATALOG_REPO_URL
    }
 
    @Override
    String getQueueName() {
        return SCU_QUEUE_NAME
    }
 
    @Override
    String getJenkinsUpdaterJobName() {        
        return "${scriptContext.env.JENKINS_URL}/job/alm/job/services/job/arch/job/alm/job/job-services-catalog-updater/build?token=${scriptContext.env.GPL_PSWR}";
    }
}
