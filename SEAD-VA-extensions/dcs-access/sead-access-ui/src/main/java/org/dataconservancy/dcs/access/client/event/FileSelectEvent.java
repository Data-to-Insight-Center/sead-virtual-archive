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
import org.dataconservancy.dcs.access.client.model.FileNode;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class FileSelectEvent extends Event<FileSelectEvent.Handler> {

    /**
     * Implemented by methods that handle MessageReceivedEvent events.
     */
    public interface Handler {
        void onMessageReceived(FileSelectEvent event);
    }

    private static final Type<FileSelectEvent.Handler> TYPE =
        new Type<FileSelectEvent.Handler>();

   
    public static HandlerRegistration register(EventBus eventBus,
    		FileSelectEvent.Handler handler) {
      return eventBus.addHandler(TYPE, handler);
    }    

    private final FileNode file;
    private final Boolean value;

    public FileSelectEvent(FileNode file, Boolean value) {
        this.file = file;
        this.value = value;
    }

    @Override
    public Type<FileSelectEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public FileNode getFile() {
        return file;
    }
    
    public Boolean getValue(){
    	return value;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onMessageReceived(this);
    }
}