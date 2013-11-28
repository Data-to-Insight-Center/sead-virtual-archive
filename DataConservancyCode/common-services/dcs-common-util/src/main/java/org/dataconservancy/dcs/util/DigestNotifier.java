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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maintains a collection of {@link org.dataconservancy.dcs.util.DigestListener}s and encapsulates notification logic.
 */
class DigestNotifier {

    private final List<DigestListener> listeners;

    public DigestNotifier() {
        this.listeners = new ArrayList<DigestListener>();
    }

    public DigestNotifier(DigestListener... listeners) {
        this.listeners = Arrays.asList(listeners);
    }

    /**
     * Add a listener to be notified of hash when it is available.
     *
     * @param listener listener for hash.
     */
    public void addListener(DigestListener listener) {
        listeners.add(listener);
    }

    /**
     * Notify the listeners of the the digest.
     *
     * @param digest the message digest
     * @throws IOException
     */
    public void notifyListeners(MessageDigest digest) throws IOException {
        byte[] bytes = digest.digest();
        for (DigestListener listener : listeners) {
            listener.notify(bytes);
        }
    }
}
