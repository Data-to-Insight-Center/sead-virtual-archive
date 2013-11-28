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
package org.dataconservancy.ui.util;

import org.dataconservancy.ui.model.DataItem;

import java.util.Comparator;

/**
 * Comparator for DataSets: uses the {@link org.dataconservancy.ui.model.DataItem#getDepositDate() deposit date}
 * for comparison.  The DataItem more recently deposited is considered to be the "greater" of the two. <em>Note:
 * this implementation is not consistent with equals.</em>
 *
 * @see java.util.SortedSet
 * @see Comparator#compare(Object, Object) 
 */
public class DataSetComparator implements Comparator<DataItem> {

    /**
     * Comparator for DataSets: uses the {@link org.dataconservancy.ui.model.DataItem#getDepositDate() deposit date}
     * for comparison.  The DataItem deposited most recently is considered to be the "greater" of the two.
     * <p/>
     * {@inheritDoc}
     *
     * @param one {@inheritDoc}
     * @param two {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compare(DataItem one, DataItem two) {
        if (one.getDepositDate() == null && two.getDepositDate() == null) {
            return 0;
        }

        if (one.getDepositDate() == null) {
            return -1;
        }

        if (two.getDepositDate() == null) {
            return 1;
        }

        if (one.equals(two)) {
            return 0;
        }

        return two.getDepositDate().compareTo(one.getDepositDate());
    }
}