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

public class UpdateDuIdEvent extends Event<UpdateDuIdEvent.Handler> {

    public interface Handler {
        void onMessageReceived(UpdateDuIdEvent event);
    }

    private static final Type<UpdateDuIdEvent.Handler> TYPE =
        new Type<UpdateDuIdEvent.Handler>();

   
    public static HandlerRegistration register(EventBus eventBus,
    		UpdateDuIdEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }    

    private final String  statusUrl;
   

    public UpdateDuIdEvent(String statusUrl) {
        this.statusUrl = statusUrl;
    }
    

    @Override
    public Type<UpdateDuIdEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public String getStatusUrl() {
        return this.statusUrl;
    }
   
    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}