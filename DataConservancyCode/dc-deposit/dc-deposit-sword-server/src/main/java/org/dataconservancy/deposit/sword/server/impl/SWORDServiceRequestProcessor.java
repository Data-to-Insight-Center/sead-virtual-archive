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

import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.WorkspaceManager;

import org.dataconservancy.deposit.sword.SWORDConfig;
import org.dataconservancy.deposit.sword.extension.SWORDExtensionFactory;

public class SWORDServiceRequestProcessor
        implements RequestProcessor {

    private SWORDConfig sword = new SWORDConfig();

    public void setSWORDConfig(SWORDConfig config) {
        sword = config;
    }

    public SWORDConfig getSWORDConfig() {
        return sword;
    }

    public ResponseContext process(RequestContext requestContext,
                                   WorkspaceManager workspaceManager,
                                   CollectionAdapter collectionAdapter) {

        Service serviceDoc =
                requestContext.getAbdera().getFactory().newService();

        serviceDoc.addExtension(SWORDExtensionFactory.VERSION).setText(sword
                .getVersion());
        serviceDoc.addExtension(SWORDExtensionFactory.NO_OP).setText(Boolean
                .toString(sword.getNoOp()));
        serviceDoc.addExtension(SWORDExtensionFactory.VERBOSE).setText(Boolean
                .toString(sword.getVerbose()));
        if (sword.getMaxUploadSize() > 0) {
            serviceDoc.addExtension(SWORDExtensionFactory.MAX_UPLOAD_SIZE)
                    .setText(Integer.toString(sword.getMaxUploadSize()));
        }

        for (WorkspaceInfo wi : workspaceManager.getWorkspaces(requestContext)) {
            serviceDoc.addWorkspace(wi.asWorkspaceElement(requestContext));
        }

        return ProviderHelper.returnBase(serviceDoc, 200, null);
    }

}
