package org.seadva.data.lifecycle.support.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores entity metadata
 */
public class Entity {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Entity> getChildren(){
        return children;
    }

    public void setChildren(List<Entity> children){
        this.children = children;
    }

    public void addChild(Entity child){
        if(this.children==null)
            this.children = new ArrayList<Entity>();
        this.children.add(child);
    }

    private String id;
    private String url;
    private String name;
    private List<Entity> children;
}
