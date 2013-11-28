/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.query.endpoint.utils.gqm;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.model.gqm.GQM;

public class Config{
    
    private static final String CONFIG_ATTR = "config";
    private static final String ENTITY_QUERY_SERVICE_IMPLEMENTATION = "gqmquery.impl";
    private static final String ENTITY_QUERY_SERVICE_INSTANCE = "gqmquery.instance";
    
    private final QueryService<GQM> query_service;
    private final String ds_url;
    
    public static synchronized Config instance(ServletContext context)
        throws ServletException {
        Config instance = (Config) context.getAttribute(CONFIG_ATTR);
        
        if (instance == null) {
            instance = new Config(context);
            context.setAttribute(CONFIG_ATTR, instance);
        }
        
        return instance;
    }
    
    private static String get_param(ServletContext context, String name)
        throws ServletException {
        String value = context.getInitParameter(name);
        
        if (value == null) {
            // Fail over to attribute
        
            Object o = context.getAttribute(name);
        
            if (o != null) {
                value = o.toString();
            }
        
            if (value == null) {
                throw new ServletException("Required context param " + name
                        + " not set.");
            }
        }
        
        return value;
    }
    
    @SuppressWarnings("unchecked")
    private Config(ServletContext context) throws ServletException {
        Object o = context.getAttribute(ENTITY_QUERY_SERVICE_INSTANCE);
        
        if (o != null) {
            query_service = (QueryService<GQM>) o;
        } else {
            String classname = get_param(context, ENTITY_QUERY_SERVICE_IMPLEMENTATION);
        
            try {
                Class<?> klass = Class.forName(classname);
        
                query_service = klass.asSubclass(QueryService.class)
                        .newInstance();
            } catch (ClassNotFoundException e) {
                throw new ServletException(e);
            } catch (InstantiationException e) {
                throw new ServletException(e);
            } catch (IllegalAccessException e) {
                throw new ServletException(e);
            }
        }
    
        ds_url = get_param(context, "datastream.url");
    }
    
    public QueryService<GQM> gqmQueryService() {
        return query_service;
    }
    
    public String publicDatastreamUrl() {
        return ds_url;
    }
}
