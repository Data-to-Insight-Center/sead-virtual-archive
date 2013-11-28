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
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.WorkspaceManager;

import org.junit.Test;

public class SWORDServiceRequestProcessorTest {

    @Test
    public void test() throws IOException {
        StringWriter writer = new StringWriter();
        new SWORDServiceRequestProcessor().process(new MockRequestContext(),
                                                   new MockWorspaceManager(),
                                                   null).writeTo(writer);
        System.out.print(writer.toString());
    }

    private class MockWorspaceManager
            implements WorkspaceManager {

        ArrayList<WorkspaceInfo> workspaces = new ArrayList<WorkspaceInfo>();

        public CollectionAdapter getCollectionAdapter(RequestContext request) {
            return null;
        }

        public Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
            return workspaces;
        }

    }
}
