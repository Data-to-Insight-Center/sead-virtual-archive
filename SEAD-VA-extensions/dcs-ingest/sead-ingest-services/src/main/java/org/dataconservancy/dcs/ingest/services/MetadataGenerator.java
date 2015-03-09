package org.dataconservancy.dcs.ingest.services;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.om.Validation;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.ROBatchIndexer;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.seadva.bagit.util.FgdcUtil;
import org.seadva.metadata.service.api.MetadataService;
import org.seadva.metadata.service.api.MetadataType;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.SeadPerson;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataGenerator extends IngestServiceBase
        implements IngestService
{
    private DcsModelBuilder builder;
    private MetadataService metadataService;
    private String baseURL;
    private SolrService solrService;
    private HashMap<String, String> d12seadFormat = new HashMap<String, String>();

    public MetadataGenerator() {

        d12seadFormat.put(MetadataType.FGDC.getText(), "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");

    }

    @Required
    public void setModelBuilder(DcsModelBuilder mb)
    {
        this.builder = mb;
    }

    @Required
    public void setMetadataService(MetadataService service) {
        this.metadataService = service;
    }

    @Required
    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    @Required
    public void setSolrService(SolrService solrService) {
        this.solrService = solrService;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;

        Dcp dcp = this.ingest.getSipStager().getSIP(sipRef);

        this.generateMetadata(dcp, MetadataType.FGDC);

    }

    private void generateMetadata(Dcp sip, MetadataType outputType) {

        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();

        DcsDeliverableUnit rootDu = null;
        for (DcsDeliverableUnit du:sip.getDeliverableUnits()){
            if(du.getParents()==null||du.getParents().size()==0){
                rootDu = du;
                break;
            }
        }
        String guid = null;
        if(rootDu.getId().contains("/")) {
            guid = rootDu.getId().split("/")[rootDu.getId().split("/").length - 1];
        } else {
            guid = rootDu.getId().split(":")[rootDu.getId().split(":").length - 1];
        }

        String filePath = System.getProperty("java.io.tmpdir")+"/" + guid + "_" + outputType.getText() + ".xml";
        File metadataFilePath = new File(filePath);
        if(!metadataFilePath.exists())
            try {
                metadataFilePath.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating FGDC metadata file");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return;
            }

        String onlink = null;
        Pattern pattern = Pattern.compile("http://([^/]*)/.*");
        Matcher matcher = pattern.matcher(this.baseURL);
        if(matcher.matches()) {
            String host = matcher.group(1);
            onlink = "http://" + host + "/sead-access/#entity;" + this.baseURL + "/entity/" + guid;
        }

        String metadata = null;
        if(outputType.equals(MetadataType.FGDC)) {
            metadata = toFGDC(sip, onlink);
        }

        if(metadata != null) {
            try {
                FileUtils.writeStringToFile(new File(filePath), metadata);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return;
        }

        ResearchObject researchObject = new ResearchObject();

        SeadFile metadataFile = new SeadFile();
        metadataFile.setId(guid + "_" + outputType.getText());
        metadataFile.setName(guid + "_" + outputType.getText());
        metadataFile.setSource("file://" + filePath);

        try {
            DigestInputStream digestStream =
                    new DigestInputStream(new FileInputStream(filePath), MessageDigest.getInstance("SHA-1"));
            if (digestStream.read() != -1) {
                byte[] buf = new byte[1024];
                while (digestStream.read(buf) != -1);
            }
            byte[] digest = digestStream.getMessageDigest().digest();
            DcsFixity fixity = new DcsFixity();
            fixity.setAlgorithm("SHA-1");
            fixity.setValue(new String(Hex.encodeHex(digest)));
            metadataFile.addFixity(fixity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        DcsFormat metadataFormat = new DcsFormat();
        metadataFormat.setFormat(d12seadFormat.get(outputType.getText()));
        metadataFile.addFormat(metadataFormat);

        String creator = "none";
        for(DcsDeliverableUnit du: dus){
            SeadDeliverableUnit sdu = (SeadDeliverableUnit)du;
            if(sdu.getParents()==null||sdu.getParents().size()==0){
                Set<SeadPerson> creators = sdu.getDataContributors();
                if(creators!=null&&creators.size()>0)
                    creator = creators.iterator().next().getName();
            }

        }
        DcsResourceIdentifier identifier = new DcsResourceIdentifier();
        identifier.setIdValue("seadva-"+creator.replace(" ","").replace(",","")+UUID.randomUUID().toString());
        identifier.setTypeId("dataone");
        metadataFile.addAlternateId(identifier);
        metadataFile.setSizeBytes(metadataFilePath.length());

        researchObject.addFile(metadataFile);

        BatchIndexer<ResearchObject> indexer = new ROBatchIndexer(this.solrService, null);
        try {
            indexer.add(researchObject);
            indexer.close();
        } catch (IndexServiceException e) {
            e.printStackTrace();
        }
    }

    public String toFGDC(Dcp sip, String onlink) {
        Iterator i = sip.getDeliverableUnits().iterator();

        SeadDeliverableUnit du;
        do {
            if(!i.hasNext()) {
                return null;
            }

            du = (SeadDeliverableUnit) i.next();
        } while(du.getParents() != null && du.getParents().size() != 0);

        HashSet<String> creators = new HashSet<String>();
        Iterator i1 = du.getDataContributors().iterator();

        while(i1.hasNext()) {
            SeadPerson creator = (SeadPerson)i1.next();
            creators.add(creator.getName());
        }

        HashSet<String> contacts = new HashSet<String>();
        contacts.add(du.getContact());

        String pubDate;
        /*if(du.getPubdate() != null) {
            pubDate = du.getPubdate();
        } else {*/
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        pubDate = sdfDate.format(now);
        //}

        String fgdcXML = FgdcUtil.makeDefaultDoc(du.getTitle(), creators, contacts, du.getAbstrct(), pubDate, onlink);
        fgdcXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + fgdcXML;

        fgdcXML = fgdcXML.replace("<metadata>","<metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:noNamespaceSchemaLocation=\"http://www.fgdc.gov/metadata/fgdc-std-001-1998.xsd\">");

        if(validateXML(fgdcXML)){
            return fgdcXML;
        } else {
            return null;
        }
    }

    private boolean validateXML(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SimpleErrorHandler());
            Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            System.out.println("Error creating FGDC metadata:");
            e.printStackTrace();
            return false;
        }
        return true;

    }

    private class SimpleErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            //System.out.println("Warning when generating FGDC metadata :"+e.getMessage());
        }

        public void error(SAXParseException e) throws SAXException {
            //System.out.println("Error when generating FGDC metadata :"+e.getMessage());
        }

        public void fatalError(SAXParseException e) throws SAXException {
            System.out.println("Fatal error when generating FGDC metadata :"+e.getMessage());
        }
    }

}