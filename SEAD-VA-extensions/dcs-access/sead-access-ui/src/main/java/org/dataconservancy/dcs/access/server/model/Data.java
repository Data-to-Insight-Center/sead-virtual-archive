package org.dataconservancy.dcs.access.server.model;

import java.util.ArrayList;
import java.util.List;

public class Data {
	private String title;
	private String link;
	private List<Data> children;
	public Data(){
		children = new ArrayList<Data>();
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public List<Data> getChildren() {
		return children;
	}
	public void setChildren(List<Data> children) {
		this.children = children;
	}
	
	public void addChild(Data child) {
		this.children.add(child);
	}
	

}
