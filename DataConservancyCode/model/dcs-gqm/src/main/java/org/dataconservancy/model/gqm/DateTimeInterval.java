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
package org.dataconservancy.model.gqm;

/**
 * Interval of time. Inclusive start and exclusive end.
 */
public class DateTimeInterval {

    private long start;
    private long end;

    public DateTimeInterval(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int hashCode() {
        return (int) (start + end);
    }

    public boolean equals(Object o) {
        DateTimeInterval dti = (DateTimeInterval) o;

        if (dti == null) {
            return false;
        }

        return start == dti.start && end == dti.end;
    }
    
    public String toString() {
        return start + "-" + end;
    }
}
