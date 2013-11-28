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
 * {@code FundingEntity} represents the sources of funding for {@link Project}.
 */
public class FundingEntity {
    private String funderName;
    private String funderAwardNumber;

    public FundingEntity() {
    }
    public FundingEntity(String _funderName, String _funderAwardNumber) {
        funderName = _funderName;
        funderAwardNumber = _funderAwardNumber;
    }
    public FundingEntity(FundingEntity _fundingEntity) {
        funderName = _fundingEntity.getFunderName();
        funderAwardNumber = _fundingEntity.getFunderAwardNumber();
    }

    public String getFunderName() {
        return funderName;
    }

    public void setFunderName(String funderName) {
        this.funderName = funderName;
    }

    public String getFunderAwardNumber() {
        return funderAwardNumber;
    }

    public void setFunderAwardNumber(String funderAwardNumber) {
        this.funderAwardNumber = funderAwardNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((null == funderName) ? 0 : funderName.hashCode());
        result = prime * result + ((null == funderAwardNumber) ? 0 : funderAwardNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (getClass() != obj.getClass())
            return false;

        FundingEntity other = (FundingEntity) obj;

        if (null != funderName ? !funderName.equals(other.getFunderName()) : null != other.getFunderName())
            return false;
        if (null != funderAwardNumber ? !funderAwardNumber.equals(other.getFunderAwardNumber()) : null != other.getFunderAwardNumber())
            return false;

        return true;
    }
}
