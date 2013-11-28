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

import org.dataconservancy.dcs.access.ui.client.SolrField.CollectionField;
import org.dataconservancy.dcs.access.ui.client.SolrField.DeliverableUnitField;

public class Search {

    // ALL is treated specially to match all lucene fields used by other
    // UserFields
    public enum UserField {
        ALL("All"), ID("Id", SolrField.EntityField.ID,
                SolrField.DeliverableUnitField.FORMER_REFS), METADATA(
                "Metadata", SolrField.CoreMetadataField.CREATOR,
                SolrField.CoreMetadataField.SUBJECT,
                SolrField.CoreMetadataField.TITLE,
                SolrField.CoreMetadataField.TYPE,
                SolrField.MetadataField.SEARCH_TEXT,
                SolrField.MetadataField.SCHEMA), FILE("File",
                SolrField.FileField.NAME, SolrField.FileField.SOURCE,
                SolrField.FormatField.FORMAT, SolrField.FormatField.NAME,
                SolrField.FormatField.SCHEMA,
                SolrField.ManifestationFileField.PATH,
                SolrField.FixityField.VALUE), RELATED("Related",
                CollectionField.PARENT, DeliverableUnitField.METADATA_REF,
                SolrField.FormatField.SCHEMA, SolrField.MetadataField.SCHEMA,
                DeliverableUnitField.COLLECTIONS,
                SolrField.RelationField.TARGET, SolrField.EventField.TARGET,
                SolrField.ManifestationField.DELIVERABLE_UNIT,
                SolrField.ManifestationFileField.FILE_REF,
                SolrField.RelationField.RELATION), LITERAL("Literal", (SolrName[])null);

        public final String display;

        public final SolrName[] fields;

        private UserField(String display, SolrName... fields) {
            this.display = display;
            this.fields = fields;
        }

        // TODO build map?
        public static UserField findByLuceneField(String lucenename) {
            for (UserField uf : values()) {
                if (uf.fields != null) {
                    for (SolrName f : uf.fields) {
                        if (f.equals(lucenename)) {
                            return uf;
                        }
                    }
                }
            }

            return null;
        }
    }

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
    public static String createLiteralQuery(String op, String... args) {
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
    public static String createLiteralQuery(String field, String string) {
        StringBuilder sb = new StringBuilder();
        createLiteralQuery(sb, field, string);
        return sb.toString();
    }

    // Tokenize a user query into lucene terms and phrases.
    // Phrases will be surrounded by "
    // Unsupported lucene characters are escaped.
    // Always returns valid lucene terms

    private static List<String> parseUserQuery(String query) {
        List<String> luceneterms = new ArrayList<String>();

        final int TERM = 1;
        final int SEP = 2;
        final int PHRASE = 3;
        final int PHRASE_PROXIMITY = 4;

        int state = SEP;
        boolean escaped = false;
        StringBuilder luceneprefix = new StringBuilder();
        StringBuilder luceneterm = new StringBuilder();
        StringBuilder lucenesuffix = new StringBuilder();

        int numquotes = 0;

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);

            if (state == PHRASE_PROXIMITY) {
                if (Character.isDigit(c)) {
                    lucenesuffix.append(c);
                } else {
                    addQueryTerm(luceneterms,
                                 luceneprefix,
                                 luceneterm,
                                 lucenesuffix);

                    // push back
                    i--;
                    state = SEP;
                }
            } else if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                if (state == TERM) {
                    addQueryTerm(luceneterms,
                                 luceneprefix,
                                 luceneterm,
                                 lucenesuffix);
                    state = SEP;
                } else if (state == PHRASE) {
                    luceneterm.append(' ');
                }
            } else if (escaped) {
                escaped = false;
                luceneterm.append('\\');
                luceneterm.append(c);
            } else if (c == '\\') {
                escaped = true;
            } else if (state == SEP && (c == '+' || c == '-')) {
                if (luceneprefix.length() == 0) {
                    luceneprefix.append(c);
                }
            } else if (c == '"') {
                numquotes++;

                if (state == PHRASE) {
                    lucenesuffix.append(c);

                    // Check for ~num

                    if (i + 2 < query.length() && query.charAt(i + 1) == '~'
                            && Character.isDigit(query.charAt(i + 2))) {
                        lucenesuffix.append(query.charAt(i + 1));
                        lucenesuffix.append(query.charAt(i + 2));
                        i += 2;
                        state = PHRASE_PROXIMITY;
                    } else {
                        addQueryTerm(luceneterms,
                                     luceneprefix,
                                     luceneterm,
                                     lucenesuffix);
                        state = SEP;
                    }
                } else {
                    if (luceneterm.length() > 0) {
                        addQueryTerm(luceneterms,
                                     luceneprefix,
                                     luceneterm,
                                     lucenesuffix);
                    }

                    luceneprefix.append(c);
                    state = PHRASE;
                }
            } else if (c == '&' || c == '|' || c == '(' || c == ')' || c == '}'
                    || c == '{' || c == '[' || c == ']' || c == ':' || c == '^'
                    || c == '!' || c == '~' || c == '+' || c == '-') {
                luceneterm.append('\\');
                luceneterm.append(c);
            } else {
                if (state == SEP) {
                    state = TERM;
                }

                luceneterm.append(c);
            }
        }

        // Phrase started, but didn't end with quote, add quote.
        if ((numquotes & 1) > 0) {
            lucenesuffix.append('\"');
        }

        addQueryTerm(luceneterms, luceneprefix, luceneterm, lucenesuffix);

        return luceneterms;
    }

    private static void addQueryTerm(List<String> terms,
                                     StringBuilder luceneprefix,
                                     StringBuilder term,
                                     StringBuilder lucenesuffix) {

        if (term.length() > 0) {
            terms.add(luceneprefix.toString() + term + lucenesuffix);
        }

        term.setLength(0);
        luceneprefix.setLength(0);
        lucenesuffix.setLength(0);
    }

    private static void buildLuceneQuery(StringBuilder sb,
                                         SolrName[] fields,
                                         List<String> terms) {

        String query = buildLuceneQuery(terms);

        if (fields == null) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            SolrName field = fields[i];

            sb.append(field.solrName());
            sb.append(":(");
            sb.append(query);
            sb.append(")");

            if (i < fields.length - 1) {
                sb.append(" OR ");
            }
        }
    }

    /**
     * Create lucene query given specified user queries in user fields.
     * 
     * @param userfields
     * @param userqueries
     * @return
     */
    public static String createQuery(UserField[] userfields,
                                     String[] userqueries) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < userfields.length; i++) {
            UserField uf = userfields[i];
            String userquery = userqueries[i];

            if (uf == null || userquery == null) {
                continue;
            }

            List<String> terms = parseUserQuery(userquery);

            if (uf == UserField.ALL) {
                for (UserField uf2 : UserField.values()) {
                    if (uf2 != UserField.ALL && uf2 != UserField.LITERAL) {
                        buildLuceneQuery(sb, uf2.fields, terms);
                    }
                }
            } else {
                if (uf == UserField.LITERAL) {
                    sb.append(userquery);
                } else {
                    buildLuceneQuery(sb, uf.fields, terms);
                }
            }
        }

        return sb.toString();
    }

    private static String buildLuceneQuery(List<String> terms) {
        StringBuilder sb = new StringBuilder();

        for (String term : terms) {
            if (!term.trim().isEmpty()) {
                sb.append('(');
                sb.append(term);
                sb.append(')');
                sb.append(' ');
            }
        }

        return sb.toString();
    }
}
