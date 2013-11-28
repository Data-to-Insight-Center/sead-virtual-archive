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
package org.dataconservancy.model.dcp;

import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * The Data Conservancy SIP package model.
 */
public class Dcp implements Iterable<DcsEntity> {

    private static final DcpModelVersion MODEL_VERSION = DcpModelVersion.VERSION_1_0;

    private Collection<DcsDeliverableUnit> deliverableUnits = CollectionFactory.newCollection();
    private Collection<DcsCollection> collections = CollectionFactory.newCollection();
    private Collection<DcsManifestation> manifestations = CollectionFactory.newCollection();
    private Collection<DcsFile> files = CollectionFactory.newCollection();
    private Collection<DcsEvent> events = CollectionFactory.newCollection();

    /**
     * Constructs a new, empty, Data Conservancy package.
     */
    public Dcp() {

    }

    /**
     * Constructs a new Data Conservancy Package, deeply copying the contents of the
     * supplied DCP.
     *
     * @param toCopy the DCP to copy
     */
    public Dcp(Dcp toCopy) {
        deepCopy(toCopy.deliverableUnits, this.deliverableUnits);
        deepCopy(toCopy.collections, this.collections);
        deepCopy(toCopy.manifestations, this.manifestations);
        deepCopy(toCopy.files, this.files);
        deepCopy(toCopy.events, this.events);
    }

    public DcpModelVersion getModelVersion() {
        return MODEL_VERSION;
    }

    public Collection<DcsDeliverableUnit> getDeliverableUnits() {
        return this.deliverableUnits;
    }

    public void setDeliverableUnits(Collection<DcsDeliverableUnit> deliverableUnits) {
        Assertion.notNull(deliverableUnits);
        Assertion.doesNotContainNull(deliverableUnits);
        this.deliverableUnits = deliverableUnits;
    }

    public void addDeliverableUnit(DcsDeliverableUnit... deliverableUnit) {
        Assertion.notNull(deliverableUnit);
        Assertion.doesNotContainNull(deliverableUnit);
        this.deliverableUnits.addAll(Arrays.asList(deliverableUnit));
    }

    public Collection<DcsCollection> getCollections() {
        return this.collections;
    }

    public void setCollections(Collection<DcsCollection> collections) {
        Assertion.notNull(collections);
        Assertion.doesNotContainNull(collections);
        this.collections = collections;
    }

    public void addCollection(DcsCollection... collection) {
        Assertion.notNull(collection);
        Assertion.doesNotContainNull(collection);
        this.collections.addAll(Arrays.asList(collection));
    }

    public Collection<DcsManifestation> getManifestations() {
        return this.manifestations;
    }

    public void setManifestations(Collection<DcsManifestation> manifestations) {
        Assertion.notNull(manifestations);
        Assertion.doesNotContainNull(manifestations);
        this.manifestations = manifestations;
    }

    public void addManifestation(DcsManifestation... manifestation) {
        Assertion.notNull(manifestation);
        Assertion.doesNotContainNull(manifestation);
        this.manifestations.addAll(Arrays.asList(manifestation));
    }

    public Collection<DcsFile> getFiles() {
        return this.files;
    }

    public void setFiles(Collection<DcsFile> files) {
        Assertion.notNull(files);
        Assertion.doesNotContainNull(files);
        this.files = files;
    }

    public void addFile(DcsFile... file) {
        Assertion.notNull(file);
        Assertion.doesNotContainNull(file);
        this.files.addAll(Arrays.asList(file));
    }

    public Collection<DcsEvent> getEvents() {
        return this.events;
    }

    public void setEvents(Collection<DcsEvent> events) {
        Assertion.notNull(events);
        Assertion.doesNotContainNull(events);
        this.events = events;
    }

    public void addEvent(DcsEvent... event) {
        Assertion.notNull(event);
        Assertion.doesNotContainNull(event);
        this.events.addAll(Arrays.asList(event));
    }

