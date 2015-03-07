package org.dataconservancy.dcs.ingest.services;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MetadataGenerator extends IngestServiceBase
        implements IngestService
{
    private DcsModelBuilder builder;
    private MetadataService metadataService;
    private SolrService solrService;
    private HashMap<String, String> d12seadFormat = new HashMap<String, String>();

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
    public void setSolrService(SolrService solrService) {
        this.solrService = solrService;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;

        Dcp dcp = this.ingest.getSipStager().getSIP(sipRef);

        d12seadFormat.put(MetadataType.FGDC.getText(), "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");

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
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        String metadata = null;
        if(outputType.equals(MetadataType.FGDC)) {
            metadata = toFGDC(sip);
            metadata = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + metadata;
        }

        if(metadata != null) {
            try {
                FileUtils.writeStringToFile(new File(filePath), metadata);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        researchObject.addFile(metadataFile);

        BatchIndexer<ResearchObject> indexer = new ROBatchIndexer(this.solrService, null);
        try {
            indexer.add(researchObject);
            indexer.close();
        } catch (IndexServiceException e) {
            e.printStackTrace();
        }
    }

    private String toFGDC(Dcp sip) {
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

        return FgdcUtil.makeDefaultDoc(du.getTitle(), creators, contacts, du.getAbstrct(), du.getPubdate());
    }

}