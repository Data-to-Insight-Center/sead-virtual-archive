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
package org.dataconservancy.deposit.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.net.URLEncoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.springframework.beans.factory.annotation.Required;

/**
 * Servlet that simply returns a status document from a pool of
 * {@link DepositManager}s.
 * <p>
 * Also functions as a {@link DepositResourceLocator} that produces URLs that
 * point to the correct item and {@linkplain DepositManager} for simple lookup.
 * </p>
 */
public abstract class DepositDocumentServlet
        extends HttpServlet
        implements DepositResourceLocator {

    private Map<String, DepositManager> depositManagers =
            new HashMap<String, DepositManager>();

    private String baseURL;

    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    public String getURL(DepositInfo status) {
        try {
            return String.format("%s/%s/%s", getBaseURL(), URLEncoder
                    .encode(status.getManagerID(), "UTF-8"), URLEncoder
                    .encode(status.getDepositID(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the base URL for generated status URLs.
     * <p>
     * Base URL is typically <code>protocol://host:port/webapp/servlet</code>
     * unless there is path rewriting or reverse proxying. No default value is
     * provided or derived, so setting the baseURL is required.
     * </p>
     * 
     * @param url
     *        baseURL
     */
    @Required
    public void setBaseURL(String url) {
        baseURL = url.replaceAll("/$", "");
    }

    /**
     * Get the base URL for generated status URLs
     * 
     * @return baseURL
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Load the deposit managers that will provide deposit status info.
     * 
     * @param mgrs
     *        Collection containing all relevant deposit managers.
     */
    @Required
    public void setDepositManagers(Collection<? extends DepositManager> mgrs) {

        HashMap<String, DepositManager> map =
                new HashMap<String, DepositManager>();

        for (DepositManager mgr : mgrs) {
            map.put(mgr.getManagerID(), mgr);
        }
        depositManagers = map;
    }

    public Collection<DepositManager> getDepositManagers() {
        return depositManagers.values();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String[] parts = req.getPathInfo().split("/", 3);

        if (parts.length != 3) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        String managerId = URLDecoder.decode(parts[1], "UTF-8");
        String depositId = URLDecoder.decode(parts[2], "UTF-8");

        if (!depositManagers.containsKey(managerId)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getOutputStream().write(String
                    .format("404\nDeposit manager not found: %s", managerId)
                    .getBytes());
            return;
        }

        DepositInfo info =
                depositManagers.get(managerId).getDepositInfo(depositId);

        if (info == null) {
            resp.getOutputStream()
                    .write(String.format("404\nDeposit info not found: %s",
                                         depositId).getBytes());
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        DepositDocument status = getDocument(info);

        resp.setContentType(status.getMimeType());
        resp.setStatus(HttpServletResponse.SC_OK);
        IOUtils.copy(status.getInputStream(), resp.getOutputStream());
    }

    abstract DepositDocument getDocument(DepositInfo depositInfo);
}
