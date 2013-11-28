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
package org.dataconservancy.dcs.archive.impl.elm;

import java.util.Random;
import java.util.UUID;

import org.dataconservancy.archive.api.EntityType;

public class DcpUtil {

    private static Random random = new Random();

    public static String randomType() {
        EntityType[] types = EntityType.values();
        return types[Math.abs(random.nextInt()) % (types.length - 1)]
                .toString();

    }

    public static String randomId() {
        return "uuid:/" + UUID.randomUUID().toString();
    }
}
