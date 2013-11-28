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
package org.dataconservancy.dcs.spring.mvc;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

import static org.dataconservancy.dcs.spring.mvc.ViewUtil.TEXT_PLAIN;

/**
 *
 */
public abstract class BaseView implements View {
    final int httpStatusCode;
    final String contentType;

    protected BaseView(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        this.contentType = TEXT_PLAIN;
    }

    protected BaseView(int httpStatusCode, String contentType) {
        this.contentType = contentType;
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse res) throws Exception {
        res.setStatus(httpStatusCode);
        res.setContentType(contentType);
        OutputStream out = res.getOutputStream();
        if (model.containsKey(ViewKey.REASON_PHRASE.name())) {
            out.write(((String) model.get(ViewKey.REASON_PHRASE.name())).getBytes());
        }
        out.flush();
        out.close();
    }
}
