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
package org.dataconservancy.deposit.sword.extension;

import javax.xml.namespace.QName;

import org.apache.abdera.util.AbstractExtensionFactory;

public class SWORDExtensionFactory extends AbstractExtensionFactory {

    /**
     * SWORD namespace
     */
    public static final String NS = "http://purl.org/net/sword/";

    /**
     * Typical sword ns prefix
     */
    public static final String PREFIX = "sword";

    public static final QName ACCEPT_PACKAGING = new QName(NS,
                                                           "acceptPackaging",
                                                           PREFIX);

    public static final QName COLLECTION_POLICY = new QName(NS,
                                                            "collectionPolicy",
                                                            PREFIX);

    public static final QName MAX_UPLOAD_SIZE = new QName(NS,
                                                          "maxUploadSize",
                                                          PREFIX);

    public static final QName MEDIATION = new QName(NS, "mediation", PREFIX);

    public static final QName NO_OP = new QName(NS, "noOp", PREFIX);

    public static final QName PACKAGING = new QName(NS, "packaging", PREFIX);

    public static final QName SERVICE = new QName(NS, "service", PREFIX);

    public static final QName TREATMENT = new QName(NS, "treatment", PREFIX);

    public static final QName USER_AGENT = new QName(NS, "userAgent", PREFIX);

    public static final QName VERBOSE = new QName(NS, "verbose", PREFIX);

    public static final QName VERBOSE_DESCRIPTION = new QName(NS,
                                                              "verboseDescription",
                                                              PREFIX);

    public static final QName VERSION = new QName(NS, "version", PREFIX);

    public SWORDExtensionFactory() {
        super(NS);
        addImpl(ACCEPT_PACKAGING, AcceptPackaging.class);
        addImpl(COLLECTION_POLICY, CollectionPolicy.class);
        addImpl(MAX_UPLOAD_SIZE, MaxUploadSize.class);
        addImpl(MEDIATION, Mediation.class);
        addImpl(NO_OP, NoOp.class);
        addImpl(PACKAGING, Packaging.class);
        addImpl(SERVICE, Service.class);
        addImpl(TREATMENT, Treatment.class);
        addImpl(USER_AGENT, UserAgent.class);
        addImpl(VERBOSE, Verbose.class);
        addImpl(VERBOSE_DESCRIPTION, VerboseDescription.class);
        addImpl(VERSION, Version.class);
    }

    public abstract class Headers {

        public static final String CONTENT_DISPOSITION = "Content-Disposition";

        public static final String CONTENT_MD5 = "Content-MD5";

        public static final String NO_OP = "X-No-Op";

        public static final String ON_BEHALF_OF = "X-On-Behalf-Of";

        public static final String PACKAGING = "X-Packaging";

        public static final String USER_AGENT = "User-Agent";

        public static final String VERBOSE = "X-Verbose";

    }
}
