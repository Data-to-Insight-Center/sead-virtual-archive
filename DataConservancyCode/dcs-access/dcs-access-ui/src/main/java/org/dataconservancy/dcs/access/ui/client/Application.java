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
package org.dataconservancy.dcs.access.ui.client;

import java.util.List;

import org.dataconservancy.dcs.access.ui.client.model.JsDcp;
import org.dataconservancy.dcs.access.ui.client.model.JsEntity;
import org.dataconservancy.dcs.access.ui.client.model.JsMatch;
import org.dataconservancy.dcs.access.ui.client.model.JsSearchResult;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.TreeViewModel;

/**
 * User interface.
 */
public class Application implements EntryPoint {
    private static final String BUG_SUBMIT_EMAIL = "http://dataconservancy.org";
    private static final int MAX_SEARCH_RESULTS = 20;

    private static String accessurl;
    private static TextBox accessurl_tb;

    private Panel content;

    private static void reportInternalError(String message, Throwable e) {
        Window.alert("This is likely a bug. Please report to "
                + BUG_SUBMIT_EMAIL
                + ". Include your operating system and version, browser and version, url you were visiting, and what you did to trigger the problem. \nInternal error: "
                + message + "\n" + e.getMessage()
                + (e.getCause() == null ? "" : "\nCaused by: " + e.getCause()));
    }

