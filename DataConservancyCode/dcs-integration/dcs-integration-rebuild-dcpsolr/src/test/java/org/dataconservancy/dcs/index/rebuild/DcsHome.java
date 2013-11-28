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

import java.util.Properties;

import org.apache.commons.io.FileUtils;

import org.slf4j.MDC;

public class DcsHome {

    public static void prepareHome(Class<?> testClass) {
        try {

            Properties defaultProps = new Properties();
            defaultProps.load(DcsHome.class
                    .getResourceAsStream("/default.properties"));

            MDC.put("testClass", testClass.getName());

            System.out.println(">>> DCS HOME "
                    + defaultProps.getProperty("dcs.home"));

            File dcsHome =
                    new File(defaultProps.getProperty("dcs.home"),
                             testClass.getName());
            
            System.out.println(">>> DCS HOME "
                    + dcsHome);

            System.setProperty("dcs.home", dcsHome.getAbsolutePath());

            if (dcsHome.exists()) {
                FileUtils.cleanDirectory(dcsHome);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
