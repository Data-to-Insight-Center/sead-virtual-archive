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
package org.dataconservancy.dcs.access.client.model;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public final class JsMatch
        extends org.dataconservancy.dcs.access.ui.client.model.JsModel{

    protected JsMatch() {
    }

    public String getEntityType() {
        return getEntity().getEntityType();
    }

    public JsEntity getEntity() {
        return (JsEntity) getObject("object");
    }

    public String getContext() {
        return getString("context");
    }

    public Widget getSummary() {
        String type = getEntityType();
        JsEntity entity = getEntity();
        String summary;

        if (type.equals("deliverableUnit")) {
            summary = ((JsDeliverableUnit) entity).summary();
        }  else if (type.equals("file")) {
            summary = ((JsFile) entity).summary();
        }  else {
            throw new RuntimeException("Unknown type: " + type);

        }

        return new Label(summary);
    }
    
    public String getSummaryStr() {
        String type = getEntityType();
        JsEntity entity = getEntity();
        String summary;

        if (type.equals("deliverableUnit")) {
            summary = ((JsDeliverableUnit) entity).summary();
        } else if (type.equals("file")) {
            summary = ((JsFile) entity).summary();
        }else {
            throw new RuntimeException("Unknown type: " + type);

        }

        return summary;
    }
    
}
