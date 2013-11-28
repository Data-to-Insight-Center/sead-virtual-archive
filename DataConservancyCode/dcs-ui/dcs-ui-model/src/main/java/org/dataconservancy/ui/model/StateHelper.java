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
package org.dataconservancy.ui.model;

/**
 * StateHelper provides a list of states to stripes.
 * usage of this can be found here: http://www.stripesframework.org/display/stripes/US+State+Helper
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This view helper returns a list of states
 * 
 */
public class StateHelper {

	/**
	 * Objects are returned of type State
	 */
	public class State {
		private String code;
		private String name;

		protected State(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}
	}

	private List<State> states = null;

	public StateHelper() {
		states = new ArrayList<State>(51);
		states.add(new State("AL", "Alabama"));
		states.add(new State("AK", "Alaska"));
		states.add(new State("AZ", "Arizona"));
		states.add(new State("AR", "Arkansas"));
		states.add(new State("CA", "California"));
		states.add(new State("CO", "Colorado"));
		states.add(new State("CT", "Connecticut"));
		states.add(new State("DE", "Delaware"));
		states.add(new State("DC", "Dist of Columbia"));
		states.add(new State("FL", "Florida"));
		states.add(new State("GA", "Georgia"));
		states.add(new State("HI", "Hawaii"));
		states.add(new State("ID", "Idaho"));
		states.add(new State("IL", "Illinois"));
		states.add(new State("IN", "Indiana"));
		states.add(new State("IA", "Iowa"));
		states.add(new State("KS", "Kansas"));
		states.add(new State("KY", "Kentucky"));
		states.add(new State("LA", "Louisiana"));
		states.add(new State("ME", "Maine"));
		states.add(new State("MD", "Maryland"));
		states.add(new State("MA", "Massachusetts"));
		states.add(new State("MI", "Michigan"));
		states.add(new State("MN", "Minnesota"));
		states.add(new State("MS", "Mississippi"));
		states.add(new State("MO", "Missouri"));
		states.add(new State("MT", "Montana"));
		states.add(new State("NE", "Nebraska"));
		states.add(new State("NV", "Nevada"));
		states.add(new State("NH", "New Hampshire"));
		states.add(new State("NJ", "New Jersey"));
		states.add(new State("NM", "New Mexico"));
		states.add(new State("NY", "New York"));
		states.add(new State("NC", "North Carolina"));
		states.add(new State("ND", "North Dakota"));
		states.add(new State("OH", "Ohio"));
		states.add(new State("OK", "Oklahoma"));
		states.add(new State("OR", "Oregon"));
		states.add(new State("PA", "Pennsylvania"));
		states.add(new State("RI", "Rhode Island"));
		states.add(new State("SC", "South Carolina"));
		states.add(new State("SD", "South Dakota"));
		states.add(new State("TN", "Tennessee"));
		states.add(new State("TX", "Texas"));
		states.add(new State("UT", "Utah"));
		states.add(new State("VT", "Vermont"));
		states.add(new State("VA", "Virginia"));
		states.add(new State("WA", "Washington"));
		states.add(new State("WV", "West Virginia"));
		states.add(new State("WI", "Wisconsin"));
		states.add(new State("WY", "Wyoming"));
	}

	public List<State> getAllStates() {
		return states;
	}

}