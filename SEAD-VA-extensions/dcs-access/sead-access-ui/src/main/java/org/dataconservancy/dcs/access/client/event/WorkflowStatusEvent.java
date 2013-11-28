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

public class WorkflowStatusEvent extends Event<WorkflowStatusEvent.Handler> {

    public interface Handler {
        void onMessageReceived(WorkflowStatusEvent event);
    }

    private static final Type<WorkflowStatusEvent.Handler> TYPE =
        new Type<WorkflowStatusEvent.Handler>();

   
    public static HandlerRegistration register(EventBus eventBus,
    		WorkflowStatusEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }    

    private final String  message;
    private final String  detail;
    private final String  symbol;
    private final int  percent;
    
    public WorkflowStatusEvent(String message, String detail, String symbol, int percent) {
        this.message = message;
        this.symbol = symbol;
        this.detail = detail;
        this.percent = percent;
    }
    

    @Override
    public Type<WorkflowStatusEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public String getMessage() {
        return this.message;
    }
    
    public String getDetail() {
        return this.detail;
    }
    
    public String getSymbol() {
        return this.symbol;
    }
    
    public int getPercent() {
        return this.percent;
    }
   
    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}