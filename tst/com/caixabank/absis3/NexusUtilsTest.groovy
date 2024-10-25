package com.caixabank.absis3

import static org.junit.Assert.*
import groovy.util.GroovyTestCase
import com.caixabank.absis3.NexusUtils

import org.junit.Test

class NexusUtilsTest extends GroovyTestCase {


    String logMaven1 = '[INFO] Progress (1): 41 MB\n' +
            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/demo-pipeline-micro-3.34.0-20181218.155205-4.jar (41 MB at 7.0 MB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/demo-pipeline-micro-3.34.0-20181218.155205-4.pom\n' +
            '[INFO] Progress (1): 947 B\n' +
            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/demo-pipeline-micro-3.34.0-20181218.155205-4.pom (947 B at 18 kB/s)\n' +
            '[INFO] Downloading from maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/maven-metadata.xml\n' +
            '[INFO] Progress (1): 781 B		\n' +
            '[INFO] Downloaded from maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/maven-metadata.xml (781 B at 30 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/maven-metadata.xml\n' +
            '[INFO] Progress (1): 1.0 kB\n' +
            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/maven-metadata.xml (1.0 kB at 20 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/maven-metadata.xml\n' +
            '[INFO] Progress (1): 781 B\n' +
            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/maven-metadata.xml (781 B at 16 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/demo-pipeline-micro-3.34.0-20181218.155205-4-sources.jar\n' +
            '[INFO] Progress (1): 2.0/3.4 kB\n' +
            '[INFO] Progress (1): 3.4 kB\n' +
            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/demo-pipeline-micro-3.34.0-20181218.155205-4-sources.jar (3.4 kB at 40 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/maven-metadata.xml\n' +
            '[INFO] Progress (1): 1.0 kB\n' +
            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/demo-pipeline-micro/3.34.0-SNAPSHOT/maven-metadata.xml (1.0 kB at 11 kB/s)\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD SUCCESS\n'

    String logMaven2 = '[INFO] Downloaded from maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/maven-metadata.xml (1.0 kB at 26 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/monitoring-api-lib-1.0.0-20190107.145804-25.jar\n' +
            '[INFO] Progress (1): 2.0/9.0 kB\n' +
            '[INFO] Progress (1): 4.1/9.0 kB\n' +
            '[INFO] Progress (1): 6.1/9.0 kB\n' +
            '[INFO] Progress (1): 8.2/9.0 kB\n' +
            '[INFO] Progress (1): 9.0 kB\n' +

            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/monitoring-api-lib-1.0.0-20190107.145804-25.jar (9.0 kB at 79 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/monitoring-api-lib-1.0.0-20190107.145804-25.pom\n' +
            '[INFO] Progress (1): 937 B\n' +

            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/monitoring-api-lib-1.0.0-20190107.145804-25.pom (937 B at 11 kB/s)\n' +
            '[INFO] Downloading from maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/maven-metadata.xml\n' +
            '[INFO] Progress (1): 313 B\n' +

            '[INFO] Downloaded from maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/maven-metadata.xml (313 B at 8.0 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/maven-metadata.xml\n' +
            '[INFO] Progress (1): 1.0 kB\n' +

            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/maven-metadata.xml (1.0 kB at 15 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/maven-metadata.xml\n' +
            '[INFO] Progress (1): 313 B\n' +

            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/maven-metadata.xml (313 B at 4.7 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/monitoring-api-lib-1.0.0-20190107.145804-25-sources.jar\n' +
            '[INFO] Progress (1): 2.0/2.9 kB\n' +
            '[INFO] Progress (1): 2.9 kB\n' +

            '[INFO] Uploaded to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/monitoring-api-lib-1.0.0-20190107.145804-25-sources.jar (2.9 kB at 51 kB/s)\n' +
            '[INFO] Uploading to maven-snapshots: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-snapshots/com/caixabank/absis/arch/monitoring/monitoring-api-lib/1.0.0-SNAPSHOT/maven-metadata.xml\n' +
            '[INFO] Progress (1): 1.0 kB\n'

    String logMavenASEBuild = '[INFO] Progress (1): 504/505 kB\n' +
        '[INFO] Progress (1): 505 kB    \n' +
        '                    \n' +
        '[INFO] Uploaded to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/1.0.0/se-restchecklist-connector-spring-boot-starter-1.0.0.jar (505 kB at 5.4 MB/s)\n' +
        '[INFO] Uploading to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/1.0.0/se-restchecklist-connector-spring-boot-starter-1.0.0.pom\n' +
        '[INFO] Progress (1): 2.6 kB\n' +
        '                    \n' +
        '[INFO] Uploaded to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/1.0.0/se-restchecklist-connector-spring-boot-starter-1.0.0.pom (2.6 kB at 21 kB/s)\n' +
        '[INFO] Downloading from maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/maven-metadata.xml\n' +
        '[INFO] Progress (1): 457 B\n' +
        '                   \n' +
        '[INFO] Downloaded from maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/maven-metadata.xml (457 B at 33 kB/s)\n' +
        '[INFO] Uploading to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/maven-metadata.xml\n' +
        '[INFO] Progress (1): 463 B\n' +
        '                   \n' +
        '[INFO] Uploaded to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/maven-metadata.xml (463 B at 11 kB/s)\n' +
        '[INFO] Uploading to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/1.0.0/se-restchecklist-connector-spring-boot-starter-1.0.0-sources.jar\n' +
        '[INFO] Progress (1): 4.1/81 kB\n' +
        '[INFO] Progress (1): 8.2/81 kB\n' +
        '[INFO] Progress (1): 12/81 kB \n' +
        '[INFO] Progress (1): 16/81 kB\n' +
        '[INFO] Progress (1): 20/81 kB\n' +
        '[INFO] Progress (1): 25/81 kB\n' +
        '[INFO] Progress (1): 29/81 kB\n' +
        '[INFO] Progress (1): 33/81 kB\n' +
        '[INFO] Progress (1): 37/81 kB\n' +
        '[INFO] Progress (1): 41/81 kB\n' +
        '[INFO] Progress (1): 45/81 kB\n' +
        '[INFO] Progress (1): 49/81 kB\n' +
        '[INFO] Progress (1): 53/81 kB\n' +
        '[INFO] Progress (1): 57/81 kB\n' +
        '[INFO] Progress (1): 61/81 kB\n' +
        '[INFO] Progress (1): 66/81 kB\n' +
        '[INFO] Progress (1): 70/81 kB\n' +
        '[INFO] Progress (1): 74/81 kB\n' +
        '[INFO] Progress (1): 78/81 kB\n' +
        '[INFO] Progress (1): 81 kB   \n' +
        '                   \n' +
        '[INFO] Uploaded to maven-releases: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/arch/backend/ase/se-restchecklist-connector-spring-boot-starter/1.0.0/se-restchecklist-connector-spring-boot-starter-1.0.0-sources.jar (81 kB at 1.3 MB/s)\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] BUILD SUCCESS\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Total time:  46.855 s\n' +
        '[INFO] Finished at: 2020-04-15T13:06:03Z\n' +
        '[INFO] ------------------------------------------------------------------------'

    @Test
    public void testExtractArtifactsFromLog() {

        def listArtifacts = NexusUtils.extractArtifactsFromLog(logMaven1)
        println("ELS VALUES 1SON DE" + listArtifacts)

        assert listArtifacts.size() == 3, 'incorrect value needs 3'
    }

    @Test
    public void testGetBuildId() {

        def listArtifacts = NexusUtils.extractArtifactsFromLog(logMaven1)
        println("ELS VALUES 1SON DE" + listArtifacts)
        println "ELS VALUES SON DE " + NexusUtils.getBuildId(listArtifacts, 'demo-pipeline-micro' + '-', '3.34.0-')
        assert NexusUtils.getBuildId(listArtifacts, 'demo-pipeline-micro' + '-', '3.34.0-').equals('20181218.155205-4'), 'expected value 20181218.155205-'
    }

    @Test
    public void testGetBuildIdFromAgregador() {

        def listArtifacts1 = NexusUtils.extractArtifactsFromLog(logMaven2)
        println("AGREGADORS ELS VALUES 1SON DE" + listArtifacts1)
        println "ELS VALUES SON DE " + NexusUtils.getBuildId(listArtifacts1, 'monitoring-micro' + '-', '1.0.0-')
        assert NexusUtils.getBuildId(listArtifacts1, 'monitoring-micro' + '-', '1.0.0-').equals('20190107.145804-25'), 'expected value 20190107.145804-25'
    }

    @Test
    public void testGetBuildIdFromASECloseRelease() {

        def dummyArtifactList = ["se-restchecklist-connector-spring-boot-starter-1.0.0.pom"]
        def values = NexusUtils.getBuildId(dummyArtifactList, '1.0.0')
        println "ELS VALUES SON DE " + values
        assert values.equals('F'), 'expected F'
    }

    @Test
    public void testGetBuildIdFromASEReleaseBuild() {

        def listArtifacts = NexusUtils.extractArtifactsFromLog(logMavenASEBuild)
        println("ELS VALUES 1SON DE" + listArtifacts)
        def values = NexusUtils.getBuildId(listArtifacts, '1.0.0')
        println "ELS VALUES SON DE " + values
        assert values.equals('F'), 'expected F'
    }

    @Test
    public void testGetBuildIdFromASEReleaseCandidateBuild() {

        def listArtifacts = NexusUtils.extractArtifactsFromLog(logMavenASEBuild.replace("1.0.0","1.0.0-RC1"))
        println("ELS VALUES 1SON DE" + listArtifacts)
        def values = NexusUtils.getBuildId(listArtifacts, '1.0.0-RC1')
        println "ELS VALUES SON DE " + values
        assert values.equals('RC1'), 'expected value RC1'
    }

    @Test
    public void testGetBuildIdFromASESnapshotBuild() {

        def listArtifacts = NexusUtils.extractArtifactsFromLog(
            logMavenASEBuild.replace("1.0.0","1.0.0-SNAPSHOT").replace("starter-1.0.0-SNAPSHOT","starter-1.0.0-20200415.111212-1")
        )
        println("ELS VALUES 1SON DE" + listArtifacts)
        def values = NexusUtils.getBuildId(listArtifacts, '1.0.0-SNAPSHOT')
        println "ELS VALUES SON DE " + values
        assert values.equals('20200415.111212-1'), 'expected value 20200415.111212-1'
    }
}
