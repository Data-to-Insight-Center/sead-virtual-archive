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
package org.dataconservancy.dcs.ingest.sip;

import java.io.InputStream;

/**
 * Perform XML validation on pass-through streams.
 */
public interface XmlStreamValidator {

    /**
     * Return a stream that XML validates the source input stream.
     * <p>
     * May return an InputStream that performs dynamic inline validation, or it
     * may perform validation upfront and return a pre-validated inputStream. In
     * the case of the former, an exception will be thrown whenever an invalid
     * section of XML is encountered from the stream as it is read. In the case
     * of the latter, an exception will be thrown at the time of calling
     * {@link XmlStreamValidator#validating(InputStream)}
     * </p>
     * 
     * @param src
     *        Source input stream
     * @return InputStream that may perform inline XML validation.
     */
    public InputStream validating(InputStream src);
}
