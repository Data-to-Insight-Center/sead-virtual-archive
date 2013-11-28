/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dataconservancy.dcs.access.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.event.CollectionClickEvent;
import org.dataconservancy.dcs.access.client.event.CollectionPassiveSelectEvent;
import org.dataconservancy.dcs.access.client.event.CollectionSelectEvent;
import org.dataconservancy.dcs.access.client.model.CollectionNode;
import org.dataconservancy.dcs.access.client.model.DatasetRelation;
import org.dataconservancy.dcs.access.client.model.CollectionNode.SubType;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.TreeViewModel;

/**
 * The {@link TreeViewModel} used to organize contacts into a hierarchy.
 */
public class CollectionTreeViewModel implements TreeViewModel {

	public interface Resources extends ClientBundle {
		 @Source("folder.png")
		  ImageResource folder();
	}
	static class CollectionCell extends AbstractCell<CollectionNode> {

		MultiSelectionModel selectionModel;
		/**
	     * The html of the image used for contacts.
	     */
		String imageHtml;
	    public CollectionCell(){
	    	 super("keydown","dblclick","click");
	    	 Resources cb = GWT.create(Resources.class);
	    	 this.imageHtml = AbstractImagePrototype.create(cb.folder()).getHTML();
	    }

	    @Override
	    public void render(Context context, CollectionNode value, SafeHtmlBuilder sb) {
	      // Value can be null, so do a null check..
	      if (value == null) {
	        return;
	      }

	      sb.appendHtmlConstant("<table>");
	      sb.appendHtmlConstant("<tr>" +
	      		"<td>");
	      sb.appendHtmlConstant(imageHtml).appendEscaped(" ");
	     
	      //String title =value.getTitle();
	      //String[] subTitle = title.split("/");
	      
	      sb.appendEscaped(value.getTitle());//subTitle[subTitle.length-1]);
	      sb.appendHtmlConstant("</td></tr></table>");
	    }
	    @Override
        public void onBrowserEvent(Context context, Element parent, CollectionNode value,
            NativeEvent event, ValueUpdater<CollectionNode> valueUpdater) {
	    	 if (value == null) {
	    		 return;
	           }
	           super.onBrowserEvent(context, parent, value, event, valueUpdater);
	           if ("click".equals(event.getType())||"keydown".equals(event.getType())||"dblclick".equals(event.getType())) {
	        	   MediciIngestPresenter.EVENT_BUS.fireEvent(new CollectionClickEvent(value));
	           }
	    }
	    
	  }
 
  /**
   * The static images used in this model.
   */

