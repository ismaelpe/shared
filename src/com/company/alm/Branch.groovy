package com.project.alm

import com.project.alm.BranchType
import java.util.List
import java.util.ArrayList
import java.text.SimpleDateFormat


class Branch {

    String branchName
    BranchType branchType
    String createdAt
    String lastCommiter
    String lastCommitAt
    List<Merge> merges = new ArrayList<Merge>()

    Branch(String nameB) {
        branchName = nameB
    }


    //2019-08-07T14:04:35.000+02:00
    boolean isNewOrMerged() {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(createdAt.substring(0, 10))
        Date actual = new Date().plus(-14)

        if (date < actual) {
            int numOpen = 0
            int numMerged = 0
            //Tiene mas de 14 dias de antiguedad
            //Vamos a ver si tiene al menos una MR abierta y ninguna opened
            //Si es asi... miraremos si el lastcommiter es jenkins
            merges.each {
                if (it.state == 'opened') {
                    numOpen = numOpen + 1
                }
                if (it.state == 'merged') {
                    numMerged = numMerged + 1
                }
            }
            if (numOpen == 0 && numMerged > 0 && lastCommiter.startsWith('jenkins')) {
                return true
            }
            return false
        } else return true
    }

    String getInfoMR() {
        String returnValue = ''
        int numOpen = 0
        int numCLosed = 0
        int numMerged = 0
        String openAuthor = ''
        String openDate = ''
        String closedAuthor = ''
        String closedDate = ''
        String mergedAuthor = ''
        String mergedDate = ''

        merges.each {
            if (it.state == 'opened') {
                numOpen = numOpen + 1
                openAuthor = it.mrAuthor
                openDate = it.createdAt
            }
            if (it.state == 'closed') {
                numCLosed = numCLosed + 1
                closedAuthor = it.mrAuthor
                closedDate = it.createdAt
            }
            if (it.state == 'merged') {
                numMerged = numMerged + 1
                mergedAuthor = it.mrAuthor
                mergedDate = it.createdAt
            }
        }
        returnValue = returnValue + "<td>${numOpen}</td><td>${openAuthor}</td><td>${openDate}</td>"
        returnValue = returnValue + "<td>${numMerged}</td><td>${mergedAuthor}</td><td>${mergedDate}</td>"
        returnValue = returnValue + "<td>${numCLosed}</td><td>${closedAuthor}</td><td>${closedDate}</td>"
        return returnValue
    }


    String toString() {
        return "BranchStructure:\n" +
                "\tbranchName: $branchName\n" +
                "\tbranchType: $branchType"
    }
}
