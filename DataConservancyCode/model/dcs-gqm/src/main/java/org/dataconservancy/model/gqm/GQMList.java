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
package org.dataconservancy.model.gqm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A container to store multiple gqms for de/serializing to and from the search results. 
 *
 */
public class GQMList {
    private List<GQM> gqms;
    
    public GQMList(){
        gqms = new ArrayList<GQM>();
    }
    
    public List<GQM> getGQMs(){
        return gqms;
    }
    
    public boolean equals(Object o) {
        GQMList gqmList = (GQMList) o;

        if (gqmList == null) {
            return false;
        }

        return new HashSet<GQM>(gqms)
                        .equals(new HashSet<GQM>(gqmList.gqms));
    }

    public String toString() {
        return "gqms" + gqms;
    }
}
