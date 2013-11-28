package org.dataconservancy.mhf.finders;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.dataconservancy.mhf.finder.api.MetadataFindingException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.PackageSerialization;

/**
 * Pick out BagIt tag files (bagit.txt, bag-info.txt), manifests (payload and tag), and fetch data.
 * See http://tools.ietf.org/html/draft-kunze-bagit.
 * 
 * Operates on PackageSerialization object.
 */
public class BagItMetadataFinder extends BaseMetadataFinder {
    
    private String checksum;

    public BagItMetadataFinder(MetadataObjectBuilder builder) {
        super(builder);
    }

    // TODO This could easily operate on a list of files instead of the PackageSerialization
    
    @Override
    public Collection<MetadataInstance> findMetadata(Object o) {
        if (!(o instanceof PackageSerialization)) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    String.format(TYPE_ERROR, o.getClass().getName(),
                            PackageSerialization.class.getName()));
            throw new MetadataFindingException(iae.getMessage(), iae);
        }

        Collection<MetadataInstance> result = new HashSet<MetadataInstance>();

        PackageSerialization pkg = (PackageSerialization) o;

        // Find the toplevel directory

        File top_dir = new File(pkg.getExtractDir(), pkg.getBaseDir().getPath());
        
        // Only top level files can be metadata

        for (File file : pkg.getFiles()) {
            if (!file.isFile()) {
                continue;
            }

            if (!file.getParentFile().equals(top_dir)) {
                continue;
            }

            String name = file.getName();

            URL url;

            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new MetadataFindingException(e);
            }

            if (name.equals("bagit.txt")) {
                result.add(new FileMetadataInstance(
                        MetadataFormatId.BAGIT_TAG_FORMAT_ID, url));
            } else if (name.equals("bag-info.txt")) {
                result.add(new FileMetadataInstance(
                        MetadataFormatId.BAGIT_TAG_FORMAT_ID, url));
            } else if (name.equals("fetch.txt")) {
                result.add(new FileMetadataInstance(
                        MetadataFormatId.BAGIT_FETCH_FORMAT_ID, url));
            } else if (name.startsWith("manifest-") && name.endsWith(".txt")) {
                if (name.contains(Checksum.MD5)) {
                    setChecksum(Checksum.MD5);
                }
                else if (name.contains(Checksum.SHA1)) {
                    setChecksum(Checksum.SHA1);
                }
                result.add(new FileMetadataInstance(
                        MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID, url));
            } else if (name.startsWith("tagmanifest-") && name.endsWith(".txt")) {
                result.add(new FileMetadataInstance(
                        MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID, url));
            }
        }

        return result;
    }
    
    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }
    
    /**
     * @param checksum
     *            the checksum to set
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
}
