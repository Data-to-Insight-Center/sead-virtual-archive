package org.dataconservancy.ui.services;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestService;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class BaseIngestService implements StatefulIngestService {

    /**
     * Logging instance
     */
    final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes: <br/>
     * Sanity-checks the incoming deposit identifier and state, insuring they have reasonable values.  Sub-classes
     * should invoke {@code super.execute(String, IngestWorkflowState)} before continuing with their logic.
     *
     * @param depositId the deposit identifier, must not be empty or {@code null}
     * @param state the state associated with identified deposit, must not be {@code null}, and must have its components
     *              set
     * @throws StatefulIngestServiceException {@inheritDoc}
     * @throws IllegalArgumentException if any of the arguments fail sanity checks
     * @throws IllegalStateException if the supplied {@code IngestWorkflowState} isn't composed properly
     */
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        if (depositId == null || depositId.trim().length() == 0) {
            throw new IllegalArgumentException("Deposit identifier must not be empty or null!");
        }

        if (state == null) {
            throw new IllegalArgumentException("Ingest state must not be empty or null!");
        }

        if (state.getAttributeSetManager() == null) {
            throw new IllegalStateException("Ingest state must have an AttributeSetManager!");
        }

        if (state.getBusinessObjectManager() == null) {
            throw new IllegalStateException("Ingest state must have an BusinessObjectManager!");
        }

        if (state.getEventManager() == null) {
            throw new IllegalStateException("Ingest state must have an EventManager!");
        }

        if (state.getPackage() == null) {
            throw new IllegalStateException("Ingest state must have a Package instance!");
        }
    }
}
