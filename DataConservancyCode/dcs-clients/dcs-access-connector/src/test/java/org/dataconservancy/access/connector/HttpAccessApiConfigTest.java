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
package org.dataconservancy.access.connector;

import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.dataconservancy.access.connector.HttpAccessApiConfig.BASE_ENDPOINT_PROPERTY;
import static org.dataconservancy.access.connector.HttpAccessApiConfig.FILE_ENTITY_EXTANT_PROPERTY;
import static org.dataconservancy.access.connector.HttpAccessApiConfig.FILE_ENTITY_NON_EXTANT_PROPERTY;
import static org.dataconservancy.access.connector.HttpAccessApiConfig.KNOWN_ENTITIES_PROPERTY;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HttpAccessApiConfigTest {

    @Test
    public void testIsLocalWithLocalhostIp() throws IOException {
        final HttpAccessApiConfig config = new HttpAccessApiConfig();
        config.setHost("127.0.0.1");
        config.setPort(80);
        config.setScheme("http");
        config.setContextPath("/");
        assertTrue(config.isLocal());
    }

    @Test
    public void testIsLocalWithLocalhostName() throws IOException {
        final HttpAccessApiConfig config = new HttpAccessApiConfig();
        config.setHost("localhost");
        config.setPort(80);
        config.setScheme("http");
        config.setContextPath("/");
        assertTrue(config.isLocal());
    }
    
}
