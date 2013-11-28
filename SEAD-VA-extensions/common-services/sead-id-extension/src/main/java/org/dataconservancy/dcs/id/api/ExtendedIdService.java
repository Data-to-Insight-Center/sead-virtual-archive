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

package org.dataconservancy.dcs.id.api;

import java.io.IOException;
import java.util.Map;

/**
 *Extended Id service interface
 */
public interface ExtendedIdService extends IdService {

    public void setCredentials(String username, String password);
    public  void setService(String service);

    public Identifier createwithMd(Map metadata, boolean update) throws IdentifierNotFoundException, IOException;

}
