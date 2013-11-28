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
package org.dataconservancy.dcs.ingest.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import org.dataconservancy.dcs.ingest.ui.client.model.Package;

// TODO use i8n stuff for ui labels

/**
 * User interface.
 */
public class Application
        implements EntryPoint {

    public static final String FILE_UPLOAD_URL =
            GWT.getModuleBaseURL() + "fileupload";

    private final DepositServiceAsync deposit =
            GWT.create(DepositService.class);

    private Panel content;

    private Panel sidebar;

    private DepositConfig depositConfig;

    private String deposit_endpoint =
            "http://dcservice.dataconservancy.org:8080/dcs/deposit/";

    private String deposit_user = "";

    private String deposit_pass = "";

    private TabPanel collections;

    private TabPanel files;

    private TabPanel dus;

    private List<String> fileids, colids, duids;

    private int colseq, fileseq, duseq;

    public void onModuleLoad() {
        Grid main = new Grid(3, 2);
        main.setStyleName("Main");

        RootPanel.get().add(main);

        sidebar = new VerticalPanel();
        content = new FlowPanel();
        collections = new DecoratedTabPanel();
        files = new DecoratedTabPanel();
        dus = new DecoratedTabPanel();

        this.fileids = new ArrayList<String>();
        this.duids = new ArrayList<String>();
        this.colids = new ArrayList<String>();

        HorizontalPanel header = new HorizontalPanel();
        Panel footer = new FlowPanel();

        CellFormatter fmt = main.getCellFormatter();

        fmt.setStylePrimaryName(0, 0, "TopHeaderLeft");
        fmt.setStylePrimaryName(0, 1, "TopHeader");
        fmt.setStylePrimaryName(1, 0, "Sidebar");
        fmt.setStylePrimaryName(1, 1, "Content");
        fmt.setStylePrimaryName(2, 0, "FooterLeft");
        fmt.setStylePrimaryName(2, 1, "Footer");

        Label toptext = Util.label("Ingest User Interface", "TopHeaderText");
        header.add(toptext);
        header.setCellVerticalAlignment(toptext,
                                        HasVerticalAlignment.ALIGN_BOTTOM);

        footer
                .add(new HTML("<a href='http://dataconservancy.org/'>http://dataconservancy.org/</a>"));

        main.setWidget(0, 0, new Image(GWT.getModuleBaseURL() + "simply_modern_logo.png"));
        main.setWidget(0, 1, header);
        main.setWidget(1, 0, sidebar);
        main.setWidget(1, 1, content);
        main.setWidget(2, 1, footer);

        History.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {
                historyChanged(event.getValue());
            }
        });

        History.newItem(State.HOME.toToken(), false);
        History.fireCurrentHistoryState();
    }

    private static Widget createSidebarHyperlink(String name, String token) {
        if (History.getToken().startsWith(token)) {
            Label l = new Label(name);
            l.addStyleName("SidebarSelected");
            return l;
        } else {
            return new Hyperlink(name, token);
        }
    }

    private String nextCollectionId() {
        return "col" + ++colseq;
    }

    private String nextFileId() {
        return "file" + ++fileseq;
    }

    private String nextDeliverableUnitId() {
        return "du" + ++duseq;
    }

    private void historyChanged(String token) {
        if (token.isEmpty()) {
            return;
        }

        State action = State.fromToken(token);
        List<String> args = State.tokenArguments(token);

        if (action == null) {
            handleHistoryTokenError(token);
            return;
        }

        if (action == State.HOME) {
            viewHome();
        } else if (depositConfig == null) {
            History.newItem(State.HOME.toToken(), false);
            viewHome();
        } else if (action == State.FILE) {
            if (args.size() != 0) {
                handleHistoryTokenError(token);
                return;
            }

            viewFiles();
        } else if (action == State.COLLECTION) {
            if (args.size() != 0) {
                handleHistoryTokenError(token);
                return;
            }

            viewCollections();
        } else if (action == State.DELIVERABLE_UNIT) {
            if (args.size() != 0) {
                handleHistoryTokenError(token);
                return;
            }

            viewDeliverableUnits();
        } else if (action == State.SUBMIT) {
            if (args.size() != 0) {
                handleHistoryTokenError(token);
                return;
            }

            viewSubmission();
        } else {
            handleHistoryTokenError(token);
            return;
        }

        setupSidebar();
    }

    private void viewHome() {
        content.clear();

        Label explain =
                new Label("The DCS Ingest UI allows simple SIPs to be created and uploaded.");
        explain.setStylePrimaryName("Explanation");
        content.add(explain);

        if (depositConfig == null) {
            content.add(new Label("Login"));

            final FlexTable table =
                    Util.createTable("Deposit endpoint:", "User:", "Pass:");

            final TextBox end_tb = new TextBox();
            end_tb.setWidth("40em");

            final TextBox user_tb = new TextBox();
            final PasswordTextBox pass_tb = new PasswordTextBox();

            end_tb.setText(deposit_endpoint);
            user_tb.setText(deposit_user);
            pass_tb.setText(deposit_pass);

            Util.addColumn(table, end_tb, user_tb, pass_tb);

            content.add(table);

            Button login = new Button("Login");

            login.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent arg0) {
                    AsyncCallback<DepositConfig> cb =
                            new AsyncCallback<DepositConfig>() {

                                public void onSuccess(DepositConfig result) {
                                    depositConfig = result;
                                    deposit_endpoint = end_tb.getText();
                                    deposit_user = user_tb.getText();
                                    deposit_pass = pass_tb.getText();
                                    viewHome();
                                }

                                public void onFailure(Throwable error) {
                                    Window.alert("Failed to login: "
                                            + error.getMessage());
                                }
                            };

                    deposit.login(end_tb.getText(), user_tb.getText(), pass_tb
                            .getText(), cb);
                }
            });

            content.add(login);
        } else {
            content.add(new Label("Logged in to: " + deposit_endpoint));
        }
    }

    private void setupSidebar() {
        sidebar.clear();

        sidebar.add(createSidebarHyperlink("Home", State.HOME.toToken()));
        sidebar.add(createSidebarHyperlink("Files", State.FILE.toToken()));
        sidebar.add(createSidebarHyperlink("Collections", State.COLLECTION
                .toToken()));
        sidebar.add(createSidebarHyperlink("Deliverable Units",
                                           State.DELIVERABLE_UNIT.toToken()));
        sidebar
                .add(createSidebarHyperlink("Submission", State.SUBMIT
                        .toToken()));
    }

    private void handleHistoryTokenError(String token) {
        Window.alert("Error parsing action: " + token);
    }

    private void viewFiles() {
        content.clear();

        Label explain =
                new Label("Upload local files for inclusion to the SIP. Remote files accessible by a URL may also be added. Files with the same technical enviroment in the same Deliverable Unit will be put in the same Manifestation.");
        explain.setStylePrimaryName("Explanation");

        content.add(explain);

        if (files.getWidgetCount() > 0) {
            files.selectTab(0);
        } else {
            files.setVisible(false);
        }

        Button upload = new Button("Upload local file");
        Button uploadremote = new Button("Add remote file");
        Button remove = new Button("Remove");

        final HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(5);
        buttons.add(upload);
        buttons.add(uploadremote);
        
        buttons.add(remove);

        content.add(files);
        content.add(buttons);

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
                displayUploadFileDialog();
            }
        });
    }

    private void displayUploadFileDialog() {
        final DialogBox db = new DialogBox(false, true);

        Panel panel = new FlowPanel();

        db.setAnimationEnabled(true);
        db.setText("Upload local file");
        db.setWidget(panel);
        db.center();

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
        depositurl.setValue(depositConfig.fileUploadUrl());

        formcontents.add(upfile);
        formcontents.add(depositurl);

        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setAction(FILE_UPLOAD_URL);

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
                db.hide();
            }
        });

        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                if (event.getResults() == null) {
                    Window.alert("File upload failed");
                    db.hide();
                    return;
                }

                String[] parts = event.getResults().split("\\^");

                if (parts.length != 4) {
                    Window.alert("File upload failed: " + event.getResults());
                    db.hide();
                    return;
                }

                String filesrc = parts[1].trim();
                // TODO String fileatomurl = parts[2].trim();

                files.setVisible(true);
                String id = nextFileId();
                fileids.add(id);
                files
                        .add(new FileEditor(id, upfile.getFilename(), filesrc),
                             id);
                files.selectTab(files.getWidgetCount() - 1);

                db.hide();
            }
        });
    }

    private void viewCollections() {
        content.clear();

        content.add(Util.label("Collections help group Deliverable Units.",
                               "Explanation"));

        content.add(collections);

        HorizontalPanel toolbar = new HorizontalPanel();

        Button remove = new Button("Remove collection");

        Button add = new Button("Add collection");

        toolbar.add(add);
        toolbar.add(remove);
        toolbar.setSpacing(5);

        content.add(toolbar);

        if (collections.getWidgetCount() > 0) {
            collections.selectTab(0);
        } else {
            collections.setVisible(false);
        }

        remove.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {

                int tab = collections.getTabBar().getSelectedTab();

                if (tab != -1) {
                    collections.remove(tab);
                    colids.remove(tab);

                    if (collections.getWidgetCount() > 0) {
                        collections.selectTab(0);
                    } else {
                        collections.setVisible(false);
                    }
                }
            }
        });

        add.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                collections.setVisible(true);

                String id = nextCollectionId();
                colids.add(id);
                collections.add(new CollectionEditor(id, colids), id);
                collections.selectTab(collections.getWidgetCount() - 1);
            }
        });
    }

    private void viewDeliverableUnits() {
        content.clear();

        content
                .add(Util
                        .label("A Deliverable Unit is a semantically meaningfull container for files and metadata.",
                               "Explanation"));

        content.add(dus);

        HorizontalPanel toolbar = new HorizontalPanel();

        Button remove = new Button("Remove Deliverable Unit");

        Button add = new Button("Add Deliverable Unit");

        toolbar.add(add);
        toolbar.add(remove);
        toolbar.setSpacing(5);

        content.add(toolbar);

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
                dus.add(new DeliverableUnitEditor(id, fileids, duids, colids),
                        id);
                dus.selectTab(dus.getWidgetCount() - 1);
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

        return pkg;
    }

    private void viewSubmission() {
        content.clear();

        Label explain =
                new Label("Constructs a SIP and submits it to the DCS. A successful submission will return an atom feed of the ingestion status.");
        explain.setStylePrimaryName("Explanation");

        Button submit = new Button("Submit SIP");

        final FlowPanel status = new FlowPanel();

        status.setStylePrimaryName("SubmissionStatus");
        
        content.add(explain);
        content.add(status);
        content.add(submit);


        submit.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                status.clear();
                status.add(new Label("Submitting SIP to: "
                        + depositConfig.sipDepositUrl() + " ..."));

                Package pkg = constructPackage();

                deposit.submitSIP(depositConfig.sipDepositUrl(),
                                  deposit_user,
                                  deposit_pass,
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

}
