package org.seadva.access.security;

import org.seadva.access.security.model.OAuthType;
import org.seadva.access.security.model.SeadAuthenticationToken;
import org.seadva.access.security.model.SeadCredentials;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private String dbUrl;

    @Required
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String token = ((SeadCredentials)authentication.getCredentials()).getToken();
        org.seadva.access.security.model.Authentication result = (new UserServiceImpl(this.dbUrl)).authenticateOAuth(
                token,
                OAuthType.GOOGLE,
                new String[1]);
        if (result.authResult()) {
            {
                Collection<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_admin"));
                Authentication auth = new SeadAuthenticationToken(authentication.getCredentials(), grantedAuths);
                return auth;
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(SeadAuthenticationToken.class);
    }
}