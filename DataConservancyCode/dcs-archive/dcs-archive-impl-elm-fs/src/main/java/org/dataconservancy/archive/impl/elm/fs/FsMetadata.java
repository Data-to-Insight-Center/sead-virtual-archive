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
package org.dataconservancy.archive.impl.elm.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import org.dataconservancy.archive.impl.elm.Metadata;

/**
 * Serializes metadata as a csv file.
 */
public class FsMetadata
        implements Metadata {

    private final String REL_SELF = "self";

    private final String REL_SRC = "src";

    private final String REL_LINK = "link";

    private final int TYPE = 1;

    private final int ID = 2;

    private final File mdFile;

    public FsMetadata(File file) {
        mdFile = file;
    }

    public FsMetadata(File file, String id, String type, String src) {
        mdFile = file;
        OutputStream out = null;
        try {
            out = FileUtils.openOutputStream(mdFile);
            write(out, REL_SELF, type, id);
            if (src != null) {
                write(out, REL_SRC, "", src);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error writing metadata file", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void addLinks(Map<String, String> idToType) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(mdFile, true);
            for (Map.Entry<String, String> entry : idToType.entrySet()) {
                write(out, REL_LINK, entry.getValue(), entry.getKey());
            }
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException("Could not write entity metadata", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String getId() {
        return getVal(REL_SELF, ID);
    }

    public String getSrc() {
        return getVal(REL_SRC, ID);
    }

    public String getType() {
        return getVal(REL_SELF, TYPE);
    }

    public Map<String, String> getLinks() {
        Map<String, String> links = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mdFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(REL_LINK)) {
                    String[] vals = line.split(",");
                    links.put(vals[ID], vals[TYPE]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading id", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return links;
    }

    private String getVal(String rel, int key) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mdFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(rel)) {
                    String[] vals = line.split(",");
                    if (vals[key].equals("")) {
                        return null;
                    } else {
                        return vals[key];
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading id", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private void write(OutputStream out, String rel, String type, String val)
            throws IOException {
        try {
            out.write(String.format("%s,%s,%s\n", rel, type, val)
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
