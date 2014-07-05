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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.dataconservancy.dcs.access.shared.*;

import java.util.List;


public interface UserServiceAsync {

    void authenticate(String endpoint,
                      String user,
                      String name,
                      AsyncCallback<Authentication> cb);

    void register(String firstName, String lastName, String email,
                  String password, String[] admins, String vivoId,
                  AsyncCallback<String> callback);

    void checkSession(String token, AsyncCallback<UserSession> callback);

    void getAllUsers(AsyncCallback<List<Person>> callback);

    void clearSession(AsyncCallback<Void> callback);

    void updateAllUsers(List<Person> userList, List<Person> sendEmailList,
                        String registryUrl, AsyncCallback<Void> callback);

    void sendEmail(String[] toAddress, String subject, String messageStr,
                   AsyncCallback<Void> callback);

    void setSession(String token, AsyncCallback<Void> callback);

    void authenticateOAuth(String token, OAuthType type, String[] admins,
                           AsyncCallback<Authentication> callback);

    void getAllRoles(AsyncCallback<List<Role>> callback);

    void emailCurators(String affiliation, AsyncCallback<Boolean> callback);

    void getUser(String registryId, AsyncCallback<Person> callback);

    void emailResearcher(String agentId, String entityUrl, AsyncCallback<Boolean> callback);

}
