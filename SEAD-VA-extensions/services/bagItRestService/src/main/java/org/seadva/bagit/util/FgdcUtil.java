/*
#
# Copyright 2012 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: XSDview
# File:  FgdcUtil.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package org.seadva.bagit.util;

import noNamespace.*;
import org.apache.xmlbeans.XmlOptions;
import org.seadva.bagit.util.SeadNCEDConstants;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Yiming Sun
 * Modified by Kavitha Chandrasekar
 */
public class FgdcUtil {
    
    public static String makeDefaultDoc(String title,
                                                  Set<String> creators,
                                                  Set<String> contacts,
                                                  String abstrct,
                                                  String publicationDate) //Arguments added by Kavitha
    {
        MetadataDocument metadataDoc = MetadataDocument.Factory.newInstance();
        MetadataType metadataType = metadataDoc.addNewMetadata();
        
        IdinfoType idinfoType = metadataType.addNewIdinfo();
        CitationType citationType = idinfoType.addNewCitation();
        CiteinfoType citeinfoType = citationType.addNewCiteinfo();



        if(creators!=null){
            Iterator<String> it = creators.iterator();
            while(it.hasNext()) {
                OriginType originType = citeinfoType.addNewOrigin();
                originType.setStringValue(it.next());
            }
        }
        else{
            OriginType originType = citeinfoType.addNewOrigin();
            originType.setStringValue(SeadNCEDConstants.DEFAULT_ORIGINATOR);
        }


        if(publicationDate!=null)
            citeinfoType.setPubdate(publicationDate);
        else
            citeinfoType.setPubdate(SeadNCEDConstants.DEFAULT_PUBDATE);

        if(title!=null)
            citeinfoType.setTitle(title);
        else
            citeinfoType.setTitle(SeadNCEDConstants.DEFAULT_UUID);
        OnlinkType onlinkType = citeinfoType.addNewOnlink();
     //   onlinkType.setStringValue(SeadConstants.DEFAULT_ONLINK);    //commented by Kavitha
        
        DescriptType descriptType = idinfoType.addNewDescript();
        if(abstrct!=null)
            descriptType.setAbstract(abstrct);
        else
            descriptType.setAbstract(SeadNCEDConstants.DEFAULT_ABSTRACT);
        
        descriptType.setPurpose(SeadNCEDConstants.DEFAULT_PURPOSE);
        
        TimeperdType timeperdType = idinfoType.addNewTimeperd();
        
        TimeinfoType timeinfoType = timeperdType.addNewTimeinfo();
        
      //  RngdatesType rngdatesType = timeinfoType.addNewRngdates();
       // rngdatesType.setBegdate(SeadNCEDConstants.DEFAULT_BEGINDATE);
      //  rngdatesType.setEnddate(SeadNCEDConstants.DEFAULT_ENDDATE);
        
        timeperdType.setCurrent(SeadNCEDConstants.DEFAULT_CURRENTREF);
        
        StatusType statusType = idinfoType.addNewStatus();
        
        statusType.setProgress(SeadNCEDConstants.DEFAULT_PROGRESS);
        statusType.setUpdate(SeadNCEDConstants.DEFAULT_MAINTUPDATEFREQ);
        
        SpdomType spdomType = idinfoType.addNewSpdom();
        BoundingType boundingType = spdomType.addNewBounding();
        boundingType.setEastbc(SeadNCEDConstants.DEFAULT_EASTBOUND);
        boundingType.setWestbc(SeadNCEDConstants.DEFAULT_WESTBOUND);
        boundingType.setNorthbc(SeadNCEDConstants.DEFAULT_NORTHBOUND);
        boundingType.setSouthbc(SeadNCEDConstants.DEFAULT_SOUTHBOUND);
        
        KeywordsType keywordsType = idinfoType.addNewKeywords();
        ThemeType themeType = keywordsType.addNewTheme();
        themeType.setThemekt(SeadNCEDConstants.DEFAULT_THEMEKT);
        themeType.setThemekeyArray(SeadNCEDConstants.DEFAULT_THEMEKEYS);
        
        
        
        PlaceType placeType = keywordsType.addNewPlace();
        placeType.setPlacekt(SeadNCEDConstants.DEFAULT_PLACEKT);
        placeType.setPlacekeyArray(SeadNCEDConstants.DEFAULT_PLACEKEYS);
        
        TemporalType temporalType = keywordsType.addNewTemporal();
        temporalType.setTempkt(SeadNCEDConstants.DEFAULT_TEMPORALKT);
        temporalType.setTempkeyArray(SeadNCEDConstants.DEFAULT_TEMPORALKEYS);
        
        idinfoType.setAccconst(SeadNCEDConstants.DEFAULT_ACCESSCONSTRAINT);
        idinfoType.setUseconst(SeadNCEDConstants.DEFAULT_USECONSTRAINT);
        
        MetainfoType metainfoType = metadataType.addNewMetainfo();
        metainfoType.setMetd(SeadNCEDConstants.DEFAULT_METD);

        if(contacts!=null){
            String contactsAppended = "";
            Iterator<String> it = contacts.iterator();
            int i=0;
            while(it.hasNext()) {
                contactsAppended+=it.next();
                if(i!=contacts.size()-1)
                    contactsAppended+=";";
                i++;
            }
            if(contactsAppended.length()>1)
            {
                MetcType metcType = metainfoType.addNewMetc();
                CntinfoType metadataContact = CntinfoType.Factory.newInstance();
                CntperpType cntperpType = metadataContact.addNewCntperp();
                cntperpType.setCntper(contactsAppended);
                CntaddrType cntaddrType = metadataContact.addNewCntaddr();
                cntaddrType.setAddrtype("Mailing");
                cntaddrType.setCity("Unknown");
                cntaddrType.setState("Unknown");
                cntaddrType.setPostal("00000");
                metcType.setCntinfo(metadataContact);
            }
        }
        else{
            MetcType metcType = metainfoType.addNewMetc();
            metcType.setCntinfo(SeadNCEDConstants.DEFAULT_METADATACONTACT);
        }

        
        metainfoType.setMetstdn(SeadNCEDConstants.DEFAULT_METADATANAME);
        metainfoType.setMetstdv(SeadNCEDConstants.DEFAULT_METADATAVERS);

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        return metadataDoc.xmlText(xmlOptions);

    }

}

