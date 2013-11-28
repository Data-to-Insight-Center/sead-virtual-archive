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

import org.dataconservancy.dcs.access.client.model.SchemaType.Name;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TransformerServiceAsync {

   void validateXML(String inputXml, String schemaURI,
			AsyncCallback<Name> callback);

	void xslTransform(Name inputSchema, Name outputSchema, String metadataXml,
			AsyncCallback<String> callback);

	void fgdcToHtml(String inputFilePath, String format, AsyncCallback<String> callback);

	void dateToString(Date date, AsyncCallback<String> callback);

}
