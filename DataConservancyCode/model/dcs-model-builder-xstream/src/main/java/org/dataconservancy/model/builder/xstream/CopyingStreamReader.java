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

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * An interface that XStream {@link com.thoughtworks.xstream.io.HierarchicalStreamReader readers} and
 * {@link com.thoughtworks.xstream.io.HierarchicalStreamWriter writers} can implement if they support copying
 * XML streams.
 */
public interface CopyingStreamReader {
    /**
     * Copies the current node and its children to the <code>sink</code>.
     *
     * @param sink the output sink
     * @throws javax.xml.stream.XMLStreamException
     */
    public void copyNode(OutputStream sink) throws XMLStreamException;

    /**
     * Copies the current node and its children to the <code>writer</code>.
     *
     * @param writer the writer to write to
     * @throws javax.xml.stream.XMLStreamException
     */
    public void copyNode(DcsStaxWriter writer) throws XMLStreamException;
}
