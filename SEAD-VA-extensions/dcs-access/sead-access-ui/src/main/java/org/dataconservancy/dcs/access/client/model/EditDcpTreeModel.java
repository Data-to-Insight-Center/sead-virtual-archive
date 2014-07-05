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

package org.dataconservancy.dcs.access.client.model;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.ui.EditPopupPanel;
import org.dataconservancy.dcs.access.client.ui.Icons;

import java.util.*;

public class EditDcpTreeModel implements TreeViewModel {
	// entity id -> kids
	// null -> collections
    interface Templates extends SafeHtmlTemplates {
        /**
         * The template for this Cell, which includes styles and a value.
         * 
         * @param styles
         *            the styles to include in the style attribute of the div
         * @param value
         *            the safe value. Since the value type is {@link com.google.gwt.safehtml.shared.SafeHtml},
         *            it will not be escaped before including it in the
         *            template. Alternatively, you could make the value type
         *            String, in which case the value would be escaped.
         * @return a {@link com.google.gwt.safehtml.shared.SafeHtml} instance
         */
        @Template("<div name=\"{0}\" style=\"{1}\">{2}</div>")
        SafeHtml cell(String name, SafeStyles styles, SafeHtml value);
}

	private static Templates templates = GWT.create(Templates.class);
	private static final SafeHtml ICON_FOLDER = makeImage(Icons.RESOURCES
            .tree_icon_collection_closed());
	private static final SafeHtml ICON_FILE = makeImage(Icons.RESOURCES
            .tree_icon_item());

	private static SafeHtml makeImage(ImageResource resource) {
        AbstractImagePrototype proto = AbstractImagePrototype.create(resource);
        return proto.getSafeHtml();
    }
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

	JsDcp dcp;
	HashMap<String, JsFile> filemap;
	String sipPath;

	public EditDcpTreeModel(JsDcp dcp, String sipPath) {
		this.dcp = dcp;
		this.kidmap = new HashMap<String, List<JsEntity>>();
		this.sipPath = sipPath;

		// File id -> file entity
		filemap = new HashMap<String, JsFile>();

		for (int i = 0; i < dcp.getFiles().length(); i++) {
			JsFile file = dcp.getFiles().get(i);

			filemap.put(file.getId(), file);
		}

		List<JsEntity> top = new ArrayList<JsEntity>();//dcp.getCollections()
				//.length());

		/*for (int i = 0; i < dcp.getCollections().length(); i++) {
			JsCollection col = dcp.getCollections().get(i);
			top.add(col);

			add_kids(filemap, col.getMetadataRefs(), col.getId());
		}*/

		// Put dus in parents
		for (int i = 0; i < dcp.getDeliverableUnits().length(); i++) {
			JsDeliverableUnit du = dcp.getDeliverableUnits().get(i);

			add_kid(du, du.getCollections());
			add_kid(du, du.getParents());
			add_kid(du, du.getMetadataRefs());

			// TODO Hack for no collections
			//if (dcp.getCollections().length() == 0) {
				top.add(du);
		//	}

			add_kids(filemap, du.getMetadataRefs(), du.getId());
		}

		for (int i = 0; i < dcp.getFiles().length(); i++) {
			JsFile file = dcp.getFiles().get(i);

			add_kids(filemap, file.getMetadataRefs(), file.getId());
		}

		// Put mans in parents and files in mans
		for (int i = 0; i < dcp.getManifestations().length(); i++) {
			JsManifestation man = dcp.getManifestations().get(i);

//			add_kid(man, man.getDeliverableUnit());

			add_kids(filemap, man.getMetadataRefs(), man.getId());

			for (int j = 0; j < man.getManifestationFiles().length(); j++) {
				JsManifestationFile mf = man.getManifestationFiles().get(j);

				JsEntity file = filemap.get(mf.getRef());

				if (file != null) {
					add_kid(file, man.getDeliverableUnit());
				}
			}

			// TODO Hack for no collections and no du
			if (//dcp.getCollections().length() == 0
					//&&
					dcp.getDeliverableUnits().length() == 0) {
				top.add(man);
			}
		}

		// Put events in targets
		for (int i = 0; i < dcp.getEvents().length(); i++) {
			JsEvent event = dcp.getEvents().get(i);

			add_kid(event, event.getTargets());
		}

		kidmap.put(null, top);

		// Sort lists

		for (List<JsEntity> kids : kidmap.values()) {
			Collections.sort(kids, new Comparator<JsEntity>() {
				public int compare(JsEntity e1, JsEntity e2) {
					return sort_key(e1).compareTo(sort_key(e2));
				}
			});
		}
	}

