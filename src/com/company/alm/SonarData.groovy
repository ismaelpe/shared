package com.project.alm

class SonarData {

    boolean found = false
      
    Float blocker_violations = 0
    Float duplicated_lines_density = 0
    Float new_critical_violations = 0
    Float comment_lines_density = 0
    Float sqale_index = 0
    Float sqale_debt_ratio = 0
    Float coverage = 0
    Float lines = 0
    

    String toString() {
        return "SonarData:\n" +
            "\tfound: ${this.found}\n" +
            "\tblocker_violations: ${this.blocker_violations}\n" +
            "\tduplicated_lines_density: ${this.duplicated_lines_density}\n" +
            "\tnew_critical_violations: ${this.new_critical_violations}\n" +
            "\tcomment_lines_density: ${this.comment_lines_density}\n" +
            "\tsqale_index: ${this.sqale_index}\n" +
            "\tsqale_debt_ratio: ${this.sqale_debt_ratio}\n" +
            "\tcoverage: ${this.coverage}\n" +
            "\tlines: ${this.lines}\n"    
    }
}
