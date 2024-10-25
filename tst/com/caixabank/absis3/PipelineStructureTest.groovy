package com.caixabank.absis3;

import static org.junit.Assert.*
import groovy.util.GroovyTestCase
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.GarAppType
import org.junit.Test
import com.caixabank.absis3.PipelineStructureType

class PipelineStructureTest extends GroovyTestCase {

    @Test
    public void testGetPipelineBuildNameFEATURE() {
        PipelineData pData = new PipelineData()

        BranchStructure obj = new BranchStructure()

        obj.branchName = 'feature/#241_Super'
        obj.branchType = BranchType.FEATURE

        pData.init(obj)

        String pipelineName = pData.getPipelineBuildName()

        String pipelineNameExpected = "FEATURE_241"

        assertToString(pipelineName, pipelineNameExpected)
    }

    @Test
    public void testGetPipelineBuildNameMASTER() {
        PipelineData pData = new PipelineData()

        BranchStructure obj = new BranchStructure()

        obj.branchName = 'master'
        obj.branchType = BranchType.MASTER

        pData.init(obj)

        String pipelineName = pData.getPipelineBuildName()

        String pipelineNameExpected = "MASTER "

        assertToString(pipelineName, pipelineNameExpected)

    }

    @Test
    public void testPushToMasterFromFeature() {
        PipelineData pData = new PipelineData()

        pData.gitAction = 'PUSH'

        BranchStructure obj = new BranchStructure()

        obj.branchName = 'master'
        obj.branchType = BranchType.MASTER


        pData.commitLog = "Merge branch 'feature/REQ1_prueba' into 'master'"
        pData.init(obj)

        assertToString(pData.eventToPush, 'REQ1')
    }

    @Test
    public void testInitGarAppType() {

        PipelineData pData = new PipelineData()

        pData.gitAction = 'PUSH'
        BranchStructure obj = new BranchStructure()

        obj.branchName = 'master'
        obj.branchType = BranchType.MASTER
        pData.commitLog = "Merge branch 'feature/REQ1_prueba' into 'master'"

        pData.gitUrl = "https://git.svb.lacaixa.es/cbk/absis3/services/apps/service/demo/demo-pipeline-micro"
        pData.init(obj, ArtifactSubType.MICRO_APP, ArtifactType.SIMPLE)
        assertSame(pData.garArtifactType, GarAppType.MICRO_SERVICE)

        pData.gitUrl = "https://git.svb.lacaixa.es/cbk/absis3/services/apps/data-service/demo/demo-pipeline-micro"
        pData.init(obj, ArtifactSubType.MICRO_APP, ArtifactType.SIMPLE)
        assertSame(pData.garArtifactType, GarAppType.DATA_SERVICE)

        pData.gitUrl = "https://git.svb.lacaixa.es/cbk/absis3/services/apps/common/demo/demo-pipeline-micro"
        pData.init(obj, ArtifactSubType.MICRO_APP, ArtifactType.SIMPLE)
        assertSame(pData.garArtifactType, GarAppType.LIBRARY)

        pData.gitUrl = "https://git.svb.lacaixa.es/cbk/absis3/services/arch/audit/sca/sca-micro"
        pData.init(obj, ArtifactSubType.ARCH_LIB, ArtifactType.SIMPLE)
        assertSame(pData.garArtifactType, GarAppType.ARCH_LIBRARY)


        pData.init(obj, ArtifactSubType.MICRO_ARCH, ArtifactType.AGREGADOR)
        assertSame(pData.garArtifactType, GarAppType.ARCH_MICRO)


        pData.init(obj, ArtifactSubType.PLUGIN, ArtifactType.AGREGADOR)
        assertSame(pData.garArtifactType, GarAppType.ARCH_PLUGIN)

        pData.init(obj, ArtifactSubType.STARTER, ArtifactType.AGREGADOR)
        assertSame(pData.garArtifactType, GarAppType.ARCH_PLUGIN)

        pData.init(obj, ArtifactSubType.PLUGIN_STARTER, ArtifactType.AGREGADOR)
        assertSame(pData.garArtifactType, GarAppType.ARCH_PLUGIN)

    }


    @Test
    public void testPushToMasterFromRelease() {
        PipelineData pData = new PipelineData()

        pData.gitAction = 'PUSH'

        BranchStructure obj = new BranchStructure()

        obj.branchName = 'master'
        obj.branchType = BranchType.MASTER


        pData.commitLog = "Merge branch 'release/1.2.43' into 'master'"
        pData.init(obj)

        assertToString(pData.eventToPush, '1.2.43')
    }

    @Test
    public void testPushToMasterFromReleaseMultiBranch() {
        PipelineData pData = new PipelineData()

        //pData.gitAction='PUSH'

        BranchStructure obj = new BranchStructure()

        obj.branchName = 'master'
        obj.branchType = BranchType.MASTER


        pData.commitLog = "6effabba7a6ddc1cdff4cba29f15de824ace76d7 Merge branch 'feature/#427_cics_client' into 'master'"
        pData.init(obj)

        assertToString(pData.eventToPush, '427')
    }


    @Test
    public void testPipelineFromFromMultiple() {
        PipelineData pData = new PipelineData(PipelineStructureType.CI, 'absis-prova-molt/important-12');
        assertToString(pData.pipelineStructure.pipelineId, 'absis-prova-moltimportant-12')

    }

    @Test
    public void testPipelineFromFromSimple() {
        PipelineData pData = new PipelineData(PipelineStructureType.CI, 'absis-prova-molt-important-12');
        assertToString(pData.pipelineStructure.pipelineId, 'absis-prova-molt-important-12')

    }
}
