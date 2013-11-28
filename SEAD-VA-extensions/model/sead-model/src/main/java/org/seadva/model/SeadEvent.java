package org.seadva.model;

import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;

/**
 * Additional Events in SEAD, mainly to incorporate DataONE log requirements like requiring ip address etc.
 */
public class SeadEvent extends DcsEvent{
    private SeadLogDetail logDetail = new SeadLogDetail();

    public SeadLogDetail getLogDetail() {
        return logDetail;
    }

    public void setLogDetail(SeadLogDetail logDetail) {
        this.logDetail = logDetail;
    }

    public SeadEvent(){}

    public SeadEvent(SeadEvent toCopy) {
        super(toCopy);
        setLogDetail(toCopy.getLogDetail());
    }

    public SeadEvent(DcsEvent toCopy) {
        super(toCopy);
    }


    @Override
    public String toString() {
        return "{DcsEvent{" +
                "eventType='" + getEventType() + '\'' +
                ", eventDate='" + getDate() + '\'' +
                ", eventDetail='" + getDetail() + '\'' +
                ", eventOutcome='" + getOutcome() + '\''+
                ", targets=" + getTargets() +
                ", logDetail=" + logDetail +
                "}" + super.toString() + "}";
    }
}
