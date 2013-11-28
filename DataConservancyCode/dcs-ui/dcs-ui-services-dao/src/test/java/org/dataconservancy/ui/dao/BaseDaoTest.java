/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.test.support.BaseSpringAwareTest;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base test class for the dcs-ui-services-dao module.  Serves as a common point for configuring the Spring-based
 * class-level annotations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/org/dataconservancy/ui/config/test-applicationContext.xml", "classpath*:/org/dataconservancy/ui/config/applicationContext.xml" })
@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
public abstract class BaseDaoTest extends BaseSpringAwareTest {

}
