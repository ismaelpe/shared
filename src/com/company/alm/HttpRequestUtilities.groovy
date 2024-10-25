package com.project.alm

import groovy.json.JsonSlurperClassic

class HttpRequestUtilities {
	
	static String prettyPrint(HttpResponseBody response) {
		
				if ( ! response ) {
		
					return "<No response was returned!>"
		
				}
				try {
		
					return response.toString()
		
				} catch(err) {
		
						return "<No valid response was returned!>"
		
				}
		
			}

    static String prettyPrint(String response) {

        if ( ! response ) {

            return "<No response was returned!>"

        }
        try {

            return groovy.json.JsonOutput.prettyPrint(response)

        } catch(err) {

            if (response.startsWith("<")) {

                try {

                    return groovy.xml.XmlUtil.serialize(response)

                } catch(err2) {

                    return response

                }


            } else {

                return response

            }

        }

    }

    static asObject(def source) {

        if ( ! source ) {

            return null

        }
        try {

            return new JsonSlurperClassic().parseText(source)

        } catch(err) {

            if (source.startsWith("<")) {

                return new XmlSlurper().parseText(source)

            } else {

                return source

            }

        }

    }

    static boolean responseCodeIsValid(int responseStatus, def validResponseCodes = []) {

        for (String responseCode in validResponseCodes) {

            if (responseCode.contains(":")) {

                def codes = responseCode.split(":")
                int lowerThreshold = codes[0].toInteger()
                int higherThreshold = codes[1].toInteger()

                if (responseStatus >= lowerThreshold && responseStatus <= higherThreshold) return true

            } else {

                int status = responseCode.toInteger()
                if (status == responseStatus) return true

            }

        }

        return false
    }

}
