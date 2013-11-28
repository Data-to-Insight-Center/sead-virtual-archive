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
package org.dataconservancy.dcs.virusscanning.event;

import java.util.ArrayList;

/**
 * Manager to send {@link ScanCompleteEvent} events, as well as register to
 * receive events.
 * <p>
 * Users will get an instance of this manager and will not instantiate their own
 * version. To send events use the sendEvent call. Listeners should use the
 * register and unregister calls.
 * </p>
 */
public class VirusScanningEventManager {

    public ArrayList<VirusScanEventListener> listeners;

    private static VirusScanningEventManager instance;

    private VirusScanningEventManager() {
        listeners = new ArrayList<VirusScanEventListener>();
    };

    /**
     * Used to get an instance of the VirusScanningEventManager. This instance
     * should be used to send events, and register/unregister a listener.
     * 
     * @return Returns a VirusScanningEventManager instance that can be used for
     *         Manager calls.
     */
    public static VirusScanningEventManager getManager() {

        if (instance == null) {
            instance = new VirusScanningEventManager();
        }

        synchronized (instance) {
            return instance;
        }
    }

    /**
     * Registers a listener with the manager. Listeners must be registered to
     * receive event broadcasts.
     * 
     * @param listener
     *        The listener to be registered.
     * @return True if the listener was successfully added false otherwise.
     */
    public boolean registerListener(VirusScanEventListener listener) {
        return listeners.add(listener);
    }

    /**
     * Unregisters a listener with the manager. Once called the listener will no
     * longer receive event broadcasts.
     * 
     * @param listener
     *        The listener to be unregistered.
     * @return True if the listener was successfully removed false otherwise.
     */
    public boolean unRegisterListener(VirusScanEventListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Broadcasts an event to all registered listeners.
     * 
     * @param event
     *        The ScanCompleteEvent to be sent to listeners.
     */
    public void sendEvent(ScanCompleteEvent event) {

        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onScanComplete(event);
        }
    }

}
