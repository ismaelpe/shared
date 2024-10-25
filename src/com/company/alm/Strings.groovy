package com.project.alm

class Strings {

    static String[] lsOrFindOutputToArray(String lsOrFindOutput) {

        def fileArray = []

        for (String filename in lsOrFindOutput.split()) {

            filename = filename.trim()

            if (filename) {
                fileArray << filename
            }
        }

        return fileArray
    }

    static String toHtml(String text) {
        return text?.replace("\n", "<br>").replace("\"", "\\\"")
    }

}
