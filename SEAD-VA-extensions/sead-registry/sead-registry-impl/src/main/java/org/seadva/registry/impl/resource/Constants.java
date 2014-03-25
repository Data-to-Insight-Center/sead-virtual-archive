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

package org.seadva.registry.impl.resource;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants
 * */
public class Constants {

    public static String hasFormat = "http://purl.org/dc/terms/hasFormat";

    static{
        try {
            metadataPredicateMap = new Constants().loadMetadataMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Map<String, Boolean> metadataPredicateMap;

    private  Map<String, Boolean> loadMetadataMapping() throws IOException{
        Map<String, Boolean> metadataPredicateMap = new HashMap<String, Boolean>();

        InputStream inputStream =
                getClass().getResourceAsStream(
                        "./multi-valued-field.properties"
                );
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        String result = writer.toString();
        String[] pairs = result.trim().split(
                "\n|\\=");


        for (int i = 0; i + 1 < pairs.length;) {
            String name = pairs[i++].trim();
            String value = pairs[i++].trim();
            if(value.equalsIgnoreCase("true"))
                metadataPredicateMap.put(name,true);
            else
                metadataPredicateMap.put(name,false);
        }
        return metadataPredicateMap;
    }

}