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
package org.dataconservancy.dcs.index.rebuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;

public class Metadata {

    private final String entityId;

    private final Class<? extends DcsEntity> type;

    private final String fileId;

    private final File dir;

    String[] words =
            "Lorem ipsum dolor amet consectetur adipiscing elit Nunc odio posuere rhoncus Sed faucibus metus pellentesque mattis laoreet Duis vitae purus pulvinar velit cursus dictum ut quis turpis. Nulla sed velit sed metus hendrerit dapibus. Morbi at felis justo, vitae eleifend quam. Maecenas eu eros dictum sem gravida mattis a in nisi. Sed convallis nisi sit amet nisl bibendum sed sollicitudin augue auctor. Donec pharetra nunc ut lorem fringilla vitae dapibus arcu luctus. Maecenas commodo lacus sed orci mattis nec consequat turpis iaculis. Suspendisse sollicitudin posuere nisl at malesuada. Duis ipsum erat, aliquam vel luctus a, facilisis sit amet mi. Maecenas tempus, turpis sit amet eleifend consectetur, nibh nisi sodales sem, nec ultrices est leo porta sem. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Morbi eu odio at purus tincidunt facilisis et vel purus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nullam nec sem vitae libero tempus ullamcorper sed sed leo. Duis condimentum elit ac ligula aliquam a cursus nibh bibendum"
                    .split(" ");

    int count = 0;

    Metadata(String entityId,
             Class<? extends DcsEntity> type,
             String fileId,
             File dir) {

        this.entityId = entityId;

        this.type = type;

        this.fileId = fileId;

        this.dir = dir;

    }

    public String getDescribingFileId() {
        return fileId;
    }

    public String getDescribedEntityId() {
        return entityId;
    }

    public Class<? extends DcsEntity> getDescribedEntityType() {
        return type;
    }

    public DcsFile toFile() {

        OutputStream out = null;
        File f = null;
        try {
            f = new File(dir, String.format("%s.xml", fileId));
            out = new FileOutputStream(f);
            out.write(String.format("<metadata>" + "<entityId>%s</entityId>"
                                            + "<type>%s</type>"
                                            + "<fileId>%s</fileId>"
                                            + "</metadata>",
                                    englishify(entityId),
                                    type.getName(),
                                    englishify(fileId)).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        DcsFormat format = new DcsFormat();
        format.setSchemeUri("www.iana.org/assignments/media-types/");
        format.setName("text/xml");

        DcsFile file = new DcsFile();
        file.setId(fileId);
        file.setSizeBytes(f.length());
        file.setSource(f.toURI().toString());
        file.setExtant(true);
        file.setName(fileId + ".xml");
        file.addFormat(format);

        return file;
    }

    private String englishify(String val) {
        //return "cow_says_moo_mo.oo_m00_m0--0-00_moooooooooooooooooooooooooooooooooooooooooooo";
        //return "metadata_file_for_coll_0_0.7ccbdce7-6723-47ed-b684-be742166ec29";
        //return val;
        return words[count++];
    }
}
