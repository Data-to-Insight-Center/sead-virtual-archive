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
package org.dataconservancy.dcs.id.impl.hibernate;

import java.lang.String;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.hibernate.TypeInfo;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * Id service that persists identifier info in hibernate.
 * <p>
 * For simplicity sake, only the uid and type are persisted. Identifier URLs are
 * generated based on the uid and a url prefix. This prefix is configurable.
 * Thus, id urls are persistent only to the degree that is dictated by policy.
 * </p>
 * <h2>Configuration</h2>
 * <p>
 * <dt>{@link #setSessionFactory(SessionFactory)}</dt>
 * <dd><b>Required</b>. Fully populated Hibernate session factory.</dd>
 * <dt>{@link #setUrlPrefix(String)}</dt>
 * <dd>Optional. Sets a specific prefix for generating identifier URLs</dd>
 * </p>
 */
@Transactional
public class HibernateIdService
        implements PrefixableIdService, BulkIdCreationService {

    private SessionFactory sessionFactory;

    private HibernateIdentifierFactory identifierFactory;

    private String urlPrefix = "http://dataconservancy.org/y1p/";

    /**
     * Specify a fully-configured hibernate session factory
     *
     * @param sessionFactory Fully configured sessionFactory.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setIdentifierFactory(HibernateIdentifierFactory identifierFactory) {
        this.identifierFactory = identifierFactory;
    }

    public Identifier create(String type) {
        Identifier newIdentifier = this.identifierFactory.createNewIdentifier(this.urlPrefix, type);

        this.sessionFactory.getCurrentSession().save(((IdWrapper) newIdentifier).getPersistentIdentity());

        return newIdentifier;
    }

    @Override
    public List<Identifier> create(int count, String type) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be a positive integer.");
        }

        List<Identifier> ids = new ArrayList<Identifier>(count);
        for (int i = 0; i < count; i++) {
            ids.add(this.identifierFactory.createNewIdentifier(this.urlPrefix, type));
        }

        final Session session = this.sessionFactory.getCurrentSession();
        for (Identifier id : ids) {
            session.save(((IdWrapper) id).getPersistentIdentity());
        }

        return ids;
    }

    public Identifier fromUid(String uid) throws IdentifierNotFoundException {
        Object retrieved =
                this.sessionFactory.getCurrentSession()
                        .get(TypeInfo.class, IdWrapper.getId(uid));
        if (retrieved != null) {
            return this.identifierFactory.createIdentifierWithExistingPersistentId(this.urlPrefix, (TypeInfo) retrieved);
        } else {
            throw new IdentifierNotFoundException();
        }
    }

    public Identifier fromUrl(URL url) throws IdentifierNotFoundException {
        String urlString = url.toString();
        int index = urlString.lastIndexOf("/") + 1;
        String uid = null;
        try {
            uid = urlString.substring(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IdentifierNotFoundException("Error parsing the uid from the url string [" + urlString + "]: " +
                    e.getMessage(), e);
        }
        return fromUid(uid);
    }

    /**
     * Set the url prefix for generating identifier URLs.
     *
     * @param prefix URL fragment that can have arbitrary content appended to.
     */
    public void setUrlPrefix(String prefix) {
        urlPrefix = prefix;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }
}
