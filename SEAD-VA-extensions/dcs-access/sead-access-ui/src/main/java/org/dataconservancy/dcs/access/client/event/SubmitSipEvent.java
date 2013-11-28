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

import java.util.List;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class SubmitSipEvent extends Event<SubmitSipEvent.Handler> {

    public interface Handler {
        void onMessageReceived(SubmitSipEvent event);
    }

    private static final Type<SubmitSipEvent.Handler> TYPE =
        new Type<SubmitSipEvent.Handler>();

   
    public static HandlerRegistration register(EventBus eventBus,
    		SubmitSipEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }    

    private final String filePath;
    private final List<String> previousStatusUrls;
    
    public SubmitSipEvent(String filePath, List<String> previousStatusUrls) {
        this.filePath = filePath;
        this.previousStatusUrls = previousStatusUrls;
    }
    

    @Override
    public Type<SubmitSipEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public String getFilePath() {
        return this.filePath;
    }
    
    public List<String> getPreviousStatusUrl() {
        return this.previousStatusUrls;
    }
 
    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}