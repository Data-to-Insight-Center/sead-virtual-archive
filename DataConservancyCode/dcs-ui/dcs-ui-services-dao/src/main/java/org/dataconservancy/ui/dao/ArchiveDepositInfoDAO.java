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
package org.dataconservancy.ui.dao;

import java.util.List;

import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Type;

/**
 * Responsible for CRUD operations on an underlying persistence store for {@link ArchiveDepositInfo} objects.
 * The deposit id is the primary key.
 */
public interface ArchiveDepositInfoDAO {

	public void add(ArchiveDepositInfo info);

	/**
	 * Given an ArchiveDepositInfo object, this method overwrite the existing
	 * record of matching deposit id with the new value contained in the fields
	 * 
	 * @param info
	 */
	public void update(ArchiveDepositInfo info);

	/**
     * Lookup information about the identified deposit.
     *
	 * @param deposit_id the deposit identifier representing a transaction with the archive
	 * @return information about a deposit attempt
	 */
	public ArchiveDepositInfo lookup(String deposit_id);


    /**
     * Lookup information about deposits that have the identified deposit as a parent.
     * <p/>
     * The {@code ArchiveService} may perform multiple deposits behind the scenes when a business object is deposited
     * to the archive.  For example, when a {@code DataItem} is deposited, a deposit occurs for the DataItem then
     * separate deposits for each {@code DataFile} in the DataItem.  In this example, the DataFile deposits will have
     * the DataItem deposit id as a parent.  Looking up the children of the DataItem deposit id should result in the
     * ADI for the DataFiles.
     *
   	 * @param deposit_id the deposit identifier representing a transaction with the archive
   	 * @return information about deposits that were attempted as a result of the supplied deposit_id
   	 */
   	public List<ArchiveDepositInfo> lookupChildren(String deposit_id);

	/**
     * Returns deposits that match the supplied criteria.  The head of the returned List will have the newest
     * deposit, the tail of the List will have the oldest.
     * 
	 * @param type
	 *            null to match all types
	 * @param status
	 *            null to match any status
	 * @return information about latest matching deposits. return empty list if nothing is found.
	 */
    public List<ArchiveDepositInfo> list(Type type, Status status);
	
	
    /**
     * Return all deposit attempts for the given object with the given status, sorted in the order that element
     * at index 0 would be the latest matching deposit attempt.
     * 
     * @param status or null for any status
     * @return matching deposits
     */
    public List<ArchiveDepositInfo> listForObject(String object_id, Status status);
}
