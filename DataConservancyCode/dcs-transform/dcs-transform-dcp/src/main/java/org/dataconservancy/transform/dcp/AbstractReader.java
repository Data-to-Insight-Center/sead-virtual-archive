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
package org.dataconservancy.transform.dcp;

import java.util.HashMap;
import java.util.Iterator;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Reader;

public class AbstractReader  implements Reader<String, Dcp> {
	
	protected HashMap<String, Dcp> resultMap;
	protected Iterator<String> keyIterator;
	protected String currentKey;
	
	/**
     * Returns the ID of the root DU.
     */
    public String getCurrentKey() {
        return currentKey;
    }

    /** Returns a Dcp containing a root DU and all descendants. */
    public Dcp getCurrentValue() {
        return resultMap.get(currentKey);
    }
    
    /** Returns true if there is another Dcp with a root DeliverableUnit. 
     * If there exists another Dcp, move the pointer to the next Dcp*/
    public boolean nextKeyValue() {
    	boolean hasNext = keyIterator.hasNext();
    	if(hasNext)
    	{
    		currentKey = keyIterator.next();
    	}
    	return hasNext;
    }
    
    public int getNumberOfResults()
    {
    	return resultMap.size();
    }
    public void close() {
    	
    }
}
