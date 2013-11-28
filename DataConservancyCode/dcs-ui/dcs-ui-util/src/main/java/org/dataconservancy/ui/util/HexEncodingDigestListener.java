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
package org.dataconservancy.ui.util;

import org.dataconservancy.dcs.util.DigestListener;

import java.io.IOException;

/**
 * DigestListener that encodes the digest as a hexadecimal string.  This class is not thread-safe, but
 * it can be re-used (that is, it doesn't need to be re-instantiated each time it is used).
 *
 * @see org.dataconservancy.dcs.util.DigestNotificationStream
 */
public class HexEncodingDigestListener implements DigestListener {

    private StringBuilder digestHolder;

    /**
     * {@inheritDoc}
     * This method populates an internal {@code StringBuilder} with the hex-encoded digest, which can be retrieved
     * using {@link #getDigest()}.
     *
     * @param digestValue {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public void notify(byte[] digestValue) throws IOException {
        digestHolder = new StringBuilder(128);
        for (byte b : digestValue) {
            digestHolder.append(Integer.toHexString(0xff & b));
        }
    }

    /**
     * Returns the value of the digest as a hex-encoded string.
     *
     * @return the digest as a hex-encoded string, or {@code null} if {@link #notify(byte[])} has not been called.
     */
    public String getDigest() {
        if (digestHolder == null) {
            return null;
        }
        return digestHolder.toString();
    }
}
