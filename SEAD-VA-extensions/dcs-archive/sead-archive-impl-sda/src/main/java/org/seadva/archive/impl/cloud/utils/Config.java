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
package org.seadva.archive.impl.cloud.utils;

import org.seadva.archive.impl.cloud.SdaArchiveStore;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class Config {

    private static final String SDA_INSTANCE = "sdaStore.instance";
    private static final String SDA_IMPLEMENTATION = "sdaStore.impl";
    private static final String CONFIG_ATTR = "archiveConfig";


    private SdaArchiveStore sdaStore = null;

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
        Object o = context.getAttribute(SDA_INSTANCE);


        if (o != null) {
            sdaStore = (SdaArchiveStore) o;
        } else {
            String classname = get_param(context, SDA_IMPLEMENTATION);

            try {
                Class<?> klass = Class.forName(classname);

                sdaStore =
                      klass.asSubclass(SdaArchiveStore.class)
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

    public SdaArchiveStore getSdaStore() {
        return sdaStore;
    }

}
