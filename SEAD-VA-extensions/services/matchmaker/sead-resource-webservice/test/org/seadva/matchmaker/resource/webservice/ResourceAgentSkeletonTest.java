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

package org.seadva.matchmaker.resource.webservice;

import org.apache.abdera.parser.stax.FOMCollection;
import org.apache.axiom.om.OMElement;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 3/18/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceAgentSkeletonTest {
    @Test
    public void testGetResources(){
        ResourceAgentServiceSkeleton resourceAgentServiceSkeleton = new ResourceAgentServiceSkeleton();
        GetResourcesRequest resourcesRequest = new GetResourcesRequest();
        OMElement paramtemp = new FOMCollection();
        resourcesRequest.setGetResourcesRequest(paramtemp);
        GetResourcesResponse response = resourceAgentServiceSkeleton.getResources(resourcesRequest);
        System.out.print(response.getResourceClassAd().length);
        for(int i=0;i<response.getResourceClassAd().length;i++)
            System.out.println(response.getResourceClassAd()[i].getCharacteristics().getCharacteristic()[3].getValue());
    }


}
