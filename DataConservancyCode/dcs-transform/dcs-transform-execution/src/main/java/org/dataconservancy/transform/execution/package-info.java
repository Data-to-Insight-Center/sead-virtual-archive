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

/** Basic execution environment tools provided by the transform framework.
 * <p>
 * While the feature extraction/transform framework is execution environment
 *  agnostic, this package contains the basic default execution
 *  environment provided by the framework.  It allows for transformations
 *  based upon (possibly chained) {@link org.dataconservancy.transform.dcp.Mapping}
 *  to be run within the java VM by an 
 *  {@link java.util.concurrent.ExecutorService}.
 * </p>
 */

package org.dataconservancy.transform.execution;
