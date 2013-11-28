package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.mhf.validators.ValidatorLogger;
import org.dataconservancy.mhf.validators.ValidatorLoggerFactory;
import org.dataconservancy.registry.api.RegistryEntry;

/**
 * Base implementation of a {@code ResourceResolver}
 */
public abstract class BaseResourceResolver<T> implements ResourceResolver<T> {

    protected final ValidatorLogger log = ValidatorLoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes: <br/>
     * Right now this method simply logs the resolution attempt and forwards to
     * {@link #resolveResourceInternal(String)} if the {@code resourceString} is not {@code null}.
     *
     * @param resourceString {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public RegistryEntry<T> resolve(String resourceString, String baseUrl) {

        if (resourceString == null) {
            log.log("Refusing to resolve a null resource, returning null.");
            return null;
        }

        log.log("Resolving resource '{}'", resourceString);

        RegistryEntry<T> result = resolveResourceInternal(resourceString, baseUrl);

        if (result != null) {
            log.log("Resolved resource '{}' to registry entry '{}'", resourceString, result.getId());
        } else {
            log.log("Resolved resource '{}' to '{}'", resourceString, null);
        }

        return result;
    }

    /**
     * Resolve the {@code resourceString} to a {@code RegistryEntry}.  If the {@code resourceString} cannot be resolved,
     * implementations should return {@code null}.
     *
     * @param resourceString the resource string to resolve to a {@code RegistryEntry}
     * @param baseUrl the base url of the resource if one exists, may be null or empty
     * @return the {@code RegistryEntry}, or {@code null} if the resource cannot be found.
     */
    protected abstract RegistryEntry<T> resolveResourceInternal(String resourceString, String baseUrl);

}
