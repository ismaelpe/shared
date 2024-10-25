package com.project.alm

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

class ExportJobs implements Serializable {
    private final def scriptContext
    private FileUtils fileUtils   
    private def exclusionList
    private boolean sanitizeExport
    
    String jenkinsHome
    String workSpace

    ExportJobs(scriptContext) {
        this(scriptContext, true)
    }
    
    ExportJobs(scriptContext, sanitizeExport) {
        this.scriptContext = scriptContext
        // The export is applied without any sanitization
        this.sanitizeExport = sanitizeExport
        this.jenkinsHome = scriptContext.env.JENKINS_HOME
        this.workSpace = scriptContext.env.WORKSPACE      
        this.fileUtils = new FileUtils(this.scriptContext)
        this.scriptContext.printOpen("Jenkins Home setted: $jenkinsHome", EchoLevel.INFO)
        this.exclusionList = ["workflow/export-jobs", "workflow/import-jobs", "workflow/maintenance/job-restore-casc-config", "workflow/maintenance/job-save-casc-config"]
    }

    def listJobs() {
        def folderList = Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).collect { return it.getConfigFile().toString() }
        def workflowJobList = Jenkins.instance.getAllItems(org.jenkinsci.plugins.workflow.job.WorkflowJob.class).collect{ return it.getConfigFile().toString() }
        def freeStyleProjectList = Jenkins.instance.getAllItems(hudson.model.FreeStyleProject.class).collect{ return it.getConfigFile().toString() }
        def workflowMultiBranchProjectList = Jenkins.instance.getAllItems(org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject.class).collect{ return it.getConfigFile().toString() }
        def matrixProjectList = Jenkins.instance.getAllItems(hudson.matrix.MatrixProject.class).collect { return it.getConfigFile().toString() }
        def matrixConfigurationList = Jenkins.instance.getAllItems(hudson.matrix.MatrixConfiguration.class).collect{ return it.getConfigFile().toString() }

