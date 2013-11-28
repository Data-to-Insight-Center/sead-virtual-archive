/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.ui.model.builder.xstream;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import javax.xml.stream.events.Attribute;

/**
 * ProjectConverter is used to serialize and deserialize {@link org.dataconservancy.ui.model.Project} objects.
 */
public class ProjectConverter extends AbstractEntityConverter implements ConverterConstants {
    
    public ProjectConverter() {
    }
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        final Project project = (Project) source;
        
        if (project != null) {
            if (!isEmptyOrNull(project.getId())) {
                writer.addAttribute(E_ID, project.getId());
            }

            if (!isEmptyOrNull(project.getName())) {
                writer.startNode(E_NAME);
                writer.setValue(project.getName());
                writer.endNode();
            }

            if (project.getNumbers() != null) {
                for (String number : project.getNumbers()) {
                    if (!isEmptyOrNull(number)) {
                        writer.startNode(E_NUMBER);
                        writer.setValue(number);
                        writer.endNode();
                    }
                }
            }
            
            if (!isEmptyOrNull(project.getDescription())) {
                writer.startNode(E_DESCRIPTION);
                writer.setValue(project.getDescription());
                writer.endNode();
            }
            
            if (!isEmptyOrNull(project.getPublisher())) {
                writer.startNode(E_PUBLISHER);
                writer.setValue(project.getPublisher());
                writer.endNode();
            }

            if (!isEmptyOrNull(String.valueOf(project.getStorageAllocated()))) {
                writer.startNode(E_STORAGEALLOCATED);
                writer.setValue(String.valueOf(project.getStorageAllocated()));
                writer.endNode();
            }
            
            if (!isEmptyOrNull(String.valueOf(project.getStorageUsed()))) {
                writer.startNode(E_STORAGEUSED);
                writer.setValue(String.valueOf(project.getStorageUsed()));
                writer.endNode();
            }
            
            if (project.getStartDate() != null) {
                writer.startNode(E_STARTDATE);
                context.convertAnother(project.getStartDate());
                writer.endNode();
            }
            
            if (project.getEndDate() != null) {
                writer.startNode(E_ENDDATE);
                context.convertAnother(project.getEndDate());
                writer.endNode();
            }
            
            if (project.getPis() != null) {
                for (String id : project.getPis()) {
                    if (id != null && !isEmptyOrNull(id)) {
                        writer.startNode(E_PRINCIPLEINVESTIGATORID);
                        writer.setValue(id);
                        writer.endNode();
                    }
                }
            }

            if (!isEmptyOrNull(project.getFundingEntity())) {
                writer.startNode(E_FUNDINGENTITY);
                writer.setValue(project.getFundingEntity());
                writer.endNode();
            }

        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        
        Project project = new Project();
        DateTime startDate = null;
        DateTime endDate = null;

        final String id = reader.getAttribute(E_ID);
        if(!isEmptyOrNull(id)){
            project.setId(id);
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String name = getElementName(reader);
            if (name.equals(E_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.setName(value);
                }
            }

            if (name.equals(E_NUMBER)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.addNumber(value);
                }
            }
            
            if (name.equals(E_DESCRIPTION)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.setDescription(value);
                }
            }
            
            if (name.equals(E_PUBLISHER)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.setPublisher(value);
                }
            }

            if (name.equals(E_STORAGEALLOCATED)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.setStorageAllocated(Long.parseLong(value));
                }
            }
            
            if (name.equals(E_STORAGEUSED)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.setStorageUsed(Long.parseLong(value));
                }
            }
            
            if (name.equals(E_STARTDATE)) {
                reader.moveDown();
                startDate = (DateTime) context.convertAnother(startDate, DateTime.class);
                project.setStartDate(startDate);
                reader.moveUp();
            }
            
            if (name.equals(E_ENDDATE)) {
                reader.moveDown();
                endDate = (DateTime) context.convertAnother(endDate, DateTime.class);
                project.setEndDate(endDate);
                reader.moveUp();
            }
            
            if (name.equals(E_PRINCIPLEINVESTIGATORID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.addPi(value);
                }
            }

            if (name.equals(E_FUNDINGENTITY)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    project.setFundingEntity(value);
                }
            }

            reader.moveUp();
        }
        return project;
    }
    
    @Override
    public boolean canConvert(Class type) {
        return Project.class == type;
    }
}
