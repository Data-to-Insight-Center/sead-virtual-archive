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

/**
 * Interface for Receiving {@link ScanCompleteEvent}. A class must implement
 * this interface if it wants to receive ScanCompleteEvents.
 */
public interface VirusScanEventListener {

    /**
     * This function will be called when a ScanCompleteEvent is broadcast.
     * 
     * @param event
     *        The ScanCompleteEvent that is being broadcast. This event contains
     *        the request object which can be used to verify it is the event the
     *        listener is interested in.
     */
    public void onScanComplete(ScanCompleteEvent event);
}
