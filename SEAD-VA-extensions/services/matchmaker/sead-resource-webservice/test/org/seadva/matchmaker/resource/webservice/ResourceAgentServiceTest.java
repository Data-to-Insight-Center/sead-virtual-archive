

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
    package org.seadva.matchmaker.resource.webservice;

    /*
     *  ResourceAgentServiceTest Junit test case
    */

import org.apache.abdera.parser.stax.FOMCollection;
import org.apache.axiom.om.OMElement;

public class ResourceAgentServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        public  void testgetResources() throws Exception{

            System.out.print("printing");
            try{
                GetResourcesRequest resourcesRequest = new GetResourcesRequest();
                OMElement paramtemp = new FOMCollection();
                resourcesRequest.setGetResourcesRequest(paramtemp);

                ResourceAgentServiceStub resourceAgentServiceStub = new ResourceAgentServiceStub();
                GetResourcesResponse resourcesResponse = resourceAgentServiceStub.getResources(resourcesRequest);
                System.out.print("printing "+resourcesResponse.getResourceClassAd()[0].getType()+" \n"+resourcesResponse.getResourceClassAd()[0].getRequirements().getRule()[0].getSubject()+" "+resourcesResponse.getResourceClassAd()[0].getRequirements().getRule()[0].getPredicate()+" "+resourcesResponse.getResourceClassAd()[0].getRequirements().getRule()[0].getObject());
            }
            catch (Exception e){
                e.printStackTrace();
            }




        }



    }
    