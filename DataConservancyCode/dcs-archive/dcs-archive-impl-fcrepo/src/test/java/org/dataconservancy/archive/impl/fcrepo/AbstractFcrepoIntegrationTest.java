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
package org.dataconservancy.archive.impl.fcrepo;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.impl.fcrepo.ri.HttpClientConfig;
import org.dataconservancy.archive.impl.fcrepo.ri.RIClient;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractFcrepoIntegrationTest {

    protected static String fedoraUrl; // e.g. http[s]://host:port/fedora
    protected static FedoraClient fedoraClient;
    protected static RIClient riClient;
    protected static FcrepoArchiveStore store;
    protected static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void setUpClass() {
        String url = getRequiredProp("fedora.url");
        String user = getRequiredProp("fedora.user");
        String pass = getRequiredProp("fedora.pass");
        FedoraCredentials creds = null;
        try {
            creds = new FedoraCredentials(url, user, pass);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL given for "
                    + "fedora.url: " + url);
        }
        fedoraUrl = creds.getBaseUrl().toString(); // normalized
        fedoraClient = new FedoraClient(creds);
        riClient = new RIClient(creds, new HttpClientConfig());
        store = new FcrepoArchiveStore(fedoraClient, riClient);
        deleteAllEntities(); // clean up from previous run
    }

    protected static String getRequiredProp(String name) {
        String value = System.getProperty(name);
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("Required system property not "
                    + "set: " + name);
        }
        return value;
    }

    protected static Set<String> toSet(Iterator<String> iter) {
        HashSet<String> set = new HashSet<String>();
        while (iter.hasNext()) {
            set.add(iter.next());
        }
        return set;
    }

    protected static void deleteAllEntities() {
        Iterator<String> iter = store.listEntities(null);
        while (iter.hasNext()) {
            store.deleteEntity(iter.next());
        }
    }

}
