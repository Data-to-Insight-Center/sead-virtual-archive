package org.seadva.access.security.model;

import java.util.Arrays;
import java.util.Collection;

import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;



public class SeadAuthenticationToken extends AbstractAuthenticationToken {

    private final Object credentials;


    public SeadAuthenticationToken(Object credentials) {
        super(null);
        this.credentials = credentials;
        setAuthenticated(false);
    }


    public SeadAuthenticationToken(Object credentials, Collection<GrantedAuthority> authorities) {
        super(authorities);
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    public Object getCredentials() {
        return this.credentials;
    }

    public Object getPrincipal() {
        return new BasicUserPrincipal("oauth");
    }
}