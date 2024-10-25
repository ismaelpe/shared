package com.project.alm

import com.project.alm.BranchType
import com.project.alm.GlobalVars

class GitUser {

    int id
    /*String name
    String username
    String state
    String avatar_url
    String web_url
*/

    String toString() {
        return "{" +
                //"name : "+ "\""+name+"\","+
                //"username : "+ "\""+username+"\","+
                "id : " + +id +//","+
                //"state : "+ "\""+state+"\","+
                //"avatar_url : "+ "\""+avatar_url+"\""
                "}"
    }
}