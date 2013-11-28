package org.dataconservancy.dcs.access.client.ui;

import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsDeliverableUnit;
import org.dataconservancy.dcs.access.client.model.JsFile;
import org.dataconservancy.dcs.access.ui.client.model.JsCollection;
import org.dataconservancy.dcs.access.ui.client.model.JsEvent;
import org.dataconservancy.dcs.access.ui.client.model.JsManifestation;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DcpDisplay extends Composite{

	VerticalPanel panel;
	

	public DcpDisplay(JsDcp dcp) {
        
		panel = new VerticalPanel();
		initWidget(panel);

        int size = dcp.size();

        if (dcp.getCollections().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Collections", "SectionHeader"));
            }

            
            JsCollection.display(panel, dcp.getCollections());
        }

        if (dcp.getDeliverableUnits().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Deliverable Units", "SectionHeader"));
            }

            JsDeliverableUnit.display(panel, dcp.getDeliverableUnits(),null);
        }

        if (dcp.getManifestations().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Manifestations", "SectionHeader"));
            }

            JsManifestation.display(panel, dcp.getManifestations());
        }

        if (dcp.getFiles().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Files", "SectionHeader"));
            }

            JsFile.display(panel, dcp.getFiles());
        }

        if (dcp.getEvents().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Events", "SectionHeader"));
            }

            JsEvent.display(panel, dcp.getEvents());
        }
;
    }
}
