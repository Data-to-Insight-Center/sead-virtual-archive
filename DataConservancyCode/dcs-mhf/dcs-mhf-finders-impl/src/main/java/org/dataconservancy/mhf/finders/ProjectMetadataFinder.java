/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.mhf.finders;

import org.dataconservancy.mhf.finder.api.MetadataFindingException;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Project;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeName.*;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.DATE_TIME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;

/**
 * Responsible for finding Business Object metadata on a {@code org.dataconservancy.ui.model.Project}.
 * <p/>
 * This implementation looks for relevant properties of the {@code Project}, with what is considered relevant has yet been
 * defined by specific use case.
 *
 */
public class ProjectMetadataFinder extends BusinessObjectMetadataFinder {

    public ProjectMetadataFinder(MetadataObjectBuilder builder) {
        super(builder);
    }

    /**
     * Extracting the following fields from an instance of {@link Project}:
     * <ul>
     *     <li>business id</li>
     *     <li>name/title</li>
     *     <li>discription</li>
     *     <li>project's start time</li>
     *     <li>project's end time</li>
     *     <li>project's award numbers</li>
     * </ul>
     *
     * @param bo {@link Project} instance
     * @return {@link MetadataAttributeSet} containing extracted attributes from a project object. If no expected metadata
     * was found, the {@link MetadataAttributeSet} will contain an empty {@code Set} of {@link MetadataAttribute}.
     *
     */
    @Override
    protected MetadataAttributeSet findCoreMetadata(BusinessObject bo) {
        checkObjectType(bo);
        Project project = (Project) bo;

        final MetadataAttributeSet attributeSet = super.findCoreMetadata(project);
        attributeSet.setName(MetadataAttributeSetName.PROJECT_CORE_METADATA);

        if (project.getName() != null) {
            attributeSet.addAttribute(new MetadataAttribute(TITLE, STRING, project.getName()));
        }

        if (project.getDescription() != null) {
            attributeSet.addAttribute(new MetadataAttribute(DESCRIPTION, STRING, project.getDescription()));
        }

        if (project.getStartDate() != null) {
            attributeSet.addAttribute(new MetadataAttribute(START_DATE, DATE_TIME,
                    String.valueOf(project.getStartDate())));
        }

        if (project.getEndDate() != null) {
            attributeSet.addAttribute(new MetadataAttribute(END_DATE, DATE_TIME,
                    String.valueOf(project.getEndDate())));
        }

        for (String number : project.getNumbers()) {
            attributeSet.addAttribute(new MetadataAttribute(AWARD_NUMER, STRING, number));
        }

        return attributeSet;

    }

    @Override
    protected MetadataAttributeSet findSystemMetadata(BusinessObject bo) {
        return super.findSystemMetadata(bo);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void checkObjectType(BusinessObject o) {
        if (!(o instanceof Project)) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    String.format(TYPE_ERROR, o.getClass().getName(), Project.class.getName()));
            throw new MetadataFindingException(iae.getMessage(), iae);
        }
    }
}
