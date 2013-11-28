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

/**
 * Identifies a DCP profile, and provides access to the {@link Profiler}.
 *
 * @param <T> the type of business object that is represented by conforming profiles
 */
public interface Profile<T> {

    /**
     * A string representing the profile type.  A type, together with its version, should be unique.
     *
     * @return the profile type.
     */
    public String getType();

    /**
     * A string representing the version of the profile.  A profile may have multiple versions as its DCP package
     * format evolves.
     *
     * @return the version of the profile.
     */
    public String getVersion();

}
