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
package org.dataconservancy.model.builder;

import org.dataconservancy.model.dcp.DcpModelVersion;

import javax.xml.namespace.QName;

/**
 * Constants used throughout the model builder.
 */
class ModelConstants {

    final static String NS_URI = DcpModelVersion.VERSION_1_0.getXmlns();

    final static String E_DCP = "dcp";

    final static String E_FILES = "Files";
    final static String E_DUS = "DeliverableUnits";
    final static String E_COLLECTIONS = "Collections";
    final static String E_EVENTS = "Events";
    final static String E_MANIFESTATIONS = "Manifestations";

    final static String E_FILE = "File";
    final static String E_DU = "DeliverableUnit";
    final static String E_COLLECTION = "Collection";
    final static String E_EVENT = "Event";
    final static String E_MANIFESTATION = "Manifestation";

    final static QName Q_DUS = new QName(NS_URI, E_DUS);
    final static QName Q_FILES= new QName(NS_URI, E_FILES);
    final static QName Q_EVENTS = new QName(NS_URI, E_EVENTS);
    final static QName Q_MANIFESTATIONS = new QName(NS_URI, E_MANIFESTATIONS);
    final static QName Q_COLLECTIONS = new QName(NS_URI, E_COLLECTIONS);    

    final static QName Q_DCP = new QName(NS_URI, E_DCP);
    final static QName Q_DU = new QName(NS_URI, E_DU);
    final static QName Q_FILE = new QName(NS_URI, E_FILE);
    final static QName Q_EVENT = new QName(NS_URI, E_EVENT);
    final static QName Q_MANIFESTATION = new QName(NS_URI, E_MANIFESTATION);
    final static QName Q_COLLECTION = new QName(NS_URI, E_COLLECTION);

    final static String E_DCP_OPEN = "<" + E_DCP + "xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\">";
    final static String E_DCP_CLOSE = "</" + E_DCP + ">";
    final static String DCP_WRAPPER = E_DCP_OPEN + "%s" + E_DCP_CLOSE;
    final static String E_FILES_OPEN = "<Files>";
    final static String E_FILES_CLOSE = "</Files>";
    final static String FILES_WRAPPER = E_FILES_OPEN + "%s" + E_FILES_CLOSE;
    final static String E_MANIFESTATIONS_OPEN = "<Manifestations>\n";
    final static String E_MANIFESTATIONS_CLOSE = "</Manifestations>";
    final static String MANIFESTATIONS_WRAPPER = E_MANIFESTATIONS_OPEN + "%s" + E_MANIFESTATIONS_CLOSE;
    final static String E_DU_OPEN = "<DeliverableUnits>";
    final static String E_DU_CLOSE = "</DeliverableUnits>";
    final static String DU_WRAPPER = E_DU_OPEN + "%s" + E_DU_CLOSE;
}
