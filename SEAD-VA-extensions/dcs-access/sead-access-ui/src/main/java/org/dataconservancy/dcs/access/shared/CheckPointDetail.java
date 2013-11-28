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

package org.dataconservancy.dcs.access.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CheckPointDetail 
				implements IsSerializable {
	boolean checkPointed;
	int numSplitSIPs;
	String resumeSipPath;
	List<String> previousStatusUrls;
	
	public boolean isCheckPointed() {
		return checkPointed;
	}
	public void setCheckPointed(boolean checkPointed) {
		this.checkPointed = checkPointed;
	}
	public int getNumSplitSIPs() {
		return numSplitSIPs;
	}
	public void setNumSplitSIPs(int numSplitSIPs) {
		this.numSplitSIPs = numSplitSIPs;
	}
	public String getResumeSipPath() {
		return resumeSipPath;
	}
	public void setResumeSipPath(String resumeSipPath) {
		this.resumeSipPath = resumeSipPath;
	}
	public List<String> getPreviousStatusUrls() {
		return previousStatusUrls;
	}
	public void setPreviousStatusUrls(List<String> previousStatusUrls) {
		this.previousStatusUrls = previousStatusUrls;
	}
}
