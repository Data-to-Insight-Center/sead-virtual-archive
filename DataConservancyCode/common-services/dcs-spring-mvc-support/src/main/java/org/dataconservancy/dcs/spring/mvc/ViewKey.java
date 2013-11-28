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
package org.dataconservancy.dcs.spring.mvc;

/**
 * Well-known keys used by the view implementations to look up objects from the {@code ModelAndView}
 */
public enum ViewKey {

    /** Key identifying the String reason phrase used in the HTTP response */
    REASON_PHRASE,

    /** Key identifying the name of a Servlet attribute carrying the ModelAndView object */
    MODEL_AND_VIEW
}
