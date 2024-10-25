package com.caixabank.absis3

import java.util.regex.Matcher
import java.util.regex.Pattern
import com.caixabank.absis3.MavenVersionUtilities
import com.caixabank.absis3.GlobalVars

class MavenUtils {

    private static final String DEPENDENCY_REGEX = /(?ms)(\[INFO] The following files have been resolved:\n)(.*?)(\[INFO]\n|\[INFO] \n)/

    static String getDeploymentRepository(String artifactVersion) {
        def deploymentRepo = ""
        // Comprobamo s si es snapshot o release para ver que repo poner.
        if (MavenVersionUtilities.isSNAPSHOT(artifactVersion)) {
            deploymentRepo = GlobalVars.MVN_SNAPSHOT_DEPLOYMENT_REPO
        } else {
            deploymentRepo = GlobalVars.MVN_RELEASE_DEPLOYMENT_REPO
        }

        return "-DaltDeploymentRepository=${deploymentRepo}"
    }

    static String sanitizeArtifactName(String name, GarAppType garAppType) {

        if (garAppType == GarAppType.ARCH_MICRO) {
            name = name - '-micro'
        } else if (garAppType == GarAppType.ARCH_PLUGIN) {
            name = name - '-lib'
            name = name - '-spring-boot-starter'
            name = name - '-starter'
            name = name - '-plugin'
        } else if (garAppType == GarAppType.ARCH_LIBRARY ) {
            if (name.length() > 16 && name.endsWith('-starter')) name = name - '-starter'
            name = name - '-lib'
        } else if (garAppType == GarAppType.LIBRARY) {
            name = name - '-lib'
            if (name.length() > 16 && name.endsWith('-starter')) name = name - '-starter'
        } else if (garAppType == GarAppType.MICRO_SERVICE) {
            name = name - '-micro'
        } else if (garAppType == GarAppType.DATA_SERVICE) {
            name = name - '-micro'
        } else if (garAppType == GarAppType.BFF_SERVICE) {
            name = name - '-bff'
        } else if (garAppType == GarAppType.SRV_CONFIG || garAppType == GarAppType.ARCH_CONFIG ) {
            name = name - '-conf'
        }
        
        return name.replace('-', '')
    }

    static String reduceLogToNLines(String stdout, int numberOfLines) {

        List<String> stdOutList = stdout.split("\n")
        int listSize = stdOutList.size()
        return stdOutList.subList(listSize - numberOfLines, listSize).join("\n")

    }

    static String reduceLogToMvnErrorLines(String stdout) {

        String regex = "(\\[ERROR].*(?:\\n*[^\\[]*\\n*)*)"
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE)
        Matcher matcher = pattern.matcher(stdout)
        List<String> matchListResult = []
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                matchListResult.add(matcher.group(i).trim())
            }
        }
        return matchListResult.join("\n")

    }

    static extractDependenciesFromLog(def log) {

        Matcher matches = log =~ DEPENDENCY_REGEX

        int numberOfMatches = matches.getCount()

        def dependencies = []

        for (int i=0; i<numberOfMatches; i++) {

            String moduleDependencies = matches[i][2]?.toString().trim().replace("[INFO]    ", "")
            dependencies += moduleDependencies.split("\n")

        }

        return dependencies
    }

}
