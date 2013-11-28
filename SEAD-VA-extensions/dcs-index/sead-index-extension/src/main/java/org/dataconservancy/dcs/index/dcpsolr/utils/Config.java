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
package org.dataconservancy.dcs.index.dcpsolr.utils;

import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.model.dcp.Dcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A singleton holding configuration info for a DcsEntity index service.
 * <p>
 * If the attribute dcpindex.impl is set, its value is used as the name of a
 * class implementing IndexService<Dcp>. Otherwise the context
 * initialization parameter dcpquery.instance must be set to an
 * implementation of IndexService<Dcp>.
 * <p>
 */
public class Config {

    private static final String CONFIG_ATTR = "indexConfig";
    private static final String ENTITY_INDEX_SERVICE_INSTANCE = "dcpindex.instance";
    private static final String ENTITY_INDEX_SERVICE_IMPLEMENTATION = "dcpindex.impl";
    private static final String EVENT_MANAGER = "eventManager";

    private DcpIndexService index_service = null;
    private EventManager event_manager = null;

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

    private static final Logger log =
            LoggerFactory.getLogger(Config.class);

    @SuppressWarnings("unchecked")
    private Config(ServletContext context) throws ServletException {
        Object o = context.getAttribute(ENTITY_INDEX_SERVICE_INSTANCE);

        log.debug("instanceOf = "+o.getClass()+"\n");
        if (o != null) {
            index_service = (DcpIndexService) o;
        } else {
            String classname = get_param(context, ENTITY_INDEX_SERVICE_IMPLEMENTATION);

            log.debug("classname = "+classname+"\n");
            try {
                Class<?> klass = Class.forName(classname);

//                SolrService solr = new SolrService();
                index_service =
//                        new DcpIndexService(solr);
                        klass.asSubclass(DcpIndexService.class)
                        .newInstance();
            } catch (ClassNotFoundException e) {
                throw new ServletException(e);
            } catch (InstantiationException e) {
                throw new ServletException(e);
            } catch (IllegalAccessException e) {
                throw new ServletException(e);
            }
        }

            Object m = context.getAttribute(EVENT_MANAGER);

            if (m != null) {
                event_manager = (EventManager) m;
                log.debug("instanceOf = "+m.getClass()+"\n");
            } else {
                String classname = get_param(context, EVENT_MANAGER);

                log.debug("classname = "+classname+"\n");
                try {
                    Class<?> klass = Class.forName(classname);

                  event_manager =
                       klass.asSubclass(EventManager.class)
                                    .newInstance();
                } catch (ClassNotFoundException e) {
                    throw new ServletException(e);
                } catch (InstantiationException e) {
                    throw new ServletException(e);
                } catch (IllegalAccessException e) {
                    throw new ServletException(e);
              }
            }
    }

    public IndexService<Dcp> dcpBatchIndexService() {
        return index_service;
    }

    public EventManager eventManager() {
        return event_manager;
    }
}
