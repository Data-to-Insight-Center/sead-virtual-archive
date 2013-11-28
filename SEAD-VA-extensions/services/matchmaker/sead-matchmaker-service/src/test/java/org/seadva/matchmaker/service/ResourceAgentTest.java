

/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ResourceAgentServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
    package org.seadva.matchmaker.service;

    /*
     *  ResourceAgentServiceTest Junit test case
    */

import org.junit.Test;

import java.util.List;

public class ResourceAgentTest extends junit.framework.TestCase{

     
       @Test
        public  void testgetResources() throws Exception{

           ResourceAgent agent = new ResourceAgent();
           List<ClassAd> classAdList = agent.getAdvertisements();
           System.out.print(classAdList.size());
           for(int i=0;i<classAdList.size();i++)
               System.out.print(classAdList.get(i).getCharacteristics().getValues().get("name"));
        }



    }
    