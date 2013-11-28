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
package org.dataconservancy.dcs.util;

import java.io.IOException;

/**
 * Listener for calculated hash value.
 * <p>
 * The hash is calculated dynamically as the stream is being read. Upon
 * encountering the last byte, but <em>before</em> returning from read(),
 * the hash value will be calculated, and all listeners notified. At that
 * point, the read() call initiated by the consumer of this stream will
 * succeed unless a listener throws an exception.
 * </p>
 */
public interface DigestListener {

    /**
     * Notification of digest value.
     * 
     * @param digestValue
     *        raw byte array of hash value.
     * @throws IOException
     *         a listener may decide to throw an IOException.
     */
    public void notify(byte[] digestValue) throws IOException;
}
