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
package org.dataconservancy.dcs.query.endpoint.utils;


import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;

public class ServletUtil {

    /**
     * Obtain the requested resource from the HttpServletRequest. The resource
     * is encoded in the path of the request URL.
     * 
     * @param req
     *        the HttpServletRequest
     * @return resource specified by request
     */
    public static String getResource(HttpServletRequest req) {
            String path = req.getPathInfo();
            
            if (path == null || path.length() < 2 || path.charAt(0) != '/') {
                return null;
            }

            return path.substring(1);
    }

    /**
     * @param req
     * @return entity identifier specified by request
     * @throws UnsupportedEncodingException
     */
    public static String getEntityId(HttpServletRequest req)
            throws UnsupportedEncodingException {
        return req.getRequestURL().toString();
    }
    
    /**
     * @param path
     * @return path encoded for inclusion in a URL.
     */
    public static String encodeURLPath(String path) {
        try {
            String s = URLEncoder.encode(path, "UTF-8");

            // Have to encode spaces (now plus) using %20

            return s.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static XStream convertFromDcpToJson() {
        XStream jsonbuilder = new XStream(new JsonHierarchicalStreamDriver());

        jsonbuilder.setMode(XStream.NO_REFERENCES);
        jsonbuilder.alias("dcp", Dcp.class);
        jsonbuilder.alias("deliverableUnit", DcsDeliverableUnit.class);
        jsonbuilder.alias("deliverableUnitRef", DcsDeliverableUnitRef.class);
        jsonbuilder.alias("collection", DcsCollection.class);
        jsonbuilder.alias("file", DcsFile.class);
        jsonbuilder.alias("manifestation", DcsManifestation.class);
        jsonbuilder.alias("event", DcsEvent.class);
        jsonbuilder.alias("metadata", DcsMetadata.class);
        jsonbuilder.alias("collectionRef", DcsCollectionRef.class);
        jsonbuilder.alias("relation", DcsRelation.class);
        jsonbuilder.alias("fixity", DcsFixity.class);
        jsonbuilder.alias("fileRef", DcsFileRef.class);
        jsonbuilder.alias("entityRef", DcsEntityReference.class);
        jsonbuilder.alias("metadataRef", DcsMetadataRef.class);
        jsonbuilder.alias("format", DcsFormat.class);

        return jsonbuilder;
    }
    
    public static XStream convertFromGQMToJson() {
        XStream jsonbuilder = new XStream(new JsonHierarchicalStreamDriver());
        
        jsonbuilder.setMode(XStream.NO_REFERENCES);
        jsonbuilder.alias("gqm", GQM.class);
        jsonbuilder.alias("relation", Relation.class);
        jsonbuilder.alias("location", Location.class);
        jsonbuilder.alias("geometry", Geometry.class);
        jsonbuilder.alias("point", Point.class);
        jsonbuilder.alias("dateTimeInterval", DateTimeInterval.class);
        return jsonbuilder;
    }
}
