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
package org.dataconservancy.dcs.lineage.http.view;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exposes the {@link ModelAndView} to the current {@code HttpServletRequest}.  This allows the {@link LineageViewResolver}
 * to use information in the model to return the appropriate {@link org.springframework.web.servlet.View view}.  Note
 * that this should probably be considered an anti-pattern, since Spring does not, by default, expose the
 * {@code ModelAndView} to {@link org.springframework.web.servlet.ViewResolver}s.
 */
public class ExposeModelHandleInterceptor implements HandlerInterceptor {

    /**
     * The request attribute name used to store the {@code ModelAndView}
     */
    public final static String LINEAGE_MODEL = "__org.dataconservancy.dcs.lineage.http.view.MODELANDVIEW";

    /**
     * This implementation does nothing.
     *
     * @param request {@inheritDoc}
     * @param response {@inheritDoc}
     * @param handler {@inheritDoc}
     * @param ex {@inheritDoc}
     * @throws Exception {@inheritDoc}
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Do nothing
    }

    /**
     * This implementation is hard-coded to return {@code true}, and proceed with the request.
     *
     * @param request {@inheritDoc}
     * @param response {@inheritDoc}
     * @param handler {@inheritDoc}
     * @return {@inheritDoc}
     * @throws Exception {@inheritDoc}
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Do nothing, proceed with request
        return true;
    }

    /**
     * Sets the {@code modelAndView} on the {@code request}.
     * <p/>
     * {@inheritDoc}
     *
     * @param request {@inheritDoc}
     * @param response {@inheritDoc}
     * @param handler {@inheritDoc}
     * @param modelAndView {@inheritDoc}
     * @throws Exception {@inheritDoc}
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Attach the model and view to the request so that the LineageViewResolver can access it
        request.setAttribute(LINEAGE_MODEL, modelAndView);
    }
}
