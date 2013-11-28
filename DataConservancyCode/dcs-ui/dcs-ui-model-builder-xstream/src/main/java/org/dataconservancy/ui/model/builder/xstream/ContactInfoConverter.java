
package org.dataconservancy.ui.model.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.Address;
import org.dataconservancy.ui.model.ContactInfo;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * ContactInfoConverter is used by xstream library to serialize and deserialize
 * {@link org.dataconservancy.ui.model.ContactInfo} objects.
 */
public class ContactInfoConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    public ContactInfoConverter() {

    }

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        ContactInfo contactInfo = (ContactInfo) source;

        if (contactInfo != null) {

            if (!isEmptyOrNull(contactInfo.getName())) {
                writer.startNode(E_CONTACT_NAME);
                writer.setValue(contactInfo.getName());
                writer.endNode();
            }

            if (!isEmptyOrNull(contactInfo.getRole())) {
                writer.startNode(E_CONTACT_ROLE);
                writer.setValue(contactInfo.getRole());
                writer.endNode();
            }

            if (!isEmptyOrNull(contactInfo.getEmailAddress())) {
                writer.startNode(E_CONTACT_EMAIL);
                writer.setValue(contactInfo.getEmailAddress());
                writer.endNode();
            }

            if (!isEmptyOrNull(contactInfo.getPhoneNumber())) {
                writer.startNode(E_CONTACT_PHONE);
                writer.setValue(contactInfo.getPhoneNumber());
                writer.endNode();
            }

            if (contactInfo.getPhysicalAddress() != null) {
                writer.startNode(E_CONTACT_ADDRESS);

                if (!isEmptyOrNull(contactInfo.getPhysicalAddress()
                        .getStreetAddress())) {
                    writer.startNode(E_STREET_ADDRESS);
                    writer.setValue(contactInfo.getPhysicalAddress()
                            .getStreetAddress());
                    writer.endNode();
                }

                if (!isEmptyOrNull(contactInfo.getPhysicalAddress().getCity())) {
                    writer.startNode(E_CITY);
                    writer.setValue(contactInfo.getPhysicalAddress().getCity());
                    writer.endNode();
                }

                if (!isEmptyOrNull(contactInfo.getPhysicalAddress().getState())) {
                    writer.startNode(E_STATE);
                    writer.setValue(contactInfo.getPhysicalAddress().getState());
                    writer.endNode();
                }

                if (!isEmptyOrNull(contactInfo.getPhysicalAddress()
                        .getZipCode())) {
                    writer.startNode(E_ZIP_CODE);
                    writer.setValue(contactInfo.getPhysicalAddress()
                            .getZipCode());
                    writer.endNode();
                }

                if (!isEmptyOrNull(contactInfo.getPhysicalAddress()
                        .getCountry())) {
                    writer.startNode(E_COUNTRY);
                    writer.setValue(contactInfo.getPhysicalAddress()
                            .getCountry());
                    writer.endNode();
                }

                writer.endNode();
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext unmarshallingContext) {
        ContactInfo ci = null;
        if (getElementName(reader).equals(E_COL_CONTACT_INFO)) {
            ci = new ContactInfo();

            while (reader.hasMoreChildren()) {
                reader.moveDown();
                if (getElementName(reader).equals(E_CONTACT_NAME)) {
                    final String value = reader.getValue();
                    if (!isEmptyOrNull(value)) {
                        ci.setName(value.trim());
                    }
                } else if (getElementName(reader).equals(E_CONTACT_ROLE)) {
                    final String value = reader.getValue();
                    if (!isEmptyOrNull(value)) {
                        ci.setRole(value.trim());
                    }
                } else if (getElementName(reader).equals(E_CONTACT_EMAIL)) {
                    final String value = reader.getValue();
                    if (!isEmptyOrNull(value)) {
                        ci.setEmailAddress(value.trim());
                    }
                } else if (getElementName(reader).equals(E_CONTACT_PHONE)) {
                    final String value = reader.getValue();
                    if (!isEmptyOrNull(value)) {
                        ci.setPhoneNumber(value.trim());
                    }
                } else if (getElementName(reader).equals(E_CONTACT_ADDRESS)) {
                    Address address = new Address();
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        if (getElementName(reader).equals(E_STREET_ADDRESS)) {
                            final String value = reader.getValue();
                            if (!isEmptyOrNull(value)) {
                                address.setStreetAddress(value.trim());
                            }
                        } else if (getElementName(reader).equals(E_CITY)) {
                            final String value = reader.getValue();
                            if (!isEmptyOrNull(value)) {
                                address.setCity(value.trim());
                            }
                        } else if (getElementName(reader).equals(E_STATE)) {
                            final String value = reader.getValue();
                            if (!isEmptyOrNull(value)) {
                                address.setState(value.trim());
                            }
                        } else if (getElementName(reader).equals(E_ZIP_CODE)) {
                            final String value = reader.getValue();
                            if (!isEmptyOrNull(value)) {
                                address.setZipCode(value.trim());
                            }
                        } else if (getElementName(reader).equals(E_COUNTRY)) {
                            final String value = reader.getValue();
                            if (!isEmptyOrNull(value)) {
                                address.setCountry(value.trim());
                            }
                        }
                        reader.moveUp();
                    }
                    ci.setPhysicalAddress(address);
                }
                reader.moveUp();
            }
        }
        return ci;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return aClass == ContactInfo.class;
    }
}
