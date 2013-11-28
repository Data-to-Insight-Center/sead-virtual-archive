package org.dataconservancy.mhf.test.support;


import junit.framework.Assert;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/org/dataconservancy/mhf/config/test-applicationContext.xml" })
public class RegistryCreationUtilWiringTest {

    @Autowired
    private TypedRegistry<DcsMetadataScheme> dcsMetadataSchemeTypedRegistry;

    /**
     * Test that wiring for the schema registry bean work properly so that the bean is initialized and contain the correct values.
     */
    @Test
    public void testWiringOfSchemaRegistry() {
        Assert.assertNotNull(dcsMetadataSchemeTypedRegistry);
        Assert.assertNotNull(dcsMetadataSchemeTypedRegistry.getType());
        Assert.assertEquals(RegistryCreationUtil.metadataFormatEntryType, dcsMetadataSchemeTypedRegistry.getType());
        Iterator itr = dcsMetadataSchemeTypedRegistry.iterator();
        while (itr.hasNext()) {
            Object object = itr.next();
            Assert.assertTrue(object instanceof BasicRegistryEntryImpl);
            BasicRegistryEntryImpl entry = (BasicRegistryEntryImpl)object;
            Assert.assertTrue(entry.getEntry() instanceof DcsMetadataFormat);
        }
    }
}
