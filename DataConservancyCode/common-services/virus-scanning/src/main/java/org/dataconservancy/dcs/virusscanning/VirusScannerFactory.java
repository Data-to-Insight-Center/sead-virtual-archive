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
package org.dataconservancy.dcs.virusscanning;

/**
 * A factory that creates {@link VirusScanner} objects.
 * <p>
 * This should be implemented for each virus scanning program being added to the
 * DCS.
 * </p>
 */
public interface VirusScannerFactory {

    /**
     * Creates a {@link VirusScanner} instance that can be used to call into a
     * 3rd party virus scanning solution.
     * 
     * @return The VirusScanner object to be used to call virus scanning
     *         software.
     */
    VirusScanner createVirusScanner();
}
