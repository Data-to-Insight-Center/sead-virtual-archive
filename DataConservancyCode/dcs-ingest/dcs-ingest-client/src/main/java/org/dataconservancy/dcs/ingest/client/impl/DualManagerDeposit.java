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
package org.dataconservancy.dcs.ingest.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.security.DigestInputStream;
import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import org.dataconservancy.dcs.ingest.client.DepositBuilder;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;

import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Uses two deposit managers (file and sip) in implementing deposit.
 */
class DualManagerDeposit
        implements DepositBuilder {

    private final String PREFIX = "entity:/";

    private int counter = 0;

    private final DepositManager sipManager;

    private final DepositManager fileManager;

    private final DcsModelBuilder modelBuilder;

    private final Dcp dcp;

    private String checksumAlgorithm;

    static {
        MimeUtil.registerMimeDetector(MagicMimeMimeDetector.class.getName());
        MimeUtil.registerMimeDetector(ExtensionMimeDetector.class.getName());
    }

    public DualManagerDeposit(Dcp initialDcp,
                              DcsModelBuilder builder,
                              DepositManager sip,
                              DepositManager file,
                              String algorithm) {
        dcp = initialDcp;
        sipManager = sip;
        fileManager = file;
        checksumAlgorithm = algorithm;
        modelBuilder = builder;
    }

    public String execute() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        modelBuilder.buildSip(dcp, out);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(HttpHeaderUtil.CONTENT_TYPE, "application/xml");
        metadata.put(HttpHeaderUtil.CONTENT_LENGTH, Integer
                .toString(out.size()));

        try {
            return sipManager
                    .deposit(new ByteArrayInputStream(out.toByteArray()),
                             "application/xml",
                             "http://dataconservancy.org/schemas/dcp/1.0",
                             metadata).getDepositID();
        } catch (PackageException e) {
            throw new RuntimeException(e);
        }
    }

    public Dcp getSip() {
        return dcp;
    }

    public <T extends DcsEntity> T add(T entity) {
        if (entity.getId() == null) {
            entity.setId(PREFIX + ++counter);
        } else {
            for (DcsEntity e : getEntities()) {
                if (e.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("There is already an entity with id "
                            + e.getId());
                }
            }
        }

        if (entity instanceof DcsCollection) {
            dcp.addCollection((DcsCollection) entity);
        } else if (entity instanceof DcsDeliverableUnit) {
            dcp.addDeliverableUnit((DcsDeliverableUnit) entity);
        } else if (entity instanceof DcsEvent) {
            dcp.addEvent((DcsEvent) entity);
        } else if (entity instanceof DcsFile) {
            dcp.addFile((DcsFile) entity);
        } else if (entity instanceof DcsManifestation) {
            dcp.addManifestation((DcsManifestation) entity);
        } else {
            throw new RuntimeException("unexpected entity " + entity.getClass());
        }
        return entity;
    }

    public DcsFile referenceFile(String path, String... manifestations) {

        if (manifestations.length == 0 && dcp.getManifestations().size() == 1) {
            return referenceFile(path, dcp.getManifestations().iterator()
                    .next().getId());
        } else if (manifestations.length == 0) {
            throw new IllegalStateException("Cannot add a file if there are no manifestations");
        }
        DcsFile file = newFile();

        URI pathUri = getUri(path);
        file.setSource(pathUri.toASCIIString());
        file.setName(getFileName(pathUri));

        for (String m : manifestations) {
            DcsManifestation manifestation = getManifestation(m);
            DcsManifestationFile dmf = new DcsManifestationFile();
            dmf.setRef(new DcsFileRef(file.getId()));
            manifestation.addManifestationFile(dmf);

            saveManifestation(manifestation);
        }

        return file;
    }

    public DcsFile referenceMetadata(String path, String... targets) {
        DcsFile file = newFile();

        URI pathUri = getUri(path);
        file.setSource(pathUri.toASCIIString());
        file.setName(getFileName(pathUri));

        for (String target : targets) {
            linkFileMetadata(file.getId(), target);
        }

        return file;
    }

    public DcsFile uploadFile(String path, String... manifestations) {

        if (manifestations.length == 0 && dcp.getManifestations().size() == 1) {
            return uploadFile(path, dcp.getManifestations().iterator().next()
                    .getId());
        } else if (manifestations.length == 0) {
            throw new IllegalStateException("Cannot add a file if there are no manifestations");
        }

        DcsFile file = newFile();

        uploadFile(file, path);

        for (String m : manifestations) {
            DcsManifestation manifestation = getManifestation(m);
            DcsManifestationFile dmf = new DcsManifestationFile();
            dmf.setRef(new DcsFileRef(file.getId()));
            manifestation.addManifestationFile(dmf);

            saveManifestation(manifestation);
        }

        return file;
    }

    @Override
    public DcsFile uploadMetadata(String path, String... targets) {
        DcsFile file = newFile();

        uploadFile(file, path);

        for (String target : targets) {
            linkFileMetadata(file.getId(), target);
        }

        return file;
    }

    private DcsFile newFile() {
        DcsFile file = new DcsFile();
        file.setId(PREFIX + ++counter);

        dcp.addFile(file);
        return new LiveFileWrapper(file, dcp);
    }

    private DcsManifestation getManifestation(String id) {
        for (DcsManifestation m : dcp.getManifestations()) {
            if (m.getId().equals(id)) {
                return m;
            }
        }

        throw new IllegalStateException("No manifestation present with id "
                + id);
    }

    void uploadFile(DcsFile file, String path) {
        try {

            File physicalFile = new File(path);

            DigestInputStream digestStream =
                    new DigestInputStream(new FileInputStream(path),
                                          MessageDigest
                                                  .getInstance(checksumAlgorithm));

            String mime =
                    MimeUtil.getMostSpecificMimeType(MimeUtil
                            .getMimeTypes(new File(path))).toString();

            /*
             * proceed to the end of the inputStream if we havent got there
             * apready. We need to visit every byte in order to have calculated
             * the digest.
             */
            if (digestStream.read() != -1) {
                byte[] buf = new byte[1024];
                while (digestStream.read(buf) != -1);
            }

            byte[] digest = digestStream.getMessageDigest().digest();

            /* Set the file name */
            file.setName(physicalFile.getName());

            /* Set the calculated fixity */
            DcsFixity fixity = new DcsFixity();
            fixity.setAlgorithm(checksumAlgorithm);
            fixity.setValue(new String(Hex.encodeHex(digest)));
            file.addFixity(fixity);

            /* Set the format */
            DcsFormat format = new DcsFormat();
            format.setSchemeUri("http://www.iana.org/assignments/media-types/");
            format.setFormat(mime);
            file.addFormat(format);

            long length = physicalFile.length();

            file.setSource(doUpload(path, mime, digest, length));
            file.setSizeBytes(length);
            file.setExtant(true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String doUpload(String path, String mime, byte[] cksum, long size)
            throws IOException, PackageException {
        File filePath = new File(path);
        FileInputStream is = new FileInputStream(filePath);

        Map<String, String> metadata = new HashMap<String, String>();
        HttpHeaderUtil.addDigest(checksumAlgorithm, cksum, metadata);
        metadata.put(HttpHeaderUtil.CONTENT_TYPE, mime);
        metadata.put(HttpHeaderUtil.CONTENT_DISPOSITION, "filename="
                + filePath.getName());
        metadata.put(HttpHeaderUtil.CONTENT_LENGTH, Long.toString(size));

        DepositInfo info = fileManager.deposit(is, mime, null, metadata);
        try {
            is.close();
        } catch (IOException e) {

        }

        return info.getMetadata().get("X-dcs-src");
    }

    private void linkFileMetadata(String fileid, String target) {

        DcsMetadataRef mdref = new DcsMetadataRef(fileid);
        boolean found = false;

        Set<DcsDeliverableUnit> deliveriableUnits =
                new HashSet<DcsDeliverableUnit>();
        for (DcsDeliverableUnit d : dcp.getDeliverableUnits()) {
            if (d.getId().equals(target)) {
                d.addMetadataRef(mdref);
                found = true;
            }
            deliveriableUnits.add(d);
        }
        if (found) {
            dcp.setDeliverableUnits(deliveriableUnits);
            return;
        }

        Set<DcsFile> files = new HashSet<DcsFile>();
        for (DcsFile f : dcp.getFiles()) {
            if (f.getId().equals(target)) {
                f.addMetadataRef(mdref);
                found = true;
            }
            files.add(f);
        }
        if (found) {
            dcp.setFiles(files);
        }

        Set<DcsCollection> collections = new HashSet<DcsCollection>();
        for (DcsCollection c : dcp.getCollections()) {
            if (c.getId().equals(target)) {
                c.addMetadataRef(mdref);
                found = true;
            }
            collections.add(c);
        }
        if (found) {
            dcp.setCollections(collections);
            return;
        }

        Set<DcsManifestation> manifestations = new HashSet<DcsManifestation>();
        for (DcsManifestation m : dcp.getManifestations()) {
            if (m.getId().equals(target)) {
                m.addMetadataRef(mdref);
                found = true;
            }
            manifestations.add(m);
        }
        if (found) {
            dcp.setManifestations(manifestations);
            return;
        }

        if (!found) {
            throw new IllegalStateException("There is no entity with id "
                    + target);
        }
    }

    private void saveManifestation(DcsManifestation man) {
        Set<DcsManifestation> manifestations = new HashSet<DcsManifestation>();

        for (DcsManifestation m : dcp.getManifestations()) {
            if (m.getId().equals(man.getId())) {
                manifestations.add(man);
            } else {
                manifestations.add(m);
            }
        }

        dcp.setManifestations(manifestations);
    }

    private Collection<DcsEntity> getEntities() {
        ArrayList<DcsEntity> entities = new ArrayList<DcsEntity>();
        entities.addAll(dcp.getCollections());
        entities.addAll(dcp.getDeliverableUnits());
        entities.addAll(dcp.getEvents());
        entities.addAll(dcp.getFiles());
        entities.addAll(dcp.getManifestations());
        return entities;
    }

    private URI getUri(String path) {
      
        try {
                if (path.startsWith("/")) {
                    return new URI("file://" + path);
                } else {
                    return new URI("file:///" + path);
                }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(URI uriPath) {
        String[] parts = uriPath.getPath().split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return uriPath.getPath();
        }
    }
}
