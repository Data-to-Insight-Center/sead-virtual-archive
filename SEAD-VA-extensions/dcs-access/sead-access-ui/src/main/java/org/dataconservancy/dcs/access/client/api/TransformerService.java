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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.dataconservancy.dcs.access.client.model.SchemaType;

import java.util.Date;


@RemoteServiceRelativePath("transform")
public interface TransformerService
        extends RemoteService {

	String xslTransform(SchemaType.Name inputSchema, SchemaType.Name outputSchema, String metadataXml) throws Exception;
    
	SchemaType.Name validateXML(String inputXml, String schemaURI) throws Exception;
	String fgdcToHtml(String inputFilePath, String format);

	String dateToString(Date date);
	String readFile(String filePath);
	
}
