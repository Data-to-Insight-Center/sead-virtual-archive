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
package org.dataconservancy.model.dcs;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * See DC-334
 */
public class DC_334 {

    @Test
    public void testXML() throws IOException, InvalidXmlException {
        final DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

        // Create a DU with some metadata
        // Note the metadata xml uses single quotes around the attribute
        final DcsDeliverableUnit du = new DcsDeliverableUnit();
        final DcsMetadata md = new DcsMetadata();
        // This works
        // md.setMetadata("<blah abc=\"def\"></blah>");
        md.setMetadata("<blah abc='def'></blah>");
        du.addMetadata(md);

        // Serialize the DU
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        mb.buildDeliverableUnit(du, os);
        os.close();

        // Deserialize the DU
        final DcsDeliverableUnit du2 =
                mb.buildDeliverableUnit(new ByteArrayInputStream(os
                        .toByteArray()));

        // The DcsMetadata objects equal each other...
        assertEquals(du.getMetadata().iterator().next(), du2.getMetadata().iterator().next());

        // But their hashCodes do not!
        assertEquals(du.getMetadata().hashCode(), du2.getMetadata().hashCode());

        // This leads to the condition where the Sets of DcsMetadata are not equal...
        assertEquals(du.getMetadata(), du2.getMetadata());
        assertEquals(du, du2);

        // the problem can be generalized; deserializing equivalent but non-identical XML fragments
        // will result in DcsMetadata objects that have different hashCodes but are equal.                
    }

}
