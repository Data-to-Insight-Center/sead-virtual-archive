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

import java.util.Collection;
import java.util.HashMap;

import org.apache.abdera.protocol.Resolver;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.CollectionInfo;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetBuilder;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.WorkspaceManager;
import org.apache.abdera.protocol.server.impl.AbstractProvider;
import org.apache.abdera.protocol.server.impl.DefaultWorkspaceManager;
import org.apache.abdera.protocol.server.impl.RouteManager;
import org.apache.abdera.protocol.server.processors.CategoriesRequestProcessor;
import org.apache.abdera.protocol.server.processors.CollectionRequestProcessor;
import org.apache.abdera.protocol.server.processors.EntryRequestProcessor;
import org.apache.abdera.protocol.server.processors.MediaRequestProcessor;
import org.apache.abdera.protocol.server.processors.ServiceRequestProcessor;

/**
 * Default SWORD-enabled Provider.
 * <p>
 * When initialized with its two-argument constructor, this Provider constitutes
 * a fully SWORD-capable Provider that can manage a set of provided
 * SWORD-enabled Collections.
 * </p>
 * <p/>
 * In general, this provider is intended to flexible so as to allow any core
 * constituent component to be replaced after initialization. It should be
 * particularly amenable to configuration and deployment through a DI framework
 * such as Spring.
 */
public class DefaultSWORDProvider extends AbstractProvider {

    private WorkspaceManager manager;

    private Resolver<Target> targetResolver;

    private TargetBuilder targetBuilder;

    /**
     * Creates a completely non-configured Provider.
     * <p>
     * All internal components will be null, so it is necessary to configure
     * via:
     * <ul>
     * <li>{@link #setRequestProcessors(java.util.Map)}</li>
     * <li>{@link #setWorkspaceManager(WorkspaceManager)}</li>
     * <li>{@link #setTargetBuilder(TargetBuilder)}</li>
     * <li>{@link #setTargetResolver(Resolver)}</li>
     * </ul>
     * Note: In order to be truly SWORD capable, the
     * {@link ServiceRequestProcessor} must be SWORD-aware.
     * {@link SWORDServiceRequestProcessor} fulfills that role.
     * </p>
     */
    public DefaultSWORDProvider() {
    }

    /**
     * Create a basic configured SWORD-capable Provider.
     * <p>
     * Creates a reasonable default SWORD-capable provider given a path context
     * (typically "/"), and a collection of SWORD workspace extension. Any
     * constituent component ({@link RequestProcessor}, {@link TargetBuilder},
     * {@link Resolver}, {@link WorkspaceManager}) may be replaced by using the
     * appropriate setter if more advanced capability is required.
     * </p>
     * <p>
     * For example, if one wishes to map workspaces and collections to
     * repository objects instead of enumerating them statically, (e.g. the
     * presence of an aggregating repository object represents a logical
     * collection), then a custom {@link WorkspaceManager} would need to be
     * written to bridge the atom feed and repository semantics.
     * </p>
     * <p>
     * If providing a static list of workspaces in the constructor, the provider
     * will create a simple default internal {@link WorkspaceManager} that
     * expects all {@link CollectionInfo} inside each workspace to also
     * implement {@link CollectionAdapter}. Unfortunately, this requirement
     * cannot be reflected entirely in the method signatures in a simple
     * fashion.
     * </p>
     *
     * @param base       String containing a base path for all atom entities, as in
     *                   <code>http://server/servlet/BASE/</code>. Typically, "/" is an
     *                   appropriate value. If null, "/" will be assumed.
     * @param workspaces Collection containing all workspace extension. If null, no
     *                   workspaces will be configured, and it is assumed that a configured
     *                   {@link #setWorkspaceManager(WorkspaceManager)} will be supplied.
     *                   The default provided workspace managed assumes that the
     *                   {@link CollectionInfo} objects contained within the workspace
     *                   extension also implement {@link CollectionAdapter}.
     */
    public DefaultSWORDProvider(String base,
                                Collection<WorkspaceInfo> workspaces) {
        DefaultWorkspaceManager dwm = new DefaultWorkspaceManager();
        dwm.setWorkspaces(workspaces);
        manager = dwm;

        RouteManager routeManager = new RouteManager().addRoute("service",
                                                                base,
                                                                TargetType.TYPE_SERVICE)
            .addRoute("feed", base + ":collection", TargetType.TYPE_COLLECTION)
            .addRoute("entry",
                      base + ":collection/:entry",
                      TargetType.TYPE_ENTRY)
            .addRoute("categories",
                      base + ":collection/:entry;categories",
                      TargetType.TYPE_CATEGORIES);

        targetBuilder = routeManager;
        targetResolver = routeManager;

        //addFilter(new DigestFilter());

        initRequestProcessors();
    }

    private void initRequestProcessors() {
        setRequestProcessors(new HashMap<TargetType, RequestProcessor>() {

            private static final long serialVersionUID = 1L;

            {
                /*
                 * Custom service request processor is necessary to be able to
                 * handle SWORD-compliant service documents
                 */
                put(TargetType.TYPE_SERVICE,
                    new SWORDServiceRequestProcessor());

                put(TargetType.TYPE_COLLECTION,
                    new CollectionRequestProcessor());
                put(TargetType.TYPE_ENTRY, new EntryRequestProcessor());
                put(TargetType.TYPE_MEDIA, new MediaRequestProcessor());
                put(TargetType.TYPE_CATEGORIES,
                    new CategoriesRequestProcessor());
            }});
    }

    public void setTargetBuilder(TargetBuilder builder) {
        this.targetBuilder = builder;
    }

    public TargetBuilder getTargetBuilder(RequestContext request) {
        return targetBuilder;
    }

    public void setTargetResolver(Resolver<Target> resolver) {
        targetResolver = resolver;
    }

    public Resolver<Target> getTargetResolver(RequestContext request) {
        return targetResolver;
    }

    public void setWorkspaceManager(WorkspaceManager newManager) {
        manager = newManager;
    }

    public WorkspaceManager getWorkspaceManager(RequestContext request) {
        return manager;
    }

}
