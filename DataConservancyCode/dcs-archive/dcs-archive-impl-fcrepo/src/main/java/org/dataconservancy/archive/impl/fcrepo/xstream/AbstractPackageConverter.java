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
package org.dataconservancy.archive.impl.fcrepo.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: The idea of this is useful but, except for logging, if the consider
 * disposing of this. DWD 
 * 
 * Abstract package converter for the DCS object model.
 * Encapsulates common logic for XStream {@link Converter converters} such as
 * nullity checks, XML namespace declarations, and
 * {@link javax.xml.namespace.QName} handling. All Data Conservancy converters
 * are expected to extend this class.
 */
abstract class AbstractPackageConverter
        implements Converter {

    // Logger
    final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Ensures <code>source</code> is not <code>null</code>.
     * 
     * @inheritDoc
     * @throws IllegalArgumentException
     *         if <code>source</code> is <code>null</code>
     */
    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        if (source == null) {
            final String msg = "Source object was null.";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

}
