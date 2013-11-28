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
package org.dataconservancy.deposit.sword.server.filters;

import java.io.IOException;
import java.io.InputStream;

import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.RequestContextWrapper;

/**
 * Calculate a digest value on the submitted content stream.
 * <p>
 * Calculates the digest of user-supplied content body as its InputStream is
 * being read by its consumer. When an EOF is encountered, the final hash value
 * will be stored in the current {@link RequestContext} in the
 * {@link #CALCULATED_DIGEST} attribute. Since it is calculated on-the-fly, the
 * digest value will not be known until <em>after</em> the caller has read the
 * entire InputStream. The <code>RequestContext</code> attribute value will be
 * null until then.
 * </p>
 */
public class DigestFilter
        implements Filter {

    /**
     * Attribute name in {@link RequestContext} for storing digest value
     */
    public static final String CALCULATED_DIGEST = "CALCULATED_DIGEST";

    /**
     * Attribute name in {@link RequestContext} for storing the name of the
     * algorithm used
     */
    public static final String CALCULATED_DIGEST_ALGORITHM =
            "CALCULATED_DIGEST_ALGORITHM";

    private String digestAlgorithm = "MD5";

    /**
     * Set the algorithm used for calculating the content digest.
     * <p>
     * If none are provided, "MD5" is used by default.
     * </p>
     * 
     * @param algo
     *        Valid digest algorithm accepted by java {@link MessageDigest}
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/guide/security/CryptoSpec.html#AppA">
     *      Standard digest names</a>
     */
    public DigestFilter setDigestAlgorithm(String algo) {
        digestAlgorithm = algo;
        return this;
    }

    /**
     * Get the digest algorithm used for calculating the content digest
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public ResponseContext filter(RequestContext request, FilterChain chain) {
        return chain.next(new Wrapper(request));
    }

    private class Wrapper
            extends RequestContextWrapper {

        public Wrapper(RequestContext request) {
            super(request);
        }

        public InputStream getInputStream() {
            try {
                return new DigestCaptureStream(super.getInputStream(),
                                               MessageDigest
                                                       .getInstance(digestAlgorithm));
            } catch (Exception e) {
                throw new RuntimeException("Could not calculate digest", e);
            }
        }

        private void registerDigestValue(byte[] digestBytes) {
            String hexDigestValue = new String(Hex.encodeHex(digestBytes));
            setAttribute(CALCULATED_DIGEST, hexDigestValue);
            setAttribute(CALCULATED_DIGEST_ALGORITHM, digestAlgorithm);
        }

        private class DigestCaptureStream
                extends DigestInputStream {

            public DigestCaptureStream(InputStream stream, MessageDigest digest) {
                super(stream, digest);
            }

            @Override
            public int read() throws IOException {
                return detectEnd(super.read());
            }

            @Override
            public int read(byte[] b) throws IOException {
                return detectEnd(super.read(b));
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return detectEnd(super.read(b, off, len));
            }

            private int detectEnd(int bytes) {
                if (bytes == -1) {
                    registerDigestValue(getMessageDigest().digest());
                }

                return bytes;
            }
        }
    }
}
