package com.project.alm

class ChangelogVersion {

    String version
    //LinkedHashMap to know if the feature exists
    def featuresMap = [:]
    def featureList = []

    public ChangelogVersion(String version) {
        this.version = version
    }

    public void addFeature(String line) {
        ChangelogFeature feature = new ChangelogFeature();
        feature.parseLine(line)
        this.featuresMap.put(feature.getFeature(), feature)
        //Add in order
        featureList.add(0, feature)
    }

    public boolean addFeature(String feature, String title, String description) {

        ChangelogFeature featureItem = featuresMap.get(feature)
        //Revisar si ya está añadida
        if (featureItem == null) {
            //Sustituimos los endlines por html para mejorar el parseo
            String parsedDescription = description != null ? description.replace("\n", "<br/>") : description
            featureItem = new ChangelogFeature(feature, title, parsedDescription);
            this.featuresMap.put(feature, featureItem)
            featureList.add(0, featureItem)

            return true
        }
        return false
    }

    @Override
    public String toString() {
        String result = ChangelogFile.VERSION_LINE + version + ChangelogFile.END_LINE
        for (ChangelogFeature feature in featureList) {
            result = result + feature.toString()
        }
        return result
    }


}

