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
package org.dataconservancy.archive.impl.fcrepo.ri;

import com.google.common.collect.AbstractIterator;
import org.apache.http.protocol.HTTP;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ntriples.NTriplesUtil;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RIQueryResult extends AbstractIterator<List<Value>> {

    private final BufferedReader reader;

    private final ValueFactory factory;

    private boolean exhausted;

    RIQueryResult(InputStream in) {
        try {
            reader = new BufferedReader(new InputStreamReader(in, HTTP.UTF_8));
        } catch (UnsupportedEncodingException wontHappen) {
            throw new RuntimeException(wontHappen);
        }
        factory = new ValueFactoryImpl();
        exhausted = false;
    }

    @Override
    protected List<Value> computeNext() {
        if (exhausted) {
            return endOfData();
        } else {
            List<Value> values = new ArrayList<Value>();
            try {
                String line = reader.readLine();
                while (line != null && line.length() > 0) {
                    values.add(parse(line));
                    line = reader.readLine();
                }
                if (values.size() == 0) {
                    return endOfData();
                } else if (line == null) {
                    exhausted = true;
                    close();
                }
                return values;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Parse a query result value of the form "name : NTriplesValue"
    private Value parse(String line) {
        int i = line.indexOf(" : ");
        if (i == -1) {
            throw new IllegalArgumentException("Malformed line in RI Query "
                    + "result (expected ' : ' delimiter): '" + line + "'");
        } else {
            return NTriplesUtil.parseValue(line.substring(i + 3), factory);
        }
    }

    @PreDestroy
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
