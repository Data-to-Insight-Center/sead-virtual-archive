package org.dataconservancy.dcs.access.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsDeliverableUnit;
import org.dataconservancy.dcs.access.client.model.JsEntity;
import org.dataconservancy.dcs.access.client.model.JsFile;
import org.dataconservancy.dcs.access.ui.client.model.JsManifestationFile;
import org.dataconservancy.dcs.access.ui.client.model.JsManifestation;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

public class FileTree implements TreeViewModel {
	// entity id -> kids
	// null -> collections
	private final Map<String, List<JsEntity>> kidmap;

	private void add_kid(JsEntity kid, JsArrayString parents) {
		for (int i = 0; i < parents.length(); i++) {
			String parent_id = parents.get(i);

			add_kid(kid, parent_id);
		}
	}

	private void add_kid(JsEntity kid, String parent_id) {
		List<JsEntity> kids = kidmap.get(parent_id);

		if (kids == null) {
			kids = new ArrayList<JsEntity>();

			kidmap.put(parent_id, kids);
		}

		kids.add(kid);
	}

	private void add_kids(HashMap<String, JsFile> map, JsArrayString kids,
			String parent_id) {
		for (int i = 0; i < kids.length(); i++) {
			String kid_id = kids.get(i);
			JsEntity kid = map.get(kid_id);

			if (kid != null) {
				add_kid(kid, parent_id);
			}
		}
	}

	public FileTree(JsDcp dcp) {
		this.kidmap = new HashMap<String, List<JsEntity>>();

		// File id -> file entity
		HashMap<String, JsFile> filemap = new HashMap<String, JsFile>();

		for (int i = 0; i < dcp.getFiles().length(); i++) {
			JsFile file = dcp.getFiles().get(i);

			filemap.put(file.getId(), file);
		}

		List<JsEntity> top = new ArrayList<JsEntity>();
		List<JsEntity> allParentDus = new ArrayList<JsEntity>();
		String topDuId = getTopDu(dcp);
		//dcp.getCollections()
			//	.length());

	/*	for (int i = 0; i < dcp.getCollections().length(); i++) {
			JsCollection col = dcp.getCollections().get(i);
			top.add(col);

			add_kids(filemap, col.getMetadataRefs(), col.getId());
		}*/

		// Put dus in parents
		for (int i = 0; i < dcp.getDeliverableUnits().length(); i++) {
			JsDeliverableUnit du = dcp.getDeliverableUnits().get(i);

			//add_kid(du, du.getCollections());
			if(du.getId().equalsIgnoreCase(topDuId))
				continue;
				
				
			add_kid(du, du.getParents());
			add_kid(du, du.getMetadataRefs());
			
			// TODO Hack for no collections
			if (dcp.getCollections().length() == 0) {
				top.add(du);
			}

			add_kids(filemap, du.getMetadataRefs(), du.getId());
		}

		for (int i = 0; i < dcp.getFiles().length(); i++) {
			JsFile file = dcp.getFiles().get(i);

			add_kids(filemap, file.getMetadataRefs(), file.getId());
		}

		// Put mans in parents and files in mans
		for (int i = 0; i < dcp.getManifestations().length(); i++) {
			JsManifestation man = dcp.getManifestations().get(i);

			//add_kid(man, man.getDeliverableUnit());

			add_kids(filemap, man.getMetadataRefs(), man.getId());

			for (int j = 0; j < man.getManifestationFiles().length(); j++) {
				JsManifestationFile mf = man.getManifestationFiles().get(j);

				JsEntity file = filemap.get(mf.getRef());

				if (file != null) {
					//if(man.getDeliverableUnit().equalsIgnoreCase(topDuId))
						top.add(file);
//					else
//						add_kid(file, man.getDeliverableUnit());//man.getId());
				}
			}
			
			
			/*if (dcp.getDeliverableUnits().length() == 0) {
				top.add(man);
			}*/
		}
		
		for (int i = 0; i < dcp.getFiles().length(); i++) {
			
				JsEntity file = filemap.get(dcp.getFiles().get(i).getId());

				if (file != null) {
					top.add(file);
				}
		}

		// Put events in targets
	/*	for (int i = 0; i < dcp.getEvents().length(); i++) {
			JsEvent event = dcp.getEvents().get(i);

			add_kid(event, event.getTargets());
		}*/

		kidmap.put(null, top);

		// Sort lists

		
		for (List<JsEntity> kids : kidmap.values()) {
			Collections.sort(kids, new Comparator<JsEntity>() {
				public int compare(JsEntity e1, JsEntity e2) {
					return sort_key(e1).compareTo(sort_key(e2));
				}
			});
		}
		

		// Put files in parents and files in mans
		for (int i = 0; i < dcp.getFiles().length(); i++) {
			JsFile file = dcp.getFiles().get(i);
			add_kid(file, file.getParent());//man.getId());
		}

	/*	int i=0;
		for (List<JsEntity> kids : kidmap.values()) {
			JsEntity tempEntity = kids.get(0);
			kids.set(0, tempEntity);
			i=1;
		}*/
		
	}

