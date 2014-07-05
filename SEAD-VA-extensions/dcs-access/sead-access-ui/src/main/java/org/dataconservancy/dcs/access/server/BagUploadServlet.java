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
package org.dataconservancy.dcs.access.server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Post a posted file to a the deposit file upload service. The return value is
 * html containing ^file_source_uri^file_upload_atom_feed_url^
 */
public class BagUploadServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Error: Request type not supported.");
            return;
        }

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List<FileItem> items = upload.parseRequest(req);

            String bagUrl = null;

            for (FileItem item : items) {
                if (item.getFieldName() != null
                        && item.getFieldName().equals("bagUrl")) {
                    bagUrl = item.getString();
                }
            }

            if (bagUrl == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Missing required paremeter: depositurl");
                return;
            }

            for (FileItem item : items) {
                String name = item.getName();

                if (item.isFormField() || name == null || name.isEmpty()) {
                    continue;
                }

                String property = "java.io.tmpdir";


                String tempDir = System.getProperty(property);

                File dir = new File(tempDir);
                String path = dir.getAbsoluteFile()+"/"+
                        item.getName();

                IOUtils.copy(item.getInputStream(), new FileOutputStream(path));
                getSIPfile(bagUrl, path, resp);
            }
        } catch (IOException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error: " + e.getMessage());
            return;
        } catch (FileUploadException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error: " + e.getMessage());
            return;
        } catch (InvalidXmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getSIPfile(String bagitEp,
                           String filename,
                           HttpServletResponse resp
    ) throws IOException, InvalidXmlException {
        Client client = Client.create();
        System.out.println(bagitEp);
        WebResource webResource = client
                .resource(bagitEp);

        File file = new File(filename);
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource
                .path("sip")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);

        if(response.getStatus()==500){
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntityInputStream(), writer);
            resp.getWriter().write(writer.toString());
            resp.setStatus(500);
            resp.flushBuffer();
            return;
        }
        String sipPath = System.getProperty("java.io.tmpdir") + "/"+ UUID.randomUUID().toString() + "_sip_0.xml";// significance of 0 is limiting

        IOUtils.copy(response.getEntityInputStream(),new FileOutputStream(sipPath));
        ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(sipPath));

        StringWriter tempWriter = new StringWriter();
        siptoJsonConverter().toXML(toQueryResult(sip),  tempWriter);
        tempWriter.append(";"+ sipPath);
        resp.getWriter().write(tempWriter.toString());
        //  resp.setHeader("localSipPath", sipPath);
        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.flushBuffer();
    }

    public QueryResult<DcsEntity> toQueryResult(ResearchObject sip){
        long total =0;

        QueryResult<DcsEntity> result = new QueryResult(0, total, "");
        List<QueryMatch<DcsEntity>> matches = new ArrayList<QueryMatch<DcsEntity>>();
        for(DcsDeliverableUnit du:sip.getDeliverableUnits()){
            total++;
            matches.add(new QueryMatch<DcsEntity>(du, ""));
        }

        for(DcsManifestation manifestation:sip.getManifestations()){
            total++;
            matches.add(new QueryMatch<DcsEntity>(manifestation,""));
        }

        for(DcsFile file:sip.getFiles()){
            total++;
            matches.add(new QueryMatch<DcsEntity>(file,""));
        }
        result.setMatches(matches);
        result.setTotal(total);
        return result;

    }

    public QueryResult<DcsEntity> toQueryResultNoManifest(ResearchObject sip){
        long total =0;

        QueryResult<DcsEntity> result = new QueryResult(0, total, "");
        List<QueryMatch<DcsEntity>> matches = new ArrayList<QueryMatch<DcsEntity>>();
        for(DcsDeliverableUnit du:sip.getDeliverableUnits()){
            total++;
            matches.add(new QueryMatch<DcsEntity>(du, ""));
        }

        /*for(DcsManifestation manifestation:sip.getManifestations()){
              total++;
              matches.add(new QueryMatch<DcsEntity>(manifestation,""));
          }*/

        for(DcsFile file:sip.getFiles()){
            total++;
            matches.add(new QueryMatch<DcsEntity>(file,""));
        }
        result.setMatches(matches);
        result.setTotal(total);
        return result;

    }
    public XStream siptoJsonConverter(){

        XStream jsonbuilder = new XStream(new JsonHierarchicalStreamDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        });

        jsonbuilder.setMode(XStream.NO_REFERENCES);
        jsonbuilder.alias("dcp", ResearchObject.class);
        jsonbuilder.alias("deliverableUnit", SeadDeliverableUnit.class);
        jsonbuilder.alias("deliverableUnitRef", DcsDeliverableUnitRef.class);
        jsonbuilder.alias("collection", DcsCollection.class);
        jsonbuilder.alias("file", SeadFile.class);
        jsonbuilder.alias("manifestation", DcsManifestation.class);
        jsonbuilder.alias("event", SeadEvent.class);
        jsonbuilder.alias("metadata", DcsMetadata.class);
        jsonbuilder.alias("collectionRef", DcsCollectionRef.class);
        jsonbuilder.alias("relation", DcsRelation.class);
        jsonbuilder.alias("fixity", DcsFixity.class);
        jsonbuilder.alias("fileRef", DcsFileRef.class);
        jsonbuilder.alias("entityRef", DcsEntityReference.class);
        jsonbuilder.alias("metadataRef", DcsMetadataRef.class);
        jsonbuilder.alias("format", DcsFormat.class);

        return jsonbuilder;
    }

    public XStream jsonToSipConverter(){

        XStream xmlbuilder = new XStream(new JettisonMappedXmlDriver());

        //	xmlbuilder.setMode(XStream.NO_REFERENCES);
        xmlbuilder.alias("queryResult", QueryResult.class);
        xmlbuilder.alias("dcp", ResearchObject.class);
        xmlbuilder.alias("deliverableUnit", SeadDeliverableUnit.class);
        xmlbuilder.alias("deliverableUnitRef", DcsDeliverableUnitRef.class);
        xmlbuilder.alias("collection", DcsCollection.class);
        xmlbuilder.alias("file", SeadFile.class);
        xmlbuilder.alias("manifestation", DcsManifestation.class);
        xmlbuilder.alias("event", SeadEvent.class);
        xmlbuilder.alias("metadata", DcsMetadata.class);
        xmlbuilder.alias("collectionRef", DcsCollectionRef.class);
        xmlbuilder.alias("relation", DcsRelation.class);
        xmlbuilder.alias("fixity", DcsFixity.class);
        xmlbuilder.alias("fileRef", DcsFileRef.class);
        xmlbuilder.alias("entityRef", DcsEntityReference.class);
        xmlbuilder.alias("metadataRef", DcsMetadataRef.class);
        xmlbuilder.alias("format", DcsFormat.class);
        xmlbuilder.alias("java.util.Collection", HashSet.class);

        return xmlbuilder;
    }
}
