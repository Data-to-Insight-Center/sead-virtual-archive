<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="contents">

        <h2 class="SectionHeader center-message">Metadata Ingest Preview</h2>
        <div class="SectionContent">
            <stripes:form beanclass="org.dataconservancy.ui.stripes.MetadataFileActionBean" class="CollectionMetadataFileForm">
                <fieldset>
                    <fmt_rt:bundle basename="pageText/metadata" prefix="metadata.">
                        <fmt:message key="ingestPreviewMessage" var="previewMessage"/>
                    </fmt_rt:bundle>
                    <div>
                        <stripes:text name="parentID" style="display:none"/>
                        <stripes:text name="redirectUrl" style="display:none"/>
                        <div id="fileName">
		        			<h3 class="metadataPreviewLeft">Filename:</h3>
		        			<div class="metadataPreviewRight">${actionBean.metadataFile.name}<br></div>
		        		</div>
		        		<c:choose>
		        			<c:when test="${!empty actionBean.message}">
				        		<div id="center-message-id" class="center-message">	
				        			<b>${actionBean.message}</b>
				        		</div>
			        		</c:when>
			        		<c:otherwise>
				        		<div id="validation">
		                            <h3 class="metadataPreviewLeft">Validation:</h3>
		                            <div class="metadataPreviewRight">
		                                <c:choose>
		                                    <c:when test="${empty actionBean.validationResult.metadataValidationErrors}">
		                                        <span class="success text-big">Successful</span> against schema.<br/><br/>
		                                        <c:forEach items="${actionBean.validationResult.metadataValidationSuccesses}" var="validationSuccess">
		                                            <span>${validationSuccess.message}</span>
		                                            <br/>
		                                        </c:forEach>
		                                    </c:when>
		                                    <c:otherwise>
		                                        <span class="error text-big">Failed</span> against schema.
		                                    </c:otherwise>
		                                </c:choose>
		                            </div>
		
		                            <c:if test="${not empty actionBean.validationResult.metadataValidationErrors}">
					        		    <h3 class="metadataPreviewLeft">Validation Errors:</h3>
		                                <div class="metadataPreviewRight">
		                                    <c:forEach items="${actionBean.validationResult.metadataValidationErrors}" var="validationError">
		                                        ${validationError.message}
		                                        <br/><br/>
		                                    </c:forEach>
		                                </div>
					        		</c:if>
				        		</div>
				        		<div id="indexing">
				        		    <c:if test="${not empty actionBean.extractionResult.metadataExtractionErrors}">
		                                <div id="indexing_errors">
		                                    <h3 class="metadataPreviewLeft">Indexing:</h3>
		                                    <div class="metadataPreviewRight">
		                                        <c:forEach items="${actionBean.extractionResult.metadataExtractionErrors}" var="extractionError">
		                                            <span class="error">${extractionError.message}</span>
		                                            <br/></br>
		                                        </c:forEach>
		                                    <div>
		                                </div>
					        		</c:if>
				        			<div id="coverage">
					        			<h3>Coverage:</h3>
					        			<div id="spatial_coverage" class="metadataPreviewCoverage">
						        			<h4>Spatial Coverage of Content:</h4>
						        			<div class="metadataPreviewCoverageIndented">
		                                        <c:forEach items="${actionBean.extractedSpatialAttributes}" var="attribute">
		                                            <br/>
		                                            West Bounding Coordinate: ${attribute[0]}<br/>
		                                            East Bounding Coordinate: ${attribute[1]}<br/>
		                                            North Bounding Coordinate: ${attribute[2]}<br/>
		                                            South Bounding Coordinate: ${attribute[3]}<br/>
		                                        </c:forEach>
							        		</div>
						        		</div>
						        		<br>
						        		<div id="temporal_coverage" class="metadataPreviewCoverage">
						        			<h4>Time Period of Content:</h4>
						        			<div class="metadataPreviewCoverageIndented">
		                                        <c:forEach items="${actionBean.extractedTemporalRangeAttributes}" var="attribute">
		                                            <br/>
		                                            ${attribute[0]}<br/>
		                                            ${attribute[1]}<br/>
		                                        </c:forEach>
							        		</div>
						        		</div>
					        		</div>
				        		</div>
		        			</c:otherwise>
	        			</c:choose>
                    </div>
                </fieldset>

                <fieldset>
                	<c:choose>
                        <c:when test="${empty actionBean.validationResult.metadataValidationErrors}">
		                    <c:if test="${empty actionBean.metadataFileID}">
		                        <stripes:submit name="saveAndAddMoreMetadataFile" value="Save and Add More"/>
		                        </br>
		                    </c:if>
		                    <c:choose>
		                        <c:when test="${empty actionBean.metadataFileID}">
		                            <stripes:submit name="saveAndDoneMetadataFile" value="Save and Go Back"/>
		                        </c:when>
		                        <c:otherwise>
		                            <stripes:submit name="saveAndDoneMetadataFile" value="Update"/>
		                        </c:otherwise>
		                    </c:choose>
		
		                    <stripes:submit name="cancel" value="Cancel"/>
	                    </c:when>
	                    <c:otherwise>
	                    	<stripes:submit name="displayMetadataFileForm()" value="Print Error Report" onclick="window.print()"/>
	                    	<stripes:submit name="displayMetadataFileForm()" value="Continue"/>
                    	</c:otherwise>
                	</c:choose>
                </fieldset>
            </stripes:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
