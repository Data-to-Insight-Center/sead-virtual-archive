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
package org.dataconservancy.dcs.index.rebuild.dcpsolr;

import java.util.ArrayList;
import java.util.Iterator;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.transform.index.IndexOutputFactory;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.dcp.ArchiveServiceIdReader;
import org.dataconservancy.transform.dcp.ArchiveStoreDcpBuilder;
import org.dataconservancy.transform.execution.MappingChain;
import org.dataconservancy.transform.execution.run.Job;
import org.dataconservancy.transform.execution.run.Plan;

public class MultiplePassIndexRebuild<Ko, Vo>
        implements Plan {

    private ArchiveStore archive;

    private IndexOutputFactory<Ko, Vo> factory =
            new IndexOutputFactory<Ko, Vo>();

    private MappingChain<String, String, Ko, Vo> mapping;

    private int passes;

    public void setArchiveStore(ArchiveStore archiveImpl) {
        archive = archiveImpl;
    }

    public void setIndexService(IndexService<Vo> idx) {
        factory.setIndexService(idx);
    }

    public void setMapping(Mapping<String, Dcp, Ko, Vo> map) {

        mapping = new MappingChain<String, String, Ko, Vo>();

        ArchiveStoreDcpBuilder dcpBuilder = new ArchiveStoreDcpBuilder();
        dcpBuilder.setArchiveStore(archive);

        mapping.setChain(dcpBuilder, map);

    }

    public void setNumberOfPasses(int count) {
        passes = count;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Job<String, String, Ko, Vo>> getJobs() {
        ArrayList<Job<String, String, Ko, Vo>> stages =
                new ArrayList<Job<String, String, Ko, Vo>>();

        for (int i = 0; i < passes; i++) {

            stages.add(new Job<String, String, Ko, Vo>(new ArchiveServiceIdReader(archive),
                                                       mapping,
                                                       factory.newOutput()));
        }
        return stages.iterator();

    }

    @Override
    public String getLabel() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Index rebuilder that iterates the archive twice";
    }

}
