package org.dataconservancy.dcs.access.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;


public interface Icons extends ClientBundle {

        public Icons RESOURCES = GWT.create(Icons.class);

        @Source("folder.png")
        ImageResource tree_icon_collection_closed();

        @Source("file.png")
        ImageResource tree_icon_item();

}