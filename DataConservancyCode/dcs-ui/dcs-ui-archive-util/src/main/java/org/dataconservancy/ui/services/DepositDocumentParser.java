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
package org.dataconservancy.ui.services;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstraction for parsing a deposit status document from the DCS.
 * <p/>
 * Practically this will involve parsing an Atom XML &lt;feed>, and composing a {@link DepositDocument}.
 */
interface DepositDocumentParser {

    /**
     * Parse the supplied {@code InputStream} which should be the Atom XML feed representing the status of a deposit
     * within the DCS.
     *
     * @param in the {@code InputStream} to the deposit feed
     * @return the {@code DepositDocument} encapsulating the properties of the feed, or {@code null} if the document
     *         cannot be parsed
     * @throws IOException if an error occurs while reading the stream
     */
    DepositDocument parse(InputStream in) throws IOException;
}
