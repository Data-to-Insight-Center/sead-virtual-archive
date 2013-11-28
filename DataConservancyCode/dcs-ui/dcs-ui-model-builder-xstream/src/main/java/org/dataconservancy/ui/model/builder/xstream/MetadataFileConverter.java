
package org.dataconservancy.ui.model.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.MetadataFile;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Serializes and de-serializes MetadataFile objects
 */
/*
 * TODO: if MetadataFile and DataFile is consolidated into one class. This test
 * is no longer needed. if MetadataFile and DataFile remain separate, remove
 * this TODO.
 */
public class MetadataFileConverter
        extends AbstractEntityConverter
        implements ConverterConstants {


    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final MetadataFile fileSource = (MetadataFile) source;
        if (fileSource != null) {

            if (!isEmptyOrNull(fileSource.getId())) {
                writer.addAttribute(E_ID, fileSource.getId());
            }

            if (!isEmptyOrNull(fileSource.getParentId())) {
                writer.startNode(E_PARENT_ID);
                writer.setValue(fileSource.getParentId());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getSource())) {
                writer.startNode(E_SOURCE);
                writer.setValue(fileSource.getSource());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getFormat())) {
                writer.startNode(E_FORMAT);
                writer.setValue(fileSource.getFormat());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getName())) {
                writer.startNode(E_NAME);
                writer.setValue(fileSource.getName());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getPath())) {
                writer.startNode(E_PATH);
                writer.setValue(fileSource.getPath());
                writer.endNode();
            }
            
            if (!isEmptyOrNull(fileSource.getMetadataFormatId())) {
                writer.startNode(E_METADATA_FORMAT);
                writer.setValue(fileSource.getMetadataFormatId());
                writer.endNode();
            }

        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        MetadataFile fileObject = new MetadataFile();
        fileObject.setId(reader.getAttribute(E_ID));

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);


            if (ename.equals(E_SOURCE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setSource(value.trim());
                }
            } else if (ename.equals(E_FORMAT)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setFormat(value.trim());
                }
            } else if (ename.equals(E_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setName(value.trim());
                }
            } else if (ename.equals(E_PATH)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setPath(value.trim());
                }
            } else if (ename.equals(E_METADATA_FORMAT)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setMetadataFormatId(value.trim());
                }
            } else if (ename.equals(E_PARENT_ID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setParentId(value.trim());
                }
            }    
            reader.moveUp();
        }

        return fileObject;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == MetadataFile.class;
    }
}
