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
package org.dataconservancy.archive.impl.elm;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashSet;
import java.util.Set;

/** Selects a single entity DIP */
public class SingleEntityLogic
        implements DipLogic {

    public Set<String> extractEntities(String id,
                                       InputStream content,
                                       Metadata md,
                                       boolean initial) {
        HashSet<String> container = new HashSet<String>();

        if (initial) {
            container.add(id);
        }

        try {
            content.close();
        } catch (IOException e) {
        }

        return container;
    }

}
