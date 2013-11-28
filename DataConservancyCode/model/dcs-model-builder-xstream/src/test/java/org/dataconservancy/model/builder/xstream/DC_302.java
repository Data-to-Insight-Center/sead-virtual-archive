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
package org.dataconservancy.model.builder.xstream;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayOutputStream;

/**
 *
 */
public class DC_302 {
    
    @Test
    public void roundTripTest() throws Exception {

        final String GOOD_SIP = "/DC_302-validSip.xml";
        Schema sch =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new StreamSource(this.getClass()
                                .getResourceAsStream("/schema/dcp.xsd")));

        /*
        * This works. The sip contains inline DU metadata, with namespace
        * xmlns:astro="http://sdss.org/astro"
        */
        sch.newValidator().validate(new StreamSource(this.getClass()
                .getResourceAsStream(GOOD_SIP)));

        DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

        /* deserializing works fine, or at least is doesn't throw an exception... */
        Dcp sip =
                builder.buildSip(this.getClass().getResourceAsStream(GOOD_SIP));

        for (DcsDeliverableUnit du : sip.getDeliverableUnits()) {
            for (DcsMetadata md : du.getMetadata()) {

                /*
                * ...you'll see that there is no xmlns:astro namespace declaration
                * here any more
                */
                // Just to keep the test outputs quiet.
                // System.out.println(md.getMetadata());
            }
        }

        /* ... which causes failure in serializing the sip! */
        builder.buildSip(sip, new ByteArrayOutputStream());

    }
}
