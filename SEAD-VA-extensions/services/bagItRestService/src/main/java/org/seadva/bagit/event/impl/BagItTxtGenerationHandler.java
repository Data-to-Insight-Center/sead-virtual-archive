package org.seadva.bagit.event.impl;

import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
//import org.seadva.bagit.util.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Handler to generate bagit.txt file
 */

public class BagItTxtGenerationHandler implements Handler {
    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;
    Writer bagit;

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        if(packageDescriptor.getPackageId()==null)
            return packageDescriptor;
        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        FileWriter bagitStream = null;
        try {
            String bagitTxtFilePath = packageDescriptor.getUntarredBagPath() + "/bagit.txt";
            bagitStream = new FileWriter(bagitTxtFilePath);
            bagit = new BufferedWriter(bagitStream);
            //generateBagittxtFile(packageDescriptor.getPackageId(), bagitTxtFilePath);
            generateBagittxtFile();
            bagit.close();
            packageDescriptor.setBagittxtFilePath(bagitTxtFilePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return packageDescriptor;
    }
    void generateBagittxtFile() throws IOException {
        bagit.write("BagIt-Version: 0.97\n");
        bagit.write("Tag-File-Character-Encoding: UTF-8");
    }
}
