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
package org.dataconservancy.dcs.ingest.services.util;

import java.util.Set;

import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.dcp.Dcp;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

/**
 * Wrapper which allows writing to one sipStager, but reading from many.
 * <p>
 * Given a single writable stager for all initial r/w operations, will fall back
 * to querying any specified secondary stagers for
 * {@link SipStager#getSIP(String)} if one is not found in the primary writable
 * stager.
 * </p>
 * <p>
 * <dl>
 * <dt>{@link #setWritableStager(SipStager)}</dt>
 * <dd><b>Required</b>. Sets the primary, read/write stager.</dd>
 * <dt>{@link #setReadableStager(SipStager...)}</dt>
 * <dd>Sets one or more secondary stagers to be polled in a read-only fashion.</dd>
 * </dl>
 * </p>
 */
public class ReadingSipStager
        implements SipStager {

    private SipStager writable;

    private SipStager[] readable = new SipStager[0];

    @Required
    public void setWritableStager(SipStager stager) {
        writable = stager;
    }

    public void setReadableStager(SipStager... stagers) {
        readable = stagers;
    }

    public String addSIP(Dcp sip) {
        return writable.addSIP(sip);
    }

    public Set<String> getKeys() {
        /* Might want to consider getting keys from every stager */
        return writable.getKeys();
    }

    public Dcp getSIP(String id) {
        Dcp sip = writable.getSIP(id);

        if (sip != null) {
            return sip;
        } else {
            for (SipStager stager : readable) {
                if ((sip = stager.getSIP(id)) != null) {
                    return sip;
                }
            }
        }

        return null;
    }

    public void removeSIP(String id) {
        writable.removeSIP(id);
    }

    public void retire(String id) {
        writable.retire(id);
    }

    public void updateSIP(Dcp sip, String id) {
        writable.updateSIP(sip, id);
    }

}
