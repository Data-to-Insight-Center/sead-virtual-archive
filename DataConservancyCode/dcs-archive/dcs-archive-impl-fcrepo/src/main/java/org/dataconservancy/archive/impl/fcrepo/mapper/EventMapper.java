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
package org.dataconservancy.archive.impl.fcrepo.mapper;

import java.util.HashMap;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.impl.fcrepo.dto.DCPackage;
import org.dataconservancy.archive.impl.fcrepo.dto.Datastream;
import org.dataconservancy.archive.impl.fcrepo.dto.DatastreamVersion;
import org.dataconservancy.archive.impl.fcrepo.dto.DublinCore;
import org.dataconservancy.archive.impl.fcrepo.dto.DublinCoreElement;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDF;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDFDescription;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDFElement;
import org.dataconservancy.archive.impl.fcrepo.dto.FedoraDigitalObject;
import org.dataconservancy.archive.impl.fcrepo.dto.ObjectProperties;
import org.dataconservancy.archive.impl.fcrepo.dto.XMLContent;
import org.dataconservancy.archive.impl.fcrepo.xstream.DatastreamConverter;
import org.dataconservancy.archive.impl.fcrepo.xstream.DatastreamVersionConverter;
import org.dataconservancy.archive.impl.fcrepo.xstream.DublinCoreConverter;
import org.dataconservancy.archive.impl.fcrepo.xstream.EmbeddedRDFConverter;
import org.dataconservancy.archive.impl.fcrepo.xstream.ObjectPropertiesConverter;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;


