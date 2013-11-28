/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.stripes;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_TERMS_OF_USE;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/home/termsofuse.action")
public class TermsOfUseActionBean extends BaseActionBean {
    
    private static final String TERMS_OF_USE_PATH = "/pages/terms_of_use.jsp";
    
    public TermsOfUseActionBean() {
        super();
        try {
            assert (messageKeys.containsKey(MSG_KEY_TERMS_OF_USE));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of " + MSG_KEY_TERMS_OF_USE + " is missing.");
        }
    }

    @DefaultHandler
    public Resolution handle() {
        return new ForwardResolution(TERMS_OF_USE_PATH);
    }
}
