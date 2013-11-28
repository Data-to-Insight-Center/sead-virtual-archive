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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;
import static org.dataconservancy.model.dcs.support.Util.isNull;

/**
 *
 */
public class ManifestationFileConverter extends AbstractEntityConverter {

    public static final String E_MANFILE = "manifestationFile";
    public static final String E_PATH = "path";
    public static final String E_REL = "relationship";

    public static final String A_REF = "ref";


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final DcsManifestationFile mf = (DcsManifestationFile) source;

        if (mf.getRef() != null) {
            writer.addAttribute(A_REF, mf.getRef().getRef());
        }

        if (!isEmptyOrNull(mf.getPath())) {
            writer.startNode(E_PATH);
            writer.setValue(mf.getPath());
            writer.endNode();
        }

        if (!isNull(mf.getRelSet()) && !mf.getRelSet().isEmpty()) {
            for (DcsRelation rel : mf.getRelSet()) {
                writer.startNode(E_REL);
                context.convertAnother(rel);
                writer.endNode();
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsManifestationFile mf = new DcsManifestationFile();

        // in <manifestationFile>

        if (!isEmptyOrNull(reader.getAttribute(A_REF))) {
            mf.setRef(new DcsFileRef(reader.getAttribute(A_REF)));
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String name = getElementName(reader); //reader.getNodeName();

            if (name.equals(E_PATH)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    mf.setPath(value);
                }
            }

            if (name.equals(E_REL)) {
                final DcsRelation rel = (DcsRelation)context.convertAnother(mf, DcsRelation.class);
                if (rel != null) {
                    mf.addRel(rel);
                }
            }

            reader.moveUp();
        }

        return mf;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsManifestationFile.class == type;
    }
}
