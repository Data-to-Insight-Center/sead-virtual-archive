package org.dataconservancy.mhf.representations;

import org.joda.time.DateTime;


public class DateTimeRange {
    private DateTime startDateTime;
    private DateTime endDateTime;

    public DateTimeRange(DateTime startDateTime, DateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public DateTime getStartDateTime() {

        return startDateTime;
    }

    @Override
    public String toString() {
        return "DateTimeRange{" +
                "startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                '}';
    }
}
