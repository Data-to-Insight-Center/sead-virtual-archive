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

import java.util.List;

import org.dataconservancy.dcs.access.shared.Authentication;
import org.dataconservancy.dcs.access.shared.OAuthType;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("user")
public interface UserService
        extends RemoteService {

    Authentication authenticate(String url, String user, String pass) throws Exception;
    
	String register(String firstName,String lastName, String email, String password,
			String confirmPwd, String[] admins) throws Exception;

	UserSession checkSession(String token);

	List<Person> getAllUsers() throws Exception;

	void clearSession();

	void updateAllUsers(List<Person> userList, List<Person> sendEmailList) throws Exception;
    
	void sendEmail(String[] toAddress, String subject, String messageStr);
	
	void setSession(String token);

	Authentication authenticateOAuth(String token, OAuthType type, String[] admins);

}
