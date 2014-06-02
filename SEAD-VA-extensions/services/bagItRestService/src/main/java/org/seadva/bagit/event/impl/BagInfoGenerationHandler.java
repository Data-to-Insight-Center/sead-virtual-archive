package org.seadva.bagit.event.impl;

import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handler to generate bag-info.txt file
 */

public class BagInfoGenerationHandler implements Handler{
    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;
    Writer baginfo;

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        if(packageDescriptor.getPackageId()==null)
            return packageDescriptor;
        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        FileWriter baginfoStream = null;
        try {
            String bagInfoTxtFilePath = packageDescriptor.getUntarredBagPath() + "/bag-info.txt";
            baginfoStream = new FileWriter(bagInfoTxtFilePath);
            baginfo = new BufferedWriter(baginfoStream);
            //generateBagittxtFile(packageDescriptor.getPackageId(), bagitTxtFilePath);
            generateBagInfoTxtFile();
            baginfo.close();
            packageDescriptor.setBagInfoTxtFilePath(bagInfoTxtFilePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return packageDescriptor;
    }
    void generateBagInfoTxtFile() throws IOException {
        baginfo.write("Source-Organization: IU MDPI\n");
        baginfo.write("Organization Address: Indiana University, Bloomington, Indiana.\n");
        baginfo.write("Contact Name: Beth Plale/Robert McDonald\n");
        baginfo.write("Contact Phone: 123-456-7890\n");
        baginfo.write("Contact Email: abcdefg@indiana.edu\n");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dateObj = new Date();
        baginfo.write("Bagging Date: "+dateFormat.format(dateObj)+"\n");
    }
}
