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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;


/**
 * Calculates a hash on a stream, and notifies listeners when it is available.  The hash is available under two
 * conditions: when the end of stream is reached, or {@link #close()} is called.  Listeners will only be notified once,
 * of the digest value. 
 */
public class DigestNotificationStream
        extends FilterInputStream {

    private final DigestNotifier notifier;

    private final MessageDigest _digest;

    private boolean notified = false;

    /**
     * Create a notification stream that will compute the given hash.
     * 
     * @param stream
     *        Stream to calculate hash as it is read.
     * @param digest
     *        Digest used to calculate hash.
     */
    public DigestNotificationStream(InputStream stream, MessageDigest digest) {
        super(stream);
        _digest = digest;
        notifier = new DigestNotifier();
    }

    /**
     * Create a notification stream with the given listeners.
     * 
     * @param stream
     *        Stream to calculate hash as it is read.
     * @param digest
     *        Digest used to calculate hash.
     * @param initialListeners
     *        Initial listeners.
     */
    public DigestNotificationStream(InputStream stream,
                                    MessageDigest digest,
                                    DigestListener... initialListeners) {
        super(stream);
        _digest = digest;
        notifier = new DigestNotifier(initialListeners);
    }

    @Override
    public int read() throws IOException {
        int result = in.read();
        if (result == -1) {
            notifier.notifyListeners(_digest);
            notified = true;
        } else {
            _digest.update((byte) result);
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result == -1) {
            notifier.notifyListeners(_digest);
            notified = true;
        } else {
            _digest.update(b, off, result);
        }
        return result;
    }

    /**
     * Closes the underlying stream, then notifies the listeners.  If the stream has already been closed,
     * <code>close()</code> is called on the underlying stream, but the listeners will not be notified.
     * This insures that listeners will only be notified once, even if <code>close()</code> is called
     * multiple times.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        super.close();
        if (!notified) {
            notifier.notifyListeners(_digest);
            notified = true;
        }
    }
}
