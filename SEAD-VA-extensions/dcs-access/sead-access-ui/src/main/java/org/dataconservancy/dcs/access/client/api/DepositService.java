/*
 * Copyright 2013 The Trustees of Indiana University
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dataconservancy.dcs.access.client.upload.DepositConfig;
import org.dataconservancy.dcs.access.client.upload.RPCException;
import org.dataconservancy.dcs.access.client.upload.model.Package;
import org.dataconservancy.dcs.access.shared.Event;

@RemoteServiceRelativePath("deposit")
public interface DepositService
        extends RemoteService {


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
    DepositConfig getDepositConfig(String endpoint);
    String checkStatus(String process, String statusUrl, int expectedCount);
    boolean checkDownload(String url);
    String getLinks(String urlStr);
	String getStatusDetails(String process, String statusUrl, int expectedCount);
	Map<Date,List<Event>> statusUpdate(String statusUrl, Date latestDate);
	void loadDuIds(List<String> statusUrl);
	boolean deleteCollection(String id, String endpoint);
}
