/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.business.client.impl;

import java.util.List;

import org.dataconservancy.business.client.ArchiveService;
import org.dataconservancy.business.client.BusinessObject;
import org.dataconservancy.business.client.DepositInfo;
import org.dataconservancy.business.client.DepositListener;
import org.dataconservancy.business.client.Mapper;
import org.springframework.beans.factory.annotation.Required;

/** Generic business archive service implementation.
 *
 * @param <A> Archival type.
 */
public class ArchiveServiceImpl<A>
        implements ArchiveService {

    private MapperFinder<A> mapperFinder;

    private DepositService<A> depositService;

    private LookupService lookupService;

    private RetrievalService<A> retrievalService;

    private CorrelationService<A> correlationService;

    @Required
    public void setMapperFinder(MapperFinder<A> mf) {
        mapperFinder = mf;
    }

    @Required
    public void setDepositService(DepositService<A> ds) {
        depositService = ds;
    }

    @Required
    public void setLookupService(LookupService ls) {
        lookupService = ls;
    }

    @Required
    public void setRetrievalService(RetrievalService<A> rs) {
        retrievalService = rs;
    }

    @Required
    public void setCorrelationService(CorrelationService<A> cs) {
        correlationService = cs;
    }

    public <T extends BusinessObject> String deposit(T businessObject,
                                                     Class<T> businessClass) {

        /* See if this is actually an update by looking up archived versions */
        List<String> versions =
                lookup(businessObject.getId(), IdentifierType.BUSINESS_ID, 1);

        if (versions.isEmpty()) {
            /* New version, null predecessor */
            return correlateAndDeposit(businessObject, null, businessClass);

        } else {
            /* Update the latest version */
            return correlateAndDeposit(businessObject,
                                       versions.get(0),
                                       businessClass);
        }
    }

    public <T extends BusinessObject> String update(T businessObject,
                                                    String predecesorArchiveId,
                                                    Class<T> businessClass) {
        return correlateAndDeposit(businessObject,
                                   predecesorArchiveId,
                                   businessClass);
    }

    public DepositInfo getDepositInfo(String depositId) {
        return depositService.getDepositInfo(depositId);
    }

    public void addListener(DepositListener listener) {
        depositService.addListener(listener);
    }

    @Override
    public <T extends BusinessObject> T retrieve(String archiveId,
                                                 Class<T> businessClass) {

        A archivalObject = retrievalService.retrieve(archiveId);
        if (archivalObject == null) {
            return null;
        }

        List<Mapper<T, A>> mappers = mapperFinder.findMapper(businessClass);

        Mapper<T, A> matchingMapper = null;

        if (mappers.isEmpty()) {
            throw new RuntimeException("No mappers found for "
                    + businessClass.getName());
        } else if (mappers.size() == 1) {
            matchingMapper = mappers.get(0);
        } else {
            for (Mapper<T, A> mapper : mappers) {
                if (mapper.getProfile().conforms(archivalObject)) {
                    matchingMapper = mapper;
                    break;
                }
            }
        }

        if (matchingMapper == null) {
            throw new RuntimeException("Could not find a mapper for profile");
        }

        return matchingMapper.fromArchivaForm(archivalObject);
    }

    public List<String> lookup(String id, IdentifierType type, int... limit) {
        return lookupService.lookup(id, type, limit);
    }

    private <T extends BusinessObject> String correlateAndDeposit(T businessObject,
                                                                  String predecessor,
                                                                  Class<T> boClass) {
        /* New version, just map, correlate and deposit */
        List<Mapper<T, A>> mappers = mapperFinder.findMapper(boClass);

        if (mappers.isEmpty()) {
            throw new RuntimeException("Could not find mapper for "
                    + boClass.getName());
        } else {
            A archivalObject =
                    correlationService
                            .correlate(mappers.get(0)
                                               .toArchivalForm(businessObject),
                                       predecessor);

            return depositService.deposit(archivalObject);
        }
    }
}
