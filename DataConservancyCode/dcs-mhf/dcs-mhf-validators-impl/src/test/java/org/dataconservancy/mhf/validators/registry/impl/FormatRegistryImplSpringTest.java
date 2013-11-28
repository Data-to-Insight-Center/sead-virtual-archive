package org.dataconservancy.mhf.validators.registry.impl;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Implementation of AbstractFormatRegistryImplTest which obtains a FormatRegistryImpl from a Spring bean definition.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/org/dataconservancy/mhf/config/applicationContext.xml",
        "classpath:/org/dataconservancy/registry/config/applicationContext.xml",
        "classpath:/org/dataconservancy/model/config/applicationContext.xml"})
public class FormatRegistryImplSpringTest extends AbstractFormatRegistryImplTest {

    @Autowired
    private FormatRegistryImpl underTest;

    @Override
    protected FormatRegistryImpl getUnderTest() {
        return underTest;
    }

}
