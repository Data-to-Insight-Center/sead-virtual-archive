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

import org.dataconservancy.dcs.ingest.deposit.AbstractDepositDocument;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;

public abstract class FileDocumentBase
        extends AbstractDepositDocument {

    private final Dcp sipPackage;

    private DcsFile file;

    private boolean initialized = false;

    public FileDocumentBase(Dcp sip) {
        sipPackage = sip;
    }

    protected Dcp getDcp() {
        return sipPackage;
    }

    protected DcsFile getFile() {
        initIfNecessary();
        return file;
    }

    private void initIfNecessary() {
        if (initialized) return;

        initialized = true;
        file = sipPackage.getFiles().iterator().next();

    }
}
