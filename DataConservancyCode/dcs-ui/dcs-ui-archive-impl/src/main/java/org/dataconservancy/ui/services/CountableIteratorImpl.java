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

import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.model.dcs.DcsEntity;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 */
public class CountableIteratorImpl implements CountableIterator<DcsEntity> {

    private Collection<DcsEntity> stuff;

    private Iterator<DcsEntity> delegate;

    public CountableIteratorImpl(Collection<DcsEntity> stuff) {
        this.stuff = stuff;
        this.delegate = stuff.iterator();
    }

    @Override
    public long count() {
        return stuff.size();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public DcsEntity next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        delegate.remove();
    }
}
