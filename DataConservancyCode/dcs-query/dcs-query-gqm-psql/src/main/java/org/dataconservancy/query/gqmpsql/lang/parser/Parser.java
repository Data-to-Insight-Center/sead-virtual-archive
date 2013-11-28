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
package org.dataconservancy.query.gqmpsql.lang.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.query.gqmpsql.lang.model.Function;
import org.dataconservancy.query.gqmpsql.lang.model.Query;
import org.dataconservancy.query.gqmpsql.lang.model.Query.Operation;

public class Parser {
    private final ParserInput input;

    public Parser(String s) {
        this(new CharSequenceParserInput(s));
    }

    public Parser(ParserInput input) {
        this.input = input;
    }

    // Parse double ending at first character not digit, "." , or "-".

    private double parseDouble() throws ParseException {
        input.mark();

        for (;;) {
            char c = input.peek();

            if (Character.isDigit(c) || c == '.' || c == '-') {
                input.next();
            } else {
                break;
            }
        }

        try {
            return Double.parseDouble(input.marked());
        } catch (NumberFormatException e) {
            throw new ParseException(input, "double", e.getMessage());
        }
    }

    private void skipWhitespace() throws ParseException {
        while (input.more() && Character.isWhitespace(input.peek())) {
            input.next();
        }
    }

    /**
     * @return the next query present in input.
     * @throws ParseException
     */
    public Query parseQuery() throws ParseException {
        skipWhitespace();

        char c = input.peek();

        if (c == '(') {
            return parseOperation();
        } else {
            return parseFunction();
        }
    }

    private Query parseOperation() throws ParseException {
        if (input.next() != '(') {
            throw new ParseException(input, "operation",
                    "Operation must start with '('");
        }

        Query q1 = parseQuery();

        skipWhitespace();
        char c = input.next();
        Operation op = null;

        if (c == '&') {
            op = Operation.AND;
        } else if (c == '|') {
            op = Operation.OR;
        } else {
            throw new ParseException(input, "operation",
                    "Invalid operation. Must be & or |");
        }

        Query q2 = parseQuery();

        skipWhitespace();

        if (input.next() != ')') {
            throw new ParseException(input, "operation",
                    "Operation must end with ')'");
        }

        return new Query(op, q1, q2);
    }

    private Query parseFunction() throws ParseException {
        String name = parseWord();

        if (input.next() != '(') {
            throw new ParseException(input, "function",
                    "Function must have ( after name");
        }

        List<Object> args = new ArrayList<Object>();

        for (;;) {
            skipWhitespace();
            char c = input.peek();

            if (c == ')') {
                input.next();
                break;
            } else {
                args.add(parseFunctionArgument());
            }
        }

        return new Query(new Function(name, args.toArray()));
    }

    private Object parseFunctionArgument() throws ParseException {
        char c = input.peek();

        if (c == '\'') {
            return parseString();
        } else if (c == '[') {
            return parseLocation();
        } else {
            throw new ParseException(input, "argument",
                    "Function argument must be location or string");
        }
    }

    private String parseWord() throws ParseException {
        skipWhitespace();
        input.mark();

        for (;;) {
            char c = input.peek();

            if (Character.isLetter(c) || c == '_' || c == '-') {
                input.next();
            } else {
                break;
            }
        }

        String s = input.marked();

        if (s.isEmpty()) {
            throw new ParseException(input, "Expecting word");
        }

        return s;
    }

    private Location parseLocation() throws ParseException {
        if (input.next() != '[') {
            throw new ParseException(input, "location",
                    "Location must start with [");
        }

        String typename = parseWord();

        Geometry.Type type = null;

        if (typename.equals("point")) {
            type = Geometry.Type.POINT;
        } else if (typename.equals("line")) {
            type = Geometry.Type.LINE;
        } else if (typename.equals("polygon")) {
            type = Geometry.Type.POLYGON;
        } else {
            throw new ParseException(input, "location",
                    "Type must be point, line, or polygon");
        }

        skipWhitespace();

        URI srid;
        try {
            srid = new URI(parseString());
        } catch (URISyntaxException e) {
            throw new ParseException(input, "location", "Invalid SRID: "
                    + e.getMessage());
        }

        List<Point> points = new ArrayList<Point>();

        for (;;) {
            skipWhitespace();
            points.add(parsePoint());
            skipWhitespace();

            char c = input.next();

            if (c == ']') {
                break;
            } else if (c != ',') {
                throw new ParseException(input, "location coordinates",
                        "Expecting , or [");
            }
        }

        Geometry g = new Geometry(type, points.toArray(new Point[] {}));
        return new Location(g, srid);
    }

    private Point parsePoint() throws ParseException {
        List<Double> coords = new ArrayList<Double>(2);

        for (;;) {
            char c = input.peek();

            if (c == '.' || c == '-' || Character.isDigit(c)) {
                coords.add(parseDouble());
            } else {
                break;
            }

            skipWhitespace();
        }

        double[] result = new double[coords.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = coords.get(i);
        }

        return new Point(result);
    }

    private String parseString() throws ParseException {
        StringBuilder s = new StringBuilder();
        char c = input.next();

        if (c != '\'') {
            throw new ParseException(input, "string",
                    "String must start with '");
        }

        boolean escaped = false;

        for (;;) {
            c = input.next();

            if (escaped) {
                s.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '\'') {
                return s.toString();
            } else {
                s.append(c);
            }
        }
    }
}
