
package org.dataconservancy.ui.model.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.PersonName;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Created with IntelliJ IDEA. User: hvu Date: 10/11/12 Time: 1:02 PM To change
 * this template use File | Settings | File Templates.
 */
public class PersonNameConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        PersonName personName = (PersonName) source;
        if (personName != null) {
            if (!isEmptyOrNull(personName.getGivenNames())) {
                writer.startNode(E_GIVEN_NAMES);
                writer.setValue(personName.getGivenNames());
                writer.endNode();
            }

            if (!isEmptyOrNull(personName.getMiddleNames())) {
                writer.startNode(E_MIDDLE_NAMES);
                writer.setValue(personName.getMiddleNames());
                writer.endNode();
            }

            if (!isEmptyOrNull(personName.getFamilyNames())) {
                writer.startNode(E_FAMILY_NAMES);
                writer.setValue(personName.getFamilyNames());
                writer.endNode();
            }

            if (!isEmptyOrNull(personName.getPrefixes())) {
                writer.startNode(E_NAME_PREFIX);
                writer.setValue(personName.getPrefixes());
                writer.endNode();
            }

            if (!isEmptyOrNull(personName.getSuffixes())) {
                writer.startNode(E_NAME_SUFFIX);
                writer.setValue(personName.getSuffixes());
                writer.endNode();
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext unmarshallingContext) {
        PersonName personName = new PersonName();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if (getElementName(reader).equals(E_GIVEN_NAMES)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personName.setGivenNames(value.trim());
                }
            } else if (getElementName(reader).equals(E_FAMILY_NAMES)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personName.setFamilyNames(value.trim());
                }
            } else if (getElementName(reader).equals(E_MIDDLE_NAMES)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personName.setMiddleNames(value.trim());
                }
            } else if (getElementName(reader).equals(E_NAME_PREFIX)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personName.setPrefixes(value.trim());
                }
            } else if (getElementName(reader).equals(E_NAME_SUFFIX)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    personName.setSuffixes(value.trim());
                }
            }
            reader.moveUp();
        }
        return personName;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return aClass == PersonName.class;
    }
}
