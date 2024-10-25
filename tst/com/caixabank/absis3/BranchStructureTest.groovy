package com.caixabank.absis3

import static org.junit.Assert.*
import groovy.util.GroovyTestCase
import com.caixabank.absis3.BranchStructure

import org.junit.Test

class BranchStructureTest extends GroovyTestCase {

    @Test
    public void testInitFeatureNumber() {
        BranchStructure obj = new BranchStructure()

        obj.branchName = 'feature/#241_Super'
        obj.branchType = BranchType.FEATURE

        obj.initFeatureFromBranchName()

        println "La artifactId es de " + obj.featureNumber
        String expected = '241'
        assertToString(obj.featureNumber, expected)


    }

    @Test
    public void testInitFeatureNumberWithOutSlash() {
        BranchStructure obj = new BranchStructure()


        obj.branchName = 'feature/#241Super'
        obj.branchType = BranchType.FEATURE

        obj.initFeatureFromBranchName()

        println "La artifactId es de " + obj.featureNumber
        String expected = '241Super'
        assertToString(obj.featureNumber, expected)

    }

    @Test
    public void testInitFeatureNumberWithOutComodin() {
        BranchStructure obj = new BranchStructure()


        obj.branchName = 'feature/241Super'
        obj.branchType = BranchType.FEATURE

        obj.initFeatureFromBranchName()

        println "La artifactId es de " + obj.featureNumber
        String expected = '241Super'
        assertToString(obj.featureNumber, expected)

    }

    @Test
    public void testInitWithOrigin() {
        BranchStructure obj = new BranchStructure()


        obj.branchName = 'feature/241Super'
        obj.init()

        String expected = 'feature/241Super'
        assertToString(obj.branchName, expected)
    }

}
