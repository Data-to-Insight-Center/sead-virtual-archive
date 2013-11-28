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

package org.dataconservancy.ui.model.builder.xstream;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * An XStream implementation of a serializer and deserializer for Person
 * business objects suitable for use in applications and storage in the DCS
 * archive.
 */
public class PersonConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final Person personSource = (Person) source;
        if (personSource != null) {

            if (!isEmptyOrNull(personSource.getId())) {
                writer.addAttribute(E_ID, personSource.getId());
            }

            if (!isEmptyOrNull(personSource.getPrefix())) {
                writer.startNode(E_PREFIX);
                writer.setValue(personSource.getPrefix());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getFirstNames())) {
                writer.startNode(E_FIRST_NAME);
                writer.setValue(personSource.getFirstNames());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getMiddleNames())) {
                writer.startNode(E_MIDDLE_NAME);
                writer.setValue(personSource.getMiddleNames());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getLastNames())) {
                writer.startNode(E_LAST_NAME);
                writer.setValue(personSource.getLastNames());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getSuffix())) {
                writer.startNode(E_SUFFIX);
                writer.setValue(personSource.getSuffix());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getEmailAddress())) {
                writer.startNode(E_EMAIL_ADDRESS);
                writer.setValue(personSource.getEmailAddress());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getPreferredPubName())) {
                writer.startNode(E_PREFERRED_PUB_NAME);
                writer.setValue(personSource.getPreferredPubName());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getBio())) {
                writer.startNode(E_BIO);
                writer.setValue(personSource.getBio());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getWebsite())) {
                writer.startNode(E_WEBSITE);
                writer.setValue(personSource.getWebsite());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getCity())) {
                writer.startNode(E_CITY);
                writer.setValue(personSource.getCity());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getState())) {
                writer.startNode(E_STATE);
                writer.setValue(personSource.getState());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getJobTitle())) {
                writer.startNode(E_JOB_TITLE);
                writer.setValue(personSource.getJobTitle());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getDepartment())) {
                writer.startNode(E_DEPARTMENT);
                writer.setValue(personSource.getDepartment());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getInstCompany())) {
                writer.startNode(E_INST_COMPANY);
                writer.setValue(personSource.getInstCompany());
                writer.endNode();
            }
            
            if (!isEmptyOrNull(personSource.getInstCompanyWebsite())) {
                writer.startNode(E_INST_COMPANY_WEBSITE);
                writer.setValue(personSource.getInstCompanyWebsite());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getPhoneNumber())) {
                writer.startNode(E_PHONE_NUMBER);
                writer.setValue(personSource.getPhoneNumber());
                writer.endNode();
            }

            if (!isEmptyOrNull(personSource.getPassword())) {
                writer.startNode(E_PASSWORD);
                writer.setValue(personSource.getPassword());
                writer.endNode();
            }

            writer.startNode(E_EXTERNAL_STORAGE_LINKED);
            if (personSource.isExternalStorageLinked()) {
                writer.setValue("true");
            }
            else {
                writer.setValue("false");
            }
            writer.endNode();
            
            if (!isEmptyOrNull(personSource.getDropboxAppKey())) {
                writer.startNode(E_DROPBOX_APP_KEY);
                writer.setValue(personSource.getDropboxAppKey());
                writer.endNode();
            }
            
            if (!isEmptyOrNull(personSource.getDropboxAppSecret())) {
                writer.startNode(E_DROPBOX_APP_SECRET);
                writer.setValue(personSource.getDropboxAppSecret());
                writer.endNode();
            }
            
            if (personSource.getRegistrationStatus() != null) {
                writer.startNode(E_REGISTRATION_STATUS);
                writer.setValue(personSource.getRegistrationStatus().name());
                writer.endNode();
            }

            writer.startNode(E_READ_ONLY);
            writer.setValue(personSource.getReadOnly() ? "true" : "false");
            writer.endNode();

            List<Role> roleList = personSource.getRoles();
            if (roleList != null && !roleList.isEmpty()) {
                for (Role role : roleList) {
                    writer.startNode(E_ROLE);
                    writer.setValue(role.toString());
                    writer.endNode();
                }
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        Person personObject = new Person();
        personObject.setId(reader.getAttribute(E_ID));
        List<Role> roleList = new ArrayList<Role>();


        final String id = reader.getAttribute(E_ID);
        if(!isEmptyOrNull(id)){
            personObject.setId(id);
        }


        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals(E_PREFIX)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setPrefix(value.trim());
                }
            } else if (ename.equals(E_FIRST_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setFirstNames(value.trim());
                }
            } else if (ename.equals(E_MIDDLE_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setMiddleNames(value.trim());
                }
            } else if (ename.equals(E_LAST_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setLastNames(value.trim());
                }
            } else if (ename.equals(E_SUFFIX)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setSuffix(value.trim());
                }
            } else if (ename.equals(E_EMAIL_ADDRESS)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setEmailAddress(value.trim());
                }
            } else if (ename.equals(E_PREFERRED_PUB_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setPreferredPubName(value.trim());
                }
            } else if (ename.equals(E_BIO)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setBio(value.trim());
                }
            } else if (ename.equals(E_WEBSITE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setWebsite(value.trim());
                }
            } else if (ename.equals(E_CITY)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setCity(value.trim());
                }
            } else if (ename.equals(E_STATE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setState(value.trim());
                }
            } else if (ename.equals(E_JOB_TITLE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setJobTitle(value.trim());
                }
            } else if (ename.equals(E_DEPARTMENT)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setDepartment(value.trim());
                }
            } else if (ename.equals(E_INST_COMPANY)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setInstCompany(value.trim());
                }
            } else if (ename.equals(E_INST_COMPANY_WEBSITE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setInstCompanyWebsite(value.trim());
                }
            } else if (ename.equals(E_PHONE_NUMBER)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setPhoneNumber(value.trim());
                }
            } else if (ename.equals(E_PASSWORD)) {
                    final String value = reader.getValue();
                    if (!isEmptyOrNull(value)) {
                        personObject.setPassword(value.trim());
                }
            } else if (ename.equals(E_EXTERNAL_STORAGE_LINKED)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setExternalStorageLinked(Boolean.parseBoolean(value.trim()));
                }
            } else if (ename.equals(E_DROPBOX_APP_KEY)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setDropboxAppKey(value.trim());
                }
            } else if (ename.equals(E_DROPBOX_APP_SECRET)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setDropboxAppSecret(value.trim());
                } 
            } else if (ename.equals(E_REGISTRATION_STATUS)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    RegistrationStatus status =
                            RegistrationStatus.valueOf(value.trim()
                                    .toUpperCase());
                    personObject.setRegistrationStatus(status);
                }
            } else if (ename.equals(E_READ_ONLY)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personObject.setReadOnly(value.trim().equals("true") ? true
                            : false);
                }
            } else if (ename.equals(E_ROLE)) {
                final String value = reader.getValue();
                 if (!isEmptyOrNull(value)) {
                     roleList.add(Role.valueOf(value.trim().toUpperCase()));
                 }
            }
            reader.moveUp();
        }
        personObject.setRoles(roleList);
        return personObject;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == Person.class;
    }

}