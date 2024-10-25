package com.caixabank.absis3

class PomVersionsValidator {

    private static final String VERSION = 'version'

    /**
     * This method checks if there are SNAPSHOT or RC dependencies in the pom. If so,
     * it also returns those versions in an array
     * @param project the pomXml parsed as XML
     * @return a map with the keys snapshots, releaseCandidates and incorrectVersions where
     * snapshots indicates if there are snapshot dependencies in the pom
     * releaseCandidates indicates if there are release candidate dependencies in the pom
     * incorrectVersion will not be empty if at least one of the flags defined above is true and it
     * will contain a list of the snapshot and/or release candidate versions
     */
    public Map validateDependencies(def project) {

        def versions = project.depthFirst().findAll() {
            it.name() == VERSION && it.parent().artifactId != project.artifactId
        }

        boolean snapshots = false
        boolean rcs = false
        List<String> snapshotsOrRcVersions = new ArrayList<>()

        for (def version : versions) {

            String versionValue = resolveVersion(version.text(), project)

            if (versionValue.contains(GlobalVars.SNAPSHOT_QUALIFIER)) {

                snapshots = true
                snapshotsOrRcVersions.add(versionValue)

            }
            else if (versionValue.contains(GlobalVars.RC_QUALIFIER)) {

                rcs = true
                snapshotsOrRcVersions.add(versionValue)

            } else {

                def matcher = versionValue =~/[0-9]+\.[0-9]+\.[0-9]+-[0-9]{8}\.[0-9]{6}-.*/
                if (matcher.matches()) {
                    snapshots = true
                    snapshotsOrRcVersions.add(versionValue)
                }

            }

            if (snapshots && rcs) break
        }

        return [hasSnapshots: snapshots, hasReleaseCandidates: rcs, snapshotsOrRcVersions: snapshotsOrRcVersions]
    }

    /**
     * Revuelva la versión incluso si es referenciada por una propiedad
     */
    private String resolveVersion(def version, def project) {
        def matcher = version =~/\$\{(?<version>.*)\}/
        if  (matcher && project.properties.size() > 0) {             
            return project.properties[matcher.group("version")]			
        } else {
            return version
        }
    }
}
