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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Calculates a hash on a stream, and notifies listeners of the hash when the stream is closed.  Listeners will only
 * be notified once, even if <code>close()</code> is called multiple times.  Listeners added after the stream is closed
 * will not be notified.
 */
public class DigestNotificationOutputStream extends FilterOutputStream {
    
    private final DigestNotifier notifier;

    private final MessageDigest _digest;

    private boolean notified = false;

    /**
     * Create a notification stream that will compute the given hash.
     *
     * @param stream Stream to calculate hash as it is written.
     * @param digest Digest used to calculate hash.
     */
    public DigestNotificationOutputStream(OutputStream stream, MessageDigest digest) {
        super(stream);
        _digest = digest;
        notifier = new DigestNotifier();
    }

    /**
     * Create a notification stream with the given listeners.
     *
     * @param stream           Stream to calculate hash as it is written.
     * @param digest           Digest used to calculate hash.
     * @param initialListeners Initial listeners.
     */
    public DigestNotificationOutputStream(OutputStream stream,
                                    MessageDigest digest,
                                    DigestListener... initialListeners) {
        super(stream);
        _digest = digest;
        notifier = new DigestNotifier(initialListeners);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        _digest.update((byte)b);
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
        if (notified) {
            super.close();
        } else {
            IOException notifierException = null;
            try {
                notifier.notifyListeners(_digest);
                notified = true;
            } catch (IOException e) {
                // Don't prevent an exception from closing the stream,
                // but save it so we can throw it later.
                notifierException = e;
            } finally {
                super.close();
            }

            if (notifierException != null) {
                throw notifierException;
            }
        }
    }
}