    public void addEntity(DcsEntity... entities) {
        Assertion.notNull(entities);
        Assertion.doesNotContainNull(entities);
        for (DcsEntity e : entities) {
            if (e instanceof DcsFile) {
                addFile((DcsFile) e);
            } else if (e instanceof DcsCollection) {
                addCollection((DcsCollection) e);
            } else if (e instanceof DcsDeliverableUnit) {
                addDeliverableUnit((DcsDeliverableUnit) e);
            } else if (e instanceof DcsManifestation) {
                addManifestation((DcsManifestation) e);
            } else if (e instanceof DcsEvent) {
                addEvent((DcsEvent) e);
            } else {
                throw new RuntimeException("Unknown DcsEntity type: " + e.getClass().getName());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dcp dcp = (Dcp) o;

        if (!Util.isEqual(collections, dcp.collections)) {
            return false;
        }

        if (!Util.isEqual(deliverableUnits, dcp.deliverableUnits)) {
            return false;
        }

        if (!Util.isEqual(events, dcp.events)) {
            return false;
        }

        if (!Util.isEqual(files, dcp.files)) {
            return false;
        }

        if (!Util.isEqual(manifestations, dcp.manifestations)) {
            return false;
        }

        return true;
    }

    /**
     * Returns an iterator over the DCS entities contained in the package.  This iterator is "fail-fast": if the contents
     * of the DCP are modified in any way after obtaining the iterator, subsequent invocations of iterator methods
     * will fail with a {@link ConcurrentModificationException}.  The <code>remove()</code> method is not supported
     * by the iterator.
     *
     * This iterator is not thread safe.
     *
     * @return an iterator over the DCS entities in this package
     * @throws ConcurrentModificationException if the contents of the package are modified in any way.
     * @throws UnsupportedOperationException if <code>remove()</code> is invoked.
     */
    @Override
    public Iterator<DcsEntity> iterator() {
        return new DelegationItr();
    }

    @Override
    public int hashCode() {
        int result = deliverableUnits != null ? Util.hashCode(deliverableUnits) : 0;
        result = 31 * result + (collections != null ? Util.hashCode(collections) : 0);
        result = 31 * result + (manifestations != null ? Util.hashCode(manifestations) : 0);
        result = 31 * result + (files != null ? Util.hashCode(files) : 0);
        result = 31 * result + (events != null ? Util.hashCode(events) : 0);
        return result;
    }

    @Override
    public String toString() {
        final String indent = "  ";
        final HierarchicalPrettyPrinter sb = new HierarchicalPrettyPrinter(indent, 0);

        sb.appendWithNewLine("DCP:");

        sb.incrementDepth();

        for (DcsDeliverableUnit du : deliverableUnits) {
            du.toString(sb);
        }

        for (DcsCollection c : collections) {
            c.toString(sb);
        }

        for (DcsManifestation m : manifestations) {
            m.toString(sb);
        }

        for (DcsFile f : files) {
            f.toString(sb);
        }

        for (DcsEvent e : events) {
            e.toString(sb);
        }

        sb.decrementDepth();

        return sb.toString();

//        return "Dcp{" +
//                "deliverableUnits=" + deliverableUnits +
//                ", collections=" + collections +
//                ", manifestations=" + manifestations +
//                ", files=" + files +
//                ", events=" + events +
//                '}';
    }

    /**
     * Implements iteration over DCP elements by delegating to the iterators of the Sets maintained by this class.
     * A best-effort is made to fail-fast when a Set maintained by Dcp is replaced (by calling <code>setXxx(...)</code>).
     *
     * Specifically, this implementation is not thread-safe.  It maintains state which is not protected.
     */
    private class DelegationItr implements Iterator<DcsEntity> {

        /** The current Iterator, managed by <code>nextDelegate()</code> */
        private Iterator<? extends DcsEntity> delegate;

        /** The current Set being iterated, managed by <code>nextDelegate()</code> */
        private int delegateOf;

        // References to the Iterators of the Sets managed by the Dcp instance
        private Iterator<DcsDeliverableUnit> duItr = deliverableUnits.iterator();
        private Iterator<DcsCollection> collItr = collections.iterator();
        private Iterator<DcsManifestation> manItr = manifestations.iterator();
        private Iterator<DcsFile> fileItr = files.iterator();
        private Iterator<DcsEvent> eventItr = events.iterator();

        // References to the Lists managed by the Dcp instance
        private Collection<DcsDeliverableUnit> duSetRef = deliverableUnits;
        private Collection<DcsCollection> collSetRef = collections;
        private Collection<DcsManifestation> manSetRef = manifestations;
        private Collection<DcsFile> fileSetRef = files;
        private Collection<DcsEvent> eventSetRef = events;

        /**
         * Initialize this iterator, starting with Deliverable Units.
         */
        private DelegationItr() {
            delegate = duItr;
            delegateOf = 1;
        }

        /**
         * Maintains the state of the {@link #delegate} and {@link #delegateOf}.  This method is called
         * each time {@link #next()} or {@link #hasNext()} is called.  If the current delegate is exhausted,
         * this method will advance to the next delegate.  If no delegates are available, {@link #delegate} is
         * set to <code>null</code>, indicating no more delegates are available.
         */
        private void nextDelegate() {
            if (delegate == null || delegate.hasNext()) {
                return;
            }

            if (!delegate.hasNext()) {
                switch (delegateOf) {
                    case 1:
                        isModified();
                        delegateOf++;
                        delegate = collItr;
                        break;

                    case 2:
                        isModified();
                        delegateOf++;
                        delegate = manItr;
                        break;

                    case 3:
                        isModified();
                        delegateOf++;
                        delegate = fileItr;
                        break;

                    case 4:
                        isModified();
                        delegateOf++;
                        delegate = eventItr;
                        break;

                    case 5:
                        delegateOf = -1;
                        delegate = null;
                }
            }
        }


        @Override
        public boolean hasNext() {

            // this construct handles delegate iterators over empty sets.
            while (delegate != null) {
                if (delegate.hasNext()) {
                    return true;
                }
                nextDelegate();
            }

            // no more sets of entities to iterate over
            return false;
        }

        @Override
        public DcsEntity next() {

            while (delegate != null) {
                if (delegate.hasNext()) {
                    return delegate.next();
                }
                nextDelegate();
            }

            throw new NoSuchElementException();
        }

        /**
         * Unsupported, always throws {@link UnsupportedOperationException}.
         *
         * @throws UnsupportedOperationException
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Removing elements from a SIP using an Iterator is not supported.");
        }

        /**
         * Throws {@code ConcurrentModificationException} if any of the referenced Sets managed by the <code>Dcp</code>
         * have been modified to refer to another set object.
         *
         * @throws ConcurrentModificationException
         */
        private void isModified() {
            if (duSetRef != deliverableUnits) {
                throw new ConcurrentModificationException();
            }

            if (collSetRef != collections) {
                throw new ConcurrentModificationException();
            }

            if (manSetRef != manifestations) {
                throw new ConcurrentModificationException();
            }

            if (fileSetRef != files) {
                throw new ConcurrentModificationException();
            }

            if (eventSetRef != events) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
