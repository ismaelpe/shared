package com.project.alm

import com.project.alm.BranchType
import com.project.alm.GlobalVars

class Merge {

    boolean mrMerged = false
    boolean mrClosed = false
    String state = ""
    String mrTitle = ""
    String mrDescription = ""
    String mrAuthor = ""
    String createdAt = ""


    String toString() {
        return "Merge:\n" +
                "\tmrMerged: " + mrMerged + "\n" +
                "\tmrMerged: " + mrClosed + "\n" +
                "\tmrTitle: " + mrTitle + "\n" +
                "\tmrDescriptio: " + mrDescription + "\n" +
                "\tmrAuthor: " + mrAuthor + "\n" +
                "\tcreated: " + createdAt
    }
}