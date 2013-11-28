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
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.MetadataFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Responsible for finding {@code MetadataInstance} objects from bytestreams.
 * <p/>
 * This implementation is responsible for finding metadata that may be contained within the bytestream of the
 * {@code DataFile}.  This implementation does not extend {@code BusinessObjectMetadataFinder} because it is not
 * responsible for discovering metadata or properties of the business object; it is only responsible for looking
 * for the file bytestream and producing a {@code MetadataInstance}.
 *
 * @see DataFileBoMetadataFinder
 */
public class EmbeddedMetadataFinder extends BaseMetadataFinder {

    public EmbeddedMetadataFinder(MetadataObjectBuilder metadataObjectBuilder) {
        super(metadataObjectBuilder);
    }

    @Override
    public Collection<MetadataInstance> findMetadata(Object o) throws MetadataFindingException {
        if (!DataFile.class.isAssignableFrom(o.getClass())) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    String.format(TYPE_ERROR, o.getClass().getName(), DataFile.class.getName()));
            throw new MetadataFindingException(iae.getMessage(), iae);
        }

        final DataFile file = (DataFile) o;



        final String formatId;
        if (file instanceof MetadataFile) {
            formatId = ((MetadataFile) file).getMetadataFormatId();
        } else {
            formatId = file.getFormat();
        }
        final String fileSource = file.getSource();

        if (fileSource == null || fileSource.trim().length() == 0) {
            throw new MetadataFindingException(
                    String.format(STREAM_ERROR, fileSource, "The 'source' field of the DataFile was null."));
        }

        final URL fileIn;
        try {
            fileIn = new URL(fileSource);
        } catch (IOException e) {
            throw new MetadataFindingException(
                    String.format(STREAM_ERROR, fileSource, e.getMessage()), e);
        }

        final FileMetadataInstance metadataInstance = new FileMetadataInstance(formatId, fileIn);
        final Collection<MetadataInstance> instances = new ArrayList<MetadataInstance>();
        instances.add(metadataInstance);

        return instances;
    }

}
