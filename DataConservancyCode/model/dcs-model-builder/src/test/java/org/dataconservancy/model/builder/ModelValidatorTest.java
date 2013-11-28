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
package org.dataconservancy.model.builder;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ModelValidatorTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private ModelValidator underTest;

    private final String DU_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!--\n" +
            "  Represents a Deliverable Unit with optional attributes and elements present.\n" +
            "  \n" +
            "  Uses <![CDATA[..]> to handle reserved XML characters.\n" +
            "-->\n" +
            "<DeliverableUnit xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\" id=\"urn:DeliverableUnit:2\">\n" +
            "  \n" +
            "  <collection ref=\"urn:Collection:4\"/>\n" +
            "  <collection ref=\"urn:Collection:5\"/>\n" +
            "  \n" +
            "  <parent ref=\"urn:DeliverableUnit:54\"/>\n" +
            "  <parent ref=\"urn:DeliverableUnit:100\"/>\n" +
            "  \n" +
            "  <type>music</type>  \n" +
            "  <title>The Twilight Saga: Eclipse (Original Motion Picture Soundtrack) [Deluxe] [+digital booklet]</title>\n" +
            "  <creator>Amazon.com</creator>\n" +
            "  <subject>music</subject>\n" +
            "  <subject>soundtracks</subject>\n" +
            "  <subject>twilight</subject>\n" +
            "  <formerExternalRef><![CDATA[http://www.amazon.com/Twilight-Saga-Eclipse-Original-Soundtrack/dp/B003P8BB5W/ref=pd_ts_zgc_dmusic_digital_music_album_display_on_website_4?ie=UTF8&s=dmusic&pf_rd_p=1264325582&pf_rd_s=right-3&pf_rd_t=101&pf_rd_i=163856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=0Z1QZ9YA0GP2ZE7VRHEW]]></formerExternalRef>  \n" +
            "  <digitalSurrogate>false</digitalSurrogate>\n" +
            "  \n" +
            "  <metadata schemaURI=\"http://amazon.com/schema.xsd\">\n" +
            "    <amzn:md xmlns:amzn=\"http://www.amazon.com\">\n" +
            "      <amzn:origReleaseDate>Original Release Date: May 25, 2010</amzn:origReleaseDate>\n" +
            "      <amzn:releaseDate>Release Date: May 25, 2010</amzn:releaseDate>\n" +
            "      <amzn:releaseLabel>Label: Chop Shop/Atlantic</amzn:releaseLabel>\n" +
            "      <amzn:copyright><![CDATA[TM & 2010 Summit Entertainment, LLC. All rights reserved]]></amzn:copyright>\n" +
            "      <amzn:totalLength>1:18:29</amzn:totalLength>\n" +
            "      <amzn:genre>Soundtracks</amzn:genre>\n" +
            "      <amzn:genre>General</amzn:genre>\n" +
            "      <amzn:asin>B003P8BB5W</amzn:asin>\n" +
            "    </amzn:md>\n" +
            "  </metadata>\n" +
            "  \n" +
            "  <relationship ref=\"urn:DeliverableUnit:221\" rel=\"amzn:book\"/>\n" +
            "  \n" +
            "</DeliverableUnit>";

    private final String WRAPPED_DU_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!--\n" +
            "  Represents a Deliverable Unit with optional attributes and elements present.\n" +
            "  \n" +
            "  Uses <![CDATA[..]> to handle reserved XML characters.\n" +
            "-->\n" +
            "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"><DeliverableUnits><DeliverableUnit id=\"urn:DeliverableUnit:2\">\n" +
            "  \n" +
            "  <collection ref=\"urn:Collection:4\"/>\n" +
            "  <collection ref=\"urn:Collection:5\"/>\n" +
            "  \n" +
            "  <parent ref=\"urn:DeliverableUnit:54\"/>\n" +
            "  <parent ref=\"urn:DeliverableUnit:100\"/>\n" +
            "  \n" +
            "  <type>music</type>  \n" +
            "  <title>The Twilight Saga: Eclipse (Original Motion Picture Soundtrack) [Deluxe] [+digital booklet]</title>\n" +
            "  <creator>Amazon.com</creator>\n" +
            "  <subject>music</subject>\n" +
            "  <subject>soundtracks</subject>\n" +
            "  <subject>twilight</subject>\n" +
            "  <formerExternalRef><![CDATA[http://www.amazon.com/Twilight-Saga-Eclipse-Original-Soundtrack/dp/B003P8BB5W/ref=pd_ts_zgc_dmusic_digital_music_album_display_on_website_4?ie=UTF8&s=dmusic&pf_rd_p=1264325582&pf_rd_s=right-3&pf_rd_t=101&pf_rd_i=163856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=0Z1QZ9YA0GP2ZE7VRHEW]]></formerExternalRef>  \n" +
            "  <digitalSurrogate>false</digitalSurrogate>\n" +
            "  \n" +
            "  <metadata schemaURI=\"http://amazon.com/schema.xsd\">\n" +
            "    <amzn:md xmlns:amzn=\"http://www.amazon.com\">\n" +
            "      <amzn:origReleaseDate>Original Release Date: May 25, 2010</amzn:origReleaseDate>\n" +
            "      <amzn:releaseDate>Release Date: May 25, 2010</amzn:releaseDate>\n" +
            "      <amzn:releaseLabel>Label: Chop Shop/Atlantic</amzn:releaseLabel>\n" +
            "      <amzn:copyright><![CDATA[TM & 2010 Summit Entertainment, LLC. All rights reserved]]></amzn:copyright>\n" +
            "      <amzn:totalLength>1:18:29</amzn:totalLength>\n" +
            "      <amzn:genre>Soundtracks</amzn:genre>\n" +
            "      <amzn:genre>General</amzn:genre>\n" +
            "      <amzn:asin>B003P8BB5W</amzn:asin>\n" +
            "    </amzn:md>\n" +
            "  </metadata>\n" +
            "  \n" +
            "  <relationship ref=\"urn:DeliverableUnit:221\" rel=\"" + DcsRelationship.IS_METADATA_FOR.asString() + "\"/>\n" +
            "  \n" +
            "</DeliverableUnit></DeliverableUnits></dcp>";

    private final String MINIMAL_DU_XML = "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"><DeliverableUnits><DeliverableUnit id=\"urn:DeliverableUnit:2\">\n" +
            "  <type>music</type>  \n" +
            "  <title>The Twilight Saga: Eclipse (Original Motion Picture Soundtrack) [Deluxe] [+digital booklet]</title>\n" +
            "  <creator>Amazon.com</creator>\n" +
            "  <subject>twilight</subject>\n" +
            "  \n" +
            "  <metadata schemaURI=\"http://amazon.com/schema.xsd\">\n" +
            "    <amzn:md xmlns:amzn=\"http://www.amazon.com\">\n" +
            "      <amzn:origReleaseDate>Original Release Date: May 25, 2010</amzn:origReleaseDate>\n" +
            "    </amzn:md>\n" +
            "  </metadata>\n" +
            "</DeliverableUnit></DeliverableUnits></dcp>";

    private final String WRAPPED_MINIMAL_DU_XML = "<DeliverableUnit xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\" id=\"urn:DeliverableUnit:2\">\n" +
            "  <type>music</type>  \n" +
            "  <title>The Twilight Saga: Eclipse (Original Motion Picture Soundtrack) [Deluxe] [+digital booklet]</title>\n" +
            "  <creator>Amazon.com</creator>\n" +
            "  <subject>twilight</subject>\n" +
            "  \n" +
            "  <metadata schemaURI=\"http://amazon.com/schema.xsd\">\n" +
            "    <amzn:md xmlns:amzn=\"http://www.amazon.com\"></amzn:md>\n" +
//            "      <amzn:origReleaseDate>Original Release Date: May 25, 2010</amzn:origReleaseDate>\n" +
//            "    </amzn:md>\n" +
            "  </metadata>\n" +
            "</DeliverableUnit>";

    @Before
    public void setUp() {
        this.underTest = new ModelValidator();
    }

    @Test
    @Ignore("FIXME: currently failing (NPE)")
    public void testValidateDuXml() throws InvalidXmlException {
        underTest.validate(IOUtils.toInputStream(DU_XML));
    }

    @Test
    public void testValidateMinimalDuXml() throws InvalidXmlException {
        underTest.validate(IOUtils.toInputStream(MINIMAL_DU_XML));
    }

    @Test
    public void testValidateWrappedDuXml() throws InvalidXmlException {
        underTest.validate(IOUtils.toInputStream(WRAPPED_DU_XML));
    }

    @Test
    @Ignore("FIXME: currently failing (NPE)")
    public void testValidateWrappedMinimalDuXml() throws InvalidXmlException {
        underTest.validate(IOUtils.toInputStream(WRAPPED_MINIMAL_DU_XML));
    }


}
