package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.validation.api.MetadataValidator;

/**
 * Base {@code MetadataValidator} that all implementations are expected to extend.
 */
public abstract class BaseValidatorImpl implements MetadataValidator {

    /**
     * Logger instance shared with implementations
     */
    protected final ValidatorLogger log = ValidatorLoggerFactory.getLogger(this.getClass());

}
