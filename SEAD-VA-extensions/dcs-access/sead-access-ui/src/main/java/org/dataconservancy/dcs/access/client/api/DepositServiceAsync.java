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

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.dataconservancy.dcs.access.client.upload.DepositConfig;
import org.dataconservancy.dcs.access.client.upload.model.Package;
import org.dataconservancy.dcs.access.shared.Event;

public interface DepositServiceAsync {

  
    void submitSIP(String endpoint,
                   String user,
                   String pass,
                   Package pkg,
                   AsyncCallback<String> cb);

	void getDepositConfig(String endpoint, AsyncCallback<DepositConfig> callback);

	void checkStatus(String process, String statusUrl, int expectedCount,
			AsyncCallback<String> callback);

	void checkDownload(String url, AsyncCallback<Boolean> callback);
	void getStatusDetails(String process, String statusUrl, int expectedCount,
			AsyncCallback<String> callback);

	void statusUpdate(String statusUrl, Date latestDate,
			AsyncCallback<Map<Date, List<Event>>> callback);

	void loadDuIds(List<String> statusUrl, AsyncCallback<Void> callback);

	void deleteCollection(String id, String endpoint, AsyncCallback<Boolean> callback);

	void getLinks(String urlStr, AsyncCallback<String> callback);
    
}
