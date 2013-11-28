<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>

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
<script type="text/javascript">
    function openWindow(url) {
        var win=window.open(url, '_blank');
        win.focus();
        document.getElementById("dropboxButton").disabled = true;
        document.getElementById("updateButton").disabled = false;
    }    
</script>
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
     </stripes:layout-component>
    
    <stripes:layout-component name="contents">
          		
          	<fieldset>
                <div>
                    <h3>Please click on the button below to open a window that takes you to Dropbox to link your Dropbox account with your Data Conservancy profile. 
                    Navigate back to this page after allowing access. 
                    </h3>
                </div>
                <br/>
                <button id="dropboxButton" name="dropboxUrl" value="Link Dropbox" onclick="openWindow('${actionBean.dropboxUrl}')">Link Dropbox</button>
            </fieldset>
	        <stripes:form beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean">
	        	<fieldset>
                    <stripes:hidden name="editedPerson.id" value="${actionBean.authenticatedUser.id}"/>
                    <stripes:hidden name="dropboxUrl" value="${actionBean.dropboxUrl}"/>
	        	    <stripes:submit id="updateButton" name="updateProfileWithDropbox" value="Update profile" disabled="true"/>
	        	    <stripes:submit name="viewUserProfile" value="Cancel"/>
	        	</fieldset>
	        </stripes:form>
	        
    </stripes:layout-component>
</stripes:layout-render>
