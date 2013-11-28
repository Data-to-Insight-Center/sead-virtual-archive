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

package org.seadva.bagit;

import org.apache.commons.io.FileUtils;
import org.dspace.foresite.OREException;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

/**
 * Converts Bag  to SIP
 */

public class BagItSipConverter {

    //#2 Download zipped bag, unzip and convert from ORE and FGDC to sip. Return SIP file
    public String bagitToSIP(
                                  String collectionId//,
    ) throws IOException, OREException, URISyntaxException {

        String guid = null;
        if(collectionId.contains("/"))
            guid = collectionId.split("/")[collectionId.split("/").length-1];
        else
            guid = collectionId.split(":")[collectionId.split(":").length-1];

        String zippedBag = Constants.bagDir+"/"+guid+".zip";
        String unzipDir = Constants.unzipDir+guid+"/";
        String sipDir = Constants.unzipDir+"sip/";
        if(!(new File(unzipDir).exists()))
            (new File(unzipDir)).mkdirs();
        if(!(new File(sipDir).exists()))
            (new File(sipDir)).mkdirs();
        ZipUtil.unzip(zippedBag, unzipDir);

        OreGenerator oreGenerator = new OreGenerator();
        oreGenerator.fromOAIORE(collectionId, null, unzipDir);

        String sipPath = sipDir+guid + "_sip.xml";
        File sipFile = new File(sipPath);

        OutputStream out = FileUtils.openOutputStream(sipFile);
        new SeadXstreamStaxModelBuilder().buildSip(oreGenerator.sip, out);
        out.close();
        return sipPath;
    }



}
