<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="net.sourceforge.stripes.integration.spring.SpringBean" %>
<%--
  ~ Copyright 2012 Johns Hopkins University
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<div id="footer">
    <span>Build: <c:out value="${statusPropertiesBean.buildNumber}"/></span>
    <span>Timestamp: <c:out value="${statusPropertiesBean.buildTimeStamp}"/></span>
    <span>Revision: <c:out value="${statusPropertiesBean.buildRevision}"/></span>
<%--

    out.println("<div id='dc.footer.debugging'>");

    out.println("<p>");
    out.println("Request Attributes:");
    out.println("<br/>");
    Enumeration attrs = request.getAttributeNames();
    if (attrs == null) {
        out.println("<span class='error'>Request Attribute names were null!</span>");
    } else {
        while (attrs.hasMoreElements()) {
            String element = (String) attrs.nextElement();
            out.println(element);
            if ("actionBean".equals(element)) {
                out.println(" = " + request.getAttribute("actionBean").getClass().getName());
            }
            out.println("<br/>");
        }
    }
    out.println("</p>");

    out.println("<p>");

    out.println("Session Attributes:");
    out.println("<br/>");
    attrs = session.getAttributeNames();
    if (attrs == null) {
        out.println("<span class='error'>Session Attribute names were null!</span>");
    } else {
        while (attrs.hasMoreElements()) {
            out.println(attrs.nextElement());
            out.println("<br/>");
        }
    }
    out.println("</p>");

    out.println("<p>");
    out.println("Page Scope Attributes:");
    out.println("<br/>");
    attrs = pageContext.getAttributeNamesInScope(pageContext.PAGE_SCOPE);
    if (attrs == null) {
        out.println("<span class='error'>Page Scope Attribute names were null!</span>");
    } else {
        while (attrs.hasMoreElements()) {
            out.println(attrs.nextElement());
            out.println("<br/>");
        }
    }
    out.println("</p>");

    out.println("<p>");
    out.println("Request Scope Attributes:");
    out.println("<br/>");
    attrs = pageContext.getAttributeNamesInScope(pageContext.REQUEST_SCOPE);
    if (attrs == null) {
        out.println("<span class='error'>Page Request Scope Attributes were null!</span>");
    } else {
        while (attrs.hasMoreElements()) {
            out.println(attrs.nextElement());
            out.println("<br/>");
        }
    }
    out.println("</p>");

    out.println("<p>");
    out.println("Application Scope Attributes:");
    out.println("<br/>");
    attrs = pageContext.getAttributeNamesInScope(pageContext.APPLICATION_SCOPE);
    if (attrs == null) {
        out.println("<span class='error'>Page Application Scope Attributes were null!</span>");
    } else {
        while (attrs.hasMoreElements()) {
            out.println(attrs.nextElement());
            out.println("<br/>");
        }
    }
    out.println("</p>");

    out.println("<p>");
    out.println("Cookies:");
    out.println("<br/>");
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
        out.println("<span class='error'>Cookies were null!</span>");
    } else {
        for (int i = 0; i < cookies.length; i++) {
            out.println(cookies[i].getName() + ": " + cookies[i].getValue());
        }
    }
    out.println("<p/>");

    out.println("</div>");
--%>

</div>