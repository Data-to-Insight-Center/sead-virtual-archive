package org.dataconservancy.mhf.validators;

/**
 *
 */
public class XmlMetadataValidatorImplTest extends BaseXmlValidatorImplTest {

    @Override
    protected XmlMetadataValidatorImpl getUnderTest() {
        return new XmlMetadataValidatorImpl(getSchemaFactory(), getFormatResourceResolver(),
                getSchemeResourceResolver(), getEventManager());
    }

}
