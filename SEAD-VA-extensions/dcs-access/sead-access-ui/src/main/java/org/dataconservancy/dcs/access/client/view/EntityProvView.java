package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EntityProvView extends Composite implements org.dataconservancy.dcs.access.client.presenter.EntityProvPresenter.Display{

	VerticalPanel activityContainer;
	
	String entityId;
	public EntityProvView(String roId) {
		entityId = roId;
		activityContainer =  new VerticalPanel();
		activityContainer.setWidth("70%");
		activityContainer.setStyleName("ActivityPanel");
	}

	@Override
	public VerticalPanel getActivityContainer() {
		return activityContainer;
	}

	@Override
	public String getEntityId() {
		return entityId;
	}
}
