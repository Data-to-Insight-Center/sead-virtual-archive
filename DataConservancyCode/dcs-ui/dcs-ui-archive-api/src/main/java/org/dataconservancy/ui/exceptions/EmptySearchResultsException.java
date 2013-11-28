/*
 * Copyright 2013 Johns Hopkins University
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

/**
 * An exception thrown by the ArchiveService when a search is conducted that yields no results.  Callers should
 * specifically handle instances of this exception according to their requirements (i.e. if empty search results
 * shouldn't be considered an exception, callers can ignore this exception when its thrown).
 */
public class EmptySearchResultsException extends ArchiveServiceException {
    public EmptySearchResultsException() {
    }

    public EmptySearchResultsException(Throwable cause) {
        super(cause);
    }

    public EmptySearchResultsException(String message) {
        super(message);
    }

    public EmptySearchResultsException(String message, Throwable cause) {
        super(message, cause);
    }
}
