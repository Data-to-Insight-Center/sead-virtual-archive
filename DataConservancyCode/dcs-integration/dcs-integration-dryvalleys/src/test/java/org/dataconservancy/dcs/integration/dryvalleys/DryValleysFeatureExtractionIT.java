/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.integration.dryvalleys;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.transform.index.IndexOutputFactory;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.index.gqmpsql.GqmIndexService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.query.gqmpsql.GqmQueryService;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;
import org.dataconservancy.transform.dcp.SipDcpReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/org/dataconservancy/config/applicationContext.xml"})
public class DryValleysFeatureExtractionIT {
    
    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();
    static final String COLLECTION_METADATA_RESOURCE = "/dryValleyCollectionMetadata.xml";
    static final String FIELD_PHOTO_METADATA_RESOURCE = "/fieldphoto.xml";
    static final String ROCK_SAMPLE_METADATA_RESOURCE = "/rocksample.xml";
    
    @Autowired
    private IndexOutputFactory<String, GQM> indexOutputFactory;
    
    @Autowired 
    private GqmQueryService queryService;
    
    @Autowired
    private GqmIndexService indexService;
    
    @Autowired
    private Mapping<String, Dcp, String, GQM> mappingChain;
    
    private Dcp sip;
    private Reader<String, Dcp> r;
    private Output<String, GQM> output;
    
    public DryValleysFeatureExtractionIT(){
   //     System.setProperty("dc.gqm.db.uri","jdbc:postgresql://ben-test.dkc.jhu.edu/gqmtest");
    //    System.setProperty("dc.gqm.db.user", "gqmtest");
   //     System.setProperty("dc.gqm.db.pass", "testybanana");
    }
    
    @Before
    public void setup() throws IOException, IndexServiceException{
        indexService.clear();
        buildSip();
        r = new SipDcpReader(sip);
        output = (Output<String, GQM>) indexOutputFactory.newOutput();        
    }
    
    @After
    public void tearDown() throws Exception {
        indexService.clear();
        indexService.shutdown();
        queryService.shutdown();
    }
    
   
    @Test
    public void testBasicPointQuery(){
        while(r.nextKeyValue()){
            r.getCurrentKey();
            r.getCurrentValue();
            mappingChain.map(r.getCurrentKey(), r.getCurrentValue(), output);
        }
        
        QueryResult<GQM> result = null;
        try {
            result = queryService.query("covered-by([polygon 'EPSG:4326' -78 160, -76 160, -76 164, -78 164])", 0, -1);
        } catch (QueryServiceException e) {
            e.printStackTrace();
        }
        
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getTotal());
        output.close();        
    }
    
    @Test
    public void testPolygonQuery(){
        while(r.nextKeyValue()){
            r.getCurrentKey();
            r.getCurrentValue();
            mappingChain.map(r.getCurrentKey(), r.getCurrentValue(), output);
        }
        
        QueryResult<GQM> result = null;
        try {
            result = queryService.query("intersects([polygon 'EPSG:4326' -79 172, -76 172, -76 168, -79 168])", 0, -1);
        } catch (QueryServiceException e) {
            e.printStackTrace();
        }
        
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getTotal());
        output.close();        
    }
    
    public void buildSip() throws IOException{
        final InputStream collectionMDStream = DryValleysFeatureExtractionIT.class.getResourceAsStream(COLLECTION_METADATA_RESOURCE);

        DcsMetadata collectionMD = new DcsMetadata();
        
        StringWriter out = new StringWriter();
        IOUtils.copy(collectionMDStream, out);
        out.close();

        collectionMD.setMetadata(out.toString());
        
        DcsCollection collection = new DcsCollection();
        collection.setTitle("McMurdoDryValleys");
        collection.setId("http://dataconservancy.org/collections/DryValleysCollection");
        collection.addMetadata(collectionMD);
        
        final InputStream fieldPhotoMDStream = DryValleysFeatureExtractionIT.class.getResourceAsStream(FIELD_PHOTO_METADATA_RESOURCE);
        
        DcsMetadata fieldPhotoMD = new DcsMetadata();
        
        StringWriter fieldPhotoMDWriter = new StringWriter();
        IOUtils.copy(fieldPhotoMDStream, fieldPhotoMDWriter);
        fieldPhotoMDWriter.close();

        fieldPhotoMD.setMetadata(fieldPhotoMDWriter.toString());
       
        DcsDeliverableUnit fieldPhotoDU = new DcsDeliverableUnit();
        fieldPhotoDU.addMetadata(fieldPhotoMD);
        fieldPhotoDU.addCollection(new DcsCollectionRef(collection.getId()));
        
        final InputStream rockSampleMDStream = DryValleysFeatureExtractionIT.class.getResourceAsStream(ROCK_SAMPLE_METADATA_RESOURCE);
        DcsMetadata rockSampleMD = new DcsMetadata();
        
        StringWriter rockSampleMDWriter = new StringWriter();
        IOUtils.copy(rockSampleMDStream, rockSampleMDWriter);
        rockSampleMDWriter.close();

        rockSampleMD.setMetadata(rockSampleMDWriter.toString());
       
        DcsDeliverableUnit rockSampleDU = new DcsDeliverableUnit();
        rockSampleDU.addMetadata(rockSampleMD);
        rockSampleDU.addCollection(new DcsCollectionRef(collection.getId()));
        
        sip = new Dcp();
        sip.addCollection(collection);
        sip.addDeliverableUnit(fieldPhotoDU, rockSampleDU);        		
    }
}