public class EventMapper
        extends DCSMapper {

    public EventMapper(Dcp aip) {
        super(aip);
    }

    @Override
    public FedoraDigitalObject map(DcsEntity in) throws AIPFormatException {
        
        DcsEvent entity = null;
        if ((in != null) && (DcsEvent.class == in.getClass())) {
            entity = (DcsEvent) in;
        } else {
            throw new AIPFormatException("Missing or incorrect type found");
        }

        // Map the DCS entity to an FDO.
        FedoraDigitalObject fdo = new FedoraDigitalObject();

        // Set the PID
        String pid = mapper.getPID(entity.getId());
        fdo.setObjectPid(pid);

        // Default Title
        String title = "Event: " + entity.getId();
        //if (entity.getTitle() != null) {
        //    title = entity.getTitle();
        //}

        // Object Properties
        // Note: ownerId is being created on ingest with no value.
        //       createdDate and lastModified data are being created on
        //       ingest with repository default values
        ObjectProperties op = new ObjectProperties();
        HashMap pmap = op.getPropertyMap();
        pmap.put(ObjectPropertiesConverter.STATE,
                 ObjectPropertiesConverter.ACTIVE);
        pmap.put(ObjectPropertiesConverter.OWNERID, "changeMe");
        pmap.put(ObjectPropertiesConverter.LABEL, title);
        fdo.setObjectProperties(op);

        // Add the Datastreams
        // The attributes for each Datastream are the ID,
        // CONTROL_GROUP, STATE, and VERSIONABLE.  Also the will
        // be different choices depending on the specific Datastream
        // for the DCS Entity.

        // Dublin Core
        DublinCore dc = new DublinCore();

        // No title in Event but Fedora needs a DC title.
        DublinCoreElement dceTitle = new DublinCoreElement();
        dceTitle.setName(DublinCoreConverter.TITLE);
        dceTitle.setValue(title);
        dc.getElementList().add(dceTitle);

        // Fedora will make a DC identifier from the hashed PID regardless.
        // But its nice to have the original ID too.
        if (entity.getId() != null) {
            DublinCoreElement dceIdentifier = new DublinCoreElement();
            dceIdentifier.setName(DublinCoreConverter.IDENTIFIER);
            dceIdentifier.setValue(title);
            dc.getElementList().add(dceIdentifier);
        }

        Datastream dcds = new Datastream();
        dcds.setId(DatastreamConverter.DCID);
        dcds.setControlGroup(DatastreamConverter.CONTROL_GROUP_X);
        dcds.setState(DatastreamConverter.STATE_A);
        dcds.setVersionable(DatastreamConverter.VERSIONABLE_T);

        DatastreamVersion dcdsv = new DatastreamVersion();
        dcdsv.setVersionID(dcds.getId()
                + DatastreamVersionConverter.BASE_VERSION);
        dcdsv.setLabel(DublinCoreConverter.VERSION_LABEL);
        dcdsv.setFormatURI(DublinCoreConverter.FORMAT_URI);
        dcdsv.setMimeType(DublinCoreConverter.MIMETYPE);

        XMLContent dcxmlc = new XMLContent();

        dcxmlc.setContent(dc);
        dcdsv.setContent(dcxmlc);
        dcds.getVersionList().add(dcdsv);
        fdo.getDatastreamMap().put(dcds.getId(), dcds);

        // RELS-EXT
        Datastream relds = new Datastream();
        relds.setId(DatastreamConverter.RELSXID);
        relds.setControlGroup(DatastreamConverter.CONTROL_GROUP_X);
        relds.setState(DatastreamConverter.STATE_A);
        relds.setVersionable(DatastreamConverter.VERSIONABLE_T);

        EmbeddedRDF erdf = new EmbeddedRDF();
        EmbeddedRDFDescription erdfd = new EmbeddedRDFDescription();
        erdfd.setAbout("info:fedora/" + fdo.getObjectPid());

        EmbeddedRDFElement erdfeid = new EmbeddedRDFElement();
        erdfeid.setName("id");
        erdfeid.setLiteral(entity.getId());

        EmbeddedRDFElement erdfecmodel = new EmbeddedRDFElement();
        erdfecmodel.setName("hasModel");
        erdfecmodel.setResource("info:fedora/dcs:Event");

        erdfd.getResourceList().add(erdfeid);
        erdfd.getResourceList().add(erdfecmodel);

        // Process the additional relationships.
        // Note: Removed for Y1P per discussion. DWD 7/28/2010
        //for (DcsRelation rel : entity.getRelations()) {
        //    
        //    String relName = rel.getRelUri();
        //    String relFragment = null;
        //    try {
        //        URI relURI = new URI(relName);
        //        relFragment = relURI.getFragment();
        //    }
        //    catch (URISyntaxException e) {
        //        System.out.println("Fragged URI");  
        //    }
        //
        //    EmbeddedRDFElement element = new EmbeddedRDFElement();
        //    String relResource = rel.getRef().getRef();
        //    element.setName(relFragment);
        //    element.setResource(relResource);
        //    erdfd.getResourceList().add(element);
        //
        //}

        DatastreamVersion reldsv = new DatastreamVersion();
        reldsv.setVersionID(relds.getId()
                + DatastreamVersionConverter.BASE_VERSION);
        reldsv.setLabel(EmbeddedRDFConverter.VERSION_LABEL);
        reldsv.setFormatURI(EmbeddedRDFConverter.FORMAT_URI);
        reldsv.setMimeType(EmbeddedRDFConverter.MIMETYPE);

        XMLContent relxmlc = new XMLContent();

        erdf.getDescriptionList().add(erdfd);
        relxmlc.setContent(erdf);
        reldsv.setContent(relxmlc);
        relds.getVersionList().add(reldsv);
        fdo.getDatastreamMap().put(relds.getId(), relds);

        // DCPXML
        DCPackage dcp = new DCPackage();

        Datastream dcpds = new Datastream();
        dcpds.setId(DatastreamConverter.DCPXML);
        dcpds.setControlGroup(DatastreamConverter.CONTROL_GROUP_X);
        dcpds.setState(DatastreamConverter.STATE_A);
        dcpds.setVersionable(DatastreamConverter.VERSIONABLE_T);

        DatastreamVersion dcpdsv = new DatastreamVersion();
        dcpdsv.setVersionID(dcpds.getId()
                + DatastreamVersionConverter.BASE_VERSION);
        dcpdsv.setLabel(DatastreamVersionConverter.DEFAULT_LABEL);
        dcpdsv.setFormatURI(DatastreamVersionConverter.DEFAULT_FORMAT_URI);
        dcpdsv.setMimeType(DatastreamVersionConverter.XMLCONTENT_MIME);

        XMLContent dcpxmlc = new XMLContent();

        dcp.getContentList().add(entity);
        dcpxmlc.setContent(dcp);
        dcpdsv.setContent(dcpxmlc);
        dcpds.getVersionList().add(dcpdsv);
        fdo.getDatastreamMap().put(dcpds.getId(), dcpds);

        return fdo;

    }

}
