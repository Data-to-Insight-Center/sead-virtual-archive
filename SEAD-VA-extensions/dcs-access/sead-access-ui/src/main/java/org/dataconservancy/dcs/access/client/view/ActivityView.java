package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ActivityView extends Composite implements org.dataconservancy.dcs.access.client.presenter.ActivityPresenter.Display{

	VerticalPanel activityContainer;
	
	public ActivityView() {
		activityContainer =  new VerticalPanel();
		activityContainer.setWidth("70%");
		activityContainer.setStyleName("ActivityPanel");
		
	}


	@Override
	public VerticalPanel getActivityContainer() {
		return activityContainer;
		
	}
}