	public <T> NodeInfo<?> getNodeInfo(T value) {
		if (value == null) {
			// root
			ListDataProvider<JsEntity> ldp = new ListDataProvider<JsEntity>(
					kidmap.get(null));
			return new DefaultNodeInfo<JsEntity>(ldp, new JsEntityCell(this.dcp, this.sipPath));
		} else if (value instanceof JsEntity) {
			ListDataProvider<JsEntity> ldp = new ListDataProvider<JsEntity>(
					kidmap.get(((JsEntity) value).getId()));
			return new DefaultNodeInfo<JsEntity>(ldp, new JsEntityCell(this.dcp, this.sipPath));
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
			typename = "Deliverable Unit";
			summary = ((JsDeliverableUnit) entity).summary();
		} else if (type.equals("file")) {
			typename = "File";
			summary = ((JsFile) entity).summary();
		} else if (type.equals("manifestation")) {
			typename = "Manifestation";
			summary = ((JsManifestation) entity).summary();
		} else if (type.equals("event")) {
			typename = "Event";
			summary = ((JsEvent) entity).summary();
		} else {
			typename = "Unknown type";
			summary = "unknown entity";
		}

		return typename + ": " + summary;
	}

	public static class JsEntityCell extends AbstractCell<JsEntity> {

		JsDcp dcp;
		String sipath;
		public JsEntityCell(JsDcp dcp, String sipath){
			super("click", "keydown");
			this.dcp = dcp;
			this.sipath = sipath;
		}
		public void render(Context context,
				JsEntity value, SafeHtmlBuilder sb) {
			String type = value.getEntityType();
			String summary;
			String typename;

			if (type.equals("deliverableUnit")) {
				typename = "Deliverable Unit";
				summary = ((JsDeliverableUnit) value).summary();
			}  else if (type.equals("file")) {
				typename = "File";
				summary = ((JsFile) value).summary();
			} else if (type.equals("manifestation")) {
				typename = "Manifestation";
				summary = ((JsManifestation) value).summary();
			} else if (type.equals("event")) {
				typename = "Event";
				summary = ((JsEvent) value).summary();
			} else {
				typename = "Unknown type";
				summary = "unknown entity";
			}

			//sb.appendEscaped(typename + ": ");

			String url = "#" + SeadState.ENTITY.toToken(value.getId());
			String link = //" <a href='" 
//			+ URL.encode(url)
			//+ "'>"
//					+ 
			SafeHtmlUtils.htmlEscape(summary)
					//+ "</a>"
			;
			SafeStyles imgStyle = SafeStylesUtils
                    .fromTrustedString("float:left;cursor:hand;cursor:pointer;");
			if(type.equals("deliverableUnit")){
				SafeHtml rendered = templates.cell("ICON_FOLDER", imgStyle, ICON_FOLDER);
	            sb.append(rendered);
			}
			else if (type.equals("file")) {
				SafeHtml rendered = templates.cell("ICON_FILE", imgStyle, ICON_FILE);
	            sb.append(rendered);
			}

			sb.append(SafeHtmlUtils.fromTrustedString(link));
		}
		 @Override
         public void onBrowserEvent(Context context, Element parent, JsEntity value,
             NativeEvent event, ValueUpdater<JsEntity> valueUpdater) {
           super.onBrowserEvent(context, parent, value, event, valueUpdater);
           
           String type = value.getEntityType();
           if ("click".equals(event.getType())) {
    		   EditPopupPanel editPopupPanel = new EditPopupPanel(value, this, this.dcp, this.sipath, type);
    		   editPopupPanel.show();
           }
         }
	}
}