package com.caixabank.absis3

import static org.junit.Assert.*
import groovy.util.GroovyTestCase

class TrazabilidadGPLTypeTest extends GroovyTestCase {


    void testValueOfTrazTypeToString() {

        assertToString(TrazabilidadGPLType.NADA.toString(), 'NO')


    }

}
