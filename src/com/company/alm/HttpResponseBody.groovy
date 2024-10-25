package com.caixabank.absis3

class HttpResponseBody {

    public int statusCode;
    public def content;
	public def message;

    @Override
	String toString() {
		return  "HttpResponse: \n"+
		        "\tstatusCode: ${statusCode}\n" +
				"\tcontent: $content\n" +
				"\tmessage: $message\n"
	}
	
	String getStatus() {
		return statusCode
	}
}

