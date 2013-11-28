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
package org.dataconservancy.ui.it.support;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.dataconservancy.dcs.id.api.Types;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * IT insuring that the CreateIdApiRequest will not create duplicate ids.
 */
public class CreateIdApiRequestIT extends BaseIT {

    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The number of IDs each test method is going to generate
     */
    private final int countToGenerate = 20000;

    /**
     * Stores the generated ids.
     */
    private final Set<String> generatedIds = Collections.synchronizedSet(new HashSet<String>());

    /**
     * Stores a count of each type of id generated.
     */
    private final ConcurrentHashMap<Types, AtomicInteger> idTypeDistribution =
            new ConcurrentHashMap<Types, AtomicInteger>();

    /**
     * The HttpClient used by the CreateIdApiRequest
     */
    private HttpClient hc = new DefaultHttpClient();

    /**
     * Generates {@link #countToGenerate} ids, and insures that they are all unique.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateUniqueIdsSingleThread() throws Exception {
        long start = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < countToGenerate; i++) {
            // Select an ID type to generate, based on a randomized seed.
            double seed = Math.random();
            log.trace("Seed is {}", seed);
            Types t = selectType(seed, Types.values());
            log.trace("Selected type {} with seed value {}", t, seed);

            // If debugging is enabled, keep track of the number of ids created for each type.
            if (log.isDebugEnabled()) {
                if (idTypeDistribution.containsKey(t)) {
                    idTypeDistribution.get(t).getAndAdd(1);
                } else {
                    idTypeDistribution.put(t, new AtomicInteger(1));
                }
            }

            // Create an ID, and keep it in a Set.
            generatedIds.add(reqFactory.createIdApiRequest(t).execute(hc));
        }

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("ID distribution:\n");
            int totalGenerated = 0;
            for (Types t : Types.values()) {
                final Integer typeTotal = idTypeDistribution.get(t).get();
                totalGenerated += typeTotal;
                sb.append("Type: ").append(t).append(" Count: ").append(typeTotal).append("\n");
            }
            sb.append("Total generated: ").append(totalGenerated).append("\n");
            sb.append("Unique generated: ").append(generatedIds.size()).append("\n");
            sb.append("Execution time: ").append(Calendar.getInstance().getTimeInMillis() - start).append(" ms\n");
            log.debug(sb.toString());
        }

        // The number of generated IDs (stored in the Set) should equal 'countToGenerate'
        assertEquals("Expected " + countToGenerate + " to be generated, but the Set contained " + generatedIds.size() +
                ".  Some ids may not have been unique.", countToGenerate, generatedIds.size());
    }


    /**
     * Generates {@link #countToGenerate} ids, and insures that they are all unique.  Uses multiple threads to
     * generate the ids.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateUniqueIdsMultipleThreads() throws Exception {
        long start = Calendar.getInstance().getTimeInMillis();
        // The threads used to generate ids
        Thread threads[] = new Thread[5];

        // HttpClient requires a ThreadSafeClientConnectionManager
        final ThreadSafeClientConnManager conman = new ThreadSafeClientConnManager();
        conman.setMaxTotal(50);
        conman.setDefaultMaxPerRoute(5);
        hc = new DefaultHttpClient(conman);

        assertEquals("The number of threads (" + threads.length + ") must evenly divide into thenumber of ids to be " +
                "generated (" + countToGenerate + ")", 0, countToGenerate % threads.length);
        final int generatePerThread = countToGenerate / threads.length;

        // Launch a thread, with each thread being responsible for generating a portion of the total ids
        for (int j = 0; j < threads.length; j++) {
            threads[j] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < generatePerThread; i++) {
                        double seed = Math.random();
                        log.trace("Seed is {}", seed);
                        Types t = selectType(seed, Types.values());
                        log.trace("Selected type {} with seed value {}", t, seed);

                        if (log.isDebugEnabled()) {
                            idTypeDistribution.putIfAbsent(t, new AtomicInteger(0));
                            idTypeDistribution.get(t).getAndAdd(1);
                        }

                        try {
                            generatedIds.add(reqFactory.createIdApiRequest(t).execute(hc));
                        } catch (IOException e) {
                            fail(e.getMessage());
                        }
                    }
                }
            }, "ID Generation Thread " + j);
            threads[j].start();
        }

        // Wait for threads to stop
        for (int j = 0; j < threads.length; j++) {
            threads[j].join();
        }

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("ID distribution:\n");
            int totalGenerated = 0;
            for (Types t : Types.values()) {
                final Integer typeTotal = idTypeDistribution.get(t).intValue();
                totalGenerated += typeTotal;
                sb.append("Type: ").append(t).append(" Count: ").append(typeTotal).append("\n");
            }
            sb.append("Total generated: ").append(totalGenerated).append("\n");
            sb.append("Unique generated: ").append(generatedIds.size()).append("\n");
            sb.append("Number of threads: ").append(threads.length).append("\n");
            sb.append("Execution time: ").append(Calendar.getInstance().getTimeInMillis() - start).append(" ms\n");
            log.debug(sb.toString());
        }

        // The number of generated IDs (stored in the Set) should equal 'countToGenerate'
        assertEquals("Expected " + countToGenerate + " to be generated, but the Set contained " + generatedIds.size() +
                ".  Some ids may not have been unique.", countToGenerate, generatedIds.size());

    }

    /**
     * Selects an Id Type based on a random seed value.
     *
     * @param randomSeed    a number equal to or greater than 0, but less than 1 (e.g. output from {@code Math.random()})
     * @param possibleTypes Id Types to select from
     * @return the Id Type based on the supplied seed.
     */
    private Types selectType(double randomSeed, Types[] possibleTypes) {
        final double increment = 1d / possibleTypes.length;

        for (int i = 0; i < possibleTypes.length; i++) {
            log.trace("i: {}, seed: {}, seed*i: {}, seed*(i+1): {}",
                    new Object[]{i, randomSeed, randomSeed * i, randomSeed * (i + 1)});
            if (randomSeed >= i * increment && randomSeed < (i + 1) * increment) {
                return possibleTypes[i];
            }
        }

        return null;
    }

}
