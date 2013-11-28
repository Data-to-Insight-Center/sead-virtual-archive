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
package org.dataconservancy.access.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

/**
 *
 */
class ExceptionHandler<T extends Throwable> {

    private Logger log;
    private String message;
    private ExceptionStrategy strategy;

    ExceptionHandler(Logger log, String message, ExceptionStrategy strategy) {
        this.log = log;
        this.message = message;
        this.strategy = strategy;

        if (strategy == null) {
            throw new IllegalArgumentException("Exception strategy must not be null");
        }
    }

    void handleException(T t) throws T {
        switch (strategy) {
            case FAIL:
                if (message != null) {
                    fail(message + " Underlying exception message was: " + t.getMessage());
                } else {
                    fail("Underlying exception message was: " + t.getMessage());
                }
                break;

            case LOG:
                Logger methodLogger;
                if (log == null) {
                    methodLogger = LoggerFactory.getLogger(this.getClass());
                } else {
                    methodLogger = log;
                }

                if (message != null) {
                    methodLogger.debug(message + " Underlying exception message was: " + t.getMessage(), t);
                } else {
                    methodLogger.debug("Underlying exception message was: " + t.getMessage(), t);
                }
                break;

            case RETHROW:
                throw t;

            case SWALLOW:
                // Do nothing - bad!
                break;

            default:
                // Do nothing - bad!
                break;
        }
    }
}
