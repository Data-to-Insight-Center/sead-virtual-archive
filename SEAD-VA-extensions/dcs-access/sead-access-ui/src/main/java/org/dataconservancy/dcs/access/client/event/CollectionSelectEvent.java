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

import org.dataconservancy.dcs.access.client.model.CollectionNode;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class CollectionSelectEvent extends Event<CollectionSelectEvent.Handler> {

    /**
     * Implemented by methods that handle MessageReceivedEvent events.
     */
    public interface Handler {
        void onMessageReceived(CollectionSelectEvent event);
    }

    private static final Type<CollectionSelectEvent.Handler> TYPE =
        new Type<CollectionSelectEvent.Handler>();

   
    public static HandlerRegistration register(EventBus eventBus,
    		CollectionSelectEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }    

    private final CollectionNode collection;
    private final Boolean value;

    public CollectionSelectEvent(CollectionNode collection, Boolean value) {
        this.collection = collection;
        this.value = value;
    }

    @Override
    public Type<CollectionSelectEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public CollectionNode getCollection() {
        return collection;
    }
    
    public Boolean getValue(){
    	return value;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}