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

import junit.framework.TestCase;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.query.gqmpsql.lang.model.Function;
import org.dataconservancy.query.gqmpsql.lang.model.Query;
import org.dataconservancy.query.gqmpsql.lang.parser.ParseException;
import org.dataconservancy.query.gqmpsql.lang.parser.Parser;

public class ParserTest extends TestCase {

    private Query parse(String s) throws ParseException {
        return new Parser(s).parseQuery();
    }

    public void testSimpleFunction() throws ParseException {
        Query test = parse("foo('maybe')");
        Query right = new Query(new Function("foo", "maybe"));

        assertTrue(test.isFunction());
        assertEquals(right, test);
    }

    public void testSimpleFunction2() throws ParseException {
        Query test = parse(" foo(  'maybe'   'cow'  )  ");
        Query right = new Query(new Function("foo", "maybe", "cow"));

        assertTrue(test.isFunction());
        assertEquals(right, test);
    }

    public void testLocation1() throws ParseException {
        URI srid = URI.create("epsg:32");
        Query test = parse("foo([point 'epsg:32' 0 0, 1 1])");

        Geometry g = new Geometry(Geometry.Type.POINT, new Point(0, 0),
                new Point(1, 1));
        Query right = new Query(new Function("foo", new Location(g, srid)));

        assertEquals(srid,
                ((Location) test.function().arguments()[0]).getSrid());
        assertEquals(new Location(g, srid), test.function().arguments()[0]);
        assertTrue(test.isFunction());
        assertEquals(right, test);
    }

    public void testEscapingString1() throws ParseException {
        Query test = parse("foo('may\\be')");
        Query right = new Query(new Function("foo", "maybe"));

        assertTrue(test.isFunction());
        assertEquals(right, test);
    }

    public void testEscapingString2() throws ParseException {
        Query test = parse("foo('\\\'maybe\\\\')");
        Query right = new Query(new Function("foo", "\'maybe\\"));

        assertTrue(test.isFunction());
        assertEquals(right, test);
    }

    public void testSimpleOperation() throws ParseException {
        Query test = parse("(foo('')&moo('too'))");
        Query right = new Query(Query.Operation.AND, new Query(new Function(
                "foo", "")), new Query(new Function("moo", "too")));

        assertTrue(test.isOperation());
        assertEquals(right, test);
    }

    public void testSimpleOperation2() throws ParseException {
        Query test = parse(" (       foo('')   |   moo('too')  )  ");
        Query right = new Query(Query.Operation.OR, new Query(new Function(
                "foo", "")), new Query(new Function("moo", "too")));

        assertTrue(test.isOperation());
        assertEquals(right, test);
    }

    public void check_invalid(String s) {
        try {
            parse(s);
            assertTrue("Invalid query string accepcted", false);
        } catch (ParseException e) {
        }
    }

    public void testNestedOperation() throws ParseException {
        Query test = parse(" (moo('') & (chew(' cud ') | fertilize([polygon 'uri:42' 0 1, 1 2, 2 3]) ))  ");

        Query right = new Query(Query.Operation.AND, new Query(new Function(
                "moo", "")), new Query(Query.Operation.OR, new Query(
                new Function("chew", " cud ")), new Query(new Function(
                "fertilize", new Location(new Geometry(Geometry.Type.POLYGON,
                        new Point(0, 1), new Point(1, 2), new Point(2, 3)),
                        URI.create("uri:42"))))));
        assertEquals(right, test);
    }

    public void testNestedOperation2() throws ParseException {
        Query test = parse("((entity-id('test:moo') & datetime-covered-by('-10' '100')) | entity-id('test:cow'))");

        Query right = new Query(Query.Operation.OR, new Query(
                Query.Operation.AND, new Query(new Function("entity-id",
                        "test:moo")), new Query(new Function(
                        "datetime-covered-by", "-10", "100"))), new Query(
                new Function("entity-id", "test:cow")));

        assertEquals(right, test);
    }

    public void testInvalidStrings() {
        check_invalid("");
        check_invalid("\'  ");
        check_invalid("@#$!QASDFA[]][]a(0");
        check_invalid("func ( 'blah')");
        check_invalid("func(blah )");
        check_invalid("func([]][][[]");
        check_invalid("(func('blah') &&sdfa|");
    }
}
