package com.caixabank.absis3;

import static org.junit.Assert.*
import groovy.util.GroovyTestCase
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.GarAppType
import org.junit.Test
import com.caixabank.absis3.PipelineStructureType
import groovy.json.StringEscapeUtils

class PipelineDataTest extends GroovyTestCase {

    public void testGivenGitUrlgetRelativeUrl() {
        PipelineData pData = new PipelineData()

        pData.gitUrl = 'https://git.svb.lacaixa.es/cbk/absis3/services/myProject.git'

        assertToString(pData.getGitUrlProjectRelative(), 'cbk/absis3/services/myProject')
    }


    public void testNewPipeline() {
        PipelineData pData = new PipelineData(PipelineStructureType.RELEASE_CANDIDATE, 'jenkins-absis3-services-arch-common-absis-micro-common-starter-feature%2F%23495_integration_tests-7')
        try {
            println("Els VALORS " + StringEscapeUtils.escapeJava('jenkins-absis3-services-arch-common-absis-micro-common-starter-feature%2F%23495_integration_tests-7'))
            println("Els VALORS " + StringEscapeUtils.unescapeJava('jenkins-absis3-services-arch-common-absis-micro-common-starter-feature%2F%23495_integration_tests-7'))
        } catch (Exception ex) {
            ex.printStackTrace()

        }
        assertToString(pData.pipelineId, StringEscapeUtils.escapeJava(pData.pipelineId))
    }
	
	
	public void testGetEventToPush(){
		PipelineData pData = new PipelineData()
		pData.gitAction='PUSH'
		pData.commitLog="07ddae321c9f4ebeb8f5dd5fd3a33a27f8e12e3e Merge branch 'feature/BBDD667Merge_Tablas' into 'master'"
		pData.gitUrl="https://git.svb.lacaixa.es/cbk/absis3/services/apps/cbk/data-service/demo/arqrun.git"
		pData.gitProject="arqrun"
		BranchStructure branchStructure=new BranchStructure()
		branchStructure.branchName='master'
		branchStructure.branchType=BranchType.MASTER
		//(BranchStructure branch, ArtifactSubType artifactSubtype, ArtifactType artifactType, boolean isArchetype, String archetypeModel)
		pData.init(branchStructure,ArtifactSubType.MICRO_APP,ArtifactType.SIMPLE,false,)
		assertToString(pData.eventToPush, "BBDD667Merge")
		
	}

}
