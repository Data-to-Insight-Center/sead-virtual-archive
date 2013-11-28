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
package org.dataconservancy.transform.dcp;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.springframework.beans.factory.annotation.Required;

public class ArchiveStoreDcpBuilder
        implements Mapping<String, String, String, Dcp> {

    private ArchiveStore archive;

    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @Required
    public void setArchiveStore(ArchiveStore arch) {
        archive = arch;
    }

    @Override
    public void map(String key, String val, Output<String, Dcp> output) {
        try {
            Dcp dcp = builder.buildSip(archive.getPackage(val));
            output.write(val, dcp);
        } catch (EntityNotFoundException c) {
            /* Do nothing, just don't return */
        } catch (InvalidXmlException e) {
            throw new RuntimeException(e);
        }
    }

}
