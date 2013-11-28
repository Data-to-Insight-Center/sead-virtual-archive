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

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exposes the {@link ModelAndView} to the current {@code HttpServletRequest}.  This allows ViewResolver implementations
 * to use information in the model to return the appropriate {@link org.springframework.web.servlet.View view}.  Note
 * that this should probably be considered an anti-pattern, since Spring does not, by default, expose the
 * {@code ModelAndView} to {@link org.springframework.web.servlet.ViewResolver}s.
 */
public class ModelExposingInterceptor implements HandlerInterceptor {

    /**
     * This implementation does nothing.
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }

    /**
     * This implementation is hard-coded to return {@code true}, and proceed with the request.
     *
     * @param request
     * @param response
     * @param handler
     * @return always {@code true}
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return true;
    }

    /**
     * Places the ModelAndView on the HttpServletRequest as a request attribute.  The attribute key used to store the
     * ModelAndView is {@link ViewKey#MODEL_AND_VIEW MODEL_AND_VIEW}.  ViewResolver implementations will be able to
     * retrieve the ModelAndView from the request like so:
     * {@code (ModelAndView) request.getAttribute(ViewKey.MODEL_AND_VIEW.name())}
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // Attach the model and view to the request so that the ViewResolver implementations can access it
        request.setAttribute(ViewKey.MODEL_AND_VIEW.name(), modelAndView);
    }

}
