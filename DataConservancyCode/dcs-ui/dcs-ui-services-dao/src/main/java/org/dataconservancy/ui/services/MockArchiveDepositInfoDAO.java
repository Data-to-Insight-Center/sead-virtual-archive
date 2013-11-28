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
package org.dataconservancy.ui.services;

import org.dataconservancy.ui.dao.ArchiveDepositInfoDAO;
import org.dataconservancy.ui.model.ArchiveDepositInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A mock implementation of the {@link ArchiveDepositInfo} interface.  Uses an internal Map to keep track of deposit
 * ids linked to deposit information.  The Map may be cleared by calling {@link #reset()}.
 */
public class MockArchiveDepositInfoDAO implements ArchiveDepositInfoDAO {

    // deposit id -> deposit info
    private final Map<String, ArchiveDepositInfo> deposit_store = new HashMap<String, ArchiveDepositInfo>();

    public void add(ArchiveDepositInfo info) {
        deposit_store.put(info.getDepositId(), info);
    }

    public void update(ArchiveDepositInfo info) {
        deposit_store.put(info.getDepositId(), info);
    }

    public ArchiveDepositInfo lookup(String deposit_id) {
        return deposit_store.get(deposit_id);
    }

    public List<ArchiveDepositInfo> list(ArchiveDepositInfo.Type type, ArchiveDepositInfo.Status status) {
        List<ArchiveDepositInfo> result = new ArrayList<ArchiveDepositInfo>();

        for (ArchiveDepositInfo info : deposit_store.values()) {
            if ((type == null || type == info.getObjectType())
                    && (status == null || status == info.getDepositStatus())) {
                result.add(info);
            }
        }
        
        // Sort the List so the most recent (defined by the deposit DateTime)
        // ArchiveDepositInfo objects are at the head of the list
        Collections.sort(result, new Comparator<ArchiveDepositInfo>() {
            @Override
            public int compare(ArchiveDepositInfo thisInfo, ArchiveDepositInfo thatInfo) {
                return thatInfo.getDepositDateTime().compareTo(thisInfo.getDepositDateTime());
            }
        });

        return result;
    }

    @Override
    public List<ArchiveDepositInfo> lookupChildren(String deposit_id) {
        List<ArchiveDepositInfo> result = new ArrayList<ArchiveDepositInfo>();
        for (ArchiveDepositInfo info : deposit_store.values()) {
            if (deposit_id.equals(info.getParentDepositId())) {
                result.add(info);
            }
        }

        return result;
    }

    public List<ArchiveDepositInfo> listForObject(String object_id,
                                                  ArchiveDepositInfo.Status status) {
        List<ArchiveDepositInfo> result = new ArrayList<ArchiveDepositInfo>();

        for (ArchiveDepositInfo info : deposit_store.values()) {
            if (object_id.equals(info.getObjectId())
                    && (status == null || status == info.getDepositStatus())) {
                result.add(info);
            }
        }

        // Sort the List so the most recent (defined by the deposit DateTime)
        // ArchiveDepositInfo objects are at the head of the list
        Collections.sort(result, new Comparator<ArchiveDepositInfo>() {
            @Override
            public int compare(ArchiveDepositInfo thisInfo, ArchiveDepositInfo thatInfo) {
                return thatInfo.getDepositDateTime().compareTo(thisInfo.getDepositDateTime());
            }
        });

        return result;
    }

    /**
     * Resets the in-memory state of this instance, allowing it to be re-used.  Often called in
     * a {@code @After} or {@code @Before} test method.
     */
    public void reset() {
        deposit_store.clear();
    }

}
