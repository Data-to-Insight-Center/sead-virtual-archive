package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CuratorView extends Composite implements org.dataconservancy.dcs.access.client.presenter.CuratorViewPresenter.Display{

	VerticalPanel publishContainer;
	CaptionPanel projectDesciptionPanel;
	CaptionPanel researchObjectPanel;
	CaptionPanel licensePanel;
	
	ListBox projectList;
	ListBox ROList;
	
	
	
	public CuratorView() {
		publishContainer =  new VerticalPanel();
		
	}

	@Override
	public VerticalPanel getPublishContainer() {
		return publishContainer;
		
	}
}
