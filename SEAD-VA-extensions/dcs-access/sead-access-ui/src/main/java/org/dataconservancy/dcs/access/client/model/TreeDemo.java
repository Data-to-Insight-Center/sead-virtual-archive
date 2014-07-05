/**
 * Copyright (c) 2013, Anthony Schiochet and Eric Citaire
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * The names Anthony Schiochet and Eric Citaire may not be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MICHAEL BOSTOCK BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataconservancy.dcs.access.client.model;

import com.github.gwtd3.api.Coords;
import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.arrays.ForEachCallback;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Transition;
import com.github.gwtd3.api.core.UpdateSelection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.functions.KeyFunction;
import com.github.gwtd3.api.layout.Link;
import com.github.gwtd3.api.layout.Node;
import com.github.gwtd3.api.layout.Tree;
import com.github.gwtd3.api.svg.Diagonal;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.api.InputService;
import org.dataconservancy.dcs.access.client.api.InputServiceAsync;

/**
 * A demonstration of how to build a d3js tree from simple JSON data with
 * collapse functionality and transitions
 *
 * @author <a href="mailto:evanshi09@gmail.com">Evan Shi </a>
 *
 * modified by Kavitha Chandrasekar (kavchand@indiana.edu)
 */
public class TreeDemo
        extends FlowPanel implements DemoCase {

    // constants of tree
    final int width = 600;
    final int height = 650;
    final int duration = 750;
    final MyResources css = Bundle.INSTANCE.css();

    // global references for demo
    static String id;
    static int i = 0;
    static TreeDemoNode root = null;
    static Selection svg = null;
    static Tree tree = null;
    static Diagonal diagonal = null;

    public static final InputServiceAsync inputService = GWT.create(InputService.class);

    public interface Bundle extends ClientBundle {
        public static final Bundle INSTANCE = GWT.create(Bundle.class);

        @Source("TreeDemoStyles.css")
        public MyResources css();
    }

    interface MyResources extends CssResource {

        String link();

        String node();

        String border();
    }

    public TreeDemo() {
        super();
        Bundle.INSTANCE.css().ensureInjected();
    }

    public void setId(String id){
        this.id = id;
    }


    @Override
    public void start() {

        final TreeDemo demo = this;
        inputService.getLineageInput(this.id, SeadApp.roUrl, new AsyncCallback<String>(
        ) {

            @Override
            public void onSuccess(String result) {

                tree = D3.layout().tree().size(width, height);
                // set the global way to draw paths
                diagonal = D3.svg().diagonal()
                        .projection(new DatumFunction<Array<Double>>() {
                            @Override
                            public Array<Double> apply(Element context, Value d,
                                                       int index) {
                                TreeDemoNode data = d.<TreeDemoNode> as();
                                return Array.fromDoubles(data.x(), data.y());
                            }
                        });

                // add the SVG
                svg = D3.select(demo).append("svg").attr("width", width + 5)
                        .attr("height", height + 80).append("g")
                        .attr("transform", "translate(10, 140)");

                // get the root of the tree and initialize it
                root = JSONParser.parseLenient(result).isObject().getJavaScriptObject()
                        .<TreeDemoNode> cast();
                root.setAttr("x0", (width - 20) / 2);
                root.setAttr("y0", 0);
//				root.setStringAttr("title", "x");
//				root.setStringAttr("link", "y");
                if (root.children() != null) {
                    root.children().forEach(new Collapse());
                }
                update(root);
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public void stop() {
    }

    static int index =0;
    // follows d3 general update pattern for handling exiting and entering
    // nodes/paths
    private void update(final TreeDemoNode source) {
        Array<Node> nodes = tree.nodes(root).reverse();
        Array<Link> links = tree.links(nodes);

        // normalize depth
        nodes.forEach(new ForEachCallback<Void>() {
            @Override
            public Void forEach(Object thisArg, Value element, int index,
                                Array<?> array) {
                TreeDemoNode datum = element.<TreeDemoNode> as();
                datum.setAttr("y", datum.depth() * 180);
                return null;
            }
        });

        //	final java.util.Iterator<Entry<String, String>> iterator = titleLink.entrySet().iterator();


        // assign ids to nodes
        UpdateSelection node = svg.selectAll("g." + css.node()).data(nodes,
                new KeyFunction<Integer>() {
                    @Override
                    public Integer map(Element context, Array<?> newDataArray,
                                       Value datum, int index) {
                        TreeDemoNode d = datum.<TreeDemoNode> as();
                        return ((d.id() == -1) ? d.id(++i) : d.id());
                    }
                });

        // add click function on node click
        Selection nodeEnter = node
                .enter()
                .append("g")
                .attr("class", css.node())
                .attr("transform",
                        "translate(" + source.getNumAttr("x0") + ","
                                + source.getNumAttr("y0") + ")")
                .on("click", new Click());

        // add circles to all entering nodes
        nodeEnter.append("circle").attr("r", 1e-6)
                .attr("r", 1e-6)
                .style("fill", new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        /*JavaScriptObject node = d.<TreeDemoNode> as()
                                      .getObjAttr("_children");
                              return (node != null) ? "lightsteelblue" : "#fff";*/
                        TreeDemoNode node = d.<TreeDemoNode> as();
                        return (node.getStringAttr("url").equalsIgnoreCase(id)) ? "red" : "white";
                    }
                });


        nodeEnter.append("text")
                .attr("x",20)
                .attr("y",20)
                .attr("font-family","sans-serif")
                .attr("font-size","20px")
                .text(new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        if(d.<TreeDemoNode> as()
                                .getStringAttr("name")==null)
                            return d.<TreeDemoNode> as()
                                    .getStringAttr("url");
                        return d.<TreeDemoNode> as()
                                .getStringAttr("name");
                    }
                })
                        //.text(source.getStringAttr("title"));
                .on("click", new HyperLink());




        // transition entering nodes
        Transition nodeUpdate = node.transition().duration(duration)
                .attr("transform", new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        TreeDemoNode data = d.<TreeDemoNode> as();
                        return "translate(" + data.x() + "," + data.y() + ")";
                    }
                });

        nodeUpdate.select("circle").attr("r", 4.5)
                .style("fill", new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        JavaScriptObject object = d.<TreeDemoNode> as()
                                .getObjAttr("_children");
                        String url = d.<TreeDemoNode> as().getStringAttr("url");
                        if(url.equalsIgnoreCase(id))
                            return "#800000";
                        else if(object!=null)
                            return "lightsteelblue";
                        else
                            return "#fff";
                    }
                });

        // transition exiting nodes
        Transition nodeExit = node.exit().transition().duration(duration)
                .attr("transform", new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        return "translate(" + source.x() + "," + source.y()
                                + ")";
                    }
                }).remove();

        nodeExit.select("circle").attr("r", 1e-6);

        // update svg paths for new node locations
        UpdateSelection link = svg.selectAll("path." + css.link()).data(links,
                new KeyFunction<Integer>() {
                    @Override
                    public Integer map(Element context, Array<?> newDataArray,
                                       Value datum, int index) {
                        return datum.<Link> as().target().<TreeDemoNode> cast()
                                .id();
                    }
                });

        link.enter().insert("svg:path", "g").attr("class", css.link())
                .attr("d", new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        Coords o = Coords.create(source.getNumAttr("x0"),
                                source.getNumAttr("y0"));
                        return diagonal.generate(Link.create(o, o));
                    }
                });


        link.transition().duration(duration).attr("d", diagonal);


        link.exit().transition().duration(duration)
                .attr("d", new DatumFunction<String>() {
                    @Override
                    public String apply(Element context, Value d, int index) {
                        Coords o = Coords.create(source.x(), source.y());
                        return diagonal.generate(Link.create(o, o));
                    }
                }).remove();

        // update locations on node
        nodes.forEach(new ForEachCallback<Void>() {
            @Override
            public Void forEach(Object thisArg, Value element, int index,
                                Array<?> array) {
                TreeDemoNode data = element.<TreeDemoNode> as();
                data.setAttr("x0", data.x());
                data.setAttr("y0", data.y());
                return null;
            }
        });
    }

    private class Collapse implements ForEachCallback<Void> {
        @Override
        public Void forEach(Object thisArg, Value element, int index,
                            Array<?> array) {
            TreeDemoNode datum = element.<TreeDemoNode> as();
            Array<Node> children = datum.children();
            if (children != null) {
                datum.setAttr("_children", children);
                datum.getObjAttr("_children").<Array<Node>> cast()
                        .forEach(this);
                datum.setAttr("children", null);
            }
            return null;
        }
    }

    private class HyperLink implements DatumFunction<Void> {
        @Override
        public Void apply(Element context, Value d, int index) {
//			Window.open(d.<TreeDemoNode> as()
//						.getStringAttr("link"), "_blank", "");
            com.google.gwt.user.client.History.newItem(SeadState.ENTITY.toToken(d.<TreeDemoNode> as()
                    .getStringAttr("url")));
            return null;
        }
    }

    private class Click implements DatumFunction<Void> {
        @Override
        public Void apply(Element context, Value d, int index) {
            TreeDemoNode node = d.<TreeDemoNode> as();
            if (node.children() != null) {
                node.setAttr("_children", node.children());
                node.setAttr("children", null);
            } else {
                node.setAttr("children", node.getObjAttr("_children"));
                node.setAttr("_children", null);
            }
            update(node);
            return null;
        }
    }

    // Perhaps a mutable JSO class would be a nice feature?
    private static class TreeDemoNode extends Node {
        protected TreeDemoNode() {
            super();
        }

        protected final native void setStringAttr(String name, String string) /*-{
		this[name] = value;
		}-*/;

        protected final native int id() /*-{
			return this.id || -1;
		}-*/;

        protected final native int id(int id) /*-{
			return this.id = id;
		}-*/;

        protected final native void setAttr(String name, JavaScriptObject value) /*-{
			this[name] = value;
		}-*/;

        protected final native double setAttr(String name, double value) /*-{
			return this[name] = value;
		}-*/;

        protected final native JavaScriptObject getObjAttr(String name) /*-{
			return this[name];
		}-*/;

        protected final native String getStringAttr(String name) /*-{
		return this[name];
		}-*/;

        protected final native double getNumAttr(String name) /*-{
			return this[name];
		}-*/;
    }
}
