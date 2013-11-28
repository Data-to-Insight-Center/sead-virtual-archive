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
package org.dataconservancy.dcs.access.server.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.access.client.upload.model.Collection;
import org.dataconservancy.dcs.access.client.upload.model.CoreMetadata;
import org.dataconservancy.dcs.access.client.upload.model.DeliverableUnit;
import org.dataconservancy.dcs.access.client.upload.model.File;
import org.dataconservancy.dcs.access.client.upload.model.Metadata;
import org.dataconservancy.dcs.access.client.upload.model.Package;
import org.dataconservancy.dcs.access.client.upload.model.Repository;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadRepository;
import org.seadva.model.pack.ResearchObject;

public class PackageUtil {

    public static Dcp constructDcp(Package pkg) {
        Dcp dcp = new Dcp();

        for (Collection col : pkg.collections()) {
            dcp.addCollection(constructCollection(col));
        }

        for (DeliverableUnit du : pkg.deliverableUnits()) {
            dcp.addDeliverableUnit(constructDeliverableUnit(du));
        }

        for (File file : pkg.files()) {
            dcp.addFile(constructFile(file));
        }

        for (org.dataconservancy.dcs.access.client.upload.model.Repository repo : pkg.getRepositories()){
        	((ResearchObject)dcp).addRepository(constructRepo(repo));
        }
        
        // Infer manifestations from dus and files

        int manseq = 0;
        
        // work around copy by value in model
        Set<DcsManifestation> mans = new HashSet<DcsManifestation>();

        for (DeliverableUnit du : pkg.deliverableUnits()) {
            for (String fileref : du.files()) {
                File file = null;

                for (File f : pkg.files()) {
                    if (f.getId().equals(fileref)) {
                        file = f;
                        break;
                    }
                }

                if (file == null) {
                    // TODO reference to unknown file
                } else {
                    boolean foundexisting = false;

                    for (DcsManifestation man : mans) {
                        String env = file.getTechnicalEnviroment();
                        
                        if (du.getId().equals(man.getDeliverableUnit())
                             )
                        	//&& (man.getTechnicalEnvironment().contains(env))
                                //|| (env.isEmpty() && man
                                     //   .getTechnicalEnvironment().isEmpty())) 
                        	{
                            // Add to existing manifestation
                            DcsManifestationFile mf =
                                    new DcsManifestationFile();
                            DcsFileRef ref = new DcsFileRef();
                            ref.setRef(file.getId());
                            mf.setRef(ref);

                            man.addManifestationFile(mf);
                            
                            foundexisting = true;
                            break;
                        }
                    }

                    if (!foundexisting) {
                        DcsManifestation man = new DcsManifestation();

                        man.setId("man" + manseq++);
                        man.setDeliverableUnit(du.getId());

                        if (!file.getTechnicalEnviroment().isEmpty()) {
                            man.addTechnicalEnvironment(file
                                    .getTechnicalEnviroment());
                        }

                        DcsManifestationFile mf = new DcsManifestationFile();
                        DcsFileRef ref = new DcsFileRef();
                        ref.setRef(file.getId());
                        mf.setRef(ref);

                        man.addManifestationFile(mf);

                        mans.add(man);
                    }
                }
            }
        }

        dcp.setManifestations(mans);
        
        return dcp;
    }

    private static DcsCollection constructCollection(Collection col) {
        DcsCollection result = new DcsCollection();

        result.setId(col.getId());
        result.setMetadata(constructMetadataSet(col.metadata()));

        CoreMetadata cmd = col.getCoreMetadata();

        if (!cmd.getTitle().isEmpty()) {
            result.setTitle(cmd.getTitle());
        }

        if (!cmd.getType().isEmpty()) {
            result.setType(cmd.getType());
        }

       // result.setCreators(new HashSet<String>(cmd.creators()));
        result.setSubjects(new HashSet<String>(cmd.subjects()));

        if (!col.getParent().isEmpty()) {
            DcsCollectionRef ref = new DcsCollectionRef();
            ref.setRef(col.getParent());
            result.setParent(ref);
        }

        return result;
    }

    private static DcsDeliverableUnit constructDeliverableUnit(DeliverableUnit du) {
        DcsDeliverableUnit result = new DcsDeliverableUnit();

        result.setId(du.getId());
        result.setMetadata(constructMetadataSet(du.metadata()));
        ((SeadDeliverableUnit)result).addSite(du.getLocation());
        ((SeadDeliverableUnit)result).setPubdate(du.getPubdate());

        CoreMetadata cmd = du.getCoreMetadata();

        if (!cmd.getTitle().isEmpty()) {
            result.setTitle(cmd.getTitle());
        }

        if (!cmd.getType().isEmpty()) {
            result.setType(cmd.getType());
        }
       
        
        if(cmd.getAbstrct()!=null){
            ((SeadDeliverableUnit)result).setAbstrct(cmd.getAbstrct());
        }
        
        if(cmd.getContact()!=null){
            ((SeadDeliverableUnit)result).setContact(cmd.getContact());
        }
      

       // result.setCreators(new HashSet<String>(cmd.creators()));
        result.setSubjects(new HashSet<String>(cmd.subjects()));

        for (String id : du.parents()) {
            if (id == null || id.isEmpty()) {
                continue;
            }

            DcsDeliverableUnitRef ref = new DcsDeliverableUnitRef();
            ref.setRef(id);
            result.addParent(ref);
        }

        for (String id : du.collections()) {
            if (id == null || id.isEmpty()) {
                continue;
            }

            DcsCollectionRef ref = new DcsCollectionRef();
            ref.setRef(id);
            result.addCollection(ref);
        }

        return result;
    }

    private static Set<DcsMetadata> constructMetadataSet(List<Metadata> set) {
        Set<DcsMetadata> result = new HashSet<DcsMetadata>();

        for (Metadata md : set) {
            DcsMetadata dmd = new DcsMetadata();

            if (!md.getSchema().isEmpty()) {
                dmd.setSchemaUri(md.getSchema());
            }

            if (!md.getContent().isEmpty()) {
                dmd.setMetadata(md.getContent());
            }

            result.add(dmd);
        }

        return result;
    }

    private static DcsFile constructFile(File file) {
        DcsFile result = new DcsFile();

        result.setId(file.getId());
        result.setMetadata(constructMetadataSet(file.metadata()));

        if (!file.getName().isEmpty()) {
            result.setName(file.getName());
        }

        if (!file.getSource().isEmpty()) {
            result.setSource(file.getSource());
        }
        
       // result.setValid(true);
        result.setExtant(true);

        return result;
    }
    
    private static SeadRepository constructRepo(Repository repo) {
        SeadRepository result = new SeadRepository();

        result.setIrId(repo.getId());
        result.setName(repo.getName());
        result.setType(repo.getType());
        result.setUrl(repo.getUrl());

        return result;
    }
}
