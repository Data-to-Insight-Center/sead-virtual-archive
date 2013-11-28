package org.dataconservancy.dcs.access.client.model;

import org.dataconservancy.dcs.access.ui.client.model.JsModel;

public final class JsAlternateId
        extends JsModel{

    protected JsAlternateId() {
    }

    public String getIdValue() {
        return getString("idValue");
    }
    
    public String getTypeId() {
        return getString("typeId");
    }

}
