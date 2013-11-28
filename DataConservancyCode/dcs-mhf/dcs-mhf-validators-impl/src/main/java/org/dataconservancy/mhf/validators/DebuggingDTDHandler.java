package org.dataconservancy.mhf.validators;

import org.xml.sax.DTDHandler;

/**
 *
 */
class DebuggingDTDHandler extends DTDHandlerWrapper {

    DebuggingDTDHandler(DTDHandler toWrap) {
        super(toWrap);
    }

    @Override
    void notationDeclInternal(String name, String publicId, String systemId) {
        log.log("notationDeclInternal: name '{}' publicId '{}' systemId '{}'",
                new Object[] { name, publicId, systemId });
    }

    @Override
    void unparsedEntityDeclInternal(String name, String publicId, String systemId, String notationName) {
        log.log("unparsedEntityDeclInternal: name '{}' publicId: '{}' systemId: '{}', notationName: '{}'",
                new Object[] { name, publicId, systemId, notationName });
    }
}
