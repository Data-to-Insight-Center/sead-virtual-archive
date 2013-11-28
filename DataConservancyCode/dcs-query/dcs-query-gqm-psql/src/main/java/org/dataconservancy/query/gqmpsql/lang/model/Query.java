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
package org.dataconservancy.query.gqmpsql.lang.model;

import java.util.Arrays;

/**
 * The leafs of the query tree are functions. Inner nodes are logical operations
 * on children.
 */
public class Query {
    public enum Operation {
        AND, OR;
    }

    private final Query[] children;
    private final Operation op;
    private final Function func;

    public Query(Function func) {
        this.children = null;
        this.func = func;
        this.op = null;
    }

    public Query(Operation op, Query... children) {
        this.op = op;
        this.children = children;
        this.func = null;
    }

    public Query[] children() {
        return children;
    }

    public boolean isFunction() {
        return func != null;
    }

    public boolean isOperation() {
        return op != null && children != null;
    }

    public Operation operation() {
        return op;
    }

    public Function function() {
        return func;
    }

    public int hashCode() {
        return isFunction() ? func.hashCode() : op.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Query)) {
            return false;
        }

        Query q = (Query) o;

        if (isFunction()) {
            return func.equals(q.func);
        } else {
            return op == q.op && Arrays.equals(children, q.children);
        }
    }

    public String toString() {
        return func != null ? func.toString() : "{" + op.name()
                + Arrays.toString(children) + "}";
    }
}
