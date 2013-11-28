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
package org.dataconservancy.deposit.sword.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.security.DigestException;

import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.context.EmptyResponseContext;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.deposit.status.DepositResourceLocator;
import org.dataconservancy.deposit.sword.extension.SWORDExtensionFactory;
import org.dataconservancy.deposit.sword.extension.Treatment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * SWORD collection adaptor which wraps a DepositManager.
 * <p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setDepositManager(DepositManager)}</dt>
 * <dd><b>Required</b>. Specifies the underlying DepositManager that this
 * adaptor is wrapping.</dd>
 * <dt>{@link #setContentResourceLocator(DepositResourceLocator)}</dt>
 * <dd><b>Required</b>. Used to generate the "content" link in an atom entry,
 * pointing to the deposited content.</dd>
 * <dt>{@link #setStatusResourceLocator(DepositResourceLocator)}</dt>
 * <dd>Optional. If set, the atom entry will contain an "alternate" link to the
 * status resource</dd>
 * <dt>{@link #setTreatment(String)}</dt>
 * <dd>Optional. Sets the sword treatment string to return to the user.</dd>
 * <dt>{@link #setAtomIdPrefix(String)}</dt>
 * <dd>Optional. If set, resulting atom entry documents will have their id
 * prefixed by this value. An opaque string apppended to this prefix should
 * result in a uri.</dd>
 * </dl>
 * </p>
 */
public class DepositManagerAdaptor
        extends SWORDCollectionAdapter {

    private DepositResourceLocator statusLinks;

    private DepositResourceLocator contentLinks;

    private DepositManager deposit;

    private String idPrefix = "deposit:/";

    private String treatment = "Deposit processing";
    
    private static final Logger log =
            LoggerFactory.getLogger(DepositManagerAdaptor.class);

    @Required
    public void setDepositManager(DepositManager mgr) {
        deposit = mgr;
    }

    public DepositManager getDepositManager() {
        return deposit;
    }

    @Required
    public void setContentResourceLocator(DepositResourceLocator locator) {
        contentLinks = locator;
    }

    public DepositResourceLocator getContentResourceLocator() {
        return contentLinks;
    }

    public void setStatusResourceLocator(DepositResourceLocator locator) {
        statusLinks = locator;
    }

    public DepositResourceLocator getStatusResourceLocator() {
        return statusLinks;
    }

    public void setTreatment(String tmnt) {
        treatment = tmnt;
    }

    public void setAtomIdPrefix(String prefix) {
        idPrefix = prefix;
    }

    public ResponseContext postMedia(RequestContext request) {

        ResponseContext response = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            DepositInfo status =
                    deposit
                            .deposit(request.getInputStream(),
                                     request.getHeader("Content-type"),
                                     request
                                             .getHeader(SWORDExtensionFactory.Headers.PACKAGING),
                                     headers);
            response = getDepositEntry(request, status, DateUtility.now());
            response.setLocation(getEntryURL(request, status.getDepositID()));
            response.setStatus(202);

            for (Map.Entry<String, String> header : status.getMetadata()
                    .entrySet()) {
                response.addHeader(header.getKey(), header.getValue());
            }

        } catch (IOException e) {
            log.error("Error posting media: " + e.getMessage(), e);

            if (e.getCause() != null && e.getCause() instanceof DigestException) {
                return ProviderHelper.preconditionfailed(request,
                                                         "Checksum mismatch");
            }
        } catch (PackageException pex) {
            log.error("Error posting media: " + pex.getMessage(), pex);
            return ProviderHelper.createErrorResponse(request.getAbdera(),
                                                      415,
                                                      pex.getMessage());
        } catch (Exception x) {
            log.error("Error posting media: " + x.getMessage(), x);
            return ProviderHelper.servererror(request, x);
        }

        return response;

    }

    public ResponseContext getEntry(RequestContext request) {

        ResponseContext response = null;
        try {
            response = getDepositEntry(request, fetchStatus(request));
        } catch (Exception e) {
            ProviderHelper.servererror(request, e);
        }

        return response;
    }

    public ResponseContext headEntry(RequestContext request) {
        DepositInfo status = fetchStatus(request);

        if (status == null) {
            return ProviderHelper.notfound(request);
        }

        ResponseContext respctx = null;
        try {
            respctx = getDepositEntry(request, fetchStatus(request));
        } catch (Exception e) {
            ProviderHelper.servererror(request, e);
        }

        ResponseContext head = new EmptyResponseContext(200);

        for (String hn : respctx.getHeaderNames()) {
            head.addHeader(hn, respctx.getHeader(hn));
        }

        for (Map.Entry<String, String> header : status.getMetadata().entrySet()) {
            head.addHeader(header.getKey(), header.getValue());
        }

        return head;
    }

    protected ResponseContext getDepositEntry(RequestContext request,
                                              DepositInfo depositInfo,
                                              Long... explicitLastMod) {
        if (depositInfo == null) {
            return ProviderHelper.notfound(request);
        }

        Entry entry = request.getAbdera().getFactory().newEntry();

        try {
            entry.setId(idPrefix
                    + URLEncoder.encode(depositInfo.getManagerID(), "UTF-8")
                    + "/"
                    + URLEncoder.encode(depositInfo.getDepositID(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            /* Should not happen */
            throw new RuntimeException(e);
        }

        entry.setContent(new IRI(getContentResourceLocator()
                .getURL(depositInfo)), depositInfo.getDepositContent()
                .getMimeType());
        entry.setTitle("Deposit " + depositInfo.getDepositID());

        if (explicitLastMod.length > 0) {
            entry.setUpdated(DateUtility.toIso8601(explicitLastMod[0]));
        } else {
            entry.setUpdated(DateUtility.toIso8601(depositInfo
                    .getDepositStatus().getLastModified()));
        }

        entry.addAuthor("Depositor");
        entry.setSummary(depositInfo.getSummary());

        /*
         * If we have a locator that can generate URLs to a status document for
         * the entry, then include this URL as an alternate link.
         */
        if (getStatusResourceLocator() != null) {

            String url = getStatusResourceLocator().getURL(depositInfo);

            /*
             * Status locator may return null if it cannot find an appropriate
             * status doc URL.
             */
            if (url != null) {
                Link statusDoc = request.getAbdera().getFactory().newLink();
                statusDoc.setHref(url);
                statusDoc.setMimeType(depositInfo.getDepositStatus()
                        .getMimeType());
                statusDoc.setRel(Link.REL_ALTERNATE);
                statusDoc.setTitle("Processing Status");

                entry.addLink(statusDoc);
            }
        }

        Treatment treat = entry.addExtension(SWORDExtensionFactory.TREATMENT);
        treat.setTreatment(treatment);

        return new BaseResponseContext<Entry>(entry);
    }

    private DepositInfo fetchStatus(RequestContext request) {
        Target target = request.getProvider().resolveTarget(request);
        String item = target.getParameter("entry");
        return deposit.getDepositInfo(item);
    }
}
