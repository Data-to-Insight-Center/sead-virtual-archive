/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.dcs.contentdetection.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dataconservancy.model.dcs.DcsFormat;

/**
 * Interface for using a service to detect format(s) of a given file that is part of a package to be ingested into the
 * system.
 * 
 */
public interface ContentDetectionService {

    /**
     * This method is to be implemented by any service that is used to detect the format(s) of a given file. Expects a
     * file and returns a list of {@code DcsFormat} objects. If the detector in the implementation fails to detect a
     * format, this method must return a format corresponding to the mime type  "application/octet-stream"
     * 
     * @param file
     * @return List<DcsFormat>
     */
    public List<DcsFormat> detectFormats(File file);

    /**
     * This method returns the name of the detector used to determine formats.
     * @return
     */
    public String getDetectorName();

    /**
     *  This method returns the version of the detector used to determine formats.
     * @return
     */
    public String getDetectorVersion();

}