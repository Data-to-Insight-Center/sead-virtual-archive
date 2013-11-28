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

package org.seadva.archive;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;

/**
 * Extension of Archive Store API
 */
public interface SeadArchiveStore extends ArchiveStore{

    ResearchObject putResearchPackage(InputStream dcpStream) throws AIPFormatException;
}
