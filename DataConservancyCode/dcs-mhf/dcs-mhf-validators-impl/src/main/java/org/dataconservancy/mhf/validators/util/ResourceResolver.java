package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.registry.api.RegistryEntry;

/**
 * Resolves strings which identify registry entries containing objects of type {@code T}.  Implementations determine
 * how to interpret the resource string.  A resource string could be a URL, a file path, a classpath resource or any
 * number of other things.
 *
 * @param <T> the type of object contained in the {@code RegistryEntry}
 */
public interface ResourceResolver<T> {

    /**
     * Resolves the registry entry identified by {@code resourceString}.  Implementations will interpret the the
     * {@code resourceString} to resolve {@code RegistryEntry} objects.
     *
     * @param resourceString the identifier or location of the resource; used to look up the {@code RegistryEntry}
     * @return a {@code RegistryEntry}, or {@code null} if it cannot be resolved
     */
    public RegistryEntry<T> resolve(String resourceString, String baseUrl);

}
