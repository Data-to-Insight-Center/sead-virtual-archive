package org.dataconservancy.dcs.access.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.DepositService;
import org.dataconservancy.dcs.access.client.api.DepositServiceAsync;
import org.dataconservancy.dcs.access.client.api.MediciService;
import org.dataconservancy.dcs.access.client.api.MediciServiceAsync;
import org.dataconservancy.dcs.access.client.ui.UploadBagDialog;
import org.dataconservancy.dcs.access.client.ui.UploadCollectionDialog;
import org.dataconservancy.dcs.access.client.ui.UploadFileDialog;
import org.dataconservancy.dcs.access.client.upload.CollectionEditor;
import org.dataconservancy.dcs.access.client.upload.DeliverableUnitEditor;
import org.dataconservancy.dcs.access.client.upload.FileEditor;
import org.dataconservancy.dcs.access.client.upload.model.Package;
import org.dataconservancy.dcs.access.client.upload.model.Repository;
import org.dataconservancy.dcs.access.shared.MediciInstance;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class UploadView extends Composite implements org.dataconservancy.dcs.access.client.presenter.UploadPresenter.Display{
	
	TabPanel uploadPanel;
	Panel filesTab;
	Panel deliverablesTab;
	Panel submitTab;
	
	TabPanel collections;
    TabPanel files;
    TabPanel dus;
    
    public static final DepositServiceAsync deposit =
            GWT.create(DepositService.class);
    
    ArrayList<String> fileids, colids, duids;
    int colseq;
	int fileseq;
	int duseq;
	
	public UploadView()
	{
		uploadPanel = new TabPanel();
		uploadPanel.setWidth("100%");
		
		//if(Application.session!=null)
			//if(Application.session.getAttribute("email")!=null)
			//{
			filesTab = new FlowPanel();
			deliverablesTab = new FlowPanel();
			submitTab = new FlowPanel();
			fileids = new ArrayList<String>();
			colids = new ArrayList<String>();
			duids = new ArrayList<String>();
			
			files = new TabPanel();
			collections = new TabPanel();
			dus = new TabPanel();
			
			uploadPanel.add(filesTab,"Files");
			uploadPanel.add(deliverablesTab,"Deliverables");
			uploadPanel.add(submitTab,"Submission");
	
			uploadPanel.addSelectionHandler(new SelectionHandler<Integer>(){
	        	  public void onSelection(SelectionEvent<Integer> event) {
	        	    if (event.getSelectedItem() == 0) {
	        	    	loadFilesTab();
	        	    }
	        	    else if (event.getSelectedItem() == 1) {
	        	    	viewDeliverableUnits();
	        	    	
	          	    }
	        	    else if (event.getSelectedItem() == 2) {
	        	    	viewSubmission();
	          	    }
	        	  }
	        	});
			//}
			uploadPanel.selectTab(0);
	}
	
	 void loadFilesTab() {
		 
	    	
		filesTab.clear();

        Label explain =
                new Label("Upload local files for inclusion to the SIP. Remote files accessible by a URL may also be added. Files with the same technical enviroment in the same Deliverable Unit will be put in the same Manifestation.");
        explain.setStylePrimaryName("Explanation");

        filesTab.add(explain);

        if (files.getWidgetCount() > 0) {
        	files.selectTab(0);
        } else {
        	files.setVisible(false);
        }

        Button upload = new Button("Upload local file");
        Button uploadremote = new Button("Add remote file");
        Button uploadMediciFile = new Button("Add Medici file");
        Button uploadMediciCollection = new Button("Add Medici Collection");
        Button getPubCollection = new Button("Get Pub Collection");
        Button uploadBag = new Button("Upload bag");
        
        Button remove = new Button("Remove");
        

        final HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(5);
        buttons.add(upload);
        buttons.add(uploadremote);
  //      buttons.add(uploadMediciFile);
  //      buttons.add(uploadMediciCollection);
        buttons.add(getPubCollection);
        buttons.add(uploadBag);
        
        buttons.add(remove);
        
        filesTab.add(files);
        filesTab.add(buttons);
        
        uploadBag.addClickHandler(new ClickHandler() {

	        public void onClick(ClickEvent event) {
	            new UploadBagDialog(SeadApp.bagIturl);
	        }
        });

        remove.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                int tab = files.getTabBar().getSelectedTab();

                if (tab != -1) {
                	files.remove(tab);
                	fileids.remove(tab);

                    if (files.getWidgetCount() > 0) {
                    	files.selectTab(0);
                    } else {
                    	files.setVisible(false);
                    }
                }
            }
        });

        uploadremote.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
            	files.setVisible(true);
                String id = nextFileId();
                fileids.add(id);
                files.add(new FileEditor(id, "name", "http://"), id);
                files.selectTab(files.getWidgetCount() - 1);
            }
        });
        
            upload.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                new UploadFileDialog(files,fileids,SeadApp.deposit_endpoint + "file");
            }
        });
            
            uploadMediciFile.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    displayEnterFileDialog();
                }
            });
            
            uploadMediciCollection.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent event) {
	              //  new UploadCollectionDialog(files,dus, fileids, duids,colids, Application.deposit_endpoint + "file");
	            	displayEnterCollectionDialog(files,dus, fileids, duids,colids, SeadApp.deposit_endpoint + "file");
	            }
	        });
	           
	 }
	 
	 
	 
	 void viewDeliverableUnits() {
	        deliverablesTab.clear();

	        deliverablesTab
	                .add(Util
	                        .label("A Deliverable Unit is a semantically meaningful container for files and metadata.",
	                               "Explanation"));
	     
	        deliverablesTab.add(dus);

	        HorizontalPanel toolbar = new HorizontalPanel();

	        Button remove = new Button("Remove Deliverable Unit");

	        Button add = new Button("Add Deliverable Unit");

	        toolbar.add(add);
	        toolbar.add(remove);
	        toolbar.setSpacing(5);

	        deliverablesTab.add(toolbar);

	        if (dus.getWidgetCount() > 0) {
	        	dus.selectTab(0);
	        } else {
	        	dus.setVisible(false);
	        }

	        remove.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent arg0) {

	                int tab = dus.getTabBar().getSelectedTab();

	                if (tab != -1) {
	                	dus.remove(tab);
	                	duids.remove(tab);

	                    if (dus.getWidgetCount() > 0) {
	                    	dus.selectTab(0);
	                    } else {
	                    	dus.setVisible(false);
	                    }
	                }
	            }
	        });

	        add.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent arg0) {
	            	dus.setVisible(true);

	                String id = nextDeliverableUnitId();
	                duids.add(id);
	                dus.add(new DeliverableUnitEditor(id, fileids, duids, colids,null),
	                        id);
	                dus.selectTab(dus.getWidgetCount() - 1);
	            }
	        });
	    }

	 ListBox ir ;
	 CheckBox cloudCopy;
	 void viewSubmission() {
		 	submitTab.clear();

	        Label explain =
	                new Label("Constructs a SIP and submits it to the DCS. A successful submission will return an atom feed of the ingestion status.");
	        explain.setStylePrimaryName("Explanation");

	        submitTab.add(explain);
	        
	        submitTab.add(new Label("Select Repository"));
	        
	        ir = new ListBox();
	        
	        ir.addItem("IU Scholarworks");
	        ir.addItem("UIUC IDeals");
	        ir.addItem("Local Dspace Instance");
	       
	        ir.setVisibleItemCount(3);
	        ir.setItemSelected(0, true);
	        submitTab.add(ir);
	        
	        cloudCopy = new CheckBox();
	        
	        cloudCopy.setText("Keep a copy in cloud (S3)");
	        cloudCopy.setValue(false);
	        
	        submitTab.add(cloudCopy);
	        
	        

	        Button submit = new Button("Submit SIP");

	        final FlowPanel status = new FlowPanel();

	        status.setStylePrimaryName("SubmissionStatus");
	        
	       
	        submitTab.add(status);
	        submitTab.add(submit);


	        submit.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent arg0) {
	                status.clear();
	                status.add(new Label("Submitting SIP to: "
	                        + 
	                		//depositConfig.sipDepositUrl() 
	                        SeadApp.deposit_endpoint + "sip"
	                        + " ..."));

	                Package pkg = constructPackage();

	                deposit.submitSIP(SeadApp.deposit_endpoint + "sip",
	                                  "",
	                                  "",
	                                  pkg,
	                                  new AsyncCallback<String>() {

	                                      public void onFailure(Throwable error) {
	                                          status.clear();
	                                          status
	                                                  .add(new Label("Error submitting SIP:"));

	                                          TextArea text = new TextArea();

	                                          text.setText(error.getMessage());
	                                          text.setReadOnly(true);
	                                          text.setWidth("50em");
	                                          text.setHeight("10em");

	                                          ScrollPanel sp =
	                                                  new ScrollPanel(text);

	                                          status.add(sp);

	                                      }

	                                      public void onSuccess(String result) {
	                                          status.clear();

	                                          com.google.gwt.xml.client.Document doc =
	                                                  XMLParser.parse(result);

	                                          NodeList list =
	                                                  doc
	                                                          .getElementsByTagName("link");
	                                          for (int i = 0; i < list.getLength(); i++) {
	                                              Element e =
	                                                      (Element) list.item(i);

	                                              String url =
	                                                      e.getAttribute("href");

	                                              if (url != null) {
	                                                  status
	                                                          .add(new Anchor("Processing status feed",
	                                                                          url,
	                                                                          "_blank"));
	                                                  break;
	                                              }
	                                          }

	                                          TextArea text = new TextArea();

	                                          text.setText(result);
	                                          text.setReadOnly(true);
	                                          text.setWidth("50em");
	                                          text.setHeight("10em");
	                                          
	                                          ScrollPanel sp =
	                                                  new ScrollPanel(text);

	                                          status
	                                                  .add(Util.label("Atom XML document", "SubSectionHeader"));
	                                          status.add(sp);
	                                      }
	                                  });
	            }
	        });
	    }
	 
	 private Package constructPackage() {
	    	
	        Package pkg = new Package();

	        for (int i = 0; i < collections.getWidgetCount(); i++) {
	            CollectionEditor ed = (CollectionEditor) collections.getWidget(i);
	            pkg.collections().add(ed.getCollection());
	        }

	        for (int i = 0; i < dus.getWidgetCount(); i++) {
	            DeliverableUnitEditor ed = (DeliverableUnitEditor) dus.getWidget(i);
	            pkg.deliverableUnits().add(ed.getDeliverableUnit());
	        }

	        for (int i = 0; i < files.getWidgetCount(); i++) {
	            FileEditor ed = (FileEditor) files.getWidget(i);
	            pkg.files().add(ed.getFile());
	        }

	        List<Repository> repositories = new ArrayList<Repository>();
	        int selected = ir.getSelectedIndex();
	        if(selected==0){
	        	Repository repo = new Repository();
	        	repo.setId("1");
	        	repo.setName("IU Scholarworks");
	        	repo.setType("DSpace");
	        	repo.setUrl("http://walnut.dlib.indiana.edu:8245/sead/communities/184");
	        	repositories.add(repo);
	        }
	        else if(selected==1){
	        	Repository repo = new Repository();
	        	repo.setId("2");
	        	repo.setName("Ideals");
	        	repo.setType("DSpace");
	        	repo.setUrl("http://test.ideals.uiuc.edu/");
	        	repositories.add(repo);
	        }
	        else if(selected==2){
	        	Repository repo = new Repository();
	        	repo.setId("3");
	        	repo.setName("DSpace local instance");
	        	repo.setType("DSpace");
	        	repo.setUrl("http://bluespruce.pti.indiana.edu:8181/sead/communities/13");
	        	repositories.add(repo);
	        }
	        
	       // repo.setCloudCopy(cloudCopy.getValue());
	        
	        pkg.setRepositories(repositories);
	        return pkg;
	    }
	 
	  static final MediciServiceAsync mediciService = GWT
				.create(MediciService.class);
	   
	 private static String nextCollectionId() {
	        return "col" + ++SeadApp.colseq;
	    }
	 
	 private static String nextFileId() {
	        return "file" + ++SeadApp.fileseq;
	    }
	
	 private static String nextDeliverableUnitId() {
	        return "du" + ++SeadApp.duseq;
	    }

	 public void displayEnterFileDialog() {
	        final DialogBox db = new DialogBox(false, true);

	        Panel panel = new FlowPanel();

	        db.setAnimationEnabled(true);
	        db.setText("Enter tag for file");
	        db.setWidget(panel);
	        db.center();

	        final HorizontalPanel buttons = new HorizontalPanel();
	        buttons.setSpacing(5);

	        Button upload = new Button("Done");
	        Button cancel = new Button("Cancel");

	        buttons.add(upload);
	        buttons.add(cancel);

	        final FormPanel form = new FormPanel();
	        FlowPanel formcontents = new FlowPanel();
	        form.add(formcontents);

	        final TextBox upfile = new TextBox();
	        upfile.setName("file");

	        Hidden depositurl = new Hidden("sparqlurl");
	        depositurl.setValue("http://sead.ncsa.illinois.edu/beta/api/image/download/");

	        formcontents.add(upfile);
	        formcontents.add(depositurl);

	        form.setMethod(FormPanel.METHOD_POST);
	        form.setEncoding(FormPanel.ENCODING_MULTIPART);
	        form.setAction(SeadApp.FILE_UPLOAD_URL);

	        panel.add(new Label("Selected files will be included in the SIP."));
	        panel.add(form);
	        panel.add(buttons);

	        upload.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent event) {
	                //form.submit();
	            	final String text= upfile.getText();
	            	db.hide();
//	            	Window.alert(text);
	            	
	            }
	        });

	        cancel.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent event) {
	                db.hide();
	            }
	        });
	    }
	
	 
	 private void displayEnterCollectionDialog(final TabPanel files, final TabPanel dus, final List<String> fileids,final List<String> duids,final List<String> colids, String fileUploadUrl) {
	        final DialogBox db = new DialogBox(false, true);

	        Panel panel = new FlowPanel();

	        db.setAnimationEnabled(true);
	        db.setText("Enter tag for collection");
	        db.setWidget(panel);
	        db.center();

	        final HorizontalPanel buttons = new HorizontalPanel();
	        buttons.setSpacing(5);

	        Button upload = new Button("Done");
	        Button cancel = new Button("Cancel");

	        buttons.add(upload);
	        buttons.add(cancel);

	        final FormPanel form = new FormPanel();
	        FlowPanel formcontents = new FlowPanel();
	        form.add(formcontents);

	        final TextBox upfile = new TextBox();
	        upfile.setName("file");

	        Hidden depositurl = new Hidden("sparqlurl");
	        depositurl.setValue("http://sead.ncsa.illinois.edu/beta/api/image/download/");

	        formcontents.add(upfile);
	        formcontents.add(depositurl);

	        form.setMethod(FormPanel.METHOD_POST);
	        form.setEncoding(FormPanel.ENCODING_MULTIPART);
	        form.setAction(SeadApp.FILE_UPLOAD_URL);

	        panel.add(new Label("Selected files will be included in the SIP."));
	        panel.add(form);
	        panel.add(buttons);

	        
	        
	        upload.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent event) {
	            }
	        });

	        cancel.addClickHandler(new ClickHandler() {

	            public void onClick(ClickEvent event) {
	                db.hide();
	            }
	        });
	    }
	 @Override
	public TabPanel getUploadPanel() {
		return uploadPanel;
	}

}
