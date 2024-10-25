package com.caixabank.absis3

import static org.junit.Assert.*

class GarAppTypeTest extends GroovyTestCase {

    void testValueOfGarAppTypeToString() {

        assertToString(GarAppType.MICRO_SERVICE.getGarName(), 'SRV.MS')


    }

}
