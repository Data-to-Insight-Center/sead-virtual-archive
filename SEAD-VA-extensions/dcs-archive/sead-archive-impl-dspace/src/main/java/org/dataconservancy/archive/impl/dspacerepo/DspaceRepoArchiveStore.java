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

package org.dataconservancy.archive.impl.dspacerepo;

import org.apache.solr.client.solrj.SolrServerException;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.*;
import org.seadva.archive.SeadArchiveStore;
import org.seadva.model.*;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.net.URL;
import java.util.*;


/**
 * DSpace Archive Store
 */
public class DspaceRepoArchiveStore implements SeadArchiveStore {

    SeadXstreamStaxModelBuilder model = new SeadXstreamStaxModelBuilder();
    SeadDSpace dspaceClient;

    String title = "default_title";
    String creator = "";
    String seadCommunity =
            "";//-dataset Community default
    SeadDeliverableUnit rootDu ;
    Map<String,String> duDspaceCollection;

    private SolrService solrService;

    @Required
    public void setSolrService(SolrService sService) {
        solrService = sService;
    }

    @Override
    public Iterator<String> listEntities(EntityType type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //This returns file contents
    @Override
    public InputStream getContent(String entityId) throws EntityNotFoundException, EntityTypeException {
        //use a mapper, i.e. query solr and get the dspace location
        DcsEntity entity ;
        String url = null;

        try
        {
            entity = solrService.lookupEntity(entityId);

            if (entity instanceof SeadDeliverableUnit) {
                if(((SeadDeliverableUnit) entity).getPrimaryLocation()!=null)
                    url =((SeadDeliverableUnit) entity).getPrimaryLocation().getLocation();
            } else if (entity instanceof SeadFile) {
                url =((SeadFile) entity).getPrimaryLocation().getLocation();
//            } else if (entity instanceof DcsEvent) {
                ;//  return DcsSolrMapper.toSolr((DcsEvent) entity);
//            } else if (entity instanceof DcsManifestation) {
                ;// return DcsSolrMapper.toSolr((DcsManifestation) entity, store);
            } else {
                throw new IllegalArgumentException("Unhandled entity type: "
                        + entity.getClass().getName());
            }
        } catch (ClassCastException e) {
        } catch (SolrServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        InputStream input = null;
        try {
            input = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    @Override
    public InputStream getPackage(String entityId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public InputStream getFullPackage(String entityId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public void putPackage(InputStream dcpStream) throws AIPFormatException {
    }


    @Override
    public ResearchObject putResearchPackage(InputStream dcpStream) throws AIPFormatException {

        ResearchObject pkg = null;
        try {
            pkg = model.buildSip(dcpStream);
        } catch (InvalidXmlException e) {
            throw new AIPFormatException(e.getMessage());
        }

        Set<SeadRepository> institionalRepositories = pkg.getRepositories();

        duDspaceCollection = new HashMap<String, String>();
        manFile = new HashMap<String, List<DcsFile>>();


        Map<String,Credential> repoCredentials = null;
        try {
            repoCredentials = Util.loadCredentials(this.getClass().getResource("/RepositoryCredentials.xml").openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String repoName = "Default Dspace Respository";
        for(SeadRepository institionalRepository:institionalRepositories) {
            seadCommunity =  institionalRepository.getUrl();
            repoName = institionalRepository.getName();

            Credential  credential = repoCredentials.get((String)institionalRepository.getIrId());
            dspaceClient = new SeadDSpace(credential.getUsername(),credential.getPassword());
        }




        Iterator<DcsDeliverableUnit> iterator = pkg.getDeliverableUnits().iterator();


        //Create hierarchy
        TreeNode relations = getRelationsTree(pkg);

        //create a community for the project
        if(rootDu.getPrimaryLocation().getLocation()==null){
            projectCommunity = dspaceClient.createSubCommunity(seadCommunity,title,rootDu.getAbstrct());
            projectCommunity.setHandle(projectCommunity.getHandle()
            );
        }


        Collection<DcsDeliverableUnit> dus = pkg.getDeliverableUnits();

        for(DcsDeliverableUnit du:dus){
            if(rootDu.getId().equals(du.getId()))  {
                if(((SeadDeliverableUnit)du).getPrimaryLocation()==null){
                    ((SeadDeliverableUnit)du).getPrimaryLocation().setType("dspace");
                    String handle = projectCommunity.getHandle();
                    ((SeadDeliverableUnit)du).getPrimaryLocation().setLocation(handle);
                    ((SeadDeliverableUnit)du).getPrimaryLocation().setName(repoName);
                }
            }
        }

        pkg.setDeliverableUnits(dus);


        parseTree(relations);

        //put all items in a manifestation into a collection --  we do not consider multiple manifestations right now

        String firstDspaceCollection = null;
        int dspaceDefaultSet =0;
        Collection<DcsFile> files = pkg.getFiles();
        Iterator it = manFile.entrySet().iterator();
        while (it.hasNext()) {

            Map.Entry pairs = (Map.Entry)it.next();
            String dspaceCollection = duDspaceCollection.get(pairs.getKey());

            if(dspaceDefaultSet==0)
            {
                firstDspaceCollection = dspaceCollection;
                dspaceDefaultSet = 1;
            }
            //submit item to DSpace
            ArrayList<SeadFile> fileList = ((ArrayList<SeadFile>)pairs.getValue());
            //fileList is the list of all files in a manifestation

            HashMap<String,ArrayList<SeadFile>> shapeFiles = getShapeFiles(fileList);
            //shapefiles has map of shapefiles

            Iterator shpIt = shapeFiles.entrySet().iterator();

            while (shpIt.hasNext()) {
                Map.Entry pair = (Map.Entry)shpIt.next();

                ArrayList<SeadFile> shpFile = (ArrayList<SeadFile>)pair.getValue();

                String[] filepaths = new String[shpFile.size()];
                String[] filenames = new String[shpFile.size()];
                int i=0;
                String actualDoi ="";
                String doiPrefixedId =null;

                for(DcsFile f:shpFile)   {

                    DcsResourceIdentifier id = null;

                    Collection<DcsResourceIdentifier> alternateFileIds = f.getAlternateIds();
                    Iterator<DcsResourceIdentifier> fileIdIt = alternateFileIds.iterator();

                    while(fileIdIt.hasNext()){
                        id = fileIdIt.next();
                        if(id.getTypeId().equalsIgnoreCase("doi")){
                            actualDoi = id.getIdValue();
                            break;
                        }
                    }

                    filepaths[i]  = f.getSource().replace("file://","");
                    filenames[i] = f.getName();
                    i++;
                }

                dspaceClient.descriptiveMetadata((String)pair.getKey(), "",creator+"(Submitted as part of SEAD project)",actualDoi,rootDu.getRights());

                File targetDir = new File(System.getProperty("java.io.tmpdir"));

                String zippedPackage = dspaceClient.createPackage(filepaths, "CUSTODIAN", "ORGANIZATION", "MyOrganization",filenames, targetDir);

                Map<String,String> tUrl = dspaceClient.uploadPackage(dspaceCollection, true,zippedPackage);

                for(SeadFile f:shpFile)   {

                    String fileUrl = tUrl.get(f.getName());

                    for(DcsFile file1:files){
                        if(file1.getId().equals(f.getId())){
                            SeadDataLocation dataLocation = new SeadDataLocation();
                            dataLocation.setType("dspace");
                            fileUrl = fileUrl.replace("https://scholarworks.iu.edu/","http://maple.dlib.indiana.edu:8245/");
                            dataLocation.setLocation(fileUrl);
                            dataLocation.setName(repoName);
                            ((SeadFile)file1).setPrimaryLocation(dataLocation);
                        }
                    }
                }

                shpIt.remove();
            }

            it.remove();
        }

        //archive metadata files
        for(DcsMetadataRef metadataRef:rootDu.getMetadataRef()){
            for(DcsFile file:files){
                if(file.getId().equalsIgnoreCase(metadataRef.getRef())){
                    String[] tempArr = rootDu.getPrimaryLocation().getLocation().split("/");
                    int number = Integer.parseInt(tempArr[tempArr.length-1]);
                    String collectionId = rootDu.getPrimaryLocation().getLocation()
                            .replace("https://scholarworks.iu.edu/","http://maple.dlib.indiana.edu:8245/")
                            .replace("iuswdark/handle","sword/deposit");
                    System.out.print("submit to "+collectionId);
                    firstDspaceCollection = collectionId;
                    dspaceClient.descriptiveMetadata(file.getName(), "",creator+"(Submitted as part of SEAD project)","",rootDu.getRights());

                    File targetDir = new File(System.getProperty("java.io.tmpdir"));
                    String[] filepaths = new String[1] ;
                    String[] filenames = new String[1] ;
                    filepaths[0]  = file.getSource().replace("file://","");
                    filenames[0] = file.getName();
                    String zippedPackage = dspaceClient.createPackage(filepaths, "CUSTODIAN", "ORGANIZATION", "MyOrganization",filenames, targetDir);

                    Map<String,String> tUrl = dspaceClient.uploadPackage(collectionId, true,zippedPackage);
                    String fileUrl = tUrl.get(file.getName());

                    SeadDataLocation dataLocation = new SeadDataLocation();
                    dataLocation.setType("dspace");
                    fileUrl = fileUrl.replace("https://scholarworks.iu.edu/","http://maple.dlib.indiana.edu:8245/");
                    dataLocation.setLocation(fileUrl);
                    dataLocation.setName(repoName);
                    ((SeadFile)file).setPrimaryLocation(dataLocation);
                }
            }

        }
        pkg.setFiles(files);

        sipArchival(pkg,firstDspaceCollection);

        return pkg;
    }

    public void sipArchival(ResearchObject pkg, String dspaceCollection){
        //SIP Archival
        OutputStream os = null;
        String sipFileName = "sip";
        File sipFile = new File(sipFileName);

        try {
            if(!sipFile.exists())
                sipFile.createNewFile();
            os = new FileOutputStream(sipFileName);
            model.buildSip(pkg, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(os!=null){
            //Sword desposit to firstDspaceCollection
            dspaceClient.descriptiveMetadata("sip", "","(Submitted as part of SEAD project)","","");

            File targetDir = new File(System.getProperty("java.io.tmpdir"));

            String[] filepaths = new String[1];
            String[] filenames = new String[1];
            filepaths[0]  = sipFileName;
            filenames[0] = "sip.xml";
            String zippedPackage = dspaceClient.createPackage(filepaths, "CUSTODIAN", "ORGANIZATION", "MyOrganization",filenames, targetDir);
            dspaceClient.uploadPackage(dspaceCollection, true, zippedPackage);

        }


    }
    DSpaceCommunity projectCommunity;


    private void parseTree(TreeNode node){

        int i=1;
        if(node.children!=null)  //leaf nodes
        {
            List<TreeNode> children = node.children;

            for(TreeNode child:children)
            {
                if(child.title.equalsIgnoreCase("manifestation"))
                {
                    //any parent whose children are leaf nodes, start creating collection
                    String dspaceCollection =
                            dspaceClient.createCollection(projectCommunity.getId(),node.title+"_collection"+i);

                    //TODO : Do not create collections for empty deliverable units
                    duDspaceCollection.put(child.id,dspaceCollection);     //manifestation id for which a collection in DSpace has been created
                    i++;
                }
                else
                {
                    parseTree(child);
                }
            }

        }
    }

    Map<String, HashMap<String,String>> duChildren;
    private TreeNode getRelationsTree(ResearchObject pkg) {
        //Not handling multiple parents as of now

        duChildren = new HashMap<String, HashMap<String,String>>();

        String rootId = null;

        for(DcsDeliverableUnit du:pkg.getDeliverableUnits())
        {
            if(du.getParents().isEmpty())
            {
                rootDu = (SeadDeliverableUnit)du;
                rootId = du.getId();
                title = du.getTitle();
                Iterator creators = ((SeadDeliverableUnit)du).getDataContributors().iterator();
                int len=creator.length();
                int i=0;
                while(creators.hasNext()) {
                    i++;

                    creator += ((SeadPerson)creators.next()).getName();
                    if(i!=len)
                        creator+=",";

                }
                continue;
            }
            String parentId = null;
            for(DcsDeliverableUnitRef duRef:du.getParents())
            {
                parentId = duRef.getRef();
                break;
            }
            HashMap<String,String> children;
            if(duChildren.containsKey(parentId))
            {
                children = duChildren.get(parentId);
                duChildren.remove(parentId);
            }
            else
                children = new  HashMap<String,String>();

            children.put(du.getId(), du.getTitle());
            duChildren.put(parentId,children);

        }



        for(DcsManifestation man:pkg.getManifestations())
        {
            String parentId = man.getDeliverableUnit();
            HashMap<String,String> children;
            if(duChildren.containsKey(parentId))
            {
                children = duChildren.get(parentId);
                duChildren.remove(parentId);
            }
            else
                children = new  HashMap<String,String>();

            children.put(man.getId(), "manifestation");
            duChildren.put(parentId,children);
            //create another folder
            Iterator<DcsManifestationFile> mFiles = man.getManifestationFiles().iterator();
            while(mFiles.hasNext())
            {
                DcsManifestationFile mFile = mFiles.next();
                for(DcsFile file:pkg.getFiles())
                {
                    if(file.getId().equals(mFile.getRef().getRef()))
                    {
                        List<DcsFile> fTemp;
                        if(manFile.containsKey(man.getId()))
                        {
                            fTemp = manFile.get(man.getId());
                            manFile.remove(man.getId());
                        }
                        else
                            fTemp = new  ArrayList<DcsFile>() ;

                        fTemp.add(file);
                        manFile.put(man.getId(),fTemp) ;
                    }
                }
            }
        }

        return createTree(rootId,title);
    }

    Map<String,List<DcsFile>> manFile;

    TreeNode createTree(String parentId, String title)
    {
        TreeNode parentNode = new TreeNode(parentId,title);

        HashMap<String,String> children = duChildren.get(parentId);

        if(children!=null)
        {
            Iterator it = children.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                TreeNode childNode = createTree((String)pairs.getKey(), (String)pairs.getValue());
                parentNode.addChild(childNode);
                it.remove();
            }
        }
        return parentNode;
    }


    class TreeNode{
        List<TreeNode> children;
        String id;
        String title;

        TreeNode(String nodeId, String nodeTitle){
            this.children = new ArrayList<TreeNode>();
            this.id = nodeId;
            this.title = nodeTitle;
        }

        void addChild(TreeNode child){
            children.add(child);
        }


    }


    private HashMap<String,ArrayList<SeadFile>> getShapeFiles(ArrayList<SeadFile> filesList){

        HashMap<String,ArrayList<SeadFile>> shapeFiles =  new HashMap<String, ArrayList<SeadFile>>();
        for(SeadFile file:filesList){
            String fileName = file.getName();
            String name = fileName.split("\\.")[0];
            ArrayList<SeadFile> files;
            if(shapeFiles.containsKey(name))
                files = shapeFiles.get(name);
            else
                files = new ArrayList<SeadFile>();

            files.add(file);
            shapeFiles.put(name,files);
        }

        return shapeFiles;
    }
}