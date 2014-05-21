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

import java.io.File;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.util.Constants;
import org.seadva.bagit.util.TarUtil;


public class TarBagHandler implements Handler{
    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        String unTarDir = packageDescriptor.getUntarredBagPath();
        String tarredBag = Constants.bagDir +packageDescriptor.getPackageName()+".tar";
        TarUtil.tarDirectory(new File(unTarDir), tarredBag);
        packageDescriptor.setBagPath(tarredBag);
        return packageDescriptor;
    }
}