        return [folders: folderList, 
                workflowJobs: workflowJobList, 
                freeStyleProjects: freeStyleProjectList, 
                workflowMultiBranchProjects: workflowMultiBranchProjectList,
                matrixProjects: matrixProjectList,
                matrixConfigurations: matrixConfigurationList]
    }

    def export(listJobs) {        
        scriptContext.printOpen("Export Resume, (sanitize jobs is '${this.sanitizeExport}'):\n" +
                            "  Folders: ${listJobs.folders.size()}\n" +
                            "  WorkflowJobList: ${listJobs.workflowJobs.size()}\n" +
                            "  FreeStyleProjectList: ${listJobs.freeStyleProjects.size()}\n" +
                            "  WorkflowMultiBranchProjectList: ${listJobs.workflowMultiBranchProjects.size()}\n" +
                            "  MatrixProjectList: ${listJobs.matrixProjects.size()}\n" +
                            "  MatrixConfigurationList: ${listJobs.matrixConfigurations.size()}", EchoLevel.INFO)
        
        createCopyFile('folders', listJobs.folders)
        createCopyFile('workflow', listJobs.workflowJobs)
        createCopyFile('freestyleprojects', listJobs.freeStyleProjects)
        createCopyFile('multibranch', listJobs.workflowMultiBranchProjects)
        createCopyFile('matrixprojects', listJobs.matrixProjects)
        createCopyFile('matrixconfiguration', listJobs.matrixConfigurations)
    }

    def createCopyFile(folder, list) {
        list.each{
            if (new File(it).exists()) {
                def jobFullPath = folder + it.replace(this.jenkinsHome, '').replace('/jobs/', '/').replace('/config.xml', '')
                if (!this.exclusionList.contains(jobFullPath)) {
                    // Sanitizamos los directorios
                    scriptContext.sh(script: "mkdir -p ${jobFullPath.replace(' ', '\\ ')} && cp ${it.replace(' ', '\\ ')} ${jobFullPath.replace(' ', '\\ ')}", returnStdout: false)                 
                    // Sanitizamos los config.xml
                    // 1. Eliminamos los secretTokens
                    scriptContext.sh(script: "sed -i -e '/<secretToken>.*<\\/secretToken>/d' ${jobFullPath.replace(' ', '\\ ')}/config.xml", returnStdout: false)
                    if (this.sanitizeExport) {
                        // 2. Eliminamos las planificaciones
                        scriptContext.sh(script: "sed -i -e '/<hudson\\.triggers\\.TimerTrigger>/,/<\\/hudson\\.triggers\\.TimerTrigger>/d' ${jobFullPath.replace(' ', '\\ ')}/config.xml", returnStdout: false)
                    }
                } else {
                     scriptContext.printOpen("$it Excluded by default",EchoLevel.INFO)
                }
            } else{ 
                scriptContext.printOpen("Something wrong $it don't exists!!", EchoLevel.ERROR)
            }
        }
    }

    def createFolderStructure(folders) {
        def path = "$workSpace/$folders"
        scriptContext.printOpen("Current path $path", EchoLevel.INFO)
        def directory = new File(path);
        createFolderStructureRecursive(directory, Jenkins.instance, true)
    }

    def createJobStructure(folders) {
        def rootElement = Jenkins.instance;
        folders.each{
            def path = "$workSpace/$it"
            def directory = new File(path);
            
            scriptContext.printOpen("Importing $it", EchoLevel.INFO)
            scriptContext.printOpen("Current path $path", EchoLevel.INFO)
            
            createFolderStructureRecursive(directory, rootElement, false)
            
            scriptContext.printOpen("Importing $it DONE!", EchoLevel.INFO)
        }
    }

    def createFolderStructureRecursive(file, previousElement, createFolders) {
        def files = file.listFiles()  

        files.each {  
            if (isExcluded(it)) {
                scriptContext.printOpen("Element $it was excluded!", EchoLevel.INFO)
            } else {
                def currentElement = null
                if (it.isDirectory()) {
                    def configFile = this.thisFolderHasConfigFile(it)
                     try {
                        currentElement = previousElement.getItem(it.getName())
                        if (configFile != null) {
                            if (currentElement == null) {
                                currentElement = previousElement.createProjectFromXML(it.getName(), new ByteArrayInputStream(configFile.getBytes())) 
                            } else {
                                currentElement.updateByXml(new StreamSource(new ByteArrayInputStream(configFile.getBytes())));
                                scriptContext.printOpen("Job $it - Updated!!", EchoLevel.INFO)
                            }
                        } else if (createFolders) {
                            if (currentElement == null) {
                                currentElement = previousElement.createProject(Folder.class, it.getName())
                            } else {
                                scriptContext.printOpen("Folder already exists $it - Nothing to do!!", EchoLevel.INFO)
                            }
                        }
                    } catch(Exception e) {
                        scriptContext.printOpen("Error in $it - $e", EchoLevel.ERROR)
                    } finally {
                        if (currentElement != null) { 
                            currentElement.save()
                            createFolderStructureRecursive(it, currentElement, createFolders)
                        }
                    }
                } 
            }             
        }   
    }

    def isExcluded(file) {
        if (exclusionList != null) {
            return exclusionList.any { file.getAbsolutePath().contains(it) }  
        } else {
            return false
        }
    }

    def thisFolderHasConfigFile(file) {
        return file.listFiles().find{ it.getName() == "config.xml" } 
    }

    def findJob(importJob) {
        def results = Jenkins.instance.getAllItems(org.jenkinsci.plugins.workflow.job.WorkflowJob.class).find{it.getName() == importJob}.collect{ return it }
        if (results != null && results.size() > 0) {
            return results[0]
        } else {
            return null
        }
    }

    def setExclusionList(exclusionList) {
        if (exclusionList != null) {
            this.exclusionList = exclusionList.tokenize("\n")
        } else {
            this.exclusionList = []
        }
    }
}
