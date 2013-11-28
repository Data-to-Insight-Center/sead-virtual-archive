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
package org.dataconservancy.dcs.lineage.http.view;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.model.dcs.DcsEntity;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ACCEPT;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.IFMODIFIEDSINCE;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LINEAGE;

/**
 * Package-private utilities for working with view objects.
 */
class ViewUtil {

    static final String TEXT_PLAIN = "text/plain";

    static final String APPLICATION_XML = "application/xml";

    static final String APPLICATION_JSON = "application/json";
    
    static final String APPLICATION_ANY = "*/*";

    private final static Logger log = LoggerFactory.getLogger(ViewUtil.class);

    static Lineage getLineage(ModelAndView mv) {
        return (Lineage) mv.getModelMap().get(LINEAGE.name());
    }

    static String getAccept(ModelAndView mv) {
        return (String) mv.getModelMap().get(ACCEPT.name());
    }

    static ModelAndView getModelAndView(ServletRequestAttributes requestAttributes) {
        return (ModelAndView) requestAttributes.getAttribute(ExposeModelHandleInterceptor.LINEAGE_MODEL,
                ServletRequestAttributes.SCOPE_REQUEST);
    }

    static List<DcsEntity> getEntities(ModelAndView mv) {
        return (List<DcsEntity>) mv.getModelMap().get(ENTITIES.name());
    }

    static DateTime getIfModifiedSince(ModelAndView mv) {
        Date d = (Date) mv.getModelMap().get(IFMODIFIEDSINCE.name());
        if (d != null) {
            return new DateTime(d);
        }

        return null;
    }

    void debugAttributesForScope(ServletRequestAttributes servletAttributes, String scopeDesc, int scope) {
        if (log.isDebugEnabled()) {
            HttpServletRequest request = servletAttributes.getRequest();
            String req = request.toString() + " (" + request.getClass().getName() + "@" +
                    Integer.toHexString(System.identityHashCode(request)) + ")";
            StringBuilder requestAttributes = new StringBuilder(scopeDesc + " attributes [").append(req).append("]:\n");
            String[] attrs = servletAttributes.getAttributeNames(scope);
            for (String attrName : attrs) {
                requestAttributes.append("[").append(attrName).append("]: ");
                Object o = servletAttributes.getAttribute(attrName, scope);
                requestAttributes.append(o).append(" (").append(o.getClass().getName()).append("@").
                        append(Integer.toHexString(System.identityHashCode(o))).append(")");
                requestAttributes.append("\n");
            }
            log.debug(requestAttributes.toString());
        }
    }


}
