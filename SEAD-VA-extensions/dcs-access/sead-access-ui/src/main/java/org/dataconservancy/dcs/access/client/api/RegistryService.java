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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.ROMetadata;
import org.dataconservancy.model.builder.InvalidXmlException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("registry")
public interface RegistryService
        extends RemoteService {

    boolean registerAgents(List<Person> persons, String registryUrl);
    List<ROMetadata> getAllCOs(String repository, String agentId, String roUrl) throws IOException;
    String getRO(String roId, String roUrl) throws IOException;
    void putRO(String sipPath, String roUrl);
	boolean trackEvent(String agentId, String entityId, String roUrl);
	boolean makeObsolete(String entityId, String roUrl);
	void updateSip(String sipPath, String entityId, String key, String value) throws Exception;
	String getROAffiliation(String entityId, String registryUrl) throws Exception;
	boolean assignToAgent(String entityId, String agentId, String registryUrl) throws Exception;
}