    public void onModuleLoad() {
        accessurl_tb = new TextBox();

        accessurl = Window.Location.getParameter("accessurl");

        if (accessurl != null) {
            updateAccessServiceUrl();
        } else {
            // load config

            HttpGet.request(GWT.getModuleBaseURL() + "Config.properties",
                    new HttpGet.Callback<String>() {

                        public void failure(String error) {
                            Window.alert("Failed to load config: " + error);
                        }

                        public void success(String result) {
                            String[] pairs = result.trim().split(
                                    "\\w*(\n|\\=)\\w*");

                            for (int i = 0; i + 1 < pairs.length;) {
                                String name = pairs[i++].trim();
                                String value = pairs[i++].trim();

                                if (name.equals("accessServiceURL")) {
                                    accessurl = value;
                                    updateAccessServiceUrl();
                                }
                            }
                        }
                    });
        }

        DockLayoutPanel main = new DockLayoutPanel(Unit.PX);
        main.setStylePrimaryName("Main");

        content = new FlowPanel();
        content.setStylePrimaryName("Content");

        Panel header = new FlowPanel();
        header.setStylePrimaryName("TopHeader");

        Panel footer = new FlowPanel();
        footer.setStylePrimaryName("Footer");

        main.addNorth(header, 110);
        main.addSouth(footer, 40);

        main.add(new ScrollPanel(content));

        Image logo = new Image(GWT.getModuleBaseURL()
                + "simply_modern_logo.png");

        ClickHandler gohome = new ClickHandler() {
            public void onClick(ClickEvent event) {
                History.newItem(State.HOME.toToken());
            }
        };

        logo.addClickHandler(gohome);

        header.add(logo);
        Label toptext = Util.label("Access User Interface", "TopHeaderText");

        toptext.addClickHandler(gohome);

        header.add(toptext);

        footer.add(createAccessServiceUrlEditor());
        footer.add(new HTML(
                "<a href='http://dataconservancy.org/'>http://dataconservancy.org/</a>"));

        RootLayoutPanel.get().add(main);

        History.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {
                historyChanged(event.getValue());
            }
        });

        History.fireCurrentHistoryState();
    }

    private void handleHistoryTokenError(String token) {
        Window.alert("Error parsing action: " + token);
    }

    private void historyChanged(String token) {
        if (token.isEmpty()) {
            viewHome();
            return;
        }

        State state = State.fromToken(token);
        List<String> args = State.tokenArguments(token);

        if (state == null) {
            handleHistoryTokenError(token);
            return;
        }

        if (state == State.HOME) {
            viewHome();
        } else if (state == State.SEARCH) {
            if (args.size() == 0) {
                viewSearch(null, null, 0, null);
                return;
            }

            if (args.size() < 2 || (args.size() & 1) == 0) {
                handleHistoryTokenError(token);
                return;
            }

            int offset;

            try {
                offset = Integer.parseInt(args.get(args.size() - 1));
            } catch (NumberFormatException e) {
                handleHistoryTokenError(token);
                return;
            }

            String[] userqueries = new String[(args.size() - 1) / 2];
            Search.UserField[] userfields = new Search.UserField[userqueries.length];

            int argindex = 0;
            for (int i = 0; i < userqueries.length; i++) {
                userfields[i] = Search.UserField.valueOf(args.get(argindex++));
                userqueries[i] = args.get(argindex++);

                if (userfields[i] == null) {
                    handleHistoryTokenError(token);
                    return;
                }
            }

            String searchtokenprefix = token.substring(0,
                    token.lastIndexOf(';'));

            viewSearch(userfields, userqueries, offset, searchtokenprefix);
        } else if (state == State.ENTITY) {
            if (args.size() != 1) {
                handleHistoryTokenError(token);
                return;
            }

            viewEntity(args.get(0));
        } else if (state == State.RELATED) {
            if (args.size() != 1) {
                handleHistoryTokenError(token);
                return;
            }

            viewRelated(args.get(0));
        } else {
            handleHistoryTokenError(token);
            return;
        }
    }

    // Only designed to work for Deliverable Units.
    // Not designed to deal with thousands of related entities.
    private void viewRelated(final String id) {
        content.clear();
        content.add(createAdvancedSearchWidget(null, null));

        String query = Search.createLiteralQuery("OR",
                SolrField.EntityField.ANCESTRY.solrName(), id,
                SolrField.EventField.TARGET.solrName(), id,
                SolrField.EntityField.ID.solrName(), id,
                SolrField.RelationField.TARGET.solrName(), id);

        // content.add(new Label(query));

        collectSearchResults(query, 0, new AsyncCallback<JsDcp>() {

            public void onFailure(Throwable caught) {
                reportInternalError("Doing related search", caught);
            }

            public void onSuccess(JsDcp result) {
                displayRelated(result, id);
            }
        });
    }

    private void viewEntity(String id) {
        String entityurl = id;

        if (!entityurl.startsWith(accessurl)) {
            Window.alert("Cannot view external entity: " + id);
            return;
        }

        JsonpRequestBuilder rb = new JsonpRequestBuilder();

        rb.requestObject(entityurl, new AsyncCallback<JsDcp>() {

            public void onFailure(Throwable caught) {
                reportInternalError("Viewing entity", caught);
            }

            public void onSuccess(JsDcp result) {
                displayEntity(result);
            }
        });
    }

    public static String datastreamURL(String id) {
        return accessurl + "datastream/" + encodeURLPath(id);
    }

    private static String encodeURLPath(String path) {
        // String s = URL.encodeComponent(path);
        // Have to encode spaces (now plus) using %20
        // return s.replace("+", "%20");

        return URL.encodePathSegment(path);
    }

    private static String searchURL(String query, int offset, boolean context,
            int max) {
        // String s =
        // accessurl + "query/" + encodeURLPath(query) + "?offset="
        // + offset + "&max=" + MAX_SEARCH_RESULTS;

        String s = accessurl + "query/?q=" + URL.encodeQueryString(query)
                + "&offset=" + offset + "&max=" + max;

        if (context) {
            return s + "&_hl=true&_hl.requireFieldMatch=true&_hl.fl="
                    + URL.encodeQueryString("*");
        }

        return s;
    }

    private void viewSearch(final Search.UserField[] userfields,
            final String[] userqueries, int offset,
            final String searchtokenprefix) {
        content.clear();

        if (userfields == null || userqueries == null) {
            content.add(createAdvancedSearchWidget(userfields, userqueries));
            return;
        }

        String query = Search.createQuery(userfields, userqueries);
        // content.add(new Label(query));
        JsonpRequestBuilder rb = new JsonpRequestBuilder();

        String searchurl = searchURL(query, offset, true, MAX_SEARCH_RESULTS);

        content.add(createAdvancedSearchWidget(userfields, userqueries));

        rb.requestObject(searchurl, new AsyncCallback<JsSearchResult>() {

            public void onFailure(Throwable caught) {
                reportInternalError("Searching", caught);
            }

            public void onSuccess(JsSearchResult result) {
                displaySearchResults(content, searchtokenprefix, result);
            }
        });
    }

    private void collectSearchResults(final String query, final int offset,
            final AsyncCallback<JsDcp> topcb) {
        collectSearchResults(query, offset, JsDcp.create(), topcb);
    }

    private void collectSearchResults(final String query, final int offset,
            final JsDcp dcp, final AsyncCallback<JsDcp> topcb) {
        String searchurl = searchURL(query, offset, false, 50);

        JsonpRequestBuilder rb = new JsonpRequestBuilder();
        rb.requestObject(searchurl, new AsyncCallback<JsSearchResult>() {

            public void onFailure(Throwable caught) {
                topcb.onFailure(caught);
            }

            public void onSuccess(JsSearchResult result) {
                for (int i = 0; i < result.matches().length(); i++) {
                    Util.add(dcp, result.matches().get(i));
                }

                int nextoffset = offset + result.matches().length();

                if (nextoffset == result.total()) {
                    topcb.onSuccess(dcp);
                } else {
                    collectSearchResults(query, nextoffset, dcp, topcb);
                }
            }
        });
    }

    private void displayEntity(JsDcp dcp) {
        content.clear();
        content.add(createAdvancedSearchWidget(null, null));
        content.add(Util.label("Entity", "SectionHeader"));
        content.add(dcp.display());
    }

    private void displayRelated(JsDcp dcp, String id) {

        // Find id entity and display it

        content.add(Util.label("Entity", "SectionHeader"));

        int i = Util.find(dcp.getCollections(), id);

        if (i == -1) {
            i = Util.find(dcp.getDeliverableUnits(), id);

            if (i == -1) {
                // TODO other types...
            } else {
                content.add(dcp.getDeliverableUnits().get(i).display());
                // Util.removeDeliverableUnit(dcp.getDeliverableUnits(), i);
            }
        } else {
            content.add(dcp.getCollections().get(i).display());
            // Util.removeCollection(dcp.getCollections(), i);
        }

        // content.add(dcp.display());

        TreeViewModel treemodel = new DcpTreeModel(dcp);

        // CellBrowser browser = new CellBrowser(treemodel, null);
        //
        // browser.setStylePrimaryName("RelatedView");
        // browser.setAnimationEnabled(true);
        // browser.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        //
        // if (browser.getRootTreeNode().getChildCount() > 0) {
        // browser.getRootTreeNode().setChildOpen(0, true);
        // }
        //
        // content.add(browser);

        CellTree tree = new CellTree(treemodel, null);

        tree.setStylePrimaryName("RelatedView");
        tree.setAnimationEnabled(true);
        tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        tree.setDefaultNodeSize(50);

        if (tree.getRootTreeNode().getChildCount() > 0) {
            tree.getRootTreeNode().setChildOpen(0, true);
        }

        content.add(Util.label("Related entities", "SectionHeader"));
        content.add(tree);
    }

    private void viewHome() {
        content.clear();

        content.add(Util
                .label("The Data Conservancy (DC) embraces a shared vision: scientific data curation is a means to collect, organize, validate and preserve data so that scientists can find new ways to address the grand research challenges that face society.  The Data Conservancy will research, design, implement, deploy and sustain data curation infrastructure for cross-disciplinary discovery with an emphasis on observational data.",
                        "Explanation"));
        content.add(createAdvancedSearchWidget(null, null));
    }

    private void updateAccessServiceUrl() {
        accessurl_tb.setText(accessurl);
        accessurl_tb.setWidth(accessurl.length() + "ex");
    }

    private Widget createAccessServiceUrlEditor() {
        FlexTable table = Util.createTable("Access service:");

        Button set = new Button("Set");

        Util.addColumn(table, accessurl_tb);
        Util.addColumn(table, set);

        set.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String s = accessurl_tb.getText().trim();

                if (!s.isEmpty()) {
                    accessurl = s;
                    History.fireCurrentHistoryState();
                }
            }
        });

        return table;
    }

    private HorizontalPanel createSearchResultsNav(int page, int numpages,
            String tokenqueryprefix) {
        HorizontalPanel nav = new HorizontalPanel();
        nav.setStylePrimaryName("ResultsNav");
        nav.setSpacing(2);

        if (numpages > 1) {
            if (page > 0) {
                nav.add(new Hyperlink("<<", tokenqueryprefix + ';' + 0));

                int offset = (page - 1) * MAX_SEARCH_RESULTS;
                Hyperlink h = new Hyperlink("Previous", tokenqueryprefix + ';'
                        + offset);
                nav.add(h);
            }

            int max_pages_shown = 10;

            int startpage = page - (max_pages_shown / 2);

            if (startpage < 0) {
                startpage = 0;
            }

            int endpage = startpage + max_pages_shown;

            if (endpage > numpages) {
                endpage = numpages;
            }

            for (int i = startpage; i < endpage; i++) {
                if (i == page) {
                    nav.add(Util.label("" + (i + 1), "CurrentNavigationPage"));
                } else {
                    int offset = i * MAX_SEARCH_RESULTS;
                    Hyperlink h = new Hyperlink("" + (i + 1), tokenqueryprefix
                            + ';' + offset);
                    nav.add(h);
                }
            }

            if (page < numpages - 1) {
                int offset = (page + 1) * MAX_SEARCH_RESULTS;
                Hyperlink h = new Hyperlink("Next", tokenqueryprefix + ';'
                        + offset);
                nav.add(h);

                nav.add(new Hyperlink(">>", tokenqueryprefix + ';'
                        + (numpages - 1) * MAX_SEARCH_RESULTS));
            }
        }

        return nav;
    }

    private void displaySearchResults(Panel panel, String tokenqueryprefix,
            JsSearchResult result) {

        panel.add(Util.label("Total matches: " + result.total(),
                "SectionHeader"));

        int numpages = (int) result.total() / MAX_SEARCH_RESULTS;

        if (result.total() % MAX_SEARCH_RESULTS != 0) {
            numpages++;
        }

        int page = result.offset() / MAX_SEARCH_RESULTS;

        HorizontalPanel nav = createSearchResultsNav(page, numpages,
                tokenqueryprefix);
        panel.add(nav);

        Grid grid = new Grid((result.matches().length() / 2)
                + (result.matches().length() % 2), 4);

        grid.setStylePrimaryName("SearchResults");

        for (int i = 0; i < result.matches().length(); i++) {
            JsMatch m = result.matches().get(i);

            int resultrow = i / 2;
            int resultcol = (i % 2) * 2;

            JsEntity entity = m.getEntity();
            grid.setWidget(
                    resultrow,
                    resultcol,
                    new Hyperlink(m.getEntityType(), true, State.ENTITY
                            .toToken(entity.getId())));

            FlowPanel desc = new FlowPanel();
            grid.setWidget(resultrow, resultcol + 1, desc);

            desc.add(m.getSummary());

            if (!m.getContext().isEmpty()) {
                // TODO hack to strip out fields, later have a data structure
                // for context
                String context = m.getContext().replaceAll(
                        "\\w+\\:|\\'\\[|\\]\\'", " ");
                desc.add(new HTML("<span class='ResultSnippet'>" + context
                        + "</span>"));
            }
        }

        panel.add(grid);
        panel.add(createSearchResultsNav(page, numpages, tokenqueryprefix));
    }

    // Fills in values if non-null

    private Widget createAdvancedSearchWidget(Search.UserField[] userfields,
            String[] userqueries) {
        FlowPanel panel = new FlowPanel();

        Button search = new Button("Search");

        final FlexTable table = new FlexTable();

        Button add = new Button("Add field");

        panel.add(table);

        // Called to search filled in query

        final ClickHandler searchlistener = new ClickHandler() {

            public void onClick(ClickEvent event) {
                // Build up search history token

                String[] data = new String[(table.getRowCount() * 2) + 1];
                int dataindex = 0;
                boolean emptyquery = true;

                for (int i = 0; i < table.getRowCount(); i++) {
                    ListBox lb = (ListBox) table.getWidget(i, 0);
                    TextBox tb = (TextBox) table.getWidget(i, 1);

                    int sel = lb.getSelectedIndex();

                    if (sel != -1) {
                        String userquery = tb.getText().trim();
                        String userfield = Search.UserField.values()[sel]
                                .name();

                        if (userquery.isEmpty()) {
                            userfield = null;
                            userquery = null;
                        } else {
                            emptyquery = false;
                        }

                        data[dataindex++] = userfield;
                        data[dataindex++] = userquery;
                    }
                }

                data[dataindex] = "0";

                if (!emptyquery) {
                    History.newItem(State.SEARCH.toToken(data));
                }
            }
        };

        ClickHandler addlistener = new ClickHandler() {

            public void onClick(ClickEvent event) {
                int row = table.getRowCount();

                table.setWidget(row, 0, createAdvancedSearchFieldSelector());

                TextBox tb = new TextBox();
                table.setWidget(row, 1, tb);

                tb.addKeyDownHandler(new KeyDownHandler() {

                    public void onKeyDown(KeyDownEvent event) {
                        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                            searchlistener.onClick(null);
                        }
                    }
                });

                final Button remove = new Button("Remove");

                table.setWidget(row, 2, remove);

                remove.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        for (int row = 0; row < table.getRowCount(); row++) {
                            if (table.getWidget(row, 2) == remove) {
                                table.removeRow(row);
                            }
                        }
                    }
                });
            }
        };

        add.addClickHandler(addlistener);

        if (userfields != null) {
            for (int i = 0; i < userfields.length; i++) {
                if (userfields[i] == null) {
                    continue;
                }

                int row = table.getRowCount();
                addlistener.onClick(null);

                ListBox lb = (ListBox) table.getWidget(row, 0);
                lb.setItemSelected(userfields[i].ordinal(), true);
                TextBox tb = (TextBox) table.getWidget(row, 1);
                tb.setText(userqueries[i]);
            }
        } else {
            addlistener.onClick(null);
        }

        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.add(add);
        hp.add(search);

        panel.add(hp);

        search.addClickHandler(searchlistener);

        return panel;
    }

    private ListBox createAdvancedSearchFieldSelector() {
        ListBox lb = new ListBox();

        for (Search.UserField uf : Search.UserField.values()) {
            lb.addItem(uf.display);
        }

        return lb;
    }

}
