/*
 * Copyright 2013 Johns Hopkins University
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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * Exactly like {@link KeyDigestPathAlgorithm} except that this class manages a pool of {@code MessageDigest} objects
 * to minimize their creation time.
 */
public class PooledKeyDigestPathAlgorithm extends KeyDigestPathAlgorithm {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final int POOL_MAX_SIZE = 20;

    private final int POOL_TIMEOUT = 30 * 1000;

    private final String algorithm;

    private final GenericObjectPool<MessageDigest> mdPool;

    /**
     * Initializes a pool of 20 MessageDigest objects.  If the pool runs out of objects, the pool will grow.
     *
     * @param algo
     * @param width
     * @param depth
     * @param suffix
     */
    public PooledKeyDigestPathAlgorithm(String algo, int width, int depth, String suffix) {
        super(algo, width, depth, suffix);

        this.algorithm = algo;
        this.mdPool = new GenericObjectPool<MessageDigest>(new MessageDigestPoolableObjectFactory(), POOL_MAX_SIZE,
                GenericObjectPool.WHEN_EXHAUSTED_GROW, POOL_TIMEOUT);
    }

    /**
     * Uses the supplied pool, externally configured.
     *
     * @param algo
     * @param width
     * @param depth
     * @param suffix
     * @param mdPool an externally configured pool of {@code MessageDigest} objects
     */
    public PooledKeyDigestPathAlgorithm(String algo, int width, int depth, String suffix,
                                        GenericObjectPool<MessageDigest> mdPool) {
        super(algo, width, depth, suffix);
        this.algorithm = algo;
        this.mdPool = mdPool;
    }

    /**
     * Calculate a hash code over the supplied string.
     *
     * @param in the string
     * @return the hash code, encoded in hexadecimal.
     */
    String hash(String in) {
        MessageDigest md = null;
        try {
            md = mdPool.borrowObject();
            return new String(Hex.encodeHex(md.digest(in.getBytes("UTF-8"))));
        } catch (Exception e) {
            throw new RuntimeException("Could not calculate id hash", e);
        } finally {
            // There's no guarantees by the JVM when the finalizer will run, so that is why we allow the pool
            // to grow if it is exhausted.
            if (md != null) {
                try {
                    mdPool.returnObject(md);
                } catch (Exception e) {
                    log.warn("Error returning MessageDigest object (0x{}) to the pool: {}",
                            new Object[]{Integer.toHexString(System.identityHashCode(md)), e.getMessage()}, e);
                }
            }
        }
    }

    private class MessageDigestPoolableObjectFactory implements PoolableObjectFactory<MessageDigest> {

        @Override
        public void activateObject(MessageDigest messageDigest) throws Exception {
            messageDigest.reset();
        }

        @Override
        public MessageDigest makeObject() throws Exception {
            return MessageDigest.getInstance(algorithm);
        }

        @Override
        public void destroyObject(MessageDigest messageDigest) throws Exception {
            messageDigest.reset();
        }

        @Override
        public boolean validateObject(MessageDigest messageDigest) {
            // no-op
            return true;
        }

        @Override
        public void passivateObject(MessageDigest messageDigest) throws Exception {
            // no-op
        }
    }
}
