import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic
import java.util.ArrayList

def call(String repoGit) {

    printOpen("---------Entrando en el repo-----------", EchoLevel.ALL)

    withCredentials([
            usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD'),
            string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'GITLAB_API_TOKEN')
    ]) {
        /**
         Analizando la rama
         [default:false, protected:false, developers_can_push:false, developers_can_merge:false,
         name:release/v1.5.0,
         commit:[author_name:jenkins.pipeline.CI,
         authored_date:2019-08-21T11:34:51.000+02:00,
         committer_email:jenkins.pipeline.CI@lacaixa.es,
         committed_date:2019-08-21T11:34:51.000+02:00,
         created_at:2019-08-21T11:34:51.000+02:00,
         author_email:jenkins.pipeline.CI@lacaixa.es,
         id:27dae10de1dd5afbf2361c59b2aa9f214e31ca14,
         short_id:27dae10d, parent_ids:null,
         title:<CI_Pipeline>:  New Release generated  1.5.0,
         message:<CI_Pipeline>:  New Release generated  1.5.0,
         committer_name:jenkins.pipeline.CI],
         merged:false,
         can_push:true]
         */


        // obtener la ruta del repositorio como urlencoded para llamar a la API de
        // gitlab
        def projectPathUrlEncoded = URLEncoder.encode(repoGit - GlobalVars.gitlabDomain - '.git', "UTF-8")

        // obtener informacion del repositorio


        def projectInfo = httpRequest consoleLogResponseBody: true,
                contentType: 'APPLICATION_JSON',
                httpMode: 'GET',
                ignoreSslErrors: true,
                customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
                url: "${GlobalVars.gitlabApiDomain}${projectPathUrlEncoded}",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                validResponseCodes: '200:300'

        def json = new JsonSlurperClassic().parseText(projectInfo.content)

        def projectId = json.id

        printOpen("Id of the project ${projectId}", EchoLevel.ALL)


        def curlRepoBranches = httpRequest consoleLogResponseBody: true,
                contentType: 'APPLICATION_FORM',
                httpMode: 'GET',
                ignoreSslErrors: true,
                customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
                url: "${GlobalVars.gitlabApiDomain}${projectId}/repository/branches?page=1&per_page=400",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                validResponseCodes: '200:300'

        printOpen("Done", EchoLevel.ALL)
        def branchesList = new JsonSlurperClassic().parseText(curlRepoBranches.content)

        if (branchesList.size() > 0) {
            Branch branchActual = null
            Merge merge = null
            def listFeatureBranch = new ArrayList()
            def listReleaseBranch = new ArrayList()
            branchesList.each {
                def branch = it
                printOpen("Analizando la rama ${it}", EchoLevel.ALL)
                if (branch.name != 'master' && branch.merged == false) {
                    branchActual = new Branch(branch.name)
                    /**
                     [{"id":51165,"iid":35,"project_id":6203,"title":"feature/us20202_soporte_n4__decplugin into master",
                     "description":"soporte n4 decplugin","state":"closed","created_at":"2019-05-29T12:07:40.860+02:00","updated_at":"2019-05-29T15:45:37.144+02:00","merged_by":null,
                     "merged_at":null,
                     "closed_by":{"id":1376,"name":"LEONARDO TORRES ALTEZ","username":"U0185731","state":"active","avatar_url":"https://secure.gravatar.com/avatar/765ac6b99497bf42f6c3c32b77733784?s=80\u0026d=identicon","web_url":"https://git.svb.lacaixa.es/U0185731"},
                     "closed_at":"2019-05-29T13:45:37.161Z","target_branch":"master","source_branch":"feature/us20202_soporte_n4__decplugin","user_notes_count":3,"upvotes":0,"downvotes":0,"assignee":null,
                     "author":{"id":1376,"name":"LEONARDO TORRES ALTEZ","username":"U0185731","state":"active","avatar_url":"https://secure.gravatar.com/avatar/765ac6b99497bf42f6c3c32b77733784?s=80\u0026d=identicon","web_url":"https://git.svb.lacaixa.es/U0185731"},
                     "assignees":[],"source_project_id":6203,"target_project_id":6203,"labels":["MR ABSIS3 SERVICIOS"],"work_in_progress":false,"milestone":null,"merge_when_pipeline_succeeds":false,"merge_status":"can_be_merged","sha":"712968d470a6192dfd3c43039ca24e2f6f259bc8","merge_commit_sha":null,"discussion_locked":null,"should_remove_source_branch":null,"force_remove_source_branch":false,"reference":"!35","web_url":"https://git.svb.lacaixa.es/cbk/absis3
                     */
                    //Tiene una MR abierta?

                    if (branch.name.startsWith('feature')) {
                        branchActual.branchType = BranchType.FEATURE
                        branchActual.createdAt = branch.commit.created_at
                        branchActual.lastCommiter = branch.commit.author_name
                        branchActual.lastCommitAt = branch.commit.authored_date
                        listFeatureBranch.add(branchActual)
                    }
                    if (branch.name.startsWith('release') || branch.name.startsWith('hotfix')) {
                        branchActual.branchType = BranchType.RELEASE
                        branchActual.createdAt = branch.commit.created_at
                        branchActual.lastCommiter = branch.commit.author_name
                        branchActual.lastCommitAt = branch.commit.authored_date
                        listReleaseBranch.add(branchActual)
                    }
                    def mergeRequests = httpRequest consoleLogResponseBody: true,
                            contentType: 'APPLICATION_FORM',
                            httpMode: 'GET',
                            ignoreSslErrors: true,
                            customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
                            url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests?source_branch=${branchActual.branchName}",
                            httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                            validResponseCodes: '200:300'

                    def jsonMR = new JsonSlurperClassic().parseText(mergeRequests.content)

                    if (jsonMR.size() > 0) {
                        branchActual.merges = new ArrayList();
                        jsonMR.each {
                            merge = new Merge()
                            branchActual.merges.add(merge)
                            merge.mrTitle = it.title
                            merge.mrDescription = it.description
                            merge.mrAuthor = it.author.name
                            merge.createdAt = it.created_at
                            merge.state = it.state
                        }
                    }
                }
            }
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("***  ${repoGit}  ***", EchoLevel.ALL)
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("***********************************************************************", EchoLevel.ALL)

            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("FEATURE:", EchoLevel.ALL)
            listFeatureBranch.each {
                printOpen("- ${it.branchName} #${it.lastCommiter}#CREATED: ${it.createdAt} ${it.merges.size()} ", EchoLevel.ALL)
            }
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("RELEASE:", EchoLevel.ALL)
            listReleaseBranch.each {
                printOpen("- ${it.branchName} #${it.lastCommiter}#CREATED: ${it.createdAt} ${it.merges.size()} ", EchoLevel.ALL)
            }
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("***********************************************************************", EchoLevel.ALL)
            printOpen("***********************************************************************", EchoLevel.ALL)
            def date = new Date()
            String datePart = date.format("dd/MM/yyyy")

            String bodyEmail = "<p>Buenos dias, </p><p>El estado de las ramas del proyecto ${repoGit} a fecha de ${datePart}:</p><p> <b>Features:</b></p>"
            bodyEmail = bodyEmail + "<table styel='width:100%'><tr><th>Branch</th><th>Created at:</th><th>Last committer</th><th>OpenMR</th><th>Author OMR</th><th>Date OMR</th><th>MergedMR</th><th>Author MMR</th><th>Date MMR</th><th>ClosedMR</th><th>Author CMR</th><th>Date CMR</th></tr>"

            listFeatureBranch.each { branch ->
                if (!branch.isNewOrMerged()) {
                    bodyEmail = bodyEmail + "<tr><td>${branch.branchName}</td><td>${branch.createdAt}</td><td>${branch.lastCommiter}</td>${branch.getInfoMR()}</tr>"
                } }

            bodyEmail = bodyEmail + "</table><p> <b>Releases:</b></p>"

            bodyEmail = bodyEmail + "<table styel='width:100%'><tr><th>Branch</th><th>Created at:</th><th>Last committer</th><th>OpenMR</th><th>Author OMR</th><th>Date OMR</th><th>MergedMR</th><th>Author MMR</th><th>Date MMR</th><th>ClosedMR</th><th>Author CMR</th><th>Date CMR</th></tr>"

            listReleaseBranch.each { branch ->
                if (!branch.isNewOrMerged()) {
                    bodyEmail = bodyEmail + "<tr><td>${branch.branchName}</td><td>${branch.createdAt}</td><td>${branch.lastCommiter}</td>${branch.getInfoMR()}</tr>"
                } }
            bodyEmail = bodyEmail + "</table>"
            printOpen("${bodyEmail}", EchoLevel.ALL)

            emailext(body: "${bodyEmail}"
                    , mimeType: 'text/html'
				    , replyTo: ''
				    , from: "${GlobalVars.EMAIL_FROM_ALM}"
                    , recipientProviders: [[$class: 'DevelopersRecipientProvider']]
                    , to: "${env.ABSIS3_SERVICES_EMAIL_DISTRIBUTION_LIST}"
                    , subject: "[Absis3 PRO] Branch Report ${repoGit} ${datePart} ")
        }

    }

}
