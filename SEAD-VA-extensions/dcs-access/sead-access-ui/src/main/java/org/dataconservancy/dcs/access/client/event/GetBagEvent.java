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

package org.dataconservancy.dcs.access.client.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.dataconservancy.dcs.access.shared.MediciInstance;

public class GetBagEvent extends Event<GetBagEvent.Handler> {

    public interface Handler {
        void onMessageReceived(GetBagEvent event);
    }

    private static final Type<GetBagEvent.Handler> TYPE =
        new Type<GetBagEvent.Handler>();


    public static HandlerRegistration register(EventBus eventBus,
    		GetBagEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }

    public MediciInstance getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    public String getDatasetId() {
        return datasetId;
    }



    private final String datasetId;
    private final MediciInstance sparqlEndpoint;


    public GetBagEvent(
                        String datasetId,
                       MediciInstance sparqlEndpoint){
        this.datasetId = datasetId;
        this.sparqlEndpoint = sparqlEndpoint;
    }


    @Override
    public Type<GetBagEvent.Handler> getAssociatedType() {
        return TYPE;
    }


 
    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}