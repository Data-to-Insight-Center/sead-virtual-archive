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
package org.dataconservancy.dcs.index.rebuild;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderedDcpEntitySourceTest {

    private static File tmpdir;

    private static OrderedDcpEntitySource src;

    @BeforeClass
    public static void setUp() {

        tmpdir =
                new File(System.getProperty("project.build.testOutputDirectory"),
                         OrderedDcpEntitySourceTest.class.getName());

        if (!tmpdir.exists()) {
            tmpdir.mkdirs();
        }

        src = new OrderedDcpEntitySource(5, tmpdir);
    }

    @Test
    public void entityMatchTest() {

        List<DcsEntity> pkgEntities = new ArrayList<DcsEntity>();

        for (Dcp dcp : src.getPackages()) {
            for (DcsEntity e : dcp) {
                pkgEntities.add(e);
            }
        }

        List<DcsEntity> givenEntities = src.getEntities(null);

        assertEquals(pkgEntities.size(), givenEntities.size());

        assertTrue(pkgEntities.containsAll(src.getEntities(null)));
    }

    @Test
    public void validDcpTest() {
        IdRelationValidator validator = new IdRelationValidator();
        for (Dcp pkg : src.getPackages()) {
            validator.validate(pkg);
        }
    }

    @Test
    public void existingDescendantsTest() {
        List<DcsEntity> givenEntities = src.getEntities(null);

        for (DcsEntity entity : src.getEntities(null)) {
            for (DcsEntity descendant : src.getDescendantsOf(entity.getId())) {
                assertTrue(givenEntities.contains(descendant));
            }
        }
    }
}
