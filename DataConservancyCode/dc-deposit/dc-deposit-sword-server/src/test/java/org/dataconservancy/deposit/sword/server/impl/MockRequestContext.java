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
package org.dataconservancy.deposit.sword.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.context.AbstractRequestContext;
import org.apache.abdera.protocol.server.provider.basic.BasicProvider;

public class MockRequestContext
        extends AbstractRequestContext {

    private InputStream istream = null;

    private Map<String, Object> attrs = new HashMap<String, Object>();

    public MockRequestContext() {
        super(new BasicProvider(), "GET", null, null);
        getProvider().init(new Abdera(), new HashMap<String, String>());
    }

    public Object getAttribute(Scope scope, String name) {
        return attrs.get(name);
    }

    public String[] getAttributeNames(Scope scope) {
        ArrayList<String> attrNames = new ArrayList<String>();
        attrNames.addAll(attrs.keySet());
        return attrNames.toArray(new String[0]);
    }

    public String getContextPath() {
        return null;
    }

    public void setInputStream(InputStream stream) {
        istream = stream;
    }

    public InputStream getInputStream() throws IOException {
        return istream;
    }

    public String getParameter(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getParameters(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Locale getPreferredLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    public Locale[] getPreferredLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getProperty(Property property) {
        // TODO Auto-generated method stub
        return null;
    }

    public Reader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTargetBasePath() {
        return null;
    }

    public boolean isUserInRole(String role) {
        return false;
    }

    public RequestContext setAttribute(Scope scope, String name, Object value) {
        return setAttribute(name, value);
    }

    public RequestContext setAttribute(String name, Object value) {
        attrs.put(name, value);
        return this;
    }

    public Date getDateHeader(String name) {
        return null;
    }

    public String getHeader(String name) {
        return null;
    }

    public String[] getHeaderNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object[] getHeaders(String name) {
        return null;
    }

}
