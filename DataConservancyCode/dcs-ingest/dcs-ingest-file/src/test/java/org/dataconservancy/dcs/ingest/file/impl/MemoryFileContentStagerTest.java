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
package org.dataconservancy.dcs.ingest.file.impl;

import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;

public class MemoryFileContentStagerTest
        extends FileContentStagerTest {

    private static SipStager sipStager = new MemoryStager();

    private static MemoryFileContentStager fileStager =
            new MemoryFileContentStager();

    static {
        fileStager.setSipStager(sipStager);
    }

    protected FileContentStager getStager() {
        return fileStager;
    }

    protected SipStager getSipStager() {
        return sipStager;
    }

}
