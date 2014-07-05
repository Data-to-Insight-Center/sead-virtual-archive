package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
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
        Image loadImage =  new Image("images/loading.gif");
        final int height = Window.getClientHeight();
        final int width = Window.getClientWidth();
        loadImage.getElement().getStyle().setProperty("marginTop",String.valueOf((height/4))+"px");
        loadImage.getElement().getStyle().setProperty("marginLeft",String.valueOf((width/4)-20)+"px");

        publishContainer.add(loadImage);

    }

    @Override
    public VerticalPanel getPublishContainer() {
        return publishContainer;

    }
}
