package com.project.alm

import com.project.alm.DataSourceGenerator


class CloudApiResponse{

	int statusCode
	def body
	
	CloudApiResponse(int httpStatusCode, def responseBody){
		statusCode=httpStatusCode
		body=responseBody
	}
	CloudApiResponse(){
		statusCode=500		
	}
	@NonCPS
    @Deprecated
    //FIXME: Remove this when all pipeline-cps-method-mismatches have been solved for this class
	public String toString() {
        return "\n" +
                "statusCode: $statusCode\n" +
                "body: $body\n"//+
    }

    public String prettyPrint() {
        return "\n" +
            "statusCode: $statusCode\n" +
            "body: $body\n"//+
    }
	
}

