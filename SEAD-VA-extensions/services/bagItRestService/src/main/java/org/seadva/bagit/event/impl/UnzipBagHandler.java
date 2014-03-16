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
package org.seadva.bagit.event.impl;

import org.apache.commons.io.FileUtils;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.util.ZipUtil;

import java.io.File;
import java.io.IOException;

/**
 * Handler to unzip zipped file
 */
public class UnzipBagHandler implements Handler{
    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {
        String zippedBag = packageDescriptor.getBagPath();
        String unzipDir = zippedBag.replace(".zip","")+"/";

        try {
            if(new File(unzipDir).exists())
                FileUtils.deleteDirectory(new File(unzipDir));
            ZipUtil.unzip(zippedBag, unzipDir);
            packageDescriptor.setUnzippedBagPath(unzipDir);
        } catch (IOException e) {
            ;//ConfigBootstrap.packageListener.execute(Event.ERROR, packageDescriptor);
        }
        return packageDescriptor;
    }
}
