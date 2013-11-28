package org.dataconservancy.mhf.validators;

import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Insures that the MhfProperties object can be properly instantiated
 */
public class MhfPropertiesTest {

    /**
     * Constructing the MHF properties with the no-arg constructor will attempt
     * to find /org/dataconservancy/mhf/config/dcs-mhf.properties on the classpath and load
     * the file.  This test insures that the resolution works (at least in test) and that
     * their is something for the properties values.
     *
     * @throws Exception
     */
    @Test
    public void testNoArgConstructor() throws Exception {
        final MhfProperties underTest = new MhfProperties();

        // invoke each getter and insure that there are non-null returns.  this
        // insures that either a) there is a property value in the .properties file for the
        // property or b) that the MhfProperties() class has sane defaults

        Method[] allMethods = underTest.getClass().getMethods();
        List<Method> getters = new ArrayList<Method>();
        for (Method m : allMethods) {
            if (m.getName().startsWith("get")) {
                getters.add(m);
            }

            if (m.getName().startsWith("is")) {
                getters.add(m);
            }
        }

        assertTrue(getters.size() > 0);

        for (Method m : getters) {
            assertNotNull(m.invoke(underTest));
        }
    }

    /**
     * Specify the properties file to load as a classpath resource.  Insure that the values
     * in the supplied properties file and the values returned by the MhfProperties object
     * are the same.
     *
     * @throws Exception
     */
    @Test
    public void testStringConstructor() throws Exception {
        final String propertiesResource = "/org/dataconservancy/mhf/validators/dcs-mhf-test.properties";
        final LogLevel expectedLogLevel = LogLevel.ERROR;
        final boolean expectedVerbosity = true;

        final MhfProperties underTest = new MhfProperties(propertiesResource);

        assertEquals(expectedLogLevel, underTest.getLogLevel());
        assertEquals(expectedVerbosity, underTest.isVerbose());
    }

    /**
     * Insures that even if an empty properties file is loaded, the the MhfProperties object
     * still provides defaults.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultsByLoadingEmptyPropertiesFile() throws Exception {

        final String propertiesResource = "/org/dataconservancy/mhf/validators/dcs-mhf-test-empty-props.properties";
        final Properties props = new Properties();
        final URL u = this.getClass().getResource(propertiesResource);
        assertNotNull(u);
        props.load(u.openStream());
        assertEquals(0, props.size());

        final MhfProperties underTest = new MhfProperties(propertiesResource);

        // invoke each getter and insure that there are non-null returns.  this
        // insures that either a) there is a property value in the .properties file for the
        // property or b) that the MhfProperties() class has sane defaults

        Method[] allMethods = underTest.getClass().getMethods();
        List<Method> getters = new ArrayList<Method>();
        for (Method m : allMethods) {
            if (m.getName().startsWith("get")) {
                getters.add(m);
            }

            if (m.getName().startsWith("is")) {
                getters.add(m);
            }
        }

        assertTrue(getters.size() > 0);

        for (Method m : getters) {
            assertNotNull(m.invoke(underTest));
        }
    }
}
