package org.seadva.registry.database.model.obj.vaRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 4/9/14
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class AggregationWrapper {


    public BaseEntity getChild() {
        return child;
    }

    public void setChild(BaseEntity child) {
        BaseEntity tempChild = new BaseEntity();
        tempChild.setId(child.getId());
        this.child = tempChild;
    }

    public BaseEntity getParent() {
        return parent;
    }

    public void setParent(BaseEntity parent) {
        BaseEntity tempParent = new BaseEntity();
        tempParent.setId(parent.getId());
        this.parent = tempParent;
    }

    public String getParentType(){
        return parentType;
    }
    public void setParentType(String parentType){
        this.parentType = parentType;
    }

    public String getChildType(){
        return childType;
    }

    public void setChildType(String childType){
        this.childType = childType;
    }

    @Expose
    private BaseEntity child;
    @Expose
    private BaseEntity parent;
    @Expose
    private String childType;
    @Expose
    private String parentType;

}
