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
package org.dataconservancy.ui.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Base class for checked exceptions thrown by the Action Beans.
 */
public class BaseUiException extends Exception {

    /**
     * The HTTP status code corresponding to this exception.
     */
    private int httpStatusCode;

    /**
     * The message key which was used to generate the message of this exception.
     */
    private String messageKey;

    // TODO: consider adding the name of the thread that the exception occurred in
    // TODO: consider adding the build information (build #, svn revision, version)

    public BaseUiException() {
    }

    public BaseUiException(String message) {
        super(message);
    }

    public BaseUiException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseUiException(Throwable cause) {
        super(cause);
    }

    /**
     * The HTTP status code that should be returned for this exception.
     *
     * @return the HTTP status code
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * The HTTP status code that should be returned for this exception.
     *
     * @param httpStatusCode the HTTP status code
     */
    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * The message key used to resolve the message in this exception.
     *
     * @return the message key
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * The message key used to resolve the message in this exception.
     *
     * @param messageKey the message key
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Returns a String containing the full stack trace.
     *
     * @return the stack trace
     */
    public String asStackTraceMessage() {
        final StringWriter stringWriter = new StringWriter(2048);
        PrintWriter writer = new PrintWriter(stringWriter);
        this.printStackTrace(writer);
        writer.flush();
        writer.close();
        return stringWriter.toString();
    }
    
}
