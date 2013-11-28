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
package org.dataconservancy.transform.execution.run;

import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;

public class Job<Ki, Vi, Ko, Vo> {

    private final Reader<Ki, Vi> reader;

    private final Mapping<Ki, Vi, Ko, Vo> mapping;

    private final Output<Ko, Vo> output;

    public Job(Reader<Ki, Vi> reader,
                        Mapping<Ki, Vi, Ko, Vo> mapping,
                        Output<Ko, Vo> out) {
        this.reader = reader;
        this.mapping = mapping;
        this.output = out;
    }

    public Reader<Ki, Vi> getReader() {
        return reader;
    }

    public Mapping<Ki, Vi, Ko, Vo> getMapping() {
        return mapping;
    }

    public Output<Ko, Vo> getOutput() {
        return output;
    }
}
