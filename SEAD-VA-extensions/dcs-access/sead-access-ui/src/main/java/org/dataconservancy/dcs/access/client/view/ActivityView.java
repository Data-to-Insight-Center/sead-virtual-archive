package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ActivityView extends Composite implements org.dataconservancy.dcs.access.client.presenter.ActivityPresenter.Display{

    VerticalPanel activityContainer;

    public ActivityView() {
        activityContainer =  new VerticalPanel();
        activityContainer.setWidth("70%");
        activityContainer.setStyleName("ActivityPanel");
        Image loadImage =  new Image("images/loading.gif");
        final int height = Window.getClientHeight();
        final int width = Window.getClientWidth();
        loadImage.getElement().getStyle().setProperty("marginTop",String.valueOf((height/4))+"px");
        loadImage.getElement().getStyle().setProperty("marginLeft",String.valueOf(width/4)+"px");

        activityContainer.add(loadImage);

    }


    @Override
    public VerticalPanel getActivityContainer() {
        return activityContainer;

    }
}
