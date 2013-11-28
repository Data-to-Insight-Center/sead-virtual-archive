<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>


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
<jsp:useBean id="stateHelper" class="org.dataconservancy.ui.model.StateHelper" scope="application" />

<stripes:layout-definition>
    <!DOCTYPE html>
        <head>
            <title>
                <%-- TODO: Figure out the best way to re-use the L10N pageTitle below in the page-title div --%>
                <stripes:layout-component name="pageTitle">
                    ${actionBean.pageTitle}
                </stripes:layout-component>
            </title>
            <link rel="stylesheet"
                  type="text/css"
                  href="${pageContext.request.contextPath}/styles/${requestScope['stripes_theme']}/default.css"/>
            <link rel="stylesheet"
                  type="text/css"
                  href="${pageContext.request.contextPath}/styles/${requestScope['stripes_theme']}/form.css"/>
            <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/images/DC-favicon.ico" >
            <stripes:layout-component name="html_head"/>
            <!-- Using JQuery 1.8.3 specifically to make sure the logout overlay works.
                 JQuery have deprecated the usage of .browser in the latest version of JQuery
                 which breaks our local script. 
             -->
            <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
            <script src="${pageContext.request.contextPath}/resources/scripts/jquery.tools.min.js"></script>
            <script src="${pageContext.request.contextPath}/resources/scripts/dc-custom.js"></script>
           <script>
		  /* $(function() {
			$("input.tip").tooltip({ position: "center right"});
			$(".user-menu-trigger[rel]").overlay({ fixed: false, top: 83, left: '80%'});
			});
			$(function(){
   var path = location.pathname.substring(1);
   if ( path )
     $('.navleft li a[href$="' + path + '"]').attr('class', 'selected');
 });
 */
		   </script>
        </head>
        
        
        
        <body>

	                    <stripes:layout-component name="header">
	                        <jsp:include page="/pages/${requestScope['stripes_pages']}/_header.jsp"/>
	                    </stripes:layout-component>
	                   
         <div class="content-wrapper">              
         <div id="dc-content" class="pageContent"> 
         				<div id="breadcrumb">
         				    <stripes:layout-component name="bread"/>
         				</div>
                        <h1 id="page-title">
                            <stripes:layout-component name="pageTitle">
                                ${actionBean.pageTitle}
                            </stripes:layout-component>
	                    </h1>


                    
                    
	        	        <div id="dc-flash">
	                        <stripes:layout-component name="flash"/>
	                    </div>
	                    
	                    <div id="dc-success">
	                        <stripes:layout-component name="success"/>
	                    </div>
                        
                        
		                
		                <stripes:layout-component name="contents"/>
		               
                        
        </div><!-- end dc-content -->                
		</div><!-- end content-wrapper -->
        </div><!-- end dc-body opened in header.jsp -->
	                <div id="dc-footer">
	                    <stripes:layout-component name="footer">
	                        <jsp:include page="/pages/${requestScope['stripes_pages']}/_footer.jsp"/>
	                        <stripes:link beanclass="org.dataconservancy.ui.stripes.TermsOfUseActionBean"
								title="TermsOfUse" event="handle">
								Terms of Use
							</stripes:link>
	                    </stripes:layout-component>
	                </div>
  
        </body>
    </html>
</stripes:layout-definition>
