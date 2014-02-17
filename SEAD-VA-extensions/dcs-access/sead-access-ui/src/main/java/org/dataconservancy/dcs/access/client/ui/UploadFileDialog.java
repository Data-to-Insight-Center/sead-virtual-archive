package org.dataconservancy.dcs.access.client.ui;

import java.util.List;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;
import org.dataconservancy.dcs.access.client.upload.FileEditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.TabPanel;

public class UploadFileDialog {

	DialogBox dBox;
	
	public UploadFileDialog(final TabPanel files, final List<String> fileids, String fileUploadUrl)
	{
		dBox = new DialogBox(false, true);

		
		Panel panel = new FlowPanel();

        dBox.setAnimationEnabled(true);
        dBox.setText("Upload local file");
        dBox.setWidget(panel);
        dBox.center();

        final HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(5);

        Button upload = new Button("Upload");
        Button cancel = new Button("Cancel");

        buttons.add(upload);
        buttons.add(cancel);

        final FormPanel form = new FormPanel();
        FlowPanel formcontents = new FlowPanel();
        form.add(formcontents);

        final FileUpload upfile = new FileUpload();
        upfile.setName("file");

        Hidden depositurl = new Hidden("depositurl");
        depositurl.setValue(fileUploadUrl);
        		//depositConfig.fileUploadUrl());

        formcontents.add(upfile);
        formcontents.add(depositurl);

        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setAction(SeadApp.FILE_UPLOAD_URL);

        panel.add(new Label("Uploaded files will be included in the SIP."));
        panel.add(form);
        panel.add(buttons);

        upload.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                form.submit();
            }
        });

        cancel.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                dBox.hide();
            }
        });

        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                if (event.getResults() == null) {
                    Window.alert("File upload failed");
                    dBox.hide();
                    return;
                }

                String[] parts = event.getResults().split("\\^");

                if (parts.length != 4) {
                    Window.alert("File upload failed: " + event.getResults());
                    dBox.hide();
                    return;
                }

                String filesrc = parts[1].trim();
                files.setVisible(true);
                String id = nextFileId();
                fileids.add(id);
                files
                        .add(new FileEditor(id, upfile.getFilename().replace("C:\\fakepath\\", ""), filesrc),
                             id);
                files.selectTab(files.getWidgetCount() - 1);

                dBox.hide();

            }
        });
	}
	
	 private String nextFileId() {
	        return "file" + ++SeadApp.fileseq;
	    } 

	   
	  
}
