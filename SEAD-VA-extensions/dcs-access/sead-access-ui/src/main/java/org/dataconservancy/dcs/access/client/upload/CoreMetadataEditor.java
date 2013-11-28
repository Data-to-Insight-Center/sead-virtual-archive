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
package org.dataconservancy.dcs.access.client.upload;

import java.util.ArrayList;
import java.util.HashMap;

import org.dataconservancy.dcs.access.client.upload.model.CoreMetadata;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;


public class CoreMetadataEditor
        extends Composite {
    private final TextBox title, abstrct, type, creators, subjects,contact;

    public CoreMetadataEditor(HashMap<String, ArrayList<String>> metadata) {
        title = new TextBox();
        abstrct = new TextBox();
        type = new TextBox();
        creators = new TextBox();
        subjects = new TextBox();
        contact = new TextBox();
        
        if(metadata!=null){
	        ArrayList<String> titles  = metadata.get("title") ;
	        if(titles!=null)
	            for(String val:titles)
	            	this.title.setText(val);
	
	        ArrayList<String> abstracts  = metadata.get("abstract") ;
	        if(abstracts!=null)
	            for(String val:abstracts)
	            	this.abstrct.setText(val);
	
	        ArrayList<String> creators  = metadata.get("creator") ;
	        String creatorsStr="";
	        int i=1;
	        int size = creators.size();
	        if(creators!=null)
	            for(String val:creators){
	            	String[] name = val.split(":");
	            	creatorsStr+= name[0].replace(","," ");
	            	if(i<size)
	            		creatorsStr+=",";
	            	i++;
	            }
	            	this.creators.setText(creatorsStr);
        }
        
        FlexTable table =
                Util.createTable("Title:", "Abstract:", "Type:", "Creators:", "Subjects:","Contact:");
        Util.addColumn(table, title, abstrct, type, creators, subjects,contact);

        if(title.getText()==null)
        	title.setText("REQUIRED");
        
        initWidget(table);
    }

    public CoreMetadata getCoreMetadata() {
        CoreMetadata core = new CoreMetadata();

        core.setTitle(title.getText().trim());
        core.setAbstrct(abstrct.getText().trim());
        core.setType(type.getText().trim());
        
        Util.addAllFromCSV(core.subjects(), subjects.getText());
        Util.addAllFromCSV(core.creators(), creators.getText());

        core.setContact(contact.getText().trim());
        return core;
    }
}
