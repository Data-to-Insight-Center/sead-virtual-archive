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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class QueryUtil {

    // TODO extract to util module, copied from Solr impl module.
    private static String createLiteralQuery(StringBuilder sb,
                                             String field,
                                             String s) {
        if (field != null) {
            sb.append(field);
            sb.append(':');
        }

        sb.append("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '&' || c == '|' || c == '(' || c == ')' || c == '}'
                    || c == '{' || c == '[' || c == ']' || c == ':' || c == '^'
                    || c == '!' || c == '\"' || c == '+' || c == '-'
                    || c == '~' || c == '*' || c == '?' || c == '\\') {
                sb.append('\\');
            }

            sb.append(c);
        }
        sb.append("\"");

        return sb.toString();
    }

    /**
     * Return a solr query as a list of terms joined by the specified operation
     * such that each term literally matches the given string in the given
     * field.
     */
    static String createLiteralQuery(String op, String... args) {
        StringBuilder sb = new StringBuilder();

        if (args.length == 0 || (args.length & 1) > 0) {
            throw new IllegalArgumentException("Arguments must be (field, string)+");
        }

        sb.append('(');

        for (int i = 0; i < args.length;) {
            String field = args[i++];
            String string = args[i++];

            createLiteralQuery(sb, field, string);

            if (i < args.length) {
                sb.append(' ');
                sb.append(op);
                sb.append(' ');
            }
        }

        sb.append(')');

        return sb.toString();
    }

    /**
     * Return a solr query that exactly matches a string in a field.
     */
    static String createLiteralQuery(String field, String string) {
        StringBuilder sb = new StringBuilder();
        createLiteralQuery(sb, field, string);
        return sb.toString();
    }
    
    /**
     * @param path
     * @return path encoded for inclusion in a URL.
     */
    static String encodeURLPath(String path) {
        try {
            String s = URLEncoder.encode(path, "UTF-8");

            // Have to encode spaces (now plus) using %20

            return s.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
