package com.project.alm

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DatesAndTimes {

    static String getLocalTimeDateNowAsString() {
        return this.getLocalTimeDateNowAsString("yyyyMMdd.HHmmss-SSS")
    }

    static String getLocalTimeDateNowAsString(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
        LocalDateTime timeDate = LocalDateTime.now()
        return timeDate.format(formatter)
    }

}
