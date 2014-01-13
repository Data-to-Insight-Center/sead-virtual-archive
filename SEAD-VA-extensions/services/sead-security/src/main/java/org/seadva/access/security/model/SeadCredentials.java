package org.seadva.access.security.model;

import com.sun.security.auth.UserPrincipal;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;

import java.security.Principal;

public class SeadCredentials implements Credentials {

    Principal userPrincipal;
    String token;
    String type;


    public SeadCredentials(String type, String token) {
        this.type = type;
        this.token = token;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(String name){
        this.userPrincipal = new BasicUserPrincipal(name);
    }

    @Override
    public String getPassword() {
        return token;
    }

    public String getType(){
        return type;
    }

    public String getToken(){
        return token;
    }

    @Override
    public String toString() {
       return this.token;
    }
}