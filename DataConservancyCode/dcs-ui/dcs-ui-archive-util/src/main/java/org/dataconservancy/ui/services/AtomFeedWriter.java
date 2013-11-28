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
package org.dataconservancy.ui.services;

import org.dataconservancy.model.dcs.DcsEvent;

import java.io.InputStream;

/**
 * Defines an interface for producing an Atom feed from DcsEvents.
 */
public interface AtomFeedWriter {

    /**
     * Provided a list of events, return an InputStream to the Atom feed which represents those events.
     *
     * @param depositId the identifier representing the deposit transaction
     * @param events the events which occurred for the transaction
     * @return an InputStream to an Atom feed which represents the events.
     */
    public InputStream toAtom(String depositId, DcsEvent... events);

}
