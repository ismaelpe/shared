package com.caixabank.absis3


class HttpRequestException extends RuntimeException {

    def request
    def response

    HttpRequestException(def request, def response) {
        this.request = request
        this.response = response
    }

    HttpRequestException(def request, def response, String message) {
        super(message)
        this.request = request
        this.response = response
    }

    HttpRequestException(def request, def response, String message, Throwable cause) {
        super(message, cause)
        this.request = request
        this.response = response
    }

    HttpRequestException(def request, def response, Throwable cause) {
        super(cause)
        this.request = request
        this.response = response
    }

    HttpRequestException(def request, def response, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
        this.request = request
        this.response = response
    }

    String prettyPrint() {

        return "HttpRequestException:\n"+
          "\n" +
          "\nRequest: ${this.request}"+
          "\nResponse: ${this.response}"

    }

}
