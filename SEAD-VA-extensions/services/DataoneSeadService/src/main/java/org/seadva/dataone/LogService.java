/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.seadva.dataone;

import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.seadva.model.SeadEvent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.TimeZone;

/*
 * Retrieves logs from DataONE Reads
 */

@Path("/mn/v1/log")
public class LogService{
    public LogService() {
    }


    @GET
    public String getLogRecords(@QueryParam("start") int start,
                             @QueryParam("count") String countStr,
                             @QueryParam("event") String event,
                             @QueryParam("pidFilter") String pidFilter,
                             @QueryParam("fromDate") String fromDate,
                             @QueryParam("toDate") String toDate) throws QueryServiceException, DatatypeConfigurationException, JiBXException {


        Log log = new Log();

        DataOneLogService.Result result = SeadQueryService.dataOneLogService.queryLog(start,countStr,event, pidFilter,fromDate, toDate);

        for(SeadEvent d1log: result.logs){

            LogEntry logEntry = new LogEntry();

            logEntry.setEntryId(d1log.getId());
            Event eventType = Event.convert(
                    SeadQueryService.sead2d1EventTypes.get(d1log.getEventType()));
            if(eventType == Event.READ)
                eventType = Event.READ;
            logEntry.setEvent(
                       eventType
            );

            Identifier identifier = new Identifier();
            identifier.setValue(d1log.getId()); //DcsEvent Identifier
            logEntry.setIdentifier(identifier);
            String ipaddress = d1log.getLogDetail().getIpAddress();

            if(ipaddress==null)
                ipaddress="N/A";

            logEntry.setIpAddress(ipaddress);

            String date = "1800-10-27T22:05:20.809Z";//default wrong date

           if(d1log.getDate()!=null)
                date = d1log.getDate();
           String[] temp = date.split("-");
           final int year = Integer.parseInt(temp[0]);
           final int month = Integer.parseInt(temp[1]);
           final int day = Integer.parseInt(temp[2].split("T")[0]);
           String[] time = temp[2].split("T")[1].split(":");


           final int hour = Integer.parseInt(time[0]);
           final int minute = Integer.parseInt(time[1]);
           final int second = Integer.parseInt(time[2].substring(0,2));
           final int millisecond = Integer.parseInt(time[2].substring(3,6));
           TimeZone utc = TimeZone.getTimeZone("UTC");
           XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
                   .newXMLGregorianCalendar(year, month, day, hour, minute, second, millisecond,0);

           logEntry.setDateLogged(calendar.toGregorianCalendar().getTime());



            String userAgent = d1log.getLogDetail().getUserAgent();
            if(userAgent==null)
                userAgent= "N/A";
            logEntry.setUserAgent(userAgent);
            Subject subject = new Subject();
            subject.setValue(d1log.getLogDetail().getSubject());
            logEntry.setSubject(subject);
            NodeReference nodeReference = new NodeReference();
            nodeReference.setValue(d1log.getLogDetail().getNodeIdentifier());
            logEntry.setNodeIdentifier(nodeReference);
            log.getLogEntryList().add(logEntry);
        }

        log.setCount(result.logs.size());
        log.setTotal((int)result.total);
        log.setStart(start);
        return SeadQueryService.marshal(log);
    }
}
