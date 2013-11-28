/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.model.builder;

import org.dataconservancy.model.dcs.support.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The client supplied invalid XML to the model builder.
 */
public class InvalidXmlException extends BaseModelBuilderException {

    private List<String> errorMessages;

    public InvalidXmlException() {
    }

    public InvalidXmlException(String message) {
        super(message);
    }

    public InvalidXmlException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidXmlException(Throwable cause) {
        super(cause);
    }

    /**
     * Obtain any error messages.
     *
     * @return error messages in the order they were reported, never <code>null</code>
     */
    public List<String> getErrorMessages() {
        if (errorMessages == null) {
            return Collections.emptyList();
        }
        return errorMessages;
    }

    /**
     * Sets the error messages.
     *
     * @param errorMessages the error messages, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>errorMessages</code> are <code>null</code>
     */
    public void setErrorMessages(List<String> errorMessages) {
        Assertion.notNull(errorMessages);
        this.errorMessages = errorMessages;
    }

    /**
     * Adds an error message.
     *
     * @param message the message to add
     */
    public void addErrorMessage(String message) {
        if (this.errorMessages == null) {
            errorMessages = new ArrayList<String>();
        }
        errorMessages.add(message);
    }

}
