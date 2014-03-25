package org.seadva.registry.impl.resource;

import org.seadva.registry.api.Resource;
import org.seadva.registry.dao.EntityDao;
import org.seadva.registry.dao.EntityTypeDao;
import org.seadva.registry.dao.PropertyDao;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 3/19/14
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersonResource implements Resource {
    //Entity + type = Person & any further relations, aggregations & properties

    private EntityDao entityDao;
    private List<EntityTypeDao> entityTypeDao;
    private List<PropertyDao> propertyDao;

    public PersonResource(){}

    public PersonResource(EntityDao entityDao, List<EntityTypeDao> entityTypeDao, List<PropertyDao> propertyDao){
        this.entityDao = entityDao;
        this.entityTypeDao = entityTypeDao;
        this.propertyDao = propertyDao;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    public List<PropertyDao> getPropertyDao() {
        return propertyDao;
    }

    public void setPropertyDao(List<PropertyDao> propertyDao) {
        this.propertyDao = propertyDao;
    }

    public List<EntityTypeDao> getEntityTypeDao() {
        return entityTypeDao;
    }

    public void setEntityTypeDao(List<EntityTypeDao> entityTypeDao) {
        this.entityTypeDao = entityTypeDao;
    }
}
