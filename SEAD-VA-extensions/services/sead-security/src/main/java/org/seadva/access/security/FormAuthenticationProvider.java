package org.seadva.access.security;

import com.sun.security.auth.UserPrincipal;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class FormAuthenticationProvider implements AuthenticationProvider {

    private String dbUrl;

    @Required
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        org.seadva.access.security.model.Authentication result = null;
        try {
            result = (new UserServiceImpl(this.dbUrl)).authenticate(name, password);
            if (result.authResult()){
                Collection<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_admin"));
                Authentication auth = new UsernamePasswordAuthenticationToken( new UserPrincipal(name), password, grantedAuths);
                return auth;
            } else {
                return null;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}