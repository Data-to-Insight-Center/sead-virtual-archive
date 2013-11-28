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
package org.dataconservancy.model.dcs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Set implementation which uses object identity instead of equality for fulfilling the {@link Set} contract.
 *
 * TODO: javadoc
 */
public class IdSet<E> implements Set<E> {

    private final Map<Integer, E> map;

    public IdSet() {
        map = new HashMap<Integer, E>();
    }

    public IdSet(int size) {
        map = new HashMap<Integer, E>(size);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Adds {@code o} if the {@code Set} doesn't already contain it.
     *
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean add(E o) {
        if (contains(o)) {
            return false;
        }

        final Integer i = System.identityHashCode(o);
        map.put(i, o);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param collection
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean modified = false;
        for (E o : collection) {
            if (add(o)) {
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return (map.remove(System.identityHashCode(o)) != null);
    }

    @Override
    public boolean removeAll(Collection objects) {
        boolean modified = false;
        for (Object o : objects) {
            if (remove(o)) {
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection objects) {
        Set<Integer> keysToRemove = new HashSet<Integer>();
        IdSet set = new IdSet();
        set.addAll(objects);

        for (Map.Entry<Integer, E> entry : map.entrySet()) {
            if (!set.contains(entry.getValue())) {
                keysToRemove.add(entry.getKey());
            }
        }

        if (!keysToRemove.isEmpty()) {
            for (Integer key : keysToRemove) {
                map.remove(key);
            }

            return true;
        }

        return false;
    }

    @Override
    public Object[] toArray() {
        return map.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] objects) {
        return map.values().toArray(objects);
    }

    @Override
    public Iterator<E> iterator() {
        return map.values().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(System.identityHashCode(o));
    }

    @Override
    public boolean containsAll(Collection c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdSet idSet = (IdSet) o;

        Collection theseValues = map.values();
        Collection thoseValues = idSet.map.values();

        return thoseValues.containsAll(theseValues) && theseValues.containsAll(thoseValues);
    }

    @Override
    public int hashCode() {
        return map != null ? map.values().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IdSet{" +
                "map=" + map +
                '}';
    }
}
