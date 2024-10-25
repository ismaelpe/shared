package com.caixabank.absis3

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import com.caixabank.absis3.GplUtilities
import com.caixabank.absis3.GplRequestStatus
import com.caixabank.absis3.GlobalVars

import org.junit.Test

class GplUtilitiesTest extends GroovyTestCase {

    @Test
    public void testMaxIteration() {
        GplRequestStatus status = new GplRequestStatus()
        def resultado = GplUtilities.evaluateResponse(null, status)
        assertEquals(status.iteration, 1)
    }

    @Test
    public void testMaxIterationExcedingMaxRetries() {
        GplRequestStatus status = new GplRequestStatus()
        status.iteration = GlobalVars.HTTP_REQUEST_MAX_RETRIES

        def resultado = GplUtilities.evaluateResponse(null, status)
        assertTrue(resultado)
    }

    @Test
    public void testMaxIterationNotExcedingMaxRetries() {
        GplRequestStatus status = new GplRequestStatus()
        status.iteration = 1

        def resultado = GplUtilities.evaluateResponse(null, status)
        assertFalse(resultado)
    }


    @Test
    public void testResponseCode200or404() {
        GplRequestStatus status = new GplRequestStatus()
        status.iteration = 1
        def response = new HttpResponseMock()
        response.status = 200

        def resultado = GplUtilities.evaluateResponse(response, status)
        assertTrue(resultado)

        response.status = 404

        resultado = GplUtilities.evaluateResponse(response, status)
        assertTrue(resultado)
    }

    @Test
    public void testResponseCode500or302() {
        GplRequestStatus status = new GplRequestStatus()
        status.iteration = 1
        def response = new HttpResponseMock()
        response.status = 500

        def resultado = GplUtilities.evaluateResponse(response, status)
        assertFalse(resultado)

        response.status = 302

        resultado = GplUtilities.evaluateResponse(response, status)
        assertFalse(resultado)
    }

}
