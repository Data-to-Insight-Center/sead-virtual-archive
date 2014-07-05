/*
 * Copyright 2014 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.client.api;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.ROMetadata;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RemoteServiceRelativePath("registry")
public interface RegistryService
        extends RemoteService {

    /* Registry calls */
    /* Used to register agents in the registry */
    boolean registerAgents(List<Person> persons, String registryUrl);
    String getRelation(String causeId, String registryUrl, String relationType) throws IOException;
    boolean assignToSubmitter(String entityId, String agentId,
                              String registryUrl) throws Exception;
    String getROAffiliation(String entityId, String registryUrl) throws Exception;
    boolean isObsolete(String entityId, String registryUrl) throws IOException;
    boolean assignToAgent(String entityId, String agentId, String registryUrl) throws Exception;
    boolean unassignFromAgent(String entityId, String agentId, String registryUrl) throws Exception;


    /* RO Subsystem calls */
    String getRO(String roId, String roUrl) throws IOException;
    void putRO(String sipPath, String roUrl);
    List<ROMetadata> getAllCOs(String repository, String agentId, String roUrl) throws IOException;
    List<ROMetadata> getAllROs(String repository, String agentId, String roUrl) throws IOException;
    boolean trackEvent(String agentId, String entityId, String roUrl);
    boolean trackRevision(String previousROId, String nextROId, String roUrl); //this shouldn't be called from here
    boolean makeObsolete(String entityId, String roUrl);
    String getSip(String roId, String roUrl) throws IOException;


    //Shouldn't be here
    void updateSip(String sipPath, String entityId, String key, String value) throws Exception;
    void updateSip(String sipPath, String entityId, Map<String, String> changes) throws Exception;
    void cleanSip(String sipPath) throws Exception;
}
