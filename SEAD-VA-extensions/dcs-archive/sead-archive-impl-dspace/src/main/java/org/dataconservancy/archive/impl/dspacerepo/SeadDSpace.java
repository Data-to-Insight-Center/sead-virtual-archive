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

package org.dataconservancy.archive.impl.dspacerepo;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import edu.mit.libraries.facade.app.DSpaceSIP;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.log4j.Logger;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.purl.eprint.epdcx.x20061116.*;
import org.purl.sword.base.DepositResponse;
import org.purl.sword.client.Client;
import org.purl.sword.client.PostMessage;
import org.purl.sword.client.Status;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;
import org.seadva.model.pack.ResearchObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.Deflater;

/**
 * This class has methods to create collections, communities in DSpace and to submit Items in DSpace through SWORD
 */

public class SeadDSpace {
    
    List<Element> decriptiveMd = new ArrayList<Element>();

    private final static Logger logger = Logger.getLogger(SeadDSpace.class.getName());
    public String createPackage(String[] fileNames,String role, String type, String agent,String[] file,File targetDir)
    {

        try
        {
            DSpaceSIP sip = new DSpaceSIP(false, Deflater.BEST_SPEED);
            String objID = UUID.randomUUID().toString();
            sip.setOBJID(objID);
            sip.addAgent(role,type,agent);
            for(int i =0;i<fileNames.length;i++)
            {
                File tempFile = new File(fileNames[i]);
                sip.addBitstream(tempFile,
                        file[i],
                        "ORIGINAL",
                        true);
            }

            for(int i=0;i<decriptiveMd.size();i++)
            {
//                sip.addDescriptiveMD("EPDCX", decriptiveMd.get(i));
                sip.addDescriptiveMD("DIM", decriptiveMd.get(i));
            }
            // Write SIP to a file
            
            File outfile = File.createTempFile(objID,".zip", targetDir);
            sip.write(outfile);
            return outfile.getAbsolutePath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }
    //get it to return the target URL of the item
    public Map<String,String> uploadPackage(String collectionUrl,boolean login, String fileName)
    {
        Client client = new Client();
        DepositResponse resp;
        Status status = null;
        Map<String,String> linkList = null;
        try {
            PostMessage message = new PostMessage();
            message.setUserAgent("DSpace Sword Client");
            String filetype ="application/zip";
            String contentDisposition = null;

            boolean useMD5 = false;
            boolean errorMD5 = false;
            boolean verbose = true;
            boolean noOp = false;


            message.setDestination(
                    collectionUrl
            );
            URL url = new URL(
                    collectionUrl
            );
            int port = url.getPort();
            if (port == -1) {
                port = 80;
            }

            client.setSocketTimeout(60*60*1000);
            client.setServer(url.getHost(), port);
            message.setFormatNamespace("http://purl.org/net/sword-types/METSDSpaceSIP");
            message.setNoOp(noOp);
            message.setVerbose(verbose);


            message.setFilepath(fileName);
            message.setFiletype(filetype);

            //if (login) {
                client.setCredentials(userName, passWord);
            //}


            resp = client.postFile(message);
//            System.out.print(resp);
            if(!( client.getStatus().getCode()==200|| client.getStatus().getCode()==202|| client.getStatus().getCode()==201))
                throw new RuntimeException("Error uploading package");

           // getItemHandle(resp.toString());
            linkList = getDspaceStreams(resp.toString())  ;
            String respString = resp.toString();
            String handle = respString.substring(respString.indexOf("<atom:id>")+9,respString.indexOf("</atom:id>"));
            linkList.put("handle",handle);

            status = client.getStatus();

            //xml pull parser to get the Item Handle

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally {
            if(client!=null)
            {    client.clearProxy();

            }

        }
        if(status.getMessage().equalsIgnoreCase("Created"))
            return  linkList;
        else
            return linkList;
    }

    public Map<String,String> getDspaceStreams(String dspaceResp){
        try {
            Map<String,String> linkList =  new HashMap<String, String>();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new ByteArrayInputStream(dspaceResp.getBytes()));

            // normalize text representation
            doc.getDocumentElement ().normalize ();


            NodeList links = doc.getElementsByTagName("atom:link");
            int totalLinks = links.getLength();
            System.out.println("Total no of links : " + totalLinks);

            for(int s=0; s<totalLinks ; s++){


                Node link = links.item(s);
                //System.out.println(link.getAttributes().getNamedItem("href") );
                String linkStr = link.getAttributes().getNamedItem("href").toString();
                String[] fName = linkStr.split("/");

                if(logger.isDebugEnabled()){
                   logger.debug(fName[fName.length-1].replace("\"","")+":"+linkStr.replace("\"","").replace("href=",""));
                }

                linkList.put(fName[fName.length-1].replace("\"",""),linkStr.replace("\"","").replace("href=",""));

                }//end of if clause
            return  linkList;

        }catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line "
                    + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());

        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();

        }catch (Throwable t) {
            t.printStackTrace ();
        }
        return null;
    }

    // creating dspace metadata in DIM format
    public void descriptiveMetadataDim(ResearchObject ro, String title) {
        // get the first one -- will there ever be more than one?
        DcsDeliverableUnit unit = ro.getDeliverableUnits().iterator().next();
        Map<String, List<String>> metadataMap = Util.extractMetadata(unit.getMetadata());

        try {
            decriptiveMd = new ArrayList<Element>();
            // generate template in jdom
            Document xmlDoc = Util.getDimTemplate();
            Element root = xmlDoc.getRootElement();

            // add metadata as children
            Util.addDimMetadata(root, "title", null, title);
            String abstr = ((SeadDeliverableUnit) unit).getAbstrct();
            Util.addDimMetadata(root, "description", "abstract", abstr);
            Util.addDimMetadata(root, "identifier", "uri",
                    "http://seadva.d2i.indiana.edu/sead-access/#entity;" + unit.getId());

            // TODO : A quick solution to eliminate submitter from creator list
            String submitter = ((SeadDeliverableUnit) unit).getSubmitter().getName().trim();
            // have to remove duplicates too
            List<String> addedList = new ArrayList<String>();
            Set<SeadPerson> creators = ((SeadDeliverableUnit)unit).getDataContributors();
            for (SeadPerson creator : creators) {
                String name = creator.getName();
                if (addedList.contains(name) || name.trim().equals(submitter)) {
                    continue;
                }
                Util.addDimMetadata(root, "contributor", "author", Util.formatName(name));
                addedList.add(name);
            }

            Collection<DcsResourceIdentifier> altIds = unit.getAlternateIds();
            for (DcsResourceIdentifier id : altIds) {
                String idVal = id.getIdValue();
                if (idVal.contains("doi")) {
                    Util.addDimMetadata(root, "identifier", "doi", idVal);
                } else {
                    Util.addDimMetadata(root, "identifier", "uri", idVal);
                }
            }

            for (Map.Entry<String, List<String>> meta : metadataMap.entrySet()) {
                String key = meta.getKey();
                for (String metaValue : meta.getValue()) {
                    String element = null;
                    String qualifier = null;
                    if (key.contains("subject")) {
                        element = "subject";
                    } else if (key.contains("coverage")) {
                        element = "coverage";
                        qualifier = "spatial";
                    } else if (key.contains("description")) {
                        element = "description";
                    } else if (key.contains("label")) {
                        element = "title";
                        qualifier = "alternative";
                    }
                    Util.addDimMetadata(root, element, qualifier, metaValue);
                }
            }

            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println(new XMLOutputter().outputString(xmlDoc));
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");

            decriptiveMd.addAll(xmlDoc.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void descriptiveMetadataDim(Map<String, List<String>> metadataMap, String title,
                                       String doi, String rights) {
        try {
            decriptiveMd = new ArrayList<Element>();
            // generate template in jdom
            Document xmlDoc = Util.getDimTemplate();
            Element root = xmlDoc.getRootElement();

            // add metadata as children
            if (doi != null && !"".equals(doi))
                Util.addDimMetadata(root, "identifier", "doi", doi);

            boolean isTitleSet = false;
            for (Map.Entry<String, List<String>> meta : metadataMap.entrySet()) {
                String key = meta.getKey();
                for (String metaValue : meta.getValue()) {
                    String element = null;
                    String qualifier = null;
                    if (key.contains("creator")) {
                        element = "contributor";
                        qualifier = "author";
                    } else if (key.contains("abstract")) {
                        element = "description";
                        qualifier = "abstract";
                    } else if (key.contains("subject")) {
                        element = "subject";
                    } else if (key.contains("coverage") || key.contains("location")) {
                        element = "coverage";
                        qualifier = "spatial";
                    } else if (key.contains("description")) {
                        element = "description";
                    } else if (key.contains("label")) {
                        element = "title";
                        qualifier = "alternative";
                    } else if (key.contains("alternative")) {
                        element = "title";
                        isTitleSet = true;
                    }
                    Util.addDimMetadata(root, element, qualifier, metaValue);
                }
            }

            if (!isTitleSet) {
                Util.addDimMetadata(root, "title", null, title);
            }

            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println(new XMLOutputter().outputString(xmlDoc));
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");

            decriptiveMd.addAll(xmlDoc.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void descriptiveMetadata(ResearchObject ro, String title) {
        // get the first one -- will there ever be more than one?
        DcsDeliverableUnit unit = ro.getDeliverableUnits().iterator().next();

        try {
            decriptiveMd = new ArrayList<Element>();

            DescriptionSetDocument descriptionSetDocument = DescriptionSetDocument.Factory.newInstance();
            DescriptionSetElement descriptionSetElement = descriptionSetDocument.addNewDescriptionSet();
            DescriptionElement descriptionElement = descriptionSetElement.addNewDescription();

            descriptionElement.setResourceId("sword-mets-epdcx-1");
            StatementElement statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/type");
            statementElement.setValueURI("http://purl.org/eprint/entityType/ScholarlyWork");

            if (title == null) {
                title = unit.getTitle();
            }
            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/title");
            ValueStringElement valueStringElement = statementElement.addNewValueString();
            valueStringElement.setStringValue(title);

            String abstr = ((SeadDeliverableUnit) unit).getAbstrct();
            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/terms/abstract");
            valueStringElement = statementElement.addNewValueString();
            valueStringElement.setStringValue(abstr);

            // TODO : A quick solution to eliminate submitter from creator list
            String submitter = ((SeadDeliverableUnit) unit).getSubmitter().getName().trim();
            // have to remove duplicates too
            List<String> addedList = new ArrayList<String>();
            Set<SeadPerson> creators = ((SeadDeliverableUnit)unit).getDataContributors();
            for (SeadPerson creator : creators) {
                String name = creator.getName();
                if (addedList.contains(name) || name.trim().equals(submitter)) {
                    continue;
                }
                statementElement = descriptionElement.addNewStatement();
                statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/creator");
                valueStringElement = statementElement.addNewValueString();
                valueStringElement.setStringValue(Util.formatName(name));
                addedList.add(name);
            }

            String rights = unit.getRights();
            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/terms/rights");
            valueStringElement = statementElement.addNewValueString();
            valueStringElement.setStringValue(rights);

            String dummyDOI = unit.getId();
            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/identifier");
            valueStringElement = statementElement.addNewValueString();
            valueStringElement.setSesURI("http://purl.org/dc/terms/URI");
            valueStringElement.setStringValue("http://seadva.d2i.indiana.edu/sead-access/#entity;" + dummyDOI);

            Collection<DcsResourceIdentifier> altIds = unit.getAlternateIds();
            for (DcsResourceIdentifier id : altIds) {
                String idVal = id.getIdValue();
                statementElement = descriptionElement.addNewStatement();
                statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/identifier");
                valueStringElement = statementElement.addNewValueString();
                valueStringElement.setSesURI("http://purl.org/dc/terms/URI");
                valueStringElement.setStringValue(idVal);
            }

            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/eprint/terms/isExpressedAs");
            statementElement.setValueURI("sword-mets-expr-1");

            DescriptionElement descriptionElement_expr = descriptionSetElement.addNewDescription();
            descriptionElement_expr.setResourceId("sword-mets-expr-1");

            statementElement = descriptionElement_expr.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/type");
            statementElement.setValueURI("http://purl.org/eprint/entityType/Expression");

            statementElement = descriptionElement_expr.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/language");
            statementElement.setVesURI("http://purl.org/dc/terms/RFC3066");
            statementElement.setValueURI("en");
            InputStream in = new ByteArrayInputStream(
                descriptionSetDocument.xmlText().getBytes()
            );

            SAXBuilder builder = new SAXBuilder();
            Document xmlDoc = builder == null ? null : builder.build(in);

            decriptiveMd.addAll(xmlDoc.getContent());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void descriptiveMetadata(Map<String, String> metadataMap, String title, String abstr,
                                    String creator, String dummyDOI, String rights)
    {
        try
        {
            decriptiveMd = new ArrayList<Element>();

            DescriptionSetDocument descriptionSetDocument = DescriptionSetDocument.Factory.newInstance();
            DescriptionSetElement descriptionSetElement = descriptionSetDocument.addNewDescriptionSet();
            DescriptionElement descriptionElement = descriptionSetElement.addNewDescription();

            descriptionElement.setResourceId("sword-mets-epdcx-1");
            StatementElement statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/type");
            statementElement.setValueURI("http://purl.org/eprint/entityType/ScholarlyWork");

            for (Map.Entry<String, String> meta : metadataMap.entrySet()) {
                statementElement = descriptionElement.addNewStatement();
                String key = meta.getKey();
                String predicate;
//                if (key.contains("title") || key.contains("creator")) {
                if (key.contains("title")) {
                    continue;
                }

                if (key.contains("creator")) {
                    predicate = "http://purl.org/dc/elements/1.1/creator";
                } else {
                    predicate = key;
                }
                statementElement.setPropertyURI(predicate);
                ValueStringElement valueStringElement = statementElement.addNewValueString();
                valueStringElement.setStringValue(meta.getValue());
            }

            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/title");
            ValueStringElement valueStringElement = statementElement.addNewValueString();
            valueStringElement.setStringValue(title);


//            statementElement = descriptionElement.addNewStatement();
//            statementElement.setPropertyURI("http://purl.org/dc/terms/abstract");
//            valueStringElement = statementElement.addNewValueString();
//            valueStringElement.setStringValue(abstr);

//            statementElement = descriptionElement.addNewStatement();
//            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/creator");
//            valueStringElement = statementElement.addNewValueString();
//            valueStringElement.setStringValue(creator);

            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/terms/rights");
            valueStringElement = statementElement.addNewValueString();
            valueStringElement.setStringValue(rights);

            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/identifier");
            valueStringElement = statementElement.addNewValueString();
            valueStringElement.setSesURI("http://purl.org/dc/terms/URI");
            valueStringElement.setStringValue(dummyDOI);


            statementElement = descriptionElement.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/eprint/terms/isExpressedAs");
            statementElement.setValueURI("sword-mets-expr-1");

            DescriptionElement descriptionElement_expr = descriptionSetElement.addNewDescription();
            descriptionElement_expr.setResourceId("sword-mets-expr-1");

            statementElement = descriptionElement_expr.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/type");
            statementElement.setValueURI("http://purl.org/eprint/entityType/Expression");

            statementElement = descriptionElement_expr.addNewStatement();
            statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/language");
            statementElement.setVesURI("http://purl.org/dc/terms/RFC3066");
            statementElement.setValueURI("en");
            InputStream in = new ByteArrayInputStream(
                descriptionSetDocument.xmlText().getBytes()
                );

            Document xmlDoc = null;
            SAXBuilder builder = new SAXBuilder();
            if (builder == null) {
                xmlDoc = null;
            } else {
                xmlDoc = builder.build(in);
            }

            decriptiveMd.addAll(xmlDoc.getContent());

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private String userName;
    private String passWord;

    public SeadDSpace(){}

    public SeadDSpace(String uName, String pwd){

        this.userName = uName;
        this.passWord= pwd;
    }

    private final String DC = "http://purl.org/dc/elements/1.1/";
    private final String DCTERMS = "http://purl.org/dc/terms/";
    private final String XSI = "http://www.w3.org/2001/XMLSchema-instance";

    private final QName DC_TYPE = new QName(DC, "type", "dc");
    private final QName DC_TITLE = new QName(DC, "title", "dc");
    private final QName DC_RIGHTS = new QName(DC, "rights", "dc");
    private final QName DC_CREATOR = new QName(DC, "creator", "dc");
    private final QName DC_DATE = new QName(DC, "date", "dc");
    private final QName DCTERMS_ALTERNATIVE = new QName(DCTERMS, "alternative", "dcterms");
    private final QName DCTERMS_ABSTRACT = new QName(DCTERMS, "abstract", "dcterms");
    private final QName DCTERMS_CONFORMSTO = new QName(DCTERMS, "conformsTo", "dcterms");
    private final QName DCTERMS_PROVENANCE = new QName(DCTERMS, "provenance", "dcterms");
    private final QName DCTERMS_CREATOR = new QName(DCTERMS, "creator", "dcterms");



    /*
      * Client for performing HTTP operations on SEAD REST Server
      *
      *	URL for Communities:
      *  @GET: http://localhost:8080/sead/communities (GETS all the communities)
      *  @POST: http://localhost:8080/sead/communities/{parentCommunityID}
      *  @PUT: http://localhost:8080/sead/communities/{communityID}
      *  @DELETE: http://localhost:8080/sead/communities/{communityID}
      *
      *  URL for Collections:
      *  @GET: http://localhost:8080/sead/communities/{parentCommunityID}/collections (GETS all the collections in given Community)
      *  @POST: http://localhost:8080/sead/communities/{parentCommunityID}/collections
      *  @PUT: http://localhost:8080/sead/communities/{communityID}/collections/{collectionID}
      *  @DELETE: http://localhost:8080/sead/communities/{communityID}/collections/{collectionID}
      */
    public String createCollection(ResearchObject pkg, String communityId, String title) {
        // read metadata from deliverable unit
        SeadDeliverableUnit unit = (SeadDeliverableUnit) pkg.getDeliverableUnits().iterator().next();
        String abstr = unit.getAbstrct();
        Map<String, List<String>> metadataMap = Util.extractMetadata(unit.getMetadata());

        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
        client.addFilter(new HTTPBasicAuthFilter(userName, passWord));

        System.out.print("Values being used for creating collection:"+userName+","+passWord+":"+communityId+"----------\n");
        WebResource webResource = client.resource(communityId+"/collections");
        //"http://localhost:8080/sead33/communities/2"
        //WebResource webResource = client.resource("http://localhost:8080/sead33/communities/2/collections");
        String collectionID = null;
        ClientResponse response = null;
        try {
            Abdera abdera = Abdera.getInstance();

            // TODO : Only these predicates work at IDEALS side
//            String name = entry.getExtension(DC_TITLE).getText();
//            String copyrightText = entry.getExtension(DC_RIGHTS).getText();
//            String shortDescription = entry.getExtension(DCTERMS_ALTERNATIVE).getText();
//            String introductoryText = entry.getExtension(DCTERMS_ABSTRACT).getText();
//            String licenseText = entry.getExtension(DCTERMS_CONFORMSTO).getText();
//            String provenanceText = entry.getExtension(DCTERMS_PROVENANCE).getText();

            Entry entry = abdera.newEntry();
            entry.addExtension(DC_TYPE).setText("Collection");
            if(unit.getRights() != null) {
                entry.addExtension(DC_RIGHTS).setText(unit.getRights());
            } else {
                entry.addExtension(DC_RIGHTS).setText("Rights statement from SEAD");
            }
            entry.addExtension(DCTERMS_ALTERNATIVE).setText("Dataset that supports a publication");
            entry.addExtension(DCTERMS_ABSTRACT).setText(abstr);
            entry.addExtension(DCTERMS_CONFORMSTO).setText("Creative Commons");
            entry.addExtension(DCTERMS_PROVENANCE).setText("Submitted by SEAD VA on " + new Date().toString());

            boolean isTitleSet = false;
            for (Map.Entry<String, List<String>> meta : metadataMap.entrySet()) {
                String key = meta.getKey();
                if (key.contains("http://purl.org/dc/terms/alternative")) {
                    String time = new Timestamp(new Date().getTime()).toString();
                    time = time.substring(0, time.lastIndexOf(':'));
                    entry.addExtension(DC_TITLE).setText(meta.getValue().get(0) + " " + time);
                    isTitleSet = true;
                }
            }

            if (!isTitleSet) {
                entry.addExtension(DC_TITLE).setText(title);
            }

            // TODO : Setting other metadata here is useless because Ideals don't recognize them
            // TODO : As an alternative, we set all metadata at ORE level

            entry.setUpdated(new Date());

            response = webResource.type("application/xml").post(ClientResponse.class,entry);
            client.destroy();
            if( response.getHeaders().get("Handle")==null) {
                throw new RuntimeException("Error creating DSpace Collection");
            }
            collectionID = response.getHeaders().get("Handle").get(0);

        } catch (UniformInterfaceException e) {
            e.printStackTrace();
            return  null;
        }
         finally {
            if (response != null) {
                response.close();
            }
            if (client!= null) {
                client.destroy();
            }

        }
        return collectionID;
    }

    public DSpaceCommunity createSubCommunity(String parentId, String title,String abstrct){
        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
        client.addFilter(new HTTPBasicAuthFilter(userName,passWord));

        WebResource webResource = client.resource(parentId);//+"/communities");
        DSpaceCommunity community = null;
        ClientResponse response = null;
        try {
            Abdera abdera = Abdera.getInstance();

            Entry entry = abdera.newEntry();

            entry.addExtension(DC_TYPE).setText("Community");
            entry.addExtension(DC_TITLE).setText(title);
            entry.addExtension(DC_RIGHTS).setText("");
            entry.addExtension(DCTERMS_ALTERNATIVE).setText("Part of SEAD Project");
            entry.addExtension(DCTERMS_ABSTRACT).setText(abstrct);

             //entry.addExtension(DCTERMS_CONFORMSTO).setText("GPU");
            //entry.addExtension(DCTERMS_PROVENANCE).setText("Provenance information");

            entry.setUpdated(new Date());

            response = webResource.type("application/xml").post(ClientResponse.class,entry);
            client.destroy();
            int status = response.getStatus();
            String textEntity = response.getEntity(String.class);
            System.out.println("Response Status"+status);
            System.out.println("Community ID" + response.getHeaders().get("Location"));
            System.out.println("Community Handle" + response.getHeaders().get("Handle"));
            System.out.println("Response text"+textEntity);

//            if(response.getHeaders().get("Location")==null)
//                throw new RuntimeException("Error creating DSpace Community");

            if(response.getHeaders().get("Location")==null)
                return null;
            String communityID = response.getHeaders().get("Location").get(0);
            community = new DSpaceCommunity();
            community.setId(communityID);
            System.out.println("communityID" +communityID+ response.toString());
            if(response.getHeaders().get("Handle")!=null)
                community.setHandle(response.getHeaders().get("Handle").get(0));

        } catch (UniformInterfaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        finally {
            if (response != null) {
                response.close();
            }
            if (client!= null) {
                client.destroy();
            }

        }
        return community;

    }

    private String getItemHandle(String response) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput( new StringReader( response ) );
        processDocument(xpp);
        return null;
    }

    public void processDocument(XmlPullParser xpp)
            throws XmlPullParserException, IOException, IOException {
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                System.out.println("Start document");
            } else if(eventType == xpp.END_DOCUMENT) {
                System.out.println("End document");
            } else if(eventType == xpp.START_TAG) {
                processStartElement(xpp);
            } else if(eventType == xpp.END_TAG) {
                processEndElement(xpp);
            } else if(eventType == xpp.TEXT) {
                processText(xpp);
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT);
    }


    public void processStartElement (XmlPullParser xpp)
    {
        String name = xpp.getName();
        String uri = xpp.getNamespace();
        if ("".equals (uri)) {
            System.out.println("Start element: " + name);
        } else {
            System.out.println("Start element: {" + uri + "}" + name);
        }
    }


    public void processEndElement (XmlPullParser xpp)
    {
        String name = xpp.getName();
        String uri = xpp.getNamespace();
        if ("".equals (uri))
            System.out.println("End element: " + name);
        else
            System.out.println("End element:   {" + uri + "}" + name);
    }

    int holderForStartAndLength[] = new int[2];

    public void processText (XmlPullParser xpp) throws XmlPullParserException
    {
        char ch[] = xpp.getTextCharacters(holderForStartAndLength);
        int start = holderForStartAndLength[0];
        int length = holderForStartAndLength[1];
        System.out.print("Characters:    \"");
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
                case '\\':
                    System.out.print("\\\\");
                    break;
                case '"':
                    System.out.print("\\\"");
                    break;
                case '\n':
                    System.out.print("\\n");
                    break;
                case '\r':
                    System.out.print("\\r");
                    break;
                case '\t':
                    System.out.print("\\t");
                    break;
                default:
                    System.out.print(ch[i]);
                    break;
            }
        }
        System.out.print("\"\n");
    }


}
