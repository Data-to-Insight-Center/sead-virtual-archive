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
package org.dataconservancy.transform.execution;

import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.springframework.beans.factory.annotation.Required;

/**
 * Wraps a chain or pipeline of mappings into a single composite unit.
 * <p>
 * Within a chain of mappings, the output of one mapping is fed directly into
 * the input of another. This class, when given a list of mappings, will feed
 * input values into the first mapping, will direct the output down the chain
 * through successive mappings, and emit the results of the last one.
 * 
 * <pre>
 * in -> map1 -> map2 -> map3 -> ... -> mapN -> out
 * </pre>
 * In general, chains of mappings are especially helpful in situations where a
 * complex operation can represented as a composition of small, independent,
 * reusable operations.
 * </p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MappingChain<Ki, Vi, Ko, Vo>
        implements Mapping<Ki, Vi, Ko, Vo> {

    private Mapping[] chain = new Mapping[0];

    /**
     * Set the particular chain of mappings to be executed.
     * <p>
     * Will be executed in order, and the output of one fed into the input of
     * another. No type checking is performed beforehand. Erroneous chains may
     * result in a ClassCastException at runtime if the output types of one
     * mapping stage do not match the input types of the next.
     * </p>
     * 
     * @param mappings
     *        array of mappings.
     */
    @Required
    public void setChain(Mapping<?, ?, ?, ?>... mappings) {
        chain = mappings;
    }

    /**
     * Perform a chain of mappings.
     */
    public void map(Ki key, Vi val, Output<Ko, Vo> writer) {

        /*
         * TODO: Cache chains as a function of the end writer. We essentially
         * create a whole new chain for each map() operation, which could
         * possibly be more expensive than it needs to be.
         */
        Output out = initChainOutput(writer);
        out.write(key, val);
    }

    /* Get the initial Output instance that will feed the chain */
    private Output initChainOutput(Output finalOutput) {

        if (chain.length == 0) {
            return finalOutput;
        } else {
            return newStage(finalOutput, chain.length - 1);
        }
    }

    /* Build a chain of Outputs backwards */
    private Output newStage(Output finalOutput, int level) {
        Output out = new MappingChainOutput(chain[level], finalOutput);

        if (level == 0) {
            /* At the top level, we're done */
            return out;
        } else {
            /* Pass this Output for the previous stage to write to */
            return newStage(out, level - 1);
        }
    }

    private class MappingChainOutput
            implements Output<Ki, Vi> {

        private final Mapping<Ki, Vi, Ko, Vo> nextMapping;

        private final Output<Ko, Vo> nextOutput;

        public MappingChainOutput(Mapping<Ki, Vi, Ko, Vo> passToMapping,
                                  Output<Ko, Vo> passToMappingOutput) {
            nextMapping = passToMapping;
            nextOutput = passToMappingOutput;
        }

        public void close() {
            /* Do nothing */
        }

        @Override
        public void write(Ki key, Vi val) {
            nextMapping.map(key, val, nextOutput);
        }
    }

}
