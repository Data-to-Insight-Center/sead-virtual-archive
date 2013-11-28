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
package org.dataconservancy.ui.eventing.events;

import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;
import org.dataconservancy.ui.model.Package;

/**
 * An Event with topic {@link EventTopic#DATA_ITEM_DEPOSIT} and class {@link EventClass#AUDIT}
 */
public class DataItemDepositEvent extends AbstractAuditEvent<Package> {
    private Package pkg;

    public DataItemDepositEvent(EventContext eventContext, Package pkg) {
        super(eventContext, EventTopic.DATA_ITEM_DEPOSIT);
        this.pkg = pkg;
    }

    @Override
    public Package getEventObject() {
        return pkg;
    }
}
