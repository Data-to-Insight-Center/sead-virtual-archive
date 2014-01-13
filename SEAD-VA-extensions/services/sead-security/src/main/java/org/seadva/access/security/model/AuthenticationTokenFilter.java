package org.seadva.access.security.model;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;
import java.util.Map;

public class AuthenticationTokenFilter implements Filter {


    @Override
    public void init(FilterConfig fc) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            // do nothing
        } else {
            Map<String,String[]> params = req.getParameterMap();
            if (!params.isEmpty() && params.containsKey("oauth_token")) {
                String token = params.get("oauth_token")[0];

                if (token != null) {
                    SeadCredentials credentials = new SeadCredentials("oauth", token);
                    SeadAuthenticationToken auth = new SeadAuthenticationToken(credentials);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }


        fc.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }



 }