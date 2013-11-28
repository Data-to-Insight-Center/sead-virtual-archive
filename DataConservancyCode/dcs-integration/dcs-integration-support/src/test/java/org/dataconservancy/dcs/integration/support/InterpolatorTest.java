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
package org.dataconservancy.dcs.integration.support;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class InterpolatorTest {

    private final static Properties props = new Properties();

    @BeforeClass
    public static void loadProperties() throws IOException {
        final URL defaultProps = InterpolatorTest.class.getResource("/default.properties");
        assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
    }

    @Test
    public void testInterpolate() {
        StringBuilder sb = new StringBuilder("${dcs.baseurl}");
        Interpolator.interpolate(sb, 0, props);
        assertEquals("http://localhost:8080/dcs-integration-main", sb.toString());

        sb = new StringBuilder("${dcs.baseurl}/entity/");
        Interpolator.interpolate(sb, 0, props);
        assertEquals("http://localhost:8080/dcs-integration-main/entity/", sb.toString());

        sb = new StringBuilder("jdbc:h2:file:${dcs.home}/identifiers/id");
        Interpolator.interpolate(sb, 0, props);
        assertEquals("jdbc:h2:file:/tmp/dcs/identifiers/id", sb.toString());

        sb = new StringBuilder("${dcs.home}${dcs.baseurl}");
        Interpolator.interpolate(sb, 0, props);
        assertEquals("/tmp/dcshttp://localhost:8080/dcs-integration-main", sb.toString());

        sb = new StringBuilder("bar${dcs.home}asdf${dcs.baseurl}foo");
        Interpolator.interpolate(sb, 0, props);
        assertEquals("bar/tmp/dcsasdfhttp://localhost:8080/dcs-integration-mainfoo", sb.toString());
    }
}
