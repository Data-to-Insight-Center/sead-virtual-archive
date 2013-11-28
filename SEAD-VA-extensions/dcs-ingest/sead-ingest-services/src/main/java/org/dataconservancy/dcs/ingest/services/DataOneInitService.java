package org.dataconservancy.dcs.ingest.services;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.seadva.metadata.service.api.MetadataType;
import org.seadva.model.pack.ResearchObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * DataONE Id and metadata creation service
 */
public class DataOneInitService extends IngestServiceBase
        implements IngestService {

    public static final String MIME_SCHEME =
            "http://www.iana.org/assignments/media-types/";

    public DataOneInitService(){
        //idService = new DataCite
    }





    public void execute(String sipRef) {
        if (isDisabled()) return;


        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

        //create DOI only for parent Deliverable Unit
        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        for (DcsDeliverableUnit d : dus) {
            Set<DcsResourceIdentifier> alternateIds = null;
            if(d.getParents().isEmpty()) {
                DcsFile oaiFile = new DcsFile();
                oaiFile.setId(MetadataType.OAIORE.getText());
                oaiFile.setName(MetadataType.OAIORE.getText());
                String oaiFilePathStr = System.getProperty("java.io.tmpdir")+"/"+MetadataType.OAIORE.getText();
                File oaiFilePath = new File(oaiFilePathStr);
                if(!oaiFilePath.exists())
                    try {
                        oaiFilePath.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                oaiFile.setSource("file://"+oaiFilePathStr);
                DcsFormat oreFormat = new DcsFormat();
                oreFormat.setFormat("http://www.openarchives.org/ore/terms");
                oreFormat.setSchemeUri(MIME_SCHEME);
                oaiFile.addFormat(oreFormat);

                DcsFormat atomFormat = new DcsFormat();
                atomFormat.setFormat("http://www.w3.org/2005/Atom");
                atomFormat.setSchemeUri(MIME_SCHEME);
                oaiFile.addFormat(atomFormat);

                sip.addFile(oaiFile);

                DcsMetadataRef oaiRef = new DcsMetadataRef();
                oaiRef.setRef(oaiFile.getId());

                DcsFile fgdcFile = new DcsFile();
                fgdcFile.setId(MetadataType.FGDC.getText());
                fgdcFile.setName(MetadataType.FGDC.getText());
                String fgdcFilePathStr = System.getProperty("java.io.tmpdir")+"/"+MetadataType.FGDC.getText();
                File fgdcFilePath = new File(fgdcFilePathStr);
                if(!fgdcFilePath.exists())
                    try {
                        fgdcFilePath.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                fgdcFile.setSource("file://"+fgdcFilePathStr);
                DcsFormat emlFormat = new DcsFormat();
                emlFormat.setFormat("-//ecoinformatics.org//eml-access-2.0.0beta4//EN");
                emlFormat.setSchemeUri(MIME_SCHEME);
                oaiFile.addFormat(emlFormat);

                sip.addFile(fgdcFile);

                DcsMetadataRef fgdcRef = new DcsMetadataRef();
                fgdcRef.setRef(fgdcFile.getId());


                d.addMetadataRef(oaiRef);
                d.addMetadataRef(fgdcRef);

                Collection<DcsManifestation> manifestations = sip.getManifestations();
                for(DcsManifestation manifestation:manifestations){
                    if(manifestation.getDeliverableUnit().equalsIgnoreCase(d.getId())){
                        DcsManifestationFile oaiMf = new DcsManifestationFile();
                        DcsFileRef oaiFr = new DcsFileRef();
                        oaiFr.setRef(oaiFile.getId());
                        oaiMf.setRef(oaiFr);

                        DcsManifestationFile fgdcMf = new DcsManifestationFile();
                        DcsFileRef fgdcFr = new DcsFileRef();
                        fgdcFr.setRef(fgdcFile.getId());
                        fgdcMf.setRef(fgdcFr);

                        manifestation.addManifestationFile(oaiMf);
                        manifestation.addManifestationFile(fgdcMf);


                        break;
                    }
                }
                sip.setManifestations(manifestations);
                break;
            }
        }

        sip.setDeliverableUnits(dus);

        addMetadataInitEvent(sipRef);


        /* save the SIP containing updated entities */
        ingest.getSipStager().updateSIP(sip, sipRef);

    }


    private void addMetadataInitEvent(String sipRef) {
        DcsEvent archiveEvent =
                ingest.getEventManager().newEvent(Events.METADATA_GENERATION);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        archiveEvent.setOutcome(Integer.toString(dcp.getFiles().size()+1));
        archiveEvent.setDetail("Metadata Initialization Successful");
      //  archiveEvent.setTargets(entities);

        ingest.getEventManager().addEvent(sipRef, archiveEvent);
    }

    public HashMap<String,ArrayList<DcsFile>> getShapeFiles(Set<DcsFile> filesList){

        HashMap<String,ArrayList<DcsFile>> shapeFiles =  new HashMap<String, ArrayList<DcsFile>>();
        for(DcsFile file:filesList){
            String fileName = file.getName();
            String[] name = fileName.split("\\.");
            ArrayList<DcsFile> files =null;
            if(shapeFiles.containsKey(name[0]))
                files = shapeFiles.get(name[0]);
            else
                files = new ArrayList<DcsFile>();

            files.add(file);
            shapeFiles.put(name[0],files);
            //angelo_meadows, list of files start with an_me
            //angelo_boundary, list if file with angelo boundary
        }
        return  shapeFiles;
    }



}
