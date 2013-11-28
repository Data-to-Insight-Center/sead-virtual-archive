package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.validation.api.InvalidInstanceException;
import org.dataconservancy.mhf.validation.api.ValidationException;

import static org.dataconservancy.mhf.eventing.events.MetadataValidationEvent.ValidationType;

/**
 * Responsible for constructing validation events and exceptions.  The exceptions are mapped into the proper events,
 * and the events are fired prior to the exception being thrown.
 */
class EventProducingExceptionHandler {

    private MetadataHandlingEventManager eventManager;

    /**
     * Fires events to the supplied {@code eventManager} when exceptions are thrown.
     *
     * @param eventManager the event manager, must not be null
     */
    EventProducingExceptionHandler(MetadataHandlingEventManager eventManager) {
        if (eventManager == null) {
            throw new IllegalArgumentException("Event manager must not be null.");
        }
        this.eventManager = eventManager;
    }

    /**
     * Fires a MetadataXmlValidationEvent and throws an {@code InvalidInstanceException}.  The event and exception
     * are composed from the parameters supplied to this method.
     *
     * @param formatId the format id of the metadata instance, may be null
     * @param schemaUrl the schema url used to validate the metadata instance, may be null
     * @param schemaSource the schema source used to validate the metadata instance, may be null
     * @param reason the reason this method is being invoked
     * @param cause the exception that caused this method to be invoked
     * @throws InvalidInstanceException the exception composed from the supplied method parameters
     */
    void throwInvalidInstanceException(String formatId, String schemaUrl, String schemaSource, String reason,
                                       Exception cause) throws InvalidInstanceException {
        final String message = constructMessage(Messages.INVALID_METADATA_INSTANCE, formatId, schemaUrl, schemaSource,
                reason);
        composeAndFireEvent(null, message, (cause != null ? cause.getMessage() : null), ValidationType.FAILURE);
        throwInvalidException(message, cause);
    }

    /**
     * Fires a MetadataXmlValidationEvent and throws an {@code ValidationException}.  The event and exception
     * are composed from the parameters supplied to this method.
     *
     * @param formatId the format id of the metadata instance, may be null
     * @param schemaUrl the schema url used to validate the metadata instance, may be null
     * @param schemaSource the schema source used to validate the metadata instance, may be null
     * @param reason the reason this method is being invoked
     * @param cause the exception that caused this method to be invoked
     * @throws ValidationException the exception composed from the supplied method parameters
     */
    void throwValidationException(String formatId, String schemaUrl, String schemaSource, String reason,
                                  Exception cause) throws ValidationException {
        final String message = constructMessage(Messages.ERROR_PERFORMING_VALIDATION, formatId, schemaUrl, schemaSource,
                reason);
        composeAndFireEvent(null, message, (cause != null ? cause.getMessage() : null), ValidationType.FAILURE);
        throwValidationException(message, cause);
    }

    /**
     * Constructs the message that will be used in the event and exception message.
     *
     * @param messageFormat
     * @param formatId
     * @param schemaUrl
     * @param schemaSource
     * @param reason
     * @return
     */
    private String constructMessage(String messageFormat, String formatId, String schemaUrl, String schemaSource,
                                    String reason) {
        return String.format(messageFormat,
                (formatId == null ? "<unknown>" : formatId),
                (schemaUrl == null ? "<unknown>" : schemaUrl),
                (schemaSource == null ? "<unknown>" : schemaSource),
                reason);
    }

    /**
     * Composes a {@code InvalidInstanceException} from the supplied message and cause, and then throws it.
     *
     * @param message the message
     * @param cause the cause, may be null
     * @throws InvalidInstanceException
     */
    private void throwInvalidException(String message, Exception cause) throws InvalidInstanceException {
        InvalidInstanceException toThrow = null;

        if (cause != null) {
            toThrow = new InvalidInstanceException(message, cause);
        } else {
            toThrow = new InvalidInstanceException(message);
        }

        throw toThrow;
    }

    /**
     * Composes a {@code ValidationException} from the supplied message and cause, and then throws it.
     *
     * @param message the message
     * @param cause the cause, may be null
     * @throws InvalidInstanceException
     */
    private void throwValidationException(String message, Exception cause) throws ValidationException {
        ValidationException toThrow = null;

        if (cause != null) {
            toThrow = new ValidationException(message, cause);
        } else {
            toThrow = new ValidationException(message);
        }

        throw toThrow;
    }

    /**
     * Composes a validation event and fires it.
     *
     * @param objectId
     * @param message
     * @param cause
     * @param errorType
     */
    private void composeAndFireEvent(String objectId, String message, String cause, ValidationType errorType) {
        final MetadataValidationEvent mve = new MetadataValidationEvent(objectId, message, cause, errorType);
        eventManager.sendEvent(mve);
    }
}
