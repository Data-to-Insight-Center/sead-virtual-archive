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
package org.dataconservancy.deposit.sword;

public class SWORDConfig {

    private String version = "1.3";

    private boolean verbose = false;

    private boolean noOp = false;

    private int maxUploadSize = 0;

    public String getVersion() {
        return version;
    }

    public void setVerbose(boolean v) {
        verbose = v;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public void setMaxUploadSize(int size) {
        maxUploadSize = size;
    }

    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setNoOp(boolean op) {
        noOp = op;
    }

    public boolean getNoOp() {
        return noOp;
    }

}
