package org.dataconservancy.mhf.resources.registry;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that transforms DcsFile source attribute values to classpath resources.
 */
class FileSrcTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(FileSrcTransformer.class);

    private static final String FILE_URL_PREFIX = "file:/";

    private static final String CLASSPATH_PREFIX = "classpath:/";

    private static final Pattern FILE_URL_PATTERN = Pattern.compile("^" + FILE_URL_PREFIX + ".*");

    private static final Pattern CLASSPATH_URL_PATTERN = Pattern.compile("^" + CLASSPATH_PREFIX + ".*");

    private static final Pattern ABS_PATH_PATTERN = Pattern.compile("^/.*");

    /**
     * Transforms the source attribute of each non-null, non-empty DcsFile source attribute found in the DCP.  When
     * a non-null, non-empty source attribute is encountered, {@link #transform(String, String)} is invoked.
     *
     *
     * @param dcpToTransform
     * @param classpathBase
     * @return the transformed DCP
     * @see #transform(String, String)
     */
    static Dcp transform(Dcp dcpToTransform, String classpathBase) {
        Dcp copy = new Dcp(dcpToTransform);
        for (DcsFile file : copy.getFiles()) {
            if (file.getSource() == null || file.getSource().trim().length() == 0) {
                continue;
            }

            String transformedSourceAttribute = transform(file.getSource(), classpathBase);
            file.setSource(transformedSourceAttribute);
        }

        return copy;
    }

    /**
     * Transforms the DcsFile source attribute into classpath resource urls.
     * <p/>
     * If the {@code fileSource} starts with a forward slash or if it starts with 'file:/' URL, the text up to the
     * first occurrence of {@code classpathBase} is stripped.  The text "classpath:" is prefixed to {@code fileSource},
     * and returned.  If {@code fileSource} already starts with "classpath:", then no transformation occurs.
     *
     * @param fileSource the DcsFile source string
     * @param classpathBase the classpath base to preserve
     * @return the transformed source string
     */
    static String transform(String fileSource, String classpathBase) {
        final String originalFileSource = fileSource;
        final Matcher startsWithFile = FILE_URL_PATTERN.matcher(fileSource);
        final Matcher startsWithSlash = ABS_PATH_PATTERN.matcher(fileSource);
        final Matcher startsWithClasspath = CLASSPATH_URL_PATTERN.matcher(fileSource);

        if (startsWithClasspath.matches()) {
            // no need to transform
            return fileSource;
        }

        if (startsWithFile.matches() || startsWithSlash.matches()) {
            int baseIndex = fileSource.indexOf(classpathBase);
            if (baseIndex > 0) {
                fileSource = fileSource.substring(baseIndex);
            } else if (startsWithFile.matches()) {
                fileSource = fileSource.substring(FILE_URL_PREFIX.length());
            }
        }



        final String transformedFileSource = "classpath:" + fileSource;
        LOG.debug("Transformed '{}' to '{}'", originalFileSource, transformedFileSource);

        return transformedFileSource;
    }


}
