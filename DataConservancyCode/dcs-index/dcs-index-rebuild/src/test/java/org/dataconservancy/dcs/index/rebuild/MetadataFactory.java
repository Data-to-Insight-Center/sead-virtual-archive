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
package org.dataconservancy.dcs.index.rebuild;

import java.io.File;

import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;

public class MetadataFactory {

    private File dir = new File(System.getProperty("java.io.tmpdir"));

    public void setDirectory(File d) {
        this.dir = d;
    }

    public Metadata forEntity(DcsEntity e) {
        return new Metadata(e.getId(), e.getClass(), "metadata_file_for_"
                + e.getId() + "." + UUID.randomUUID().toString(), dir);
    }

    @SuppressWarnings("unchecked")
    public Metadata fromFile(DcsFile file) {
        try {
            Document md =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(file.getSource());
            String entityId =
                    md.getElementsByTagName("entityId").item(0)
                            .getTextContent();
            String fileId =
                    md.getElementsByTagName("fileId").item(0).getTextContent();
            Class<? extends DcsEntity> type =
                    (Class<? extends DcsEntity>) Thread
                            .currentThread()
                            .getContextClassLoader()
                            .loadClass(md.getElementsByTagName("type").item(0)
                                    .getTextContent());
            return new Metadata(entityId, type, fileId, dir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
