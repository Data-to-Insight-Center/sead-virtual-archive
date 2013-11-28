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
package org.dataconservancy.archive.impl.elm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.archive.api.EntityType.COLLECTION;
import static org.dataconservancy.archive.api.EntityType.DELIVERABLE_UNIT;
import static org.dataconservancy.archive.api.EntityType.EVENT;
import static org.dataconservancy.archive.api.EntityType.FILE;
import static org.dataconservancy.archive.api.EntityType.MANIFESTATION;

/**
 * Produces a Dcp document stream by "concatenating" the given entity
 * serialization.
 * <p>
 * Stored entities are assumed to be XML fragments from dcp entities. The
 * implementation concatenates like entities, and wraps such concatinated sets
 * within the appropriate container elements.
 * </p>
 */
public class DcpEntityStream
        extends InputStream {

    private static final Logger log =
            LoggerFactory.getLogger(DcpEntityStream.class);

    private final InputStream DCP_OPEN =
            this
                    .getClass()
                    .getResourceAsStream("/org/dataconservancy/archive/impl/elm/dcp_open");

    private final InputStream DCP_CLOSE =
            this
                    .getClass()
                    .getResourceAsStream("/org/dataconservancy/archive/impl/elm/dcp_close");

    private final EntityType[] dcpOrder =
            {DELIVERABLE_UNIT, COLLECTION, MANIFESTATION, FILE, EVENT};

    private final EntityStore eStore;

    private final InputStream dcpStream;

    public DcpEntityStream(Map<String, Set<String>> typeMap, EntityStore store) {
        eStore = store;

        final List<InputStream> streams = new ArrayList<InputStream>();
        streams.add(DCP_OPEN);
        for (EntityType etype : dcpOrder) {

            String type = etype.toString();

            if (typeMap.containsKey(type)) {
                streams.add(openContainer(type));
                for (String id : typeMap.get(type)) {
                    streams.add(new EntityInputStream(id));
                }
                streams.add(closeContainer(type));
            }
        }
        streams.add(DCP_CLOSE);

        dcpStream = new SequenceInputStream(new Enumeration<InputStream>() {

            private Iterator<InputStream> i = streams.iterator();

            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public InputStream nextElement() {
                return i.next();
            }
        });
    }

    @Override
    public int read() throws IOException {
        return dcpStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return dcpStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return dcpStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        dcpStream.close();
    }

    private class EntityInputStream
            extends InputStream {

        private final String id;

        private InputStream stream;

        private boolean initialized = false;

        public EntityInputStream(String entityId) {
            id = entityId;
        }

        @Override
        public int read() throws IOException {
            initIfnecessary();
            return stream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            initIfnecessary();
            return stream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            initIfnecessary();
            return stream.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        private void initIfnecessary() throws IOException {
            if (initialized) return;

            try {
                initialized = true;
                stream = eStore.get(id);
            } catch (EntityNotFoundException e) {
                /* Log, add an xml comment, and move on */
                String message = "missing entity " + id;
                String xmlMessage = String.format("\n<!-- %s -->\n", message);
                stream = new ByteArrayInputStream(xmlMessage.getBytes("UTF-8"));

                log.warn(message);
            }
        }
    }

    private InputStream openContainer(String type) {
        try {
            return new ByteArrayInputStream(String.format("<%ss>", type)
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream closeContainer(String type) {
        try {
            return new ByteArrayInputStream(String.format("</%ss>", type)
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
