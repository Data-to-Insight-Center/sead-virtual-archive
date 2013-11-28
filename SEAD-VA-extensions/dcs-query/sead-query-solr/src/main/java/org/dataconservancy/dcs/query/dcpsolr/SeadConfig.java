/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.query.dcpsolr;

import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A singleton holding configuration info for a DcsEntity query service.
 * <p>
 * If the attribute dcpquery.impl is set, its value is used as the name of a
 * class implementing QueryService<DcsEntity>. Otherwise the context
 * initialization parameter dcpquery.instance must be set to an
 * implementation of QueryService<DcsEntity>.
 * <p>
 * The context initialization parameter datastream.url must be set to the base
 * url to be used in File entity source attributes.
 */
public class SeadConfig {

    private static final String CONFIG_ATTR = "config";
    private static final String ENTITY_QUERY_SERVICE_IMPLEMENTATION = "dcpquery.impl";
    private static final String ENTITY_QUERY_SERVICE_INSTANCE = "dcpquery.instance";
    private static final String MODEL_BUILDER = "modelbuilder.impl";
    private static final String UTIL = "sead.converter.util";

    private static String sdahost;
    private static String sdauser;
    private static String sdapwd;
    private static String sdamount;

    private final QueryService<DcsEntity> query_service;
    private final String ds_url;
    private DcsModelBuilder modelBuilder;
    private SeadUtil util;

    public static synchronized SeadConfig instance(ServletContext context)
            throws ServletException {
        SeadConfig instance = (SeadConfig) context.getAttribute(CONFIG_ATTR);

        if (instance == null) {
            instance = new SeadConfig(context);
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

    public String getSdahost() {
        return sdahost;
    }

    public String getSdauser() {
        return sdauser;
    }

    public String getSdapwd() {
        return sdapwd;
    }

    public String getSdamount() {
        return sdamount;
    }

    @SuppressWarnings("unchecked")
    private SeadConfig(ServletContext context) throws ServletException {
        Object o = context.getAttribute(ENTITY_QUERY_SERVICE_INSTANCE);

        if (o != null) {
            query_service = (QueryService<DcsEntity>) o;
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

        String classname = get_param(context, MODEL_BUILDER);

        Class<?> klass = null;
        try {
            klass = Class.forName(classname);
            modelBuilder = klass.asSubclass(DcsModelBuilder.class).newInstance();
            klass = Class.forName( get_param(context, UTIL));
            util = klass.asSubclass(SeadUtil.class).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ds_url = get_param(context, "datastream.url");
        sdahost = get_param(context, "sdahost");
        sdauser = get_param(context, "sdauser");
        sdapwd = get_param(context, "sdapwd");
        sdamount = get_param(context, "sdamount");
    }

    public QueryService<DcsEntity> dcpQueryService() {
        return query_service;
    }

    public DcsModelBuilder modelBuilder() {
        return modelBuilder;
    }

    public SeadUtil util() {
        return util;
    }

    public String publicDatastreamUrl() {
        return ds_url;
    }
}
