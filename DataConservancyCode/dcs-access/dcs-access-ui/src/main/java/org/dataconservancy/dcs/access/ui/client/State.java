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
package org.dataconservancy.dcs.access.ui.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping between user states and history tokens. Arguments to user actions are
 * separated by ';' with ';;' for literal ';'.
 */

public enum State {
    HOME("home"), SEARCH("search"), ENTITY("entity"), RELATED("related");

    private final String prefix;

    State(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Build up a token with the given arguments. Null arguments are ignored.
     * 
     * @param args
     * @return token
     */

    public String toToken(String... args) {
        if (args.length == 0) {
            return prefix;
        }

        StringBuilder sb = new StringBuilder(prefix);

        // Escape ; with ;;

        for (String arg : args) {
            if (arg == null) {
                continue;
            }

            sb.append(';');

            if (arg.contains(";")) {
                sb.append(arg.replaceAll(";", ";;"));
            } else {
                sb.append(arg);
            }
        }

        return sb.toString();
    }

    public static List<String> tokenArguments(String token) {
        List<String> args = new ArrayList<String>(4);

        StringBuilder arg = new StringBuilder();
        boolean foundsemicolon = false;
        int i = token.indexOf(';');

        if (i == -1) {
            return args;
        }

        for (i++; i < token.length(); i++) {
            char c = token.charAt(i);

            if (foundsemicolon) {
                if (c != ';') {
                    args.add(arg.toString());
                    arg.setLength(0);
                }

                arg.append(c);
                foundsemicolon = false;
            } else {
                if (c == ';') {
                    foundsemicolon = true;
                } else {
                    arg.append(c);
                }
            }
        }

        if (arg.length() > 0) {
            args.add(arg.toString());
        }

        return args;
    }

    /**
     * @param token
     * @return corresponding value or null if the token is invalid
     */
    public static State fromToken(String token) {
        int i = token.indexOf(';');

        if (i != -1) {
            token = token.substring(0, i);
        }

        for (State state : State.values()) {
            if (state.prefix.equals(token)) {
                return state;
            }
        }

        return null;
    }
}
