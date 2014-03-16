/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.seadva.bagit.event.impl;

import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.event.api.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener that maps events to handlers
 */
public class PackageListener implements Listener {

    static Map<Event,String> eventHandlerMap = new HashMap<Event, String>();
    @Override
    public void map(Event event, String handlerClass) {
        eventHandlerMap.put(event,handlerClass);
    }

    @Override
    public PackageDescriptor execute(Event event, PackageDescriptor packageDescriptor) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Handler handler = (Handler) Class.forName(eventHandlerMap.get(event)).newInstance();
        return handler.execute(packageDescriptor);
    }
}