  private final Cell<CollectionNode> collectionCell;
  private final DefaultSelectionEventManager<CollectionNode> selectionManager =
      DefaultSelectionEventManager.createCheckboxManager();
  private final SelectionModel<CollectionNode> selectionModel;
  Map<String, CollectionNode> dusMap;
  private Map<String, String> parentMap ;
  String root;
  
 
  
  
  public CollectionTreeViewModel(
		  final SelectionModel<CollectionNode> selectionModel,
		  final DatasetRelation relations, String root) {
    this.selectionModel = selectionModel;
    this.dusMap = relations.getDuAttrMap();
    this.parentMap = relations.getParentMap();
    this.root = root;
    
    // Construct a composite cell for contacts that includes a checkbox.
    //adding
    List<HasCell<CollectionNode, ?>> hasCells = new ArrayList<HasCell<CollectionNode, ?>>();
    hasCells.add(new HasCell<CollectionNode, Boolean>() {

      private CheckboxCell cell = new CheckboxCell(true, false);

      public Cell<Boolean> getCell() {
        return cell;
      }

   

      public Boolean getValue(CollectionNode object) {
        return selectionModel.isSelected(object);
      }

      

    private void updateChildNodes(CollectionNode object, Boolean value){
    	 
    	List<String> subCollections = object.getSub().get(SubType.Collection);
			if(subCollections!=null)//why is this running twice?
     	{
				for(int i=0;i<subCollections.size();i++){
					selectionModel.setSelected((CollectionNode)dusMap.get(subCollections.get(i)), value);
					MediciIngestPresenter.EVENT_BUS.fireEvent(new CollectionPassiveSelectEvent((CollectionNode)dusMap.get(subCollections.get(i)),value));
					updateChildNodes((CollectionNode)dusMap.get(subCollections.get(i)), value);
				}
     	}
    }
      
	@Override
	public FieldUpdater<CollectionNode, Boolean> getFieldUpdater() {
		// TODO Auto-generated method stub
		//return null;
		return new FieldUpdater<CollectionNode, Boolean>() {
				public void update(int index, CollectionNode object, Boolean value) {
					
					//Update child Nodes
		           /* List<String> subCollections = object.getSub().get(SubType.Collection);
					if(subCollections!=null)//why is this running twice?
	            	{
						for(int i=0;i<subCollections.size();i++)
							selectionModel.setSelected((CollectionNode)dusMap.get(subCollections.get(i)), value);
	            	}*/
					
					//Update child collection nodes
					updateChildNodes(object, value);
					
					//update the Parent Node
					String parentCollection = parentMap.get(object.getId());
					if(parentCollection!=null){
						List<String> siblingCollections = dusMap.get(parentCollection).getSub().get(SubType.Collection);
						
						int allSelected = 1;
						if(value){
							
							for(String sibling:siblingCollections){
								if(!selectionModel.isSelected(dusMap.get(sibling)))
								{
									allSelected=0;
									break;
								}
							}
						}
						if(allSelected == 1&&value){
							//set parent true
							selectionModel.setSelected((CollectionNode)dusMap.get(parentCollection), true);
							
						}
						else{
							//set parent false
							selectionModel.setSelected((CollectionNode)dusMap.get(parentCollection), false);
						}
						
					}
					MediciIngestPresenter.EVENT_BUS.fireEvent(new CollectionSelectEvent(object,value));
		        }
			};
		}
    });
    
    
    hasCells.add(new HasCell<CollectionNode, CollectionNode>() {

    
      private CollectionCell cell = new CollectionCell();

      public Cell<CollectionNode> getCell() {
        return cell;
      }

      public FieldUpdater<CollectionNode, CollectionNode> getFieldUpdater() {
        return null;
      }

      public CollectionNode getValue(CollectionNode object) {
        return object;
      }
    });

    collectionCell = new CompositeCell<CollectionNode>(hasCells) {
      @Override
      public void render(Context context, CollectionNode value, SafeHtmlBuilder sb) {
    	  if(value==null)
    		  return;
        sb.appendHtmlConstant("<table><tbody><tr>");
        super.render(context, value, sb);
        sb.appendHtmlConstant("</tr></tbody></table>");
      }	

     
      @Override
      protected Element getContainerElement(Element parent) {
        // Return the first TR element in the table.
        return parent.getFirstChildElement().getFirstChildElement().getFirstChildElement();
      }

      @Override
      protected <X> void render(Context context, CollectionNode value,
          SafeHtmlBuilder sb, HasCell<CollectionNode, X> hasCell) {
    	  if(value!=null){
        Cell<X> cell = hasCell.getCell();
        sb.appendHtmlConstant("<td>");
        cell.render(context, hasCell.getValue(value), sb);
        sb.appendHtmlConstant("</td>");
    	  }
      }
    };
    
  }

  public <T> NodeInfo<?> getNodeInfo(T value) {
    if (value == null) {
      // Return top level categories.
    	List<CollectionNode> roots = new ArrayList<CollectionNode>();
        CollectionNode rootCollection = this.dusMap.get(this.root);
        
        roots.add(rootCollection);
        
        ListDataProvider<CollectionNode> dataProvider = new ListDataProvider<CollectionNode>(
        		roots);
        return new DefaultNodeInfo<CollectionNode>(
            dataProvider, collectionCell, selectionModel, selectionManager, null);
    }
    else if (value instanceof CollectionNode) {
    	List<CollectionNode> childNodes = new ArrayList<CollectionNode>();
    	List<String> children= dusMap.get(((CollectionNode) value).getId()).getSub().get(SubType.Collection);
		if(children!=null){
			for(String child:children){
				if(dusMap.containsKey(child)){
					CollectionNode childNode = dusMap.get(child);
					childNodes.add(childNode);
				}
			}
		
    	       
        ListDataProvider<CollectionNode> dataProvider = new ListDataProvider<CollectionNode>(
        		childNodes);
        return new DefaultNodeInfo<CollectionNode>(
            dataProvider, collectionCell, selectionModel, selectionManager, null);
		}
    }

    // Unhandled type.
    String type = value.getClass().getName();
    throw new IllegalArgumentException("Unsupported object type: " + type);
  }

  public boolean isLeaf(Object value) {
	if(value!=null){
		List<String> subs = ((CollectionNode) value).getSub().get(SubType.Collection);//put hashmap with file and subs
		if(subs!=null){
			if(subs.size()==0)
				return true;
		}
		else
		{
			return true;
		}
		return false;
		
	  }
	return false;
  }
  
}
