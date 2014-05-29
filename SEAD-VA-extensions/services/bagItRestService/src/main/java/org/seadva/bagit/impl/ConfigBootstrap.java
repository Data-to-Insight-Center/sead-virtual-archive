/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.seadva.bagit.impl;

import org.seadva.bagit.api.Bootstrap;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.event.impl.*;

/**
 * Loads events and handler mappings
 */
public class ConfigBootstrap implements Bootstrap {
    public static PackageListener packageListener = new PackageListener();
    @Override
    public void load() {

        packageListener.map(Event.UNZIP_BAG, UnzipBagHandler.class.getName());
        packageListener.map(Event.GENERATE_FGDC, FgdcGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_ORE, OreGenerationHandler.class.getName());
        packageListener.map(Event.PARSE_FETCH, FetchParseHandler.class.getName());
        packageListener.map(Event.PARSE_DIRECTORY, DirectoryParseHandler.class.getName());
        packageListener.map(Event.PARSE_ACR_COLLECTION, AcrQueryHandler.class.getName());
        packageListener.map(Event.GENERATE_SIP, SipGenerationHandler.class.getName());
        packageListener.map(Event.ZIP_BAG, ZipBagHandler.class.getName());
        packageListener.map(Event.GENERATE_MANIFEST, ManifestGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_FETCH, FetchGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_DATA_DIR, DataDirCreationHandler.class.getName());
        packageListener.map(Event.PARSE_SIP, SipParseHandler.class.getName());
        packageListener.map(Event.TAR_BAG,TarBagHandler.class.getName());
        packageListener.map(Event.UNTAR_BAG, UntarBagHandler.class.getName());
        packageListener.map(Event.GENERATE_BAGITTXT, BagItTxtGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_BAGINFO,BagInfoGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_TAGMANIFEST,TagManifestGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_DPNTAGFILE,DPNTagFileGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_DPNORE,DPNOreGenerationHandler.class.getName());
        packageListener.map(Event.GENERATE_DPNSIP,DPNSipGenerationHandler.class.getName());
    }
}
