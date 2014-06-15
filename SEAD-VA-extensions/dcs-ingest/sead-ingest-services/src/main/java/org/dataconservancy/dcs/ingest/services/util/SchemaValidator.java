package org.dataconservancy.dcs.ingest.services.util;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.ddialliance.ddiftp.validator.ddi_3_0.ValidatorDDI3;
import org.seadva.model.pack.ResearchObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema validator for DDI metadata
 */
public final class SchemaValidator {

    public static boolean validateDDI(Collection<DcsMetadataRef> metadataRefs, ResearchObject researchObject){

        /*XmlObject xmlObject, Boolean idValidation,
                Boolean referenceValidation, Boolean schemaValidation,
                Boolean groupedContent*/

        Map<String, DcsFile> fileMap = new HashMap<String, DcsFile>();
        for(DcsFile file: researchObject.getFiles())
            fileMap.put(file.getId(), file);

        for(DcsMetadataRef metadataRef:metadataRefs){
            String filePath;
            if(fileMap.containsKey(metadataRef.getRef()))
                filePath = fileMap.get(metadataRef.getRef()).getSource();
            else
                continue;
            XmlObject ddiObject = null;
            try {
                ddiObject = XmlObject.Factory.parse(new File(
                       filePath.replace("file://","")
                ));
                ValidatorDDI3 validator = new ValidatorDDI3(
                        ddiObject,
                        false,
                        false,
                        true,
                        false
                );

                // check errors
                List schemaErrors = validator.getErrorSchemaValidations();
                List idErrors = validator.getErrorIds();
                List referencesErrors = validator.getErrorReferences();
                if(schemaErrors==null||schemaErrors.size()==0)
                    return true;
                else
                    return false;
            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return false;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return false;
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return false;
            }

        }
        return false;
    }
}
