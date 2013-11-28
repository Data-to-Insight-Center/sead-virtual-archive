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

import java.util.Collection;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;
import static org.dataconservancy.model.dcs.support.Util.isNull;

/**
 * Converts the xml representation of a {@link DcsManifestation} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;Manifestation id="urn:sdss:12345/manifestation"&gt;
 * &lt;deliverableUnit ref="urn:sdss:12345" /&gt;
 * &lt;manifestationFile ref="urn:sdss:12345/FITS_FILE"&gt;
 * &lt;path&gt;/scans/5/&lt;/path&gt;
 * &lt;/manifestationFile&gt;
 * &lt;/Manifestation&gt;
 * </pre>
 */
public class ManifestationConverter extends AbstractEntityConverter {

    public static final String E_MANIFESTATION = "Manifestation";
    public static final String E_DUNIT = "deliverableUnit";
    public static final String E_TECHENV = "technicalEnvironment";
    public static final String E_TYPE = "type";

    public static final String A_ID = "id";
    public static final String A_REF = "ref";
    public static final String A_DATE_CREATED = "dateCreated";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final DcsManifestation man = (DcsManifestation) source;
        final Collection<DcsMetadata> metadata = man.getMetadata();
        final Collection<DcsManifestationFile> files = man.getManifestationFiles();
        final Collection<DcsMetadataRef> metadataRef = man.getMetadataRef();
        final Collection<String> techenv = man.getTechnicalEnvironment();
        final String type = man.getType();

        if (!isEmptyOrNull(man.getId())) {
            writer.addAttribute(A_ID, man.getId());
        }
        
        if (!isEmptyOrNull(man.getDateCreated())) {
            writer.addAttribute(A_DATE_CREATED, man.getDateCreated());
        }

        if (!isNull(man.getDeliverableUnit())) {
            writer.startNode(E_DUNIT);
            writer.addAttribute(A_REF, man.getDeliverableUnit());
            writer.endNode();
        }

        if (!isNull(techenv)) {
            for (String e : techenv) {
                if (!isEmptyOrNull(e)) {
                    writer.startNode(E_TECHENV);
                    writer.setValue(e);
                    writer.endNode();
                }
            }
        }

        if (!isEmptyOrNull(type)) {
            writer.startNode(E_TYPE);
            writer.setValue(type);
            writer.endNode();
        }
        
        if (!isNull(files)) {
            for (DcsManifestationFile mf : files) {
                writer.startNode(ManifestationFileConverter.E_MANFILE);
                context.convertAnother(mf);
                writer.endNode();
            }
        }

        if (!isNull(metadataRef)) {
            for (DcsMetadataRef md : metadataRef) {
                writer.startNode(MetadataConverter.E_METADATA);
                context.convertAnother(md);
                writer.endNode();
            }
        }

        if (!isNull(metadata)) {
            for (DcsMetadata md : metadata) {
                writer.startNode(MetadataConverter.E_METADATA);
                context.convertAnother(md);
                writer.endNode();
            }
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsManifestation m = new DcsManifestation();

        // inside <Manifestation>

        if (!isEmptyOrNull(reader.getAttribute(A_ID))) {
            m.setId(reader.getAttribute(A_ID));
        }
        
        if (!isEmptyOrNull(reader.getAttribute(A_DATE_CREATED))) {
            m.setDateCreated(reader.getAttribute(A_DATE_CREATED));
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String name = getElementName(reader);

            if (name.equals(E_DUNIT)) {
                if (!isEmptyOrNull(reader.getAttribute(A_REF))) {
                    m.setDeliverableUnit(reader.getAttribute(A_REF));
                }
            }

            if (name.equals(ManifestationFileConverter.E_MANFILE)) {
                final DcsManifestationFile mf = (DcsManifestationFile)
                        context.convertAnother(m, DcsManifestationFile.class);
                if (mf != null) {
                    m.addManifestationFile(mf);
                }
            }

            if (name.equals(MetadataConverter.E_METADATA)) {
                final String ref = reader.getAttribute(MetadataConverter.A_REF);
                if (!isEmptyOrNull(ref)) {
                    final DcsMetadataRef mdRef = (DcsMetadataRef) context.convertAnother(m, DcsMetadataRef.class);
                    if (mdRef != null) {
                        m.addMetadataRef(mdRef);
                    }
                } else {
                    final DcsMetadata md = (DcsMetadata) context.convertAnother(m, DcsMetadata.class);
                    if (md != null) {
                        m.addMetadata(md);
                    }
                    continue;  // MetadataConverter essentially calls reader.moveUp()
                }
            }

            if (name.equals(E_TECHENV)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    m.addTechnicalEnvironment(value);
                }
            }

            if (name.equals(E_TYPE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    m.setType(value);
                }
            }

            reader.moveUp();
        }

        return m;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsManifestation.class == type;
    }
}
