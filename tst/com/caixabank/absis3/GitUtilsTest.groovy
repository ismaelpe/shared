package com.caixabank.absis3

import static org.junit.Assert.*
import groovy.util.GroovyTestCase
import com.caixabank.absis3.GitUtils

import org.junit.Test

class GitUtilsTest extends GroovyTestCase {


    @Test
    public void testGetGitProjectFromUrl() {

        def gitProject = GitUtils.getProjectFromUrl('https://git.svb.lacaixa.es/carpeta/dominio/proyect.git')
        println("El Proyect es de" + gitProject)

        assert 'proyect'.equals(gitProject), 'incorrect gitProject'
    }

    @Test
    public void testGetGitProjectFromProject() {

        def gitProject = GitUtils.getProjectFromUrl('proyect.git')
        println("El Proyect es de" + gitProject)

        assert 'proyect'.equals(gitProject), 'incorrect gitProject'
    }

    @Test
    public void testGetGitProjectFromProjectIncorrect() {

        def gitProject = GitUtils.getProjectFromUrl('proyect')
        println("El Proyect es de" + gitProject)

        assert 'proyect'.equals(gitProject), 'incorrect gitProject'
    }


    @Test
    public void testGivenGitCbkUrlGetDomain() {

        String domain = GitUtils.getDomainFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/apps/cbk/data-service/demo/arqrun-micro')

        println("El Dominio es =" + domain)

        assert domain == 'demo'

    }

    @Test
    public void testGivenGitCbkBizarreUrlGetDomain() {

        String domain = GitUtils.getDomainFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/apps/cbk/data-service/demo/ideins/arqrun-micro')

        println("El Dominio es =" + domain)

        assert domain == 'ideins'

    }

    @Test
    public void testGivenGitBpiUrlGetDomain() {


        String domain = GitUtils.getDomainFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/apps/bpi/data-service/demo/app/arqrun-micro')


        assert domain == 'demo'

    }

    @Test
    public void testGivenGitUrlGetDomain() {


        String domain = GitUtils.getDomainFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/apps/cbk/data-service/demo/arqrun-micro')

        assertToString(domain, 'demo')
    }

    @Test
    public void testIsBpiRepoTrue() {

        boolean isBpiRepoApps = GitUtils.isBpiRepo('https://git.svb.lacaixa.es/cbk/absis3/services/apps/bpi/data-service/demo/app/arqrun-micro')
        boolean isBpiRepoCics = GitUtils.isBpiRepo('https://git.svb.lacaixa.es/cbk/absis3/services/definitions/cics/cics-his')

        assert (isBpiRepoApps && isBpiRepoCics) == true
    }

    @Test
    public void testIsBpiRepoFalse() {

        boolean isBpiRepo = GitUtils.isBpiRepo('https://git.svb.lacaixa.es/cbk/absis3/services/apps/cbk/data-service/demo/arqrun-micro')

        assert isBpiRepo == false
    }

    @Test
    public void testisBpiArchRepoTrue() {

        boolean isBpiArchRepo = GitUtils.isBpiArchRepo('https://git.svb.lacaixa.es/cbk/absis3/services/arch/bpi/audit/je/je-micro')

        assert isBpiArchRepo == true
    }

    @Test
    public void testisBpiArchRepoFalse() {

        boolean isBpiArchRepo = GitUtils.isBpiArchRepo('https://git.svb.lacaixa.es/cbk/absis3/services/arch/core/absis3core-lib')

        assert isBpiArchRepo == false

    }

    @Test
    public void testIsCbkCompanyTrue() {
        PipelineData pData = new PipelineData()

        String company = GitUtils.getCompanyFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/apps/cbk/data-service/demo/arqrun-micro')

        assertToString(company, 'CBK')

    }

    @Test
    public void testIsBpiCompanyTrue() {
        PipelineData pData = new PipelineData()

        String company = GitUtils.getCompanyFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/apps/bpi/data-service/demo/app/arqrun-micro')

        assertToString(company, 'BPI')

    }

    @Test
    public void testIsCorpCompanyTrue() {
        PipelineData pData = new PipelineData()

        String company = GitUtils.getCompanyFromUrl('https://git.svb.lacaixa.es/cbk/absis3/services/arch/backend/cics/cics-micro')

        assertToString(company, 'CORP')

    }
}
