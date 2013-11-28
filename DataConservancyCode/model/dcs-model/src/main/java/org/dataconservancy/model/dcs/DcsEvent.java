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

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import java.util.Arrays;
import java.util.Collection;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * Models a Data Conservancy Event
 */
public class DcsEvent extends DcsEntity {
    private String eventType;
    private String date;
    private String detail;
    private String outcome;
    private Collection<DcsEntityReference> targets = CollectionFactory.newCollection();

    /**
     * Constructs a new DcsEvent with no state.
     */
    public DcsEvent() {

    }

    /**
     * Copy constructor for a DcsEvent.  The state of <code>toCopy</code> is copied
     * to this.  Note that if {@code toCopy} is modified while constructing this DcsEvent,
     * the state of this DcsEvent is undefined.
     *
     * @param toCopy the dcs event to copy
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public DcsEvent(DcsEvent toCopy) {
        super(toCopy);
        this.eventType = toCopy.eventType;
        this.date = toCopy.date;
        this.detail = toCopy.detail;
        this.outcome = toCopy.outcome;
        deepCopy(toCopy.targets, this.targets);
    }

    /**
     * The event type
     *
     * @return the event type, may be {@code null}
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * The event type.
     *
     * @param eventType the event type, must not be {@code null}, the empty or zero-length string.
     * @throws IllegalArgumentException if {@code eventType} is {@code null}, the empty or zero-length string.
     */
    public void setEventType(String eventType) {
        Assertion.notEmptyOrNull(eventType);
        this.eventType = eventType;
    }

    /**
     * The date the event occurred
     *
     * @return the date the event occurred, may be {@code null}
     */
    public String getDate() {
        return date;
    }

    /**
     * The date the event occurred
     *
     * @param date the date the event occurred, must not be {@code null} the empty or zero-length string
     * @throws IllegalArgumentException if the {@code date} is {@code null} or the empty or zero-length string
     */
    public void setDate(String date) {
        Assertion.notEmptyOrNull(date);
        this.date = date;
    }

    /**
     * The event detail
     *
     * @return the event detail, , may be {@code null}
     */
    public String getDetail() {
        return detail;
    }

    /**
     * The event detail
     *
     * @param detail the event detail, must not be {@code null} the empty or zero-length string
     * @throws IllegalArgumentException if the {@code detail} is {@code null} or the empty or zero-length string
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * The event outcome
     *
     * @return the event outcome, may be {@code null}
     */
    public String getOutcome() {
        return outcome;
    }

    /**
     * The event outcome
     *
     * @param outcome the event outcome, must not be {@code null} the empty or zero-length string
     * @throws IllegalArgumentException if the {@code outcome} is {@code null} or the empty or zero-length string
     */
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    /**
     * The target(s) of the event
     *
     * @return the target(s) of the event, may be empty but never {@code null}
     */
    public Collection<DcsEntityReference> getTargets() {
        return this.targets;
    }

    /**
     * The target(s) of the event.  Note: this nullifies any existing targets on this DcsEvent.
     *
     * @param targets the target(s) of the event, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code targets} is {@code null} or contains {@code null} references
     */
    public void setTargets(Collection<DcsEntityReference> targets) {
        Assertion.notNull(targets);
        Assertion.doesNotContainNull(targets);
        this.targets = targets;
    }

    /**
     * Adds event targets.
     *
     * @param targets the event targets, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code targets} is {@code null} or contains {@code null} references
     */
    public void addTargets(DcsEntityReference... targets) {
        Assertion.notNull(targets);
        Assertion.doesNotContainNull(targets);
        this.targets.addAll(Arrays.asList(targets));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DcsEvent dcsEvent = (DcsEvent) o;

        if (date != null ? !date.equals(dcsEvent.date) : dcsEvent.date != null) return false;
        if (detail != null ? !detail.equals(dcsEvent.detail) : dcsEvent.detail != null) return false;
        if (outcome != null ? !outcome.equals(dcsEvent.outcome) : dcsEvent.outcome != null) return false;
        if (!Util.isEqual(targets, dcsEvent.targets)) {
            return false;
        }
        if (eventType != null ? !eventType.equals(dcsEvent.eventType) : dcsEvent.eventType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (detail != null ? detail.hashCode() : 0);
        result = 31 * result + (outcome != null ? outcome.hashCode() : 0);
        result = 31 * result + (targets != null ? Util.hashCode(targets) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{DcsEvent{" +
                "type='" + eventType + '\'' +
                ", date='" + date + '\'' +
                ", detail='" + detail + '\'' +
                ", outcome='" + outcome + '\'' +
                ", targets=" + targets +
                "}" + super.toString() + "}";
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Event: ");
        sb.incrementDepth();
        super.toString(sb);
        sb.appendWithIndent("type: ").appendWithNewLine(eventType);
        sb.appendWithIndent("date: ").appendWithNewLine(date);
        sb.appendWithIndent("detail: ").appendWithNewLine(detail);
        sb.appendWithIndent("outcome: ").appendWithNewLine(outcome);
        sb.appendWithIndentAndNewLine("targets: ");
        for (DcsEntityReference ref : targets) {
            ref.toString(sb, "");
        }
        sb.decrementDepth();
    }
}
