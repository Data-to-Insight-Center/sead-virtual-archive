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
import org.dataconservancy.model.builder.DcsModelBuilderTest;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * This is the test class for the {@link DcsModelBuilder} {@link DcsModelBuilderTest test suite}.  It provides a
 * a <code>DcsXstreamStaxModelBuilder</code> (which is configured to validate incoming XML) and the
 * <code>DcsModelBuilderTest</code> does the rest.
 */
public class DcsXstreamStaxModelBuilderTest extends DcsModelBuilderTest {

    /**
     * Provides an instance of the <code>DcsXstreamStaxModelBuilder</code>, configured to validate incoming XML.
     *
     * @return the DcsXstreamStaxModelBuilder instance under test
     */
    @Override
    public DcsModelBuilder getUnderTest() {
        return new DcsXstreamStaxModelBuilder(true);
    }

}
