package org.dataconservancy.mhf.finders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.test.support.BuilderTestUtil;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.junit.Before;
import org.junit.Test;

public class BagItMetadataFinderTest {
    private BagItMetadataFinder finder;

    @Before
    public void setUp() {
        finder = new BagItMetadataFinder(
                BuilderTestUtil.newXstreamModelBuilder());
    }

    private static class SimplePackageSerialization implements
            PackageSerialization {
        List<File> files = new ArrayList<File>();
        private File extractDir;
        private File baseDir;

        @Override
        public void setPackageMetadata(Map<String, String> arg0) {
        }

        @Override
        public void setFiles(List<File> files) {
            this.files = files;
        }

        @Override
        public void setChecksums(Map<String, List<Checksum>> arg0) {
        }

        @Override
        public String getPackageMetadata(String arg0) {
            return null;
        }

        @Override
        public Map<String, String> getPackageMetadata() {
            return null;
        }

        @Override
        public List<File> getFiles() {
            return files;
        }

        @Override
        public List<File> getFiles(boolean relativize) {
            return files;
        }

        @Override
        public List<Checksum> getChecksums(String arg0) {
            return null;
        }

        @Override
        public Map<String, List<Checksum>> getChecksums() {
            return null;
        }

        @Override
        public String getChecksum(String arg0, String arg1) {
            return null;
        }

        @Override
        public void addPackageMetadata(String arg0, String arg1) {

        }

        @Override
        public void addFile(File file) {
            files.add(file);
        }

        @Override
        public void addChecksum(String arg0, String arg1, String arg2) {

        }

        @Override
        public File getBaseDir() {
            return baseDir;
        }

        @Override
        public void setBaseDir(File basedir) {
            this.baseDir = basedir;            
        }

        @Override
        public File getExtractDir() {
            return extractDir;
        }

        @Override
        public void setExtractDir(File extractDir) {
            this.extractDir = extractDir;
        }
    }

    /**
     * Create a temporary package on the file system whose files have the given
     * relative paths and contents.
     * 
     * For simplicity of checking if multiple metadata share the same format, they should have the same content.
     * 
     * @param paths
     * @param contents
     * @return
     * @throws IOException
     */
    private PackageSerialization create_pkg(String[] paths, byte[] contents)
            throws IOException {
        File top = File.createTempFile("testbag", null);
        top.delete();
        top.mkdir();
        top.deleteOnExit();

        SimplePackageSerialization pkg = new SimplePackageSerialization();
        pkg.setExtractDir(new File(System.getProperty("java.io.tmpdir")));
        pkg.setBaseDir(new File(top.getName()));
        
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            byte content = contents[i];

            File file = new File(top, path);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                parent.deleteOnExit();
            }

            OutputStream os = new FileOutputStream(file);
            os.write(content);
            os.close();

            file.deleteOnExit();

            pkg.addFile(file);
        }

        return pkg;
    }

    /**
     * Check expected metadata formats and contents.
     * 
     * @param results
     * @param format_ids
     * @param contents
     * @throws IOException
     */
    private void check(Collection<MetadataInstance> results,
            String[] format_ids, byte[] contents) throws IOException {

        Assert.assertEquals(format_ids.length, contents.length);

        for (int i = 0; i < format_ids.length; i++) {
            String format_id = format_ids[i];
            byte content = contents[i];

            boolean found = false;

            for (MetadataInstance result : results) {
                if (format_id.equals(result.getFormatId())) {
                    found = true;

                    InputStream is = result.getContent();
                    int b = is.read();
                    is.close();

                    Assert.assertFalse(b == -1);
                    Assert.assertEquals(content, b);

                    break;
                }
            }

            Assert.assertTrue(found);
        }

        Assert.assertEquals(format_ids.length, results.size());
    }

    /**
     * Test a minimal bag.
     * 
     * @throws IOException
     */
    @Test
    public void testSimple() throws IOException {
        PackageSerialization pkg = create_pkg(new String[] { "data/cow.jpg",
                "bagit.txt", "manifest-md5.txt" }, new byte[] { 0, 1, 2 });

        Collection<MetadataInstance> results = finder.findMetadata(pkg);

        check(results, new String[] { MetadataFormatId.BAGIT_TAG_FORMAT_ID,
                MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID },
                new byte[] { 1, 2 });
    }

    /**
     * Test a bag missing a manifest with fetch metadata.
     * 
     * @throws IOException
     */
    @Test
    public void testFetch() throws IOException {
        PackageSerialization pkg = create_pkg(new String[] { "data/cow.jpg",
                "bagit.txt", "fetch.txt" }, new byte[] { 0, 1, 2 });

        Collection<MetadataInstance> results = finder.findMetadata(pkg);

        check(results, new String[] { MetadataFormatId.BAGIT_TAG_FORMAT_ID,
                MetadataFormatId.BAGIT_FETCH_FORMAT_ID }, new byte[] { 1, 2 });
    }

    /**
     * Test a bag with manifests, bag-info.txt, and a file in the payload
     * with the same name as a metadata file.
     * 
     * @throws IOException
     */
    @Test
    public void testComplex() throws IOException {
        PackageSerialization pkg = create_pkg(new String[] { "data/cow.jpg",
                "bagit.txt", "data/fetch.txt", "bag-info.txt", "manifest-md5.txt","tagmanifest-sha1.txt" }, new byte[] {
                0, 1, 2, 1, 3, 3 });

        Collection<MetadataInstance> results = finder.findMetadata(pkg);

        check(results, new String[] { MetadataFormatId.BAGIT_TAG_FORMAT_ID,
                MetadataFormatId.BAGIT_TAG_FORMAT_ID, MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID, MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID }, new byte[] { 1, 1, 3, 3});
    }
}
