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
package org.dataconservancy.dcs.ingest.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dataconservancy.dcs.ingest.ui.client.model.Package;

@RemoteServiceRelativePath("deposit")
public interface DepositService
        extends RemoteService {

    DepositConfig login(String url, String user, String pass)
            throws RPCException;

    /**
     * @param endpoint
     * @param user
     * @param pass
     * @param pkg
     * @return Ticket for SIP.
     * @throws RPCException
     */
    String submitSIP(String endpoint, String user, String pass, Package pkg)
            throws RPCException;
}
