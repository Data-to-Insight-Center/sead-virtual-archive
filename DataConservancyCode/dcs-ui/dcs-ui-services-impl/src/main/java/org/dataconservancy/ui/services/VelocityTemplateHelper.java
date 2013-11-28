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
package org.dataconservancy.ui.services;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

/**
 * Executes Velocity templates.
 */
public class VelocityTemplateHelper {

    private VelocityEngine engine;

    public VelocityTemplateHelper(VelocityEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("VelocityEngine must not be null.");
        }

        this.engine = engine;
    }

    public String execute(String templateName, VelocityContext context) {
        Template t = engine.getTemplate(templateName);
        StringWriter out = new StringWriter();
        t.merge(context, out);

        return out.toString();
    }

}
