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
package org.dataconservancy.dcs.integration.bootstrap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.ingest.file.StagedFileUploadManager;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class MetadataFormatRegistryBootstrap {

    static final String FILE_PREFIX = "file:/";
    static final String CLASSPATH_PREFIX = "classpath:";
    static final String APPLICATION_XML = "application/xml";
    static final String DCP_PACKAGING = "http://dataconservancy.org/schemas/dcp/1.0";
    static final String SRC_HEADER = "X-dcs-src";

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TypedRegistry<DcsMetadataFormat> memoryRegistry;
    private LookupQueryService queryService;
    private DepositManager manager;
    private StagedFileUploadManager fileManager;
    private DcsModelBuilder modelBuilder;
    private boolean isDisabled = false;

    public MetadataFormatRegistryBootstrap(DepositManager manager, StagedFileUploadManager fileManager, TypedRegistry<DcsMetadataFormat> memoryRegistry,
                                           DcsDataModelQueryService queryService,
                                           DcsModelBuilder modelBuilder) {
        if (manager == null) {
            throw new IllegalArgumentException("Deposit Manager must not be null.");
        }
        
        if (fileManager == null) {
            throw new IllegalArgumentException("File Deposit Manager must not be null.");
        }

        if (memoryRegistry == null) {
            throw new IllegalArgumentException("The memory registry must not be null.");

        }

        if (queryService == null) {
            throw new IllegalArgumentException("The query service must not be null.");
        }

        if (modelBuilder == null) {
            throw new IllegalArgumentException("The model service must not be null.");
        }

        this.manager = manager;
        this.fileManager = fileManager;
        this.memoryRegistry = memoryRegistry;
        this.queryService = new LookupQueryServiceImpl(queryService);
        this.modelBuilder = modelBuilder;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public void bootstrapFormats() throws InterruptedException, IOException {

        if (isDisabled) {
            log.info("MetadataFormatRegistryBootstrap is disabled; not executing bootstrapping process.");
            return;
        }

        long bootStart = System.currentTimeMillis();
        log.info("Bootstrapping the DCS... ");
        MetadataSchemeMapper schemeMapper = new MetadataSchemeMapper();

        MetadataFormatMapper mapper = new MetadataFormatMapper(schemeMapper);

        Iterator<RegistryEntry<DcsMetadataFormat>> iter = memoryRegistry.iterator();
        
        List<DepositInfo> depositStatus = new ArrayList<DepositInfo>();

        while (iter.hasNext()) {
            RegistryEntry<DcsMetadataFormat> registryEntry = iter.next();
           
            try {
                if (queryService.lookup(registryEntry.getId()) != null) {
                    // The archive already has the entry
                    log.debug("Not bootstrapping registry entry {}: it is already in the archive.",
                            registryEntry.getId());
                    continue;
                }
            } catch (QueryServiceException e) {
                log.warn("Error depositing DCP (skipping it): " + e.getMessage(), e);
                continue;
            }

            final Dcp entryDcp = mapper.to(registryEntry, null);

            for (DcsFile dcsFile : entryDcp.getFiles()) {
                final Resource r;
                if (dcsFile.getSource().startsWith(FILE_PREFIX)) {
                    r = new FileSystemResource(new URL(dcsFile.getSource()).getPath());
                } else if (dcsFile.getSource().startsWith(CLASSPATH_PREFIX)) {
                    r = new ClassPathResource(dcsFile.getSource().substring(CLASSPATH_PREFIX.length()));
                } else {
                    throw new RuntimeException("Unknown file source " + dcsFile.getSource() + " for file name " +
                            dcsFile.getName());
                }

                if (!r.exists()) {
                    throw new RuntimeException("Resource " + r.getFilename() + " doesn't exist.");
                }

                Map<String, String> metadata = new HashMap<String, String>();
                HttpHeaderUtil.addDigest("SHA-1", calculateChecksum("SHA-1", r), metadata);
                metadata.put(HttpHeaderUtil.CONTENT_TYPE, APPLICATION_XML);
                metadata.put(HttpHeaderUtil.CONTENT_DISPOSITION, "filename="
                        + r.getFilename());
                metadata.put(HttpHeaderUtil.CONTENT_LENGTH, Long.toString(r.contentLength()));
                DepositInfo info = fileManager.deposit(r.getInputStream(), APPLICATION_XML, null, metadata);
                dcsFile.setSource(info.getMetadata().get(SRC_HEADER));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            modelBuilder.buildSip(entryDcp, baos);
            try {
                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put(HttpHeaderUtil.CONTENT_TYPE, APPLICATION_XML);
                metadata.put(HttpHeaderUtil.CONTENT_LENGTH, Integer
                        .toString(baos.size()));
                final ByteArrayInputStream byteIn = new ByteArrayInputStream(baos.toByteArray());
                depositStatus.add(manager.deposit(byteIn,
                        APPLICATION_XML, DCP_PACKAGING, metadata));
            } catch (PackageException e) {
                log.warn("Error depositing DCP: " + e.getMessage(), e);
                continue;
            }
        }

        Iterator<DepositInfo> statusItr = depositStatus.iterator();

        while (statusItr.hasNext()) {
            DepositInfo info = statusItr.next();
            try {
                if (!checkStatusUntilTimeout(info, 600000)) {
                    File f;
                    FileOutputStream fos = null;
                    try {
                        f = File.createTempFile("bootstrap-", ".xml");
                        fos = new FileOutputStream(f);
                        IOUtils.copy(info.getDepositStatus().getInputStream(), fos);
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                    }
                    log.warn("Error bootstrapping the DCS; error depositing package {}: see {} for more information.",
                            info.getDepositID(), f.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warn("Error bootstrapping the DCS; error depositing package {}: {}",
                        new Object[]{info.getDepositID(), e.getMessage(), e});
            }
        }

        log.info("Bootstrap complete in {} ms", System.currentTimeMillis() - bootStart);
    }

    /**
     * Polls the DCP SIP deposit manager for a deposit result.  If {@code timeOut} is exceeded, a Exception is thrown.
     *
     * @param info the DepositInfo to poll for
     * @param timeOut the maximum amount of time to poll for, in milliseconds
     * @return true if the deposit succeeded, false otherwise
     * @throws Exception if the timeout is exceeded
     */
    private boolean checkStatusUntilTimeout(DepositInfo info, long timeOut) throws Exception {
        if (timeOut < 0) {
            throw new IllegalArgumentException("Timeout value must be greater than 0");
        }

        if (info.hasCompleted()) {
            return info.isSuccessful();
        }

        long step = 10000; // 10 seconds
        if (timeOut < step) {
            step = timeOut;
        }

        long runtime = 0;

        while (runtime <= timeOut) {
            runtime += step;
            Thread.sleep(step);
            info = manager.getDepositInfo(info.getDepositID());
            if (info.hasCompleted()) {
                log.info("{} deposited package {}", (info.isSuccessful() ? "Successfully" : "Unsuccessfully"),
                        info.getDepositID());
                return info.isSuccessful();
            }
        }

        if (runtime > timeOut) {
            throw new Exception("Polling for deposit " + info.getDepositID() + " timed out after " + runtime + " ms");
        }

        return false;
    }

    /**
     * A query service which looks up objects from the index using formerExternalRef instead of the entity id.
     */
    private static class LookupQueryServiceImpl implements LookupQueryService {
        private static final String[] EMPTY_PARAMS = new String[]{};
        final DcsDataModelQueryService delegate;

        private LookupQueryServiceImpl(DcsDataModelQueryService delegate) {
            this.delegate = delegate;
        }

        /**
         * Searches objects in the index by querying the former external ref instead of the entity id.
         *
         * @param id the id, matched against the former external ref
         * @return {@inheritDoc}
         * @throws QueryServiceException
         */
        @Override
        public Object lookup(String id) throws QueryServiceException {
            final String query = SolrQueryUtil.createLiteralQuery("former", id);
            QueryResult result = delegate.query(query, 0, 1, EMPTY_PARAMS);
            if (result.getMatches() != null && !result.getMatches().isEmpty() && result.getMatches().get(0) != null) {
                return result.getMatches().get(0);
            }

            return null;
        }

        @Override
        public QueryResult query(String query, long offset, int matches, String... params)
                throws QueryServiceException {
            return delegate.query(query, offset, matches, params);
        }

        @Override
        public void shutdown() throws QueryServiceException {
            delegate.shutdown();
        }
    }

    /**
     * Calculates a checksum for the supplied resource, using the supplied algorithm.
     *
     * @param algo the algorithm
     * @param r the resource
     * @return the byte representation of the checksum
     * @throws RuntimeException if the checksum cannot be calculated for whatever reason
     */
    private byte[] calculateChecksum(String algo, Resource r) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        InputStream in = null;
        try {
            in = r.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Unable to obtain inputstream for Resource " + r.getFilename() + ": " +
                    e.getMessage(), e);
        }
        int read = 0;
        int size = 1024;
        byte[] buf = new byte[size];
        try {
            while ((read = in.read(buf, 0, size)) != -1) {
                md.update(buf, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to calculate checksum for Resource " + r.getFilename() + ": " +
                                e.getMessage(), e);
        }

        return md.digest();
    }

}