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
package org.dataconservancy.dcs.id.api;

/**
 * Thrown when an identifier could not be found in the Id Store.
 * 
 * @author Bill Steel
 * @version $Id: IdentifierNotFoundException.java 146 2010-06-03 21:30:40Z
 *          bill.steel $
 */
public class IdentifierNotFoundException
        extends Exception {

    private static final long serialVersionUID = 1L;

    public IdentifierNotFoundException() {
    }

    public IdentifierNotFoundException(String message) {
        super(message);
    }

    public IdentifierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentifierNotFoundException(Throwable cause) {
        super(cause);
    }
}