	private String getTopDu(JsDcp dcp) {
		HashMap<String,JsArrayString> parentDus = new HashMap<String,JsArrayString>();
		HashMap<String,String> allDus = new HashMap<String,String>();
		for (int i = 0; i < dcp.getDeliverableUnits().length(); i++) {
			JsDeliverableUnit du = dcp.getDeliverableUnits().get(i);
			parentDus.put(du.getId(), du.getParents());
			allDus.put(du.getId(), "exists");
		}
		
	 	Iterator it = parentDus.entrySet().iterator();
	   	   while (it.hasNext()) {
	   	       Map.Entry pairs = (Map.Entry)it.next();
	   	       JsArrayString parents = (JsArrayString)pairs.getValue();
	   	       int noParent=0;
	   	       for(int i=0;i<parents.length();i++){
	   	    	   if(allDus.get(parents.get(i))!=null){
	   	    		   noParent=1;
	   	    		   break;
	   	    	   }
	   	       }
	   	       if(noParent==0)
	   	    	   return (String)pairs.getKey();
	   	   }
		return null;
	}

	public <T> NodeInfo<?> getNodeInfo(T value) {
		if (value == null) {
			// root
			ListDataProvider<JsEntity> ldp = new ListDataProvider<JsEntity>(
					kidmap.get(null));
			return new DefaultNodeInfo<JsEntity>(ldp, new JsEntityCell());
		} else if (value instanceof JsEntity) {
			ListDataProvider<JsEntity> ldp = new ListDataProvider<JsEntity>(
					kidmap.get(((JsEntity) value).getId()));
			return new DefaultNodeInfo<JsEntity>(ldp, new JsEntityCell());
		}

		return null;
	}

	public boolean isLeaf(Object value) {
		JsEntity entity = (JsEntity) value;

		return entity != null && kidmap.get(entity.getId()) == null;
	}

	// Produce a sort key representative of entity cell display 
	private static String sort_key(JsEntity entity) {
		String type = entity.getEntityType();
		String summary;
		String typename;

		if (type.equals("deliverableUnit")) {
			typename = "Collection";
			summary = ((JsDeliverableUnit) entity).summary();
		}/* else if (type.equals("collection")) {
			typename = "Collection";
			summary = ((JsCollection) entity).summary();
		}*/ else if (type.equals("file")) {
			typename = "File";
			summary = ((JsFile) entity).summary();
		}/* else if (type.equals("manifestation")) {
			typename = "Manifestation";
			summary = ((JsManifestation) entity).summary();
		} else if (type.equals("event")) {
			typename = "Event";
			summary = ((JsEvent) entity).summary();
		}*/ else {
			typename = "Unknown type";
			summary = "unknown entity";
		}

		return typename + ": " + summary;
	}

	private static class JsEntityCell extends AbstractCell<JsEntity> {
		public void render(com.google.gwt.cell.client.Cell.Context context,
				JsEntity value, SafeHtmlBuilder sb) {
			String type = value.getEntityType();
			String summary;
			String typename;

			if (type.equals("deliverableUnit")) {
				typename = "Sub-Collection";
				summary = ((JsDeliverableUnit) value).summary();
			} /*else if (type.equals("collection")) {
				typename = "Collection";
				summary = ((JsCollection) value).summary();
			}*/ else if (type.equals("file")) {
				typename = "File";
				summary = ((JsFile) value).summary();
			}/* else if (type.equals("manifestation")) {
				typename = "Manifestation";
				summary = ((JsManifestation) value).summary();
			} else if (type.equals("event")) {
				typename = "Event";
				summary = ((JsEvent) value).summary();
			} */else {
				typename = "Unknown type";
				summary = "unknown entity";
			}

			sb.append(SafeHtmlUtils.fromString(typename + ": "));

			String url = "#" + SeadState.ENTITY.toToken(value.getId());
			String link = " <a href='" + URL.encode(url) + "'>"
					+ SafeHtmlUtils.htmlEscape(summary) + "</a>";

			sb.append(SafeHtmlUtils.fromTrustedString(link));
		}
	}
	
	private static class JsParentDu extends AbstractCell<JsEntity> {
		public void render(com.google.gwt.cell.client.Cell.Context context,
				JsEntity value, SafeHtmlBuilder sb) {
			String type = value.getEntityType();
			String typename = "Collection";
			sb.appendEscaped(typename);
		}
	}
}
