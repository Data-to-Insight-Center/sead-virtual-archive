package org.seadva.registry.database.model.obj.vaRegistry;


import com.google.gson.annotations.Expose;
import org.seadva.registry.database.model.obj.vaRegistry.Relation;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 6/16/14
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollectionWrapper {// extends org.seadva.registry.database.model.obj.vaRegistry.CollectionWrapper{

    public Collection getCollection() {
        return collection;
    }
    @Expose
    org.seadva.registry.database.model.obj.vaRegistry.Collection collection;
    public CollectionWrapper(org.seadva.registry.database.model.obj.vaRegistry.Collection collection){
        this.collection = collection;
    }

    @Expose
	private Set<Relation> relations = new HashSet<Relation>();

    public Set<Relation> getRelations() {
        return relations;
    }

    public void setRelations(Set<Relation> relations) {
        this.relations = relations;
    }
}
