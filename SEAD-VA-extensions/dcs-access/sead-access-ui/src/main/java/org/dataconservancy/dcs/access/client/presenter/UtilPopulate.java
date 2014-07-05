package org.dataconservancy.dcs.access.client.presenter;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import org.dataconservancy.dcs.access.client.model.JsProvDocument;

public class UtilPopulate {
	
	private JsArrayString entitiesStr = JavaScriptObject.createArray().cast();
	
	public void populateEntities(String entityId, JsProvDocument document) {
		  
		entitiesStr.push(entityId);
		JsArrayString tempentitiesStr=  document.getRelatedEntities(entityId);
			 for(int i=0;i<tempentitiesStr.length();i++){
				 int count = entitiesStr.length();
				 boolean found = false;
				  for(int k =0;k<count;k++){
					  if(entitiesStr.get(k).equalsIgnoreCase(tempentitiesStr.get(i)))
					  {  
						  found = true;
						  break;
					  }
				  }
				  if(!found){
					  populateEntities(tempentitiesStr.get(i), document);
				  }
			  }
			
		}
	
		public JsArrayString getEArrayString(){
			return entitiesStr;
		}
}
