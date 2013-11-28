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

package org.dataconservancy.dcs.access.server;

import java.io.FileInputStream;
import java.io.IOException;

import org.dataconservancy.dcs.access.client.api.GoogleHelper;
import org.dataconservancy.dcs.access.shared.GoogleDetails;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public final class GoogleAuthHelper extends RemoteServiceServlet
implements GoogleHelper{

	private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	private static GoogleClientSecrets clientSecrets;
	public GoogleAuthHelper(){}
	
	//Get accessToken from client
	public Userinfo getUserInfo(final String accessToken) throws IOException {
 
		final Credential credential = new GoogleCredential().setAccessToken(accessToken);
		 HttpRequestFactory requestFactory =
				  HTTP_TRANSPORT.createRequestFactory(credential);
         GenericUrl url = new GenericUrl(USER_INFO_URL);
         HttpRequest request = requestFactory.buildGetRequest(url);
         request.getHeaders().setContentType("application/json");
         String jsonIdentity = request.execute().parseAsString();
         
         return new Gson().fromJson(jsonIdentity, Userinfo.class);

	}
	
	private void loadClientSecrets() throws IOException{
		clientSecrets =GoogleClientSecrets.load(new JacksonFactory(),
		       		  getClass().getResourceAsStream(
		       				  "../../../../../client_secret.json"
		       				  ));	
	}

	GoogleDetails details;
	
	@Override
	public GoogleDetails getClientId(){
		try{
			if(clientSecrets==null){
				loadClientSecrets();
				details = new GoogleDetails();
				details.setClientId(clientSecrets.getDetails().getClientId());
				details.setGoogleAuthUrl(clientSecrets.getDetails().getAuthUri());
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return details;
	}
	
	
}