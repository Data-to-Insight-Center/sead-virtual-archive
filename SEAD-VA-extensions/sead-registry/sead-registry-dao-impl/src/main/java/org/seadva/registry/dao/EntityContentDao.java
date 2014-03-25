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

package org.seadva.registry.dao;

import java.io.InputStream;


public class EntityContentDao {
    public int getEntityContentId() {
        return entityContentId;
    }

    public void setEntityContentId(int entityContentId) {
        this.entityContentId = entityContentId;
    }

    public InputStream getEntityContent() {
        return entityContent;
    }

    public void setEntityContent(InputStream entityContent) {
        this.entityContent = entityContent;
    }

    private int entityContentId;
    private InputStream entityContent;


}
