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
package org.dataconservancy.ui.profile;

import org.dataconservancy.model.dcp.Dcp;

import java.util.Map;
import java.util.Set;

/**
 * A Profiler encapsulates the logic for determining whether or not a DCP (archival) package encodes or otherwise
 * represents a business object in the UI {@link org.dataconservancy.ui.model model}.  Furthermore, a profiler
 * also has logic (perhaps supplied by supporting classes in the {@link org.dataconservancy.ui.dcpmap dcpmap package})
 * to deconstruct the archival package, compose, and return the business object.
 *
 * A Profiler is responsible for:
 * <ol>
 *     <li>Determining whether or not a DCP package conforms to this profile</li>
 *     <li>Enumerating the archival identifiers for the business objects contained in the DCP package</li>
 *     <li>Being able to compose an instance of an identified business object</li>
 * </ol>
 *
 * @param <T> the type of the business object
 */
public interface Profiler<T> {

    /**
     * Determines whether or not the supplied package conforms to this profile.  If a package does not conform to
     * this profile, then the {@link #discover(org.dataconservancy.model.dcp.Dcp)} and {@link #get(String, java.util.Map)}
     * methods will (what?) TODO
     *
     * @param candidatePackage the candidate package
     * @return true if the package conforms to this profile, false otherwise
     */
    public boolean conforms(Dcp candidatePackage);

    /**
     * For conforming packages (that is, {@link #conforms(org.dataconservancy.model.dcp.Dcp)} returns {@code true}),
     * this method will introspect over the package and return the archival (i.e. DCS)  identifiers of the business
     * objects represented in the package.  Identifiers contained in the returned {@code Set} can then be used in calls
     * to {@link #get(String, java.util.Map)}.
     *
     * @param conformingPackage a package that conforms to this profile
     * @return a {@code Set} of archival identifiers for the business objects contained in the package
     */
    public Set<String> discover(Dcp conformingPackage);

    /**
     * For conforming packages (that is, {@link #conforms(org.dataconservancy.model.dcp.Dcp)} returns {@code true}),
     * this method will introspect over the package, compose, and return the identified business object.  It may be
     * the case that the archival package does not fully encode all of the properties of the business object, so the
     * caller may supply additional {@code context}.
     *
     * It is expected that each identifier returned by {@link #discover(org.dataconservancy.model.dcp.Dcp)} is a valid
     * parameter to this method, and should result in a business object being returned.  If an identifier cannot be
     * resolved to a business object, this method may return {@code null}.
     *
     * @param identifier the archival (DCS entity) identifier of the business object to retrieve
     * @param context additional context for the implementation to compose the business object, may be {@code null}
     * @return an instance of the business object, or {@code null} if not found
     */
    public T get(String identifier, Map<String, Object> context);

}
