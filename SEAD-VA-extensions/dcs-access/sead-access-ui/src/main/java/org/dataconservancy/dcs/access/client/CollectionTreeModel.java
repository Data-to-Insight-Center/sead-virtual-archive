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
package org.dataconservancy.dcs.access.client;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class CollectionTreeModel implements TreeViewModel {

  /**
  * A list of songs.
  */
  public static class Playlist {
     private final String name;
     private final List<String> songs = new ArrayList<String>();

     public Playlist(String name) {
       this.name = name;
     }

      /**
      * Add a song to the playlist.
      * 
      * @param name the name of the song
      */
      public void addSong(String name) {
         songs.add(name);
      }

      public String getName() {
         return name;
      }

      /**
      * Return the list of songs in the playlist.
      */
      public List<String> getSongs() {
         return songs;
      }
   }

  class ContactCell extends AbstractCell<Playlist> {

	    /**
	     * The html of the image used for contacts.
	     */
	   
	    public ContactCell() {
	      //this.imageHtml = AbstractImagePrototype.create(image).getHTML();
	    }

	    @Override
	    public void render(Context context, Playlist value, SafeHtmlBuilder sb) {
	      // Value can be null, so do a null check.
	      if (value == null) {
	        return;
	      }

	      sb.appendEscaped(value.getName());
	      
	    }

		
	  }
   /**
   * A composer of classical music.
   */
   private static class Composer {
      private final String name;
      private final List<Playlist> playlists = new ArrayList<Playlist>();

      public Composer(String name) {
         this.name = name;
      }

      /**
      * Add a playlist to the composer.
      * 
      * @param playlist the playlist to add
      */
      public Playlist addPlaylist(Playlist playlist) {   
         playlists.add(playlist);
         return playlist;
      }

      public String getName() {
         return name;
      }

      /**
      * Return the rockin' playlist for this composer.
      */
      public List<Playlist> getPlaylists() {
         return playlists;
      }
   }

   /**
   * The model that defines the nodes in the tree.
   */
  
   private final List<Composer> composers;

   /**
   * This selection model is shared across all leaf nodes. 
   * A selection model can also be shared across all nodes 
   * in the tree, or each set of child nodes can have 
   * its own instance. This gives you flexibility to 
   * determine how nodes are selected.
   */
   private final SelectionModel<Playlist> selectionModel; 
   //= new SingleSelectionModel<Playlist>();
   private final Cell<Playlist> contactCell;

      public CollectionTreeModel(final SelectionModel<Playlist> selectionModel) {
    	  this.selectionModel = selectionModel;
         // Create a database of information.
         composers = new ArrayList<Composer>();

         // Add compositions by Beethoven.
         {
            Composer beethoven = new Composer("Beethoven");
            composers.add(beethoven);
   
            Playlist concertos = beethoven.addPlaylist(
            new Playlist("Concertos"));
           
            Playlist quartets = beethoven.addPlaylist(
            new Playlist("Quartets"));
            
            Playlist sonatas = beethoven.addPlaylist(
            new Playlist("Sonatas"));
            

            Playlist symphonies = beethoven.addPlaylist(
            new Playlist("Symphonies"));
            
         }

         // Add compositions by Brahms.
         {
            Composer brahms = new Composer("Brahms");
            composers.add(brahms);
            Playlist concertos = brahms.addPlaylist(
            new Playlist("Concertos"));
           
            Playlist quartets = brahms.addPlaylist(
            new Playlist("Quartets"));
          
            Playlist sonatas = brahms.addPlaylist(
            new Playlist("Sonatas"));
          
            Playlist symphonies = brahms.addPlaylist(
            new Playlist("Symphonies"));
          
         }

         // Add compositions by Mozart.
         {
            Composer mozart = new Composer("Mozart");
            composers.add(mozart);
            Playlist concertos = mozart.addPlaylist(
            new Playlist("Concertos"));
           
         }
         //Added code
         List<HasCell<Playlist, ?>> hasCells = new ArrayList<
     	        HasCell<Playlist, ?>>();
         
         //adding top level playlist
     	    hasCells.add(new HasCell<Playlist, Boolean>() {

     	      private CheckboxCell cell = new CheckboxCell(true);

     	      public Cell<Boolean> getCell() {
     	        return cell;
     	      }

     	      public FieldUpdater<Playlist, Boolean> getFieldUpdater() {
     	        return new FieldUpdater<Playlist, Boolean>() {
     	          public void update(int index, Playlist object, Boolean value) {
     	            selectionModel.setSelected(object, value);
     	          }
     	        };
     	      }

     	      public Boolean getValue(Playlist object) {
     	        return selectionModel.isSelected(object);
     	      }
     	    });
     	    
     	  /*  //adding lower level playlist
     	    hasCells.add(new HasCell<Playlist, Playlist>() {

     	      private ContactCell cell = new ContactCell();

     	      public Cell<Playlist> getCell() {
     	        return cell;
     	      }

     	      public FieldUpdater<Playlist, Playlist> getFieldUpdater() {
     	        return null;
     	      }

     	      public Playlist getValue(Playlist object) {
     	        return object;
     	      }
     	    });*/
     	    
     	   hasCells.add(new HasCell<Playlist, Boolean>() {

      	      private CheckboxCell cell = new CheckboxCell(true);

      	      public Cell<Boolean> getCell() {
      	        return cell;
      	      }

      	      public FieldUpdater<Playlist, Boolean> getFieldUpdater() {
      	        return new FieldUpdater<Playlist, Boolean>() {
      	          public void update(int index, Playlist object, Boolean value) {
      	            selectionModel.setSelected(object, value);
      	          }
      	        };
      	      }

      	      public Boolean getValue(Playlist object) {
      	        return selectionModel.isSelected(object);
      	      }
      	    });
     	    
     	    contactCell = new CompositeCell<Playlist>(hasCells) {
     	    	
     	    	@Override
     	    	  public void onBrowserEvent(Context context, Element parent, Playlist value,
     	    	      NativeEvent event, ValueUpdater<Playlist> valueUpdater){
     	    		if ("keyup".equals(event.getType())
     	     	            && event.getKeyCode() == KeyCodes.KEY_ENTER) {
     	     	          selectionModel.setSelected(value, !selectionModel.isSelected(value));
     	     	        }
     	    	}
     	    	
     	    	@Override
     	      public void render(Context context, Playlist value, SafeHtmlBuilder sb) {
     	    		
     	    		sb.appendHtmlConstant("<table><tbody><tr>");	
     	    		super.render(context,value, sb);
     	    		sb.appendHtmlConstant("</tr></tbody></table>");
     	      }

     	      @Override
     	      protected Element getContainerElement(Element parent) {
     	        // Return the first TR element in the table.
     	        return parent.getFirstChildElement().getFirstChildElement().getFirstChildElement();
     	      }

     	      @Override
     	      protected <X> void render(Context context, Playlist value,
     	    	      SafeHtmlBuilder sb, HasCell<Playlist, X> hasCell) {
     	        Cell<X> cell = hasCell.getCell();
     	        sb.appendHtmlConstant("<td>");
     	        cell.render(context, hasCell.getValue(value), sb);
     	        sb.appendHtmlConstant("</td>");
     	      }
     	    };
     	  }

      

      

      /**
      * Get the {@link NodeInfo} that provides the children of the 
      * specified value.
      */
      public <T> NodeInfo<?> getNodeInfo(T value) {
         if (value == null) {
            
            ListDataProvider<Composer> dataProvider 
            = new ListDataProvider<Composer>(
            composers);

            // Create a cell to display a composer.
            Cell<Composer> cell 
            = new AbstractCell<Composer>() {
               @Override
               public void render(com.google.gwt.cell.client.Cell.Context context,Composer value,
               SafeHtmlBuilder sb) {
                  if (value != null) {
                	 sb.appendHtmlConstant("  <input type=\"checkbox\">   "); 
                     sb.appendEscaped(value.getName());
                  }				
               }
            };

            // Return a node info that pairs type filter textthe data provider and the cell.
            return new DefaultNodeInfo<Composer>(dataProvider, cell);
         } else if (value instanceof Composer) {
            // LEVEL 1.
            // We want the children of the composer. Return the playlists.
            ListDataProvider<Playlist> dataProvider
            = new ListDataProvider<Playlist>(
            ((Composer) value).getPlaylists());
            
            
            Cell<Playlist> cell = contactCell;//new ContactCell();
        /*    new AbstractCell<Playlist>() {
            	
            @Override
               public void render(Cell.Context context,Playlist value,
               SafeHtmlBuilder sb) {
                  if (value != null) {
                	 sb.appendHtmlConstant(" <input type=\"checkbox\" >    "); 
                     sb.appendEscaped(value.getName());
                  }
               }

            };*/
            
           
            return new DefaultNodeInfo<Playlist>(dataProvider, cell,selectionModel,null);
         
        } /*else if (value instanceof Playlist) {
            // LEVEL 2 - LEAF.
            // We want the children of the playlist. Return the songs.
            ListDataProvider<String> dataProvider 
            = new ListDataProvider<String>(
            ((Playlist) value).getSongs());

            // Use the shared selection model.
            return new DefaultNodeInfo<String>(dataProvider, new TextCell(),
            selectionModel, null);
         }*/
         return null;
      }

      /**
      * Check if the specified value represents a leaf node. 
      * Leaf nodes cannot be opened.
      */
      public boolean isLeaf(Object value) {
      // The leaf nodes are the songs, which are Strings.
      if (value instanceof Playlist) {
         return true;
      }
      return false;
      }
      
   
   }

