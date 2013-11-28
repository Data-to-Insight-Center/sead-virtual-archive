<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

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
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="flash">
        <stripes:messages key="authentication.failure"/>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
    	<div class="SectionContent">
	        <stripes:form name='f' action='/j_spring_security_check' method='POST' focus="j_username">
	        	<fieldset>
	        		<div class="content-left-col">
		        		<div class="label-field-group"> 	        	
			        		<stripes:label class="left-col" name="Username" for="j_username"/>
			        		<stripes:text name="j_username" value=""/>
		        		</div>
		        		<div class="label-field-group"> 
			        		<stripes:label class="left-col" name="Password" for="j_password"/>
			        		<stripes:password name="j_password"/>
		        		</div>
		        		<div>
		        		<stripes:link beanclass="org.dataconservancy.ui.stripes.PasswordResetActionBean" title="Forgot Password?">Forget your password?</stripes:link>
		        		</div>
		        	</div>
	        	</fieldset>
	        	<fieldset>
	        		<stripes:submit name="submit"/>
	        		<stripes:reset name="reset"/>
	        	</fieldset>
	        </stripes:form>
	    </div>
    </stripes:layout-component>
</stripes:layout-render>
