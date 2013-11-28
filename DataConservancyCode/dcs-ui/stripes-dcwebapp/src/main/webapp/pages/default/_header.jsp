 <%@ page import="org.springframework.security.core.context.SecurityContext" %>
<%@ page import="org.springframework.security.web.context.HttpSessionSecurityContextRepository" %>
<%@ page import="org.springframework.security.core.userdetails.UserDetails" %>
<%@ page import="javax.mail.Session" %>
<%@ page import="java.util.List" %>
<%@ page import="org.dataconservancy.ui.model.Person" %>
<%@ page import="org.dataconservancy.ui.model.Role" %>
<%@ page import="org.dataconservancy.ui.stripes.BaseActionBean" %>
<%@ page import="java.util.concurrent.atomic.AtomicBoolean" %>
<%@ page import="javax.xml.ws.handler.MessageContext" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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


<div id="header">

<c:set var="authUser" scope="page" value='${actionBean.authenticatedUser}'/>
        <%
            Person authUser = (Person) pageContext.getAttribute("authUser", PageContext.PAGE_SCOPE);
            if (authUser != null) {
                List<Role> roles = authUser.getRoles();
                pageContext.setAttribute("isAdmin", roles.contains(Role.ROLE_ADMIN), PageContext.PAGE_SCOPE);
            }
        %>
    <nav id="user">
    	    <c:if test="${ empty pageScope.authUser}">
	       <p><stripes:link beanclass="org.dataconservancy.ui.stripes.RegistrationActionBean"
	                              title="Register">Register</stripes:link> |
	       <stripes:link beanclass="org.dataconservancy.ui.stripes.LoginActionBean"
	                              title="Login">Login</stripes:link></p>
        </c:if>
        <c:if test="${not empty pageScope.authUser}">
        <a href="#" class="user-menu-trigger" rel="#user-menu">Welcome ${actionBean.authenticatedUser.firstNames}</a>
        <div id="user-menu">
        <ul>
             <li><stripes:link beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean"  title="Edit" event="editUserProfile">User profile</stripes:link></li>
             <li><stripes:link href="/j_spring_security_logout">Logout</stripes:link></li>
        </ul>
        </div>
        </c:if>
    
    </nav>

<a rel="home" title="Data Conservancy" href="http://dataconservancy.org/">
<h1>Data Conservancy Software</h1>
</a>

</div>

<div id="dc-body">

<nav id="dc-default-navbar" title="DC Default Navbar Div">
        <ul class="navleft">
          <li><stripes:link beanclass="org.dataconservancy.ui.stripes.HomeActionBean"
	                              title="Home">Home</stripes:link></li>
                                  
         <c:if test="${not empty pageScope.authUser}">
            <li>
		    <stripes:link beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean"
	                                  title="Profile">Profile</stripes:link></li>
            <li>
		    <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
	                                  title="Projects">Projects</stripes:link></li>
	        <li>
		    <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
	                                  title="Collection" event="render">Collections</stripes:link></li>
	        <li>    
            <stripes:link beanclass="org.dataconservancy.ui.stripes.IngestPackageActionBean"
                                      title="Collection" event="render">Ingest Package</stripes:link></li>                      
	        <li>
		    <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean"
	                                  title="Dropbox" event="render">Dropbox</stripes:link></li>                         
	                                  
            <c:if test="${pageScope.isAdmin == true}">
                <li>
	            <stripes:link beanclass="org.dataconservancy.ui.stripes.AdminHomeActionBean"
                                           title="Admin">Admin</stripes:link></li>
            </c:if>


        </c:if>                        
        </ul>

</nav>

  
