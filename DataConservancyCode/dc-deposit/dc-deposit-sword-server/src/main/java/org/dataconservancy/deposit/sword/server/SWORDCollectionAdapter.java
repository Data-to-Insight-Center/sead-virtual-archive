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

import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.model.Collection;
import org.apache.abdera.protocol.server.CategoriesInfo;
import org.apache.abdera.protocol.server.MediaCollectionAdapter;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

import org.dataconservancy.deposit.sword.SWORDPackaging;
import org.dataconservancy.deposit.sword.extension.AcceptPackaging;
import org.dataconservancy.deposit.sword.extension.CollectionPolicy;
import org.dataconservancy.deposit.sword.extension.Mediation;
import org.dataconservancy.deposit.sword.extension.SWORDExtensionFactory;
import org.dataconservancy.deposit.sword.extension.Service;
import org.dataconservancy.deposit.sword.extension.Treatment;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base abstract adaptor class for implementing SWORD collections in Abdera.
 * <p>
 * Provides a useful foundation for implementing SWORD-compliant collections. By
 * default, it disallows many methods that are not used in a basic SWORD
 * application (e.g. put/delete/post Entry, put Media, etc). One may override
 * these methods and implement additional functionality as desired, of course.
 * </p>
 * <p/>
 * In addition to providing adaptor functionality, this class also functions as
 * a {@link org.apache.abdera.protocol.server.CollectionInfo} bean, which are
 * typically used for generating an app:collection entries in service documents.
 * <h2>Configuration</h2>
 * <p>
 * <dl>
 * <dt>{@link #setTitle(String)}</dt>
 * <dd><b>Required.</b> Species the atom title for a given collection.</dd>
 * <dt>{@link #setId(String)}</dt>
 * <dd><b>Required</b>. Specifies a unique ID used for referring to a specific
 * collection in URLs</dd>
 * <dt>{@link #setAccepts(String...)}</dt>
 * <dd>Optional. Specifies the accepted content MIME types, which will be
 * reported in the service document. If not set, this defaults to * / * (i.e.
 * accept everything)</dd>
 * <dt>{@link #setCategoriesInfo(CategoriesInfo...)}</dt>
 * <dd>Optional. Specifies any atom categories for this collection.</dd>
 * <dt>{@link #setCollectionPolicy(String)}</dt>
 * <dd>Optional. The supplied text or url will be included in the service
 * document for this collection.</dd>
 * <dt>{@link #setHref(String)}</dt>
 * <dd>Optional. Used to explicitly specify the colletion URI</dd>
 * <dt>{@link #setMediation(boolean)}</dt>
 * <dd>Specifies whether sword mediation is supported.</dd>
 * <dt>{@link #setServiceURIs(String...)}</dt>
 * <dd>Optional. Any uris specified here will appear in the service document.</dd>
 * <dt>{@link #setTreatment(String)}</dt>
 * <dd>Optional. Provided text or uri will appear in service document to
 * describe the treatment a deposited object will receive.</dd>
 * <dt>{@link SWORDCollectionAdapter#setAcceptedPackaging(SWORDPackaging...)}</dt>
 * <dd>Optional. Specifies the accepted sword packaging formats, which will be
 * reported in the service document. Absence of any explicit values implies that
 * package format is not relevant, and any format will be accepted.</dd>
 * </dl>
 * </p>
 */
public abstract class SWORDCollectionAdapter
        implements MediaCollectionAdapter, SWORDCollectionInfo {

    /*
     * SWORD properties
     */

    private SWORDPackaging[] acceptedPacking = new SWORDPackaging[0];

    private String treatment;

    private boolean hasMediation = false;

    private String collectionPolicy;

    private String[] serviceURIs = new String[0];

    /*
     * Atom/Pub properties
     */

    private String title;

    private String collectionId;

    private String href;

    private String baseURL;

    private String[] accepts = {"*/*"};

    private CategoriesInfo[] categoryInfo = new CategoriesInfo[0];

    public SWORDCollectionAdapter() {
    }

    public ResponseContext deleteEntry(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext postEntry(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext putEntry(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext optionsEntry(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext getMedia(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext deleteMedia(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext headMedia(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext optionsMedia(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext putMedia(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext getFeed(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext extensionRequest(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public ResponseContext getCategories(RequestContext request) {
        return ProviderHelper.notallowed(request);
    }

    public abstract ResponseContext postMedia(RequestContext request);

    /**
     * Set atomPub collection title
     * 
     * @param title
     *        The title
     */
    @Required
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get atomPub collection title
     */
    public String getTitle(RequestContext request) {
        return title;
    }

    /**
     * Set SWORD accepted packaging formats.
     * <p>
     * Optional. Zero or or more packaging formats may be specified.
     * </p>
     * 
     * @param pkg
     *        varargs containing all accepted packaging formats.
     */
    public void setAcceptedPackaging(SWORDPackaging... pkg) {
        if (pkg != null) {
            acceptedPacking = pkg;
        } else {
            acceptedPacking = new SWORDPackaging[0];
        }
    }

    public SWORDPackaging[] getAcceptedPackaging() {
        return acceptedPacking;
    }

    /**
     * Set SWORD collection policy text or URI
     * <p>
     * Optional
     * </p>
     * 
     * @param policy
     *        Human description of collection policy, or a URI pointing to the
     *        same
     */
    public void setCollectionPolicy(String policy) {
        collectionPolicy = policy;
    }

    public String getCollectionPolicy() {
        return collectionPolicy;
    }

    /**
     * Set URIs for nested SWORD service documents.
     */
    public void setServiceURIs(String... uris) {
        serviceURIs = uris;
    }

    public String[] getServiceURIs() {
        return serviceURIs;
    }

    /**
     * Set SWORD collection-level treatment statement or URI.
     * <p>
     * Optional, human readable text or a URI pointing to the same.
     * </p>
     * 
     * @param treat
     *        treatment text or URI
     */
    public void setTreatment(String treat) {
        treatment = treat;
    }

    public String getTreatment() {
        return treatment;
    }

    /**
     * Set SWORD support for mediated deposit
     * 
     * @param mediation
     *        True if mediation is supported.
     */
    public void setMediation(boolean mediation) {
        hasMediation = mediation;
    }

    public boolean getMediation() {
        return hasMediation;
    }

    public Collection asCollectionElement(RequestContext request) {

        Collection collection =
                request.getAbdera().getFactory().newCollection();
        String href = stripContext(request, getHref(request));
        collection.setHref(getUrlBase(request) + href);
        collection.setTitle(getTitle(request));
        collection.setAccept(getAccepts(request));
        for (CategoriesInfo catsinfo : getCategoriesInfo(request)) {
            collection.addCategories(catsinfo.asCategoriesElement(request));
        }

        /* SWORD-specific elements */

        for (SWORDPackaging packaging : getAcceptedPackaging()) {
            AcceptPackaging p =
                    collection
                            .addExtension(SWORDExtensionFactory.ACCEPT_PACKAGING);
            p.setAcceptedPackaging(packaging.getPackaging());
            if (packaging.hasPreference()) {
                p.setPreference(packaging.getPreference());
            }
        }

        if (getCollectionPolicy() != null) {
            CollectionPolicy p =
                    collection
                            .addExtension(SWORDExtensionFactory.COLLECTION_POLICY);
            p.setPolicy(getCollectionPolicy());
        }

        if (getTreatment() != null) {
            Treatment t =
                    collection.addExtension(SWORDExtensionFactory.TREATMENT);
            t.setTreatment(getTreatment());
        }

        Mediation m = collection.addExtension(SWORDExtensionFactory.MEDIATION);
        m.setMediation(getMediation());

        for (String serviceURI : getServiceURIs()) {
            Service s = collection.addExtension(SWORDExtensionFactory.SERVICE);
            s.setServiceDocumentURI(serviceURI);
        }

        return collection;
    }

    /**
     * Specify accepted mime types.
     * <p>
     * If not specified, the default is to accept any content (* /*)
     * </p>
     * 
     * @param acc
     *        vargs containing all accepted mime types.
     */
    public void setAccepts(String... acc) {
        accepts = acc;
    }

    /**
     * Get all accepted mime types.
     * 
     * @param request
     *        Required by interface but unused - may be null.
     */
    public String[] getAccepts(RequestContext request) {
        return accepts;
    }

    /**
     * Set AtomPub Categories.
     * <p>
     * Optional
     * </p>
     * 
     * @param ci
     *        varargs containing category info
     */
    public void setCategoriesInfo(CategoriesInfo... ci) {
        this.categoryInfo = ci;
    }

    public CategoriesInfo[] getCategoriesInfo(RequestContext request) {
        return categoryInfo;
    }

    /**
     * Explicitly set the collection feed URL.
     * <p>
     * If not explicitly set, the URL will be derived based upon servlet and
     * provider configuration.
     * </p>
     * 
     * @param ref
     *        URL to the collection feed.
     */
    public void setHref(String ref) {
        href = ref;
    }

    public String getHref(RequestContext request) {

        if (href == null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("collection", getId());
            return request.urlFor("feed", params);
        } else {
            return href;
        }
    }

    /**
     * Build a URL that will link back to the entry document.
     * 
     * @param request
     *        Abdera request context
     * @param entryid
     *        Atom id of the entry;
     * @return String containing a resolvable URL.
     */
    protected String getEntryURL(RequestContext request, String entryid) {
        HashMap<String, String> params = new HashMap<String, String>();
        params
                .put("collection", request.getTarget()
                        .getParameter("collection"));
        params.put("entry", entryid);
        String entryHref = stripContext(request, request.urlFor("entry", params));
        return getUrlBase(request) + entryHref;
    }

    /**
     * Set the unique ID for this collection. Used for differentiating and
     * routing requests to particular adapters. Must be unique, and ideally
     * would be a URL-friendly value, as it may be used in a path name when
     * constructing URL which link to feeds or entries.
     * 
     * @param name
     */
    @Required
    public void setId(String id) {
        collectionId = id;
    }

    public String getId() {
        return collectionId;
    }

    public void setBaseURL(String base) {
        baseURL = base;
    }

    private String getUrlBase(RequestContext request) {
        String base = "";

        if (baseURL != null) {
            base = baseURL;
        } else if (request != null && request.getBaseUri() != null) {
            base =
                    request.getBaseUri().toASCIIString();
        }

        return base.replaceAll("/$", "");
    }

    /**
     * Determines whether or not the <code>href</code> starts with the webapp context path, and strips it off.  If
     * the <code>href</code> does not start with the webapp context path, it is returned untouched.
     * <p/>
     *
     * @param request the request context, used to determine the webapp context path
     * @param href the href (e.g. <code>/dcs/deposit/sip</code>)
     * @return the href, sans webapp context path (e.g. <code>/deposit/sip</code>)
     */
    private String stripContext(RequestContext request, String href) {
        if (href != null && request != null) {
            if (request.getContextPath() != null) {
                if (href.startsWith(request.getContextPath())) {
                    href = href.replaceFirst(request.getContextPath(), "");
                }
            }
        }

        return href;
    }
}
