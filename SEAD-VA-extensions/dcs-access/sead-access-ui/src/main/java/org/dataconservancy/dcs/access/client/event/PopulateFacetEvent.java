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

import org.dataconservancy.dcs.access.ui.client.model.JsModel;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class PopulateFacetEvent extends Event<PopulateFacetEvent.Handler> {

    public interface Handler {
        void onMessageReceived(PopulateFacetEvent event);
    }

    private static final Type<PopulateFacetEvent.Handler> TYPE =
        new Type<PopulateFacetEvent.Handler>();

   
    public static HandlerRegistration register(EventBus eventBus,
    		PopulateFacetEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }    

    private final JsModel result;

    public PopulateFacetEvent(JsModel result) {
        this.result = result;
    }

    @Override
    public Type<PopulateFacetEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public JsModel getSearchResult() {
        return this.result;
    }
   
    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}