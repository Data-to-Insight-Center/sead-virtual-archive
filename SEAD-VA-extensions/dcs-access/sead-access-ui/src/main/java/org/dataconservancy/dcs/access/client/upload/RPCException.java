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
package org.dataconservancy.dcs.access.client.upload;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Exception wrapper to pass the exception message back through RPC.
 */
public class RPCException
        extends Exception
        implements IsSerializable {

    private static final long serialVersionUID = 1L;

    private String message;

    public RPCException() {
    }

    public RPCException(String message) {
        super(message);
        this.message = message;
    }

    public RPCException(Throwable cause) {
        this.message = cause.getMessage();
    }

    public RPCException(String message, Throwable cause) {
        this.message = message + " , Caused by: " + cause.getMessage();
    }

    public String getMessage() {
        return message;
    }
}
