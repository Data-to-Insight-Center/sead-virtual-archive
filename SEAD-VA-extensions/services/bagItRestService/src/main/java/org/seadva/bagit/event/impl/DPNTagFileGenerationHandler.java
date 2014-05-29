package org.seadva.bagit.event.impl;

import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
//import org.seadva.bagit.util.Constants;

import java.io.*;
import java.util.List;
import java.util.Map;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;

public class DPNTagFileGenerationHandler implements Handler{
    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;
    Writer dpntag;

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        if(packageDescriptor.getPackageId()==null)
            return packageDescriptor;
        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

//        Constants.untarDir = Constants.bagDir + "dpn-tags/";
//
//        if(!new File(Constants.bagDir).exists()) {
//            new File(Constants.bagDir).mkdirs();
//        }
//        if(!new File(Constants.untarDir).exists()) {
//            new File(Constants.untarDir).mkdirs();
//        }

        FileWriter dpntagStream = null;
        try {
            String dpntagTxtFileDir = packageDescriptor.getUntarredBagPath() + "/dpn-tags";
            if(!new File(dpntagTxtFileDir).exists()) {
                new File(dpntagTxtFileDir).mkdirs();
            }
            String dpntagTxtFilePath = dpntagTxtFileDir + "/dpn-info.txt";
            dpntagStream = new FileWriter(dpntagTxtFilePath);
            dpntag = new BufferedWriter(dpntagStream);
            generateDPNTagTxtFile(packageDescriptor);
            dpntag.close();
            packageDescriptor.setDpntagTxtFilePath(dpntagTxtFilePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return packageDescriptor;
    }
    void generateDPNTagTxtFile(PackageDescriptor packageDescriptor) throws IOException {
        dpntag.write("DPN-Object-ID: "+packageDescriptor.getPackageName()+"\n");
        dpntag.write("Local-ID: POD-ID\n");
        dpntag.write("First-Node-Name: Indiana University\n");
        dpntag.write("First-Node-Address: Indiana University, Bloomington\n");
        dpntag.write("First-Node-Contact-Name: Beth Plale/Robert McDonald\n");
        dpntag.write("First-Node-Contact-Email: abcdefg@indiana.edu\n");
        dpntag.write("Version-Number: SomeNumber\n");
        dpntag.write("Previous-Version-Object-ID: Object-ID\n");
        dpntag.write("First-Version-Object-ID: Object-ID\n");
        dpntag.write("Brightening-Object-ID: Object-ID\n");
        dpntag.write("Rights-Object-ID: Object-ID\n");
        dpntag.write("Object-Type: data");
    }
}
