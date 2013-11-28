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

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Output;

public class DcpOutput implements Output<String, Dcp> {

	private Dcp value;

	public DcpOutput() {
		value = null;
	}

	/**
	 * Returns value of the output instance
	 * 
	 * @return <code>Dcp</code> - Dcp package that had been written to this
	 *         output
	 *         <p>
	 *         If the output has not been written to, Dcp package would be
	 *         <code>null</code>
	 */
	public Dcp getValue() {
		return value;
	}

	/**
	 * Determines whether the output instance as a value
	 * 
	 * @return <ul>
	 *         <li> <code>true</code> - if value is null</li>
	 *         <li> <code>false</code> - if value is not null</li>
	 *         </ul>
	 */
	public boolean isValueEmpty() {
		if (value == null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void write(String key, Dcp value) {
		this.value = value;
	}

	@Override
	public void close() {

	}
}
