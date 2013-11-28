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
package org.dataconservancy.dcs.access.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.ui.client.model.JsCollection;
import org.dataconservancy.dcs.access.ui.client.model.JsDcp;
import org.dataconservancy.dcs.access.ui.client.model.JsDeliverableUnit;
import org.dataconservancy.dcs.access.ui.client.model.JsEntity;
import org.dataconservancy.dcs.access.ui.client.model.JsEvent;
import org.dataconservancy.dcs.access.ui.client.model.JsFile;
import org.dataconservancy.dcs.access.ui.client.model.JsManifestation;
import org.dataconservancy.dcs.access.ui.client.model.JsManifestationFile;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

public class DcpTreeModel implements TreeViewModel {
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

	public DcpTreeModel(JsDcp dcp) {
		this.kidmap = new HashMap<String, List<JsEntity>>();

		// File id -> file entity
		HashMap<String, JsFile> filemap = new HashMap<String, JsFile>();

		for (int i = 0; i < dcp.getFiles().length(); i++) {
			JsFile file = dcp.getFiles().get(i);

			filemap.put(file.getId(), file);
		}

		List<JsEntity> top = new ArrayList<JsEntity>(dcp.getCollections()
				.length());

		for (int i = 0; i < dcp.getCollections().length(); i++) {
			JsCollection col = dcp.getCollections().get(i);
			top.add(col);

			add_kids(filemap, col.getMetadataRefs(), col.getId());
		}

		// Put dus in parents
		for (int i = 0; i < dcp.getDeliverableUnits().length(); i++) {
			JsDeliverableUnit du = dcp.getDeliverableUnits().get(i);

			add_kid(du, du.getCollections());
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

			add_kid(man, man.getDeliverableUnit());

			add_kids(filemap, man.getMetadataRefs(), man.getId());

			for (int j = 0; j < man.getManifestationFiles().length(); j++) {
				JsManifestationFile mf = man.getManifestationFiles().get(j);

				JsEntity file = filemap.get(mf.getRef());

				if (file != null) {
					add_kid(file, man.getId());
				}
			}

			// TODO Hack for no collections and no du
			if (dcp.getCollections().length() == 0
					&& dcp.getDeliverableUnits().length() == 0) {
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
			typename = "Deliverable Unit";
			summary = ((JsDeliverableUnit) entity).summary();
		} else if (type.equals("collection")) {
			typename = "Collection";
			summary = ((JsCollection) entity).summary();
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

	private static class JsEntityCell extends AbstractCell<JsEntity> {
		public void render(com.google.gwt.cell.client.Cell.Context context,
				JsEntity value, SafeHtmlBuilder sb) {
			String type = value.getEntityType();
			String summary;
			String typename;

			if (type.equals("deliverableUnit")) {
				typename = "Deliverable Unit";
				summary = ((JsDeliverableUnit) value).summary();
			} else if (type.equals("collection")) {
				typename = "Collection";
				summary = ((JsCollection) value).summary();
			} else if (type.equals("file")) {
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

			sb.appendEscaped(typename + ": ");

			String url = "#" + State.ENTITY.toToken(value.getId());
			String link = " <a href='" + URL.encode(url) + "'>"
					+ SafeHtmlUtils.htmlEscape(summary) + "</a>";

			sb.append(SafeHtmlUtils.fromTrustedString(link));
		}
	}
}
