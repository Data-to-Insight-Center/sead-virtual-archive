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
package org.dataconservancy.dcs.util.stream.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * An interface providing access to streams.
 */
public interface StreamSource {

    /**
     * Returns an Iterator over all stream identifiers available to this source.
     * <p/>
     * This interface makes no guarantees as to the durability of these identifiers.  Callers should assume that identifiers
     * are only durable within the scope of this StreamSource's instance.
     *
     * @return an Iterable over String identifiers of InputStreams
     */
    public Iterable<String> streams();

    /**
     * Returns an Iterator over all stream identifiers available to this source that have been modified since the supplied
     * date.
     * <p/>
     * This interface makes no guarantees as to the durability of these identifiers.  Callers should assume that identifiers
     * are only durable within the scope of this StreamSource's instance.
     *
     * @param since only streams modified after this date will be returned
     * @return an Iterator over String identifiers of InputStreams
     */
    public Iterable<String> streams(Calendar since);

    /**
     * Obtain a stream by its identifier.
     *
     * @param id the identifier
     * @return the stream
     * @throws java.io.IOException if there is an error accessing the stream
     */
    public InputStream getStream(String id) throws IOException;

}
