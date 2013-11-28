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
package org.dataconservancy.mhf.eventing.manager;

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;

public class MetadataHandlingEventManager {
    
    private static MetadataHandlingEventManager instance;
    private Set<MetadataHandlingEventListener> listeners;
    
    private MetadataHandlingEventManager() {
        listeners = new HashSet<MetadataHandlingEventListener>();
    }
    
    /**
     * Used to get an instance of the currently active event manager
     * @return A metadata handling event manager to use for registering listeners and sending events.
     */
    public static MetadataHandlingEventManager getInstance() {
        if(instance == null) {
            instance = new MetadataHandlingEventManager();
        }
        
        return instance;
    }
    
    /**
     * Used to register a listener with the event manager. Listener must implement {@code MetadataHandlingEventListener#onEventRecieved(MetadataHandlingEvent)} to receive events. 
     * Listeners should use {@link #unRegisterListener(MetadataHandlingEventListener)} to unregister from the manager when they no longer wish to receive events.
     * @param listener The listener to register with the manager.
     */
    public void registerListener(MetadataHandlingEventListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Used to unregister a listener with the event manager. 
     * @param listener The listener to remove from the manager.
     * @return true if the listener was removed, false if the listner wasn't currently registered with the manager
     */
    public boolean unRegisterListener(MetadataHandlingEventListener listener) {
        return listeners.remove(listener);
    }
    
    /**
     * Sends a {@code MetadataHandlingEvent} to all registered {@code MetadataHandlingEventListeners}.
     * @param event The {@code MetadataHandlingEvent} to be sent to all registered listeners.
     */
    public void sendEvent(MetadataHandlingEvent event) {
        for(MetadataHandlingEventListener listener : listeners) {
            listener.onMetadataFileHandlingEvent(event);
        }
    }
}
