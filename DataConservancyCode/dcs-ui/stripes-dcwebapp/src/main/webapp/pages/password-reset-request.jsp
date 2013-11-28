<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>


<%--
  ~ Copyright 2013 Johns Hopkins University
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
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="flash">
       <stripes:messages key="request"/>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
            <h2 class="SectionHeader">Request New Password</h2>
	        <div class="SectionContent">
                <stripes:form beanclass="org.dataconservancy.ui.stripes.PasswordResetActionBean">
                    <fieldset>
                        <div class="label-field-group">
                           <stripes:label name="email" for="email"/>
                           <stripes:text name="emailAddress"/>
                           <stripes:errors field="emailAddress"/>
                        </div>
                    </fieldset>
                         <stripes:submit name="submitPasswordResetRequest" value="Submit Request" />
                    <fieldset>
                    </fieldset>
                </stripes:form>
            </div>

           <div>
               <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                   <fmt:message key="requestInstructions"/>
               </fmt_rt:bundle>
           </div>


    </stripes:layout-component>
</stripes:layout-render>
