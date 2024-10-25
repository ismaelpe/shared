package com.caixabank.absis3

import org.junit.Test

class HttpRequestUtilitiesTest extends GroovyTestCase {

    String someOtherResponse = 'Treating class org.apache.http.conn.HttpHostConnectException(Connect to proxyserv.svb.lacaixa.es:8080 [proxyserv.svb.lacaixa.es/10.119.252.228] failed: Connection timed out (Connection timed out)) as 408 Request Timeout'


    @Test
    public void test_given_NoResponse_returns_warning() {

        def response = ''
        assert HttpRequestUtilities.prettyPrint(response) == '<No response was returned!>'

    }

    @Test
    public void test_given_PureStringResponse_returns_AsIs() {

        assert HttpRequestUtilities.prettyPrint(someOtherResponse) == someOtherResponse

    }

    @Test
    public void test_given_XMLResponse_returns_prettyXML() {

        String xmlString = '<?xml version="1.0" encoding="UTF-8"?><languages><language id="1">Groovy</language><language id="2">Java</language><language id="3">Scala</language></languages>'
        assert HttpRequestUtilities.prettyPrint(xmlString) == '<?xml version="1.0" encoding="UTF-8"?><languages>\r\n' +
            '  <language id="1">Groovy</language>\r\n' +
            '  <language id="2">Java</language>\r\n' +
            '  <language id="3">Scala</language>\r\n' +
            '</languages>\r\n'

    }

    @Test
    public void test_given_JSONResponse_returns_prettyJSON() {

        def jsonString = '{"id":"jenkins-absis3-services-apps-cbk-common-demoALM-demoarqalmlib-lib-192_2021-03-12_11:16:10905","estado":"running","fechaInicio": "2021-03-12T11:22:05+0000"}'
        assert HttpRequestUtilities.prettyPrint(jsonString) == '{\n' +
            '    "id": "jenkins-absis3-services-apps-cbk-common-demoALM-demoarqalmlib-lib-192_2021-03-12_11:16:10905",\n' +
            '    "estado": "running",\n' +
            '    "fechaInicio": "2021-03-12T11:22:05+0000"\n' +
            '}'

    }

    @Test
    public void test_given_IndividualValidStatusCode_returns_validBoolean() {

        def validResponseCodes = "100,200:399,404".split(",")
        assert HttpRequestUtilities.responseCodeIsValid(100, validResponseCodes)
        assert HttpRequestUtilities.responseCodeIsValid(404, validResponseCodes)
        assert HttpRequestUtilities.responseCodeIsValid(405, validResponseCodes) == false

    }

    @Test
    public void test_given_ValidStatusCodeOnLowerThreshold_returns_validBoolean() {

        def validResponseCodes = "10,100:399,404".split(",")
        assert HttpRequestUtilities.responseCodeIsValid(100, validResponseCodes)
        assert HttpRequestUtilities.responseCodeIsValid(101, validResponseCodes)
        assert HttpRequestUtilities.responseCodeIsValid(99, validResponseCodes) == false

    }

    @Test
    public void test_given_ValidStatusCodeOnHigherThreshold_returns_validBoolean() {

        def validResponseCodes = "10,100:399,404".split(",")
        assert HttpRequestUtilities.responseCodeIsValid(398, validResponseCodes)
        assert HttpRequestUtilities.responseCodeIsValid(399, validResponseCodes)
        assert HttpRequestUtilities.responseCodeIsValid(400, validResponseCodes) == false

    }

}
