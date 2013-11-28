<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0">

<!-- ********************************************************************************************** -->
<!-- Copyright 2007 The Trustees of Indiana University.  All rights reserved.                       -->
<!--                                                                                                -->
<!-- Redistribution and use in source and binary forms, with or without modification, are           -->
<!-- permitted provided that the following conditions are met:                                      -->
<!--                                                                                                -->
<!-- 1) All redistributions of source code must retain the above copyright notice, the              -->
<!--    list of authors in the original source code, this list of conditions and the                -->
<!--    disclaimer listed in this license;                                                          -->
<!--                                                                                                -->
<!-- 2) All redistributions in binary form must reproduce the above copyright notice, this          -->
<!--    list of conditions and the disclaimer listed in this license in the documentation           -->
<!--    and/or other materials provided with the distribution;                                      -->
<!--                                                                                                -->
<!-- 3) Any documentation included with all redistributions must include the following              -->
<!--    acknowledgement:                                                                            -->
<!--    This product includes software developed by the Center for Data and Search Informatics      -->
<!--    at Indiana University.  For further information contact Beth Plale at plale@cs.indiana.edu. -->
<!--    Alternatively, this acknowledgement may appear in the software itself, and wherever         -->
<!--    such third-party acknowledgments normally appear.                                           -->
<!--                                                                                                -->
<!-- 4) The name "myLEAD" or "myLEAD Agent" shall not be used to endorse or promote products        -->
<!--    derived from this software without prior written permission from Indiana University.        -->
<!--    For written permission, please contact Beth Plale at plale@cs.indiana.edu.                  -->
<!--                                                                                                -->
<!-- 5) Products derived from this software may not be called "myLEAD" or "myLEAD Agent",           -->
<!--    nor may "myLEAD" or "myLEAD Agent" appear in their name, without prior written permission   -->
<!--    of Indiana University.                                                                      -->
<!--                                                                                                -->
<!-- Indiana University provides no reassurances that the source code provided does                 -->
<!-- not infringe the patent or any other intellectual property rights of any other                 -->
<!-- entity.  Indiana  University  disclaims  any  liability to any  recipient  for                 -->
<!-- claims  brought by any other  entity  based on  infringement  of  intellectual                 -->
<!-- property rights or otherwise.                                                                  -->
<!--                                                                                                -->
<!-- LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS  TO          -->
<!-- CAPABILITIES  OR  ACCURACY  ARE  MADE.  INDIANA  UNIVERSITY  GIVES  NO WARRANTIES AND          -->
<!-- MAKES NO REPRESENTATION  THAT SOFTWARE IS FREE OF INFRINGEMENT OF THIRD  PARTY  PATENT,        -->
<!-- COPYRIGHT,  OR  OTHER  PROPRIETARY  RIGHTS.  INDIANA UNIVERSITY  MAKES NO WARRANTIES           -->
<!-- THAT SOFTWARE IS FREE FROM "BUGS",  "VIRUSES", "TROJAN  HORSES",  "TRAP  DOORS",  "WORMS",     -->
<!-- OR OTHER  HARMFUL  CODE.  LICENSEE ASSUMES THE ENTIRE RISK AS TO THE  PERFORMANCE OF           -->
<!-- SOFTWARE  AND/OR  ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION     -->
<!-- GENERATED USING SOFTWARE.                                                                      -->
<!-- ********************************************************************************************** -->

<!-- author: Scott Jensen scjensen at cs.indiana.edu -->

	<xsl:strip-space elements="*" />
	<xsl:output method="html" encoding="iso-8859-1" indent="yes" />
	<xsl:template match="/">
		<!-- Define Variable For the Root Element of the Schema -->
		<xsl:variable name="metadata" select="./metadata" />

		<!-- Title for the Object  and its Global ID -->
		<!-- the title is in section 8 of the FGDC -->
		<table>
			<tr>
				<td><h3>Name:</h3></td>
				<td><xsl:value-of select="$metadata/idinfo/citation/citeinfo/title" /></td>
			</tr>
<!--  In the FGDC wehave not identified a field for the unique identifier.  For now in the 
      default implementation for FGDC we are using the title as the unique ID, which is 
      already displayed above as the name.
			<tr>
				<td><h3>Global ID:</h3></td>
				<td><xsl:value-of select="$metadata/le:resourceID" /></td>
			</tr>
-->
		</table>
		<p />
		<!-- ********** CALL TEMPLATES ********** -->
		<div style="margin-left: 10px">
			<xsl:call-template name="Details">
				<xsl:with-param name="detailNode"
					select="$metadata" />
			</xsl:call-template>
		</div>
	</xsl:template>

	<!-- ********* Main Template Rules ********* -->
	<!-- The metadata element from the FGDC      -->
	<!-- schema passed as a parameter.           -->
	<!-- *************************************** -->
	<xsl:template name="Details">
		<xsl:param name="detailNode" />

		<!-- the calls to the templates is done here in the order of the 
		     categories defined for the FGDC schema in XMC Cat 
		-->

		<xsl:call-template name="GeneralInfo">
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>

		<xsl:call-template name="KeyWords">
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>

		<xsl:call-template name="SpatialTemporal">
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>
		
		<!--Contact Information of person  knowledgeable about data-->
		<xsl:call-template name="DataContactInfo">
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>
		
		<!--Information about related  data sets (multiple) - part of idinfo-->
		<xsl:call-template name="Crossref">
			<xsl:with-param  name="data" select="$detailNode" />
		</xsl:call-template>
		<!--**Security Information about the data set   part of idinfo-->
		<xsl:call-template name="SecInfo">
			<xsl:with-param name="data" select="$detailNode"  />
		</xsl:call-template>
		<!--**Browse Graphic-part of idinfo-->
		<xsl:call-template name="Browse">
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>
		<!--***Data Quality  information****-->
		<xsl:call-template name="Dataqual"  >
			<xsl:with-param name="data"  select="$detailNode" />
		</xsl:call-template>
		<!--***Spatial Data Organization Information****-->
		<xsl:call-template name="Spdoinfo"  >
			<xsl:with-param name="data"  select="$detailNode" />
		</xsl:call-template>
		<!--Spatial Reference Information-->
		<xsl:call-template name="Spref"  >
			<xsl:with-param  name="data" select="$detailNode" />
		</xsl:call-template>
		
		<!--Entity Atrribute info-->
		<xsl:call-template name="Eainfo"  >
			<xsl:with-param name="data"  select="$detailNode" />
		</xsl:call-template>
		
		<!--Distribution Information-->
		<xsl:call-template  name="DistInfo" >
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>
		
		<!--Metadata Reference Information-->
		<xsl:call-template  name="MetaInfo" >
			<xsl:with-param name="data" select="$detailNode" />
		</xsl:call-template>
	</xsl:template>

	<!-- ********** GENERAL INFO ********** -->
	<xsl:template name="GeneralInfo">
		<xsl:param name="data" />
		<h2>General Information</h2>
		<table>
			<xsl:choose>
				<xsl:when
					test="count($data/idinfo/citation/citeinfo/origin) = 1">
					<tr>
						<td valign="top">Owner:</td>
						<td>
							<xsl:value-of
								select="$data/idinfo/citation/citeinfo/origin" />
						</td>
					</tr>
				</xsl:when>
				<xsl:otherwise>
					<tr>
						<td valign="top">Owners:</td>
						<td>
							<xsl:for-each
								select="$data/idinfo/citation/citeinfo/origin">
								<xsl:value-of select="." />
								<xsl:if test="position() != last()">
									<br />
								</xsl:if>
							</xsl:for-each>
						</td>
					</tr>
				</xsl:otherwise>
			</xsl:choose>
			<tr>
				<td valign="top">Abstract:</td>
				<td>
					<xsl:value-of
						select="$data/idinfo/descript/abstract" />
				</td>
			</tr>
			<tr>
				<td valign="top">Purpose:</td>
				<td>
					<xsl:value-of
						select="$data/idinfo/descript/purpose" />
				</td>
			</tr>
			<xsl:if test="count($data/idinfo/descript/supplinf) > 0">
				<tr>
					<td valign="top">Additional Information:</td>
					<td>
						<xsl:for-each select="$data/idinfo/descript/supplinf">
							<xsl:value-of select="$data/idinfo/descript/supplinf" />
							<xsl:if test="position() != last()">
								<br />
							</xsl:if>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:if>
			<tr>
				<td valign="top">Status:</td>
				<td>
					<xsl:value-of
						select="$data/idinfo/status/progress" />
					<br />
					update frequency:
					<xsl:value-of
						select="$data/idinfo/status/update" />
				</td>
			</tr>
			<tr>
				<td valign="top">Access Constraints:</td>
				<td>
					<xsl:value-of select="$data/idinfo/accconst" />
				</td>
			</tr>
			<tr>
				<td valign="top">Usage Constraints:</td>
				<td>
					<xsl:value-of select="$data/idinfo/useconst" />
				</td>
			</tr>
		</table>
		<h3>Publishing Details</h3>
		<table>
			<tr>
				<td valign="top">When:</td>
				<td>
					<xsl:text>Date: </xsl:text>
					<xsl:value-of
						select="$data/idinfo/citation/citeinfo/pubdate" />
					<xsl:if	test="count($data/idinfo/citation/citeinfo/pubtime) > 0">
						<br />
						<xsl:text>Time: </xsl:text>
						<xsl:value-of select="$data/idinfo/citation/citeinfo/pubtime" />
					</xsl:if>
				</td>
			</tr>
			<xsl:if
				test="count($data/idinfo/citation/citeinfo/edition) > 0">
				<tr>
					<td>Version:</td>
					<td>
						<xsl:value-of
							select="$data/idinfo/citation/citeinfo/edition" />
					</td>
				</tr>
			</xsl:if>
			<xsl:if
				test="count($data/idinfo/citation/citeinfo/serinfo) > 0">
				<tr>
					<td valign="top">Published In:</td>
					<td>
						<xsl:value-of
							select="$data/idinfo/citation/citeinfo/serinfo/sername" />
						<br />
						<xsl:value-of
							select="$data/idinfo/citation/citeinfo/pubinfo/issue" />
					</td>
				</tr>
			</xsl:if>
			<xsl:if
				test="count($data/idinfo/citation/citeinfo/pubinfo) > 0">
				<tr>
					<td valign="top">Published By:</td>
					<td>
						<xsl:value-of
							select="$data/idinfo/citation/citeinfo/pubinfo/publish" />
						<br />
						<xsl:value-of
							select="$data/idinfo/citation/citeinfo/pubinfo/pubplace" />
					</td>
				</tr>
			</xsl:if>
			<xsl:if
				test="count($data/idinfo/citation/citeinfo/onlink) > 0">
				<tr>
					<td valign="top">Available At:</td>
					<td>
						<xsl:for-each
							select="$data/idinfo/citation/citeinfo/onlink">
							<xsl:value-of select="." />
							<xsl:if test="position() != last()">
								<br />
							</xsl:if>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:if>
			<xsl:if
				test="count($data/idinfo/citation/citeinfo/othercit) > 0">
				<tr>
					<td valign="top">Additional Information:</td>
					<td>
						<xsl:value-of
							select="$data/idinfo/citation/citeinfo/othercit" />
					</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>
	<!-- End of the General Information Template -->

	<!-- ********** KEY TERMS (keywords) ********** -->
	<xsl:template name="KeyWords">
		<xsl:param name="data" />
		<h2>Keyword Terms</h2>
		<table>
			<tr>
				<td>Subject Keyword Terms</td>
			</tr>
			<!--  The theme keywords are grouped by their defining thesearus and    -->
			<!--  both the thesearus and related keywords are sorted alphabetically -->
			<xsl:for-each
				select="$data/idinfo/keywords/theme/themekt">
				<xsl:sort select="." />
				<xsl:variable name="themeName" select="." />
				<xsl:if
					test="generate-id() = generate-id(../../theme/themekt[(.) = $themeName][1])">
					<tr style="font-style: Italic">
						<td valign="top">Defined by:</td>
						<td>
							<xsl:value-of select="." />
						</td>
					</tr>
					<xsl:for-each
						select="../../theme[themekt = $themeName]/themekey">
						<xsl:sort select="." />
						<tr>
							<td></td>
							<td>
								<xsl:value-of select="." />
							</td>
						</tr>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>
			<!-- The place keywords are grouped by their defining thesearus and    -->
			<!-- both the thesearus and related keywords are sorted alphabetically -->
			<xsl:for-each
				select="$data/idinfo/keywords/place/placekt">
				<xsl:sort select="." />
				<xsl:variable name="placeName" select="." />
				<xsl:if
					test="generate-id() = generate-id(../../place/placekt[(.) = $placeName][1])">
					<tr style="font-style: Italic">
						<td valign="top">Defined by:</td>
						<td>
							<xsl:value-of select="." />
						</td>
					</tr>
					<xsl:for-each
						select="../../place[placekt = $placeName]/placekey">
						<xsl:sort select="." />
						<tr>
							<td></td>
							<td>
								<xsl:value-of select="." />
							</td>
						</tr>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>
			<!-- The stratum keywords are grouped by their defining thesearus and  -->
			<!-- both the thesearus and related keywords are sorted alphabetically -->
			<xsl:for-each
				select="$data/idinfo/keywords/stratum/stratkt">
				<xsl:sort select="." />
				<xsl:variable name="stratName" select="." />
				<xsl:if
					test="generate-id() = generate-id(../../stratum/stratkt[(.) = $stratName][1])">
					<tr style="font-style: Italic">
						<td valign="top">Defined by:</td>
						<td>
							<xsl:value-of select="." />
						</td>
					</tr>
					<xsl:for-each
						select="../../stratum[stratkt = $stratName]/stratkey">
						<xsl:sort select="." />
						<tr>
							<td></td>
							<td>
								<xsl:value-of select="." />
							</td>
						</tr>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>
			<!-- The temporal keywords are grouped by their defining thesearus and  -->
			<!-- both the thesearus and related keywords are sorted alphabetically  -->
			<xsl:for-each
				select="$data/idinfo/keywords/temporal/tempkt">
				<xsl:sort select="." />
				<xsl:variable name="temporalName" select="." />
				<xsl:if
					test="generate-id() = generate-id(../../temporal/tempkt[(.) = $temporalName][1])">
					<tr style="font-style: Italic">
						<td valign="top">Defined by:</td>
						<td>
							<xsl:value-of select="." />
						</td>
					</tr>
					<xsl:for-each
						select="../../temporal[tempkt = $temporalName]/tempkey">
						<xsl:sort select="." />
						<tr>
							<td></td>
							<td>
								<xsl:value-of select="." />
							</td>
						</tr>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- End of keywords template -->

	<!-- ********** SPATIAL AND TEMPORAL ********** -->
	<xsl:template name="SpatialTemporal">
		<xsl:param name="data" />
		<xsl:if test="(count($data/idinfo/timeperd) > 0) or (count($data/idinfo/spdom) > 0)">
			<h2>When and Where</h2>
			<xsl:variable name="timeInfoNode"
				select="$data/idinfo/timeperd/timeinfo" />
			<table>
				<xsl:variable name="dateCnt"
					select="count($timeInfoNode//sngdate)" />
				<xsl:if test="$dateCnt > 0">
					<tr>
						<xsl:if test="$dateCnt = 1">
							<td>Date:</td>
						</xsl:if>
						<xsl:if test="$dateCnt > 1">
							<td valign="top">Dates:</td>
						</xsl:if>
						<td>
							<xsl:for-each
								select="$timeInfoNode//sngdate">
								<xsl:value-of select="caldate" />
								<xsl:if test="count(time) > 0">
									<xsl:text> at </xsl:text>
									<xsl:value-of select="time" />
								</xsl:if>
								<xsl:if test="position() != last()">
									<br />
								</xsl:if>
							</xsl:for-each>
						</td>
					</tr>
				</xsl:if>
				<xsl:if test="count($timeInfoNode/rngdates) > 0">
					<tr>
						<td valign="top">Date Range:</td>
						<td>
							<xsl:text>Starting: </xsl:text>
							<xsl:value-of
								select="$timeInfoNode/rngdates/begdate" />
							<xsl:if
								test="count($timeInfoNode/rngdates/begtime) > 0">
								<xsl:text> at </xsl:text>
								<xsl:value-of
									select="$timeInfoNode/rngdates/begtime" />
							</xsl:if>
							<br />
							<xsl:text>Ending: </xsl:text>
							<xsl:value-of
								select="$timeInfoNode/rngdates/enddate" />
							<xsl:if
								test="count($timeInfoNode/rngdates/endtime) > 0">
								<xsl:text> at </xsl:text>
								<xsl:value-of
									select="$timeInfoNode/rngdates/endtime" />
							</xsl:if>
						</td>
					</tr>
				</xsl:if>
<!--
				<xsl:if
					test="count($timeInfoNode/le:rngdatesInd) > 0">
					<tr>
						<td>Date:</td>
						<td>Indederminate</td>
					</tr>
				</xsl:if>
-->
				<!-- ***** Spatial ***** -->
				<tr>
					<xsl:variable name="bounds" select="$data/idinfo/spdom/bounding" />
					<xsl:variable name="nBound">
						<xsl:call-template name="FormatPoint">
							<xsl:with-param name="dataPoint" select="$bounds/northbc" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="sBound">
						<xsl:call-template name="FormatPoint">
							<xsl:with-param name="dataPoint" select="$bounds/southbc" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="eBound">
						<xsl:call-template name="FormatPoint">
							<xsl:with-param name="dataPoint" select="$bounds/eastbc" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="wBound">
						<xsl:call-template name="FormatPoint">
							<xsl:with-param name="dataPoint" select="$bounds/westbc" />
						</xsl:call-template>
					</xsl:variable>

					<xsl:variable name="mapUrl" select="concat('http://maps.googleapis.com/maps/api/staticmap?sensor=false&amp;size=256x256&amp;path=color:0x0000ff%7Cweight:5%7C',$nBound,',',$eBound,'%7C',$sBound,',',$eBound,'%7C',$sBound,',',$wBound,'%7C',$nBound,',',$wBound,'%7C',$nBound,',',$eBound,'&amp;maptype=roadmap')"/>

					<td>Bounding Coordinates:
					<br/>
					<!-- north -->
					<xsl:text>North: </xsl:text>
					<xsl:value-of select="$nBound"/>
					<br/>
					<!-- south -->
					<xsl:text>South: </xsl:text>
					<xsl:value-of select="$sBound"/>
					<br/>
					<!-- east -->
					<xsl:text>East: </xsl:text>
					<xsl:value-of select="$eBound"/>
					<br/>
					<!-- west -->
					<xsl:text>West: </xsl:text>
					<xsl:value-of select="$wBound"/>
					</td>
					<td>
						<xsl:element name="img">
							<xsl:attribute name="src">
								<xsl:value-of select="$mapUrl"/>
							</xsl:attribute>
						</xsl:element>
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>
	<!-- ***** Template to Format a Point in the Spatial Data ***** -->
	<xsl:template name="FormatPoint">
		<xsl:param name="dataPoint" />
		<xsl:value-of
			select="concat( substring-before($dataPoint, '.'), '.', substring( substring-after($dataPoint, '.'), 1, 4) )" />
	</xsl:template>
	<!-- End of Temporal and Spatial -->


	<!-- ********** DYNAMIC (detailed) METADATA ATTRIBUTES and ELEMENTS ********** -->
<!--
	<xsl:template name="AddAttrElement">
		<xsl:param name="attrNode" />
		<td valign="top">
			<xsl:value-of select="$attrNode/attrlabl" />
		</td>
		<xsl:if test="count($attrNode/le:attrv) > 0">
			<td>
				<xsl:for-each select="$attrNode/le:attrv">
					<xsl:value-of select="." />
					<xsl:if test="position() != last()">
						<br />
					</xsl:if>
				</xsl:for-each>
			</td>
		</xsl:if>
-->
		<!-- sub-properties -->
<!--
		<xsl:if test="count($attrNode/attr) > 0">
			<td>
				<table>
					<xsl:for-each select="$attrNode/attr">
						<xsl:variable name="subNode" select="." />
						<tr>
							<xsl:call-template name="AddAttrElement">
								<xsl:with-param name="attrNode"
									select="$subNode" />
							</xsl:call-template>
						</tr>
					</xsl:for-each>
				</table>
			</td>
		</xsl:if>
	</xsl:template>
-->
 <!--Data Set G-Polygon-->
  <xsl:template name="Dsgpoly">
    <xsl:param name="data" />
    <xsl:if test="(count ($data/dsgpoly) > 0)">
      <h3>Polygon Area covered by the data set</h3>
      <xsl:for-each select="$data/dsgpoly">
        <br/>
        <xsl:call-template name="Dsgpolyo">
          <xsl:with-param name="data" select="." />
        </xsl:call-template>
        <xsl:call-template name="Dsgpolyx">
          <xsl:with-param name="data" select="." />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
   </xsl:template>
  <xsl:template name="Dsgpolyo">
    <xsl:param name="data" />
    <xsl:variable name="dsgpolyoNode" select="$data/dsgpolyo"/>
    <xsl:if test="count($dsgpolyoNode)>0">
    <h4>Boundary of Interior area</h4>
    <xsl:choose>
      <xsl:when test="count($dsgpolyoNode/grngpoin)=1">
        <xsl:call-template name="Grngpoin">
          <xsl:with-param name="data" select="$dsgpolyoNode" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <table>
          <tr>
            <td>
              gring:
            </td>
            <td>
              <xsl:value-of select="$dsgpolyoNode/gring"/>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Grngpoin">
    <xsl:param name="data" />
    <xsl:variable name="grngpoinNode" select="$data/grngpoin"/>
    <table>
      <xsl:for-each select="$grngpoinNode">
      <tr>
        <td>
          Point:
        </td>
        <td>
          lat:<xsl:value-of select="./gringlat"/>
          <br/>
          lon:<xsl:value-of select="./gringlon"/>
        </td>
      </tr>
      </xsl:for-each>
    </table>
  </xsl:template>
  
    <xsl:template name="Dsgpolyx">
      <xsl:param name="data" />
      <xsl:variable name="dsgpolyxNode" select="$data/dsgpolyx"/>
      <xsl:if test="count($dsgpolyxNode)>0">
      <h4>Boundary of Closed area</h4>
      <xsl:choose>
        <xsl:when test="count($dsgpolyxNode/grngpoin)=1">
          <xsl:call-template name="Grngpoin">
            <xsl:with-param name="data" select="$dsgpolyxNode" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <table>
            <tr>
              <td>
                gring:
              </td>
              <td>
                <xsl:value-of select="$dsgpolyxNode/gring"/>
              </td>
            </tr>
          </table>
        </xsl:otherwise>
      </xsl:choose>
      </xsl:if>
    </xsl:template>
  
 


  <!--  ***************Contact-Info about the data cited***************-->
  <xsl:template name="DataContactInfo">

    <xsl:param name="data" />
    
    <xsl:if test="(count ($data/idinfo/ptcontac/cntinfo) > 0)">
      <h3>Contact Information of a person with knowledge on the data  set</h3>
      <xsl:call-template name="Cntinfo">
        <xsl:with-param name="data" select="$data/idinfo/ptcontac"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <!--****************End of contact information about person with knowledge of data set-->
  <!-- **********  INFORMATION ABOUT RELATED DATASETS (multiple-put in a for loop-think this itself is fine) ********** -->
  <xsl:template name="Crossref">
    <xsl:param  name="data" />
    <xsl:variable name="crossrefNode"
       select="$data/idinfo/crossref" />
    <xsl:if test="(count ($crossrefNode) > 0)">
      <h3>Information on Related Datasets</h3> 
    <xsl:for-each select="$crossrefNode">
      <xsl:call-template name="Citeinfo">
        <xsl:with-param name="data"
         select="." />
      </xsl:call-template>
    </xsl:for-each>
    </xsl:if>

    </xsl:template>
  <!--Citeinfo template-->
  <xsl:template name="Citeinfo" >
    <xsl:param  name="data" />
    <xsl:variable name="citeinfoNode"
      select="$data/citeinfo" />
    <h4>
        <xsl:value-of
            select="$citeinfoNode/title"/>
    </h4>
    <table>
      
      <xsl:choose>
        <xsl:when
		 			test="count($citeinfoNode/origin) = 1">
          <tr>
            <td  valign="top">Owner:</td>
            <td>
              <xsl:value-of
						 		select="$citeinfoNode/origin" />
            </td>
          </tr>
        </xsl:when>
        <xsl:otherwise>
          <tr>
            <td valign="top">Owners:</td>
            <td>
              <xsl:for-each
                select="$citeinfoNode/origin">
                <xsl:value-of select="." />
                <xsl:if test="position() != last()">
                  <br />
                </xsl:if>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:otherwise>
      </xsl:choose>
    </table>
    
    
    <h4>Publishing  Details</h4>
    <table>
      <tr>
        <td valign="top">When:</td>
        
        <td>
          <xsl:text>Date:  </xsl:text>
          <xsl:value-of
            select="$citeinfoNode/pubdate" />
          <xsl:if	test="count ($citeinfoNode/pubtime) > 0">
            <br />
            <xsl:text>Time: </xsl:text>
            <xsl:value-of select="$citeinfoNode/pubtime" />
          </xsl:if>
        </td>
      
      </tr>
      <xsl:if
				test="count($citeinfoNode/edition) > 0">
        <tr>
          <td>Version:</td>
          <td>
            <xsl:value-of
					 		select="$citeinfoNode/edition" />
          </td>
        </tr>
      </xsl:if>
      <xsl:if
       test="count($citeinfoNode/serinfo) > 0">
        <tr>
          <td valign="top">Published In:</td>
          <td>
            <xsl:value-of
				 			select="$citeinfoNode/serinfo/sername" />
            <br />
            <xsl:value-of
              select="$citeinfoNode/serinfo/issue" />
          </td>
        </tr>
      </xsl:if>
      <xsl:if
       test="count($citeinfoNode/pubinfo) > 0">
        <tr>
          <td valign="top">Published By:</td>
          <td>
            <xsl:value-of
					 		select="$citeinfoNode/pubinfo/publish" />
            <br />
            <xsl:value-of
              select="$citeinfoNode/pubinfo/pubplace" />
          </td>
        </tr>
      </xsl:if>
      <xsl:if
       test="count($citeinfoNode/onlink) > 0">
        <tr>
          <td valign="top">Available At:</td>
          <td>
            <xsl:for-each
					 		select="$citeinfoNode/onlink">
              <xsl:value-of select="." />
              <xsl:if test="position() != last()">
                <br />
              </xsl:if>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      <xsl:if
				test="count ($citeinfoNode/othercit) > 0">
        <tr>
          <td valign="top">Additional  Information:</td>
          <td>
            <xsl:value-of
							 select="$citeinfoNode/othercit" />
          </td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>
 
  <!-- End of the General Information Template -->
  <!-- SECINFO-->
  <xsl:template  name="SecInfo">
    <xsl:param name="data" />
    <xsl:if test="(count ($data/idinfo/secinfo) > 0)">
    <h3>Security Information</h3>
    <table>
      <tr>
        <td  valign="top">Security Classification:</td>
        <td>
          <xsl:value-of
             select="$data/idinfo/secinfo/secclass" />
        </td>
      </tr>
      <tr>
        <td valign="top">Security  Classification System:</td>
        <td>
          <xsl:value-of
            select="$data/idinfo/secinfo/secsys"  />
        </td>
      </tr>
      <tr>
        <td valign="top">Security Handling Description:</td>
        <td>
          <xsl:value-of
           select="$data/idinfo/secinfo/sechandl" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <!--End of SECINFO-->


  <!-- BROWSE -->
  <xsl:template name="Browse">
    <xsl:param  name="data" />
    <!--xsl:copy-of select="."/-->
    <xsl:if test="(count($data/idinfo/browse) > 0)">
      
    <h3>Graphic Illustraton of data set</h3>
      <xsl:for-each select="$data/idinfo/browse">
        <h5>
            <xsl:value-of
               select="./browsen" />
        </h5>
    
       <table>
        <tr>
        <td valign="top">Browse  Graphic File Description:</td>
        <td>
          <xsl:value-of
             select="./browsed" />
        </td>
      </tr>
      <tr>
        <td valign="top">Browse  Graphic File Type:</td>
        <td>
          <xsl:value-of
            select="./browset" />
        </td>
      </tr>
      
    </table>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <!--End of BROWSE-->
  <!--EAINFO-->
  <!-- OVERVIEW -->
  <xsl:template name="Eainfo">
    <xsl:param name="data" />
    <xsl:variable name="eainfoNode"
   select="$data/eainfo" />
    <xsl:if test="(count ($eainfoNode) > 0)">
    <h2>Entity and Attribute Information</h2>
    <table>
      <xsl:if test="count($eainfoNode/eaover)>0">
      <tr>
        <td valign="top">Entity and Attribute Overview:</td>
        <td>
          <xsl:value-of
            select="$eainfoNode/eaover" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="count($eainfoNode/eadetcit)>0">
      <tr>
        <td valign="top">Entity and Attribute Detail Citation:</td>
        <td>
          <xsl:value-of
             select="$eainfoNode/eadetcit" />
        </td>
      </tr>
      </xsl:if>
    </table>
    <xsl:call-template name="Detailed">
      <xsl:with-param name="data" select="$eainfoNode"/>
    </xsl:call-template>
    <xsl:call-template name="Overview">
      <xsl:with-param name="data" select="$eainfoNode"/>
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <!-- DETAILED -->
  <xsl:template name="Detailed" >
    <xsl:param name="data" />
    <xsl:if test="(count ($data/detailed) > 0)">
      <h3>Detailed Description</h3>
      <xsl:variable name="detailedNode"
                     select="$data/detailed" />
      <xsl:for-each select="$detailedNode">

        <xsl:call-template name="Enttyp">
          <xsl:with-param name="data"
           select="." />
        </xsl:call-template>
        <br/>
        <xsl:call-template name="Attr">
          <xsl:with-param name="data"
           select="." />
        </xsl:call-template>
        <br/>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Enttyp" >
    <xsl:param  name="data" />
 
    <xsl:variable name="enttypNode"
                  select="$data/enttyp" />
    <xsl:if test="count($enttypNode)>0">
      <h3><xsl:value-of
        select="$enttypNode/enttypl" /></h3>
    <table>
      <tr>
        <td valign="top">Source:</td>
        <td>
          <xsl:value-of
            select="$enttypNode/enttypds" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Defined By:</td>
        <td>
          <xsl:value-of
            select="$enttypNode/enttypd" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <xsl:template  name="Attr" >
    <xsl:param name="data" />
    <xsl:variable name="attrNode"
                   select="$data/attr" />
    <xsl:if test="(count ($attrNode) > 0)">
   
    <xsl:for-each select="$attrNode">
      <table style="margin-left: 30px;">
        <tr><td style="font-weight:bold;font-size:115%"> <xsl:value-of
        select="./attrlabl" /></td></tr></table>
      <table style="margin-left: 30px;" >
        <tr>
          <td valign="top">Source:</td>
          <td>
            <xsl:value-of
              select="./attrdefs" />
          </td>
        </tr>
    <tr>
        <td valign="top">Defined By:</td>
        <td>
          <xsl:value-of
             select="./attrdef" />
        </td>
      </tr>
    
        <xsl:if test="(count (./begdatea) > 0)">
        <tr>
          <td valign="top">Beginning Date of Validity:</td>
          <td>
            <xsl:value-of
              select="./begdatea" />
          </td>
        </tr>
        </xsl:if>
        <xsl:if test="(count (./enddatea) > 0)">
          <tr>
            <td valign="top">Ending Date of Validity:</td>
            <td>
              <xsl:value-of
                select="./enddatea" />
            </td>
          </tr>
        </xsl:if>
         <xsl:if test="(count (./attrmfrq) > 0)">
           <tr>
             <td valign="top">Measurement Frequency:</td>
             <td>
               <xsl:value-of
                 select="./attrmfrq" />
             </td>
           </tr>
         </xsl:if>
    </table>
      
    <xsl:call-template name="Attrdomv">
      <xsl:with-param name="data"
       select="." />
    </xsl:call-template>
    <xsl:call-template name="Attrvai">
      <xsl:with-param name="data"
       select="." />
    </xsl:call-template>

      <br/>
    </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Attrdomv">
    <xsl:param name="data" />
    <xsl:variable  name="attrdomvNode"
                  select="$data/attrdomv" />
    <xsl:if test="(count ($attrdomvNode) > 0)">
      <xsl:for-each select="$attrdomvNode">
        <xsl:choose>
          <xsl:when
           test="count(./udom) = 1">

            <table style="margin-left: 30px;">
              <tr>
                <td style="font-weight:bold;">
                  Unrepresentable Domain:
                </td>
                <td>
                  <xsl:value-of
                     select="./udom" />
                </td>
              </tr>
            </table>

          </xsl:when>
          <xsl:otherwise>

            <xsl:choose>
              <xsl:when
               test="count(./edom) = 1">

                <xsl:call-template name="Edom">
                  <xsl:with-param name="data"
                   select="." />
                </xsl:call-template>

              </xsl:when>
              <xsl:otherwise>

                <xsl:choose>
                  <xsl:when
                   test="count(./rdom) = 1">

                    <xsl:call-template name="Rdom">
                      <xsl:with-param name="data"
                       select="." />
                    </xsl:call-template>


                  </xsl:when>
                  <xsl:otherwise>

                    <xsl:call-template name="Codesetd">
                      <xsl:with-param name="data"
                       select="." />
                    </xsl:call-template>

                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>

          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Edom">
    <xsl:param name="data" />
    <xsl:variable name="edomNode"
                 select="$data/edom" />
    <xsl:if test="(count ($edomNode) > 0)">
     <xsl:for-each select="$edomNode">
      <table style="margin-left: 30px;">
        <tr>
          <td style="font-weight:bold;">
            Enumerated Domain: 
          </td>
          <td> <xsl:value-of
            select="./edomv" /></td>
        </tr>
      <tr>
        <td valign="top">Defined By:</td>
        <td>
          <xsl:value-of
           select="./edomvd" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Source:</td>
        <td>
          <xsl:value-of
            select="./edomvds" />
        </td>
      </tr>
      <!--missing <tr>
        <td valign="top">Attribute:</td>
        <td>
          <xsl:value-of
             select="$edomNode/attr" />
        </td>
      </tr>-->
    </table>
    
    </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Rdom" >
    <xsl:param name="data" />
    <xsl:variable  name="rdomNode"
                  select="$data/rdom" />
    <xsl:if test="(count ($rdomNode) > 0)">
     
      <table style="margin-left: 30px;">
        <tr>
          <td style="font-weight:bold;">Domain Range</td>
        </tr>
      <tr>
        <td valign="top">Minimum Domain Value:</td>
        <td>
          <xsl:value-of
            select="$rdomNode/rdommin" />
        </td>
      </tr>
      <tr>
        <td valign="top">Maximum Domain Value:</td>
        <td>
          <xsl:value-of
             select="$rdomNode/rdommax" />
        </td>
      </tr>
        <xsl:if test="(count ($rdomNode/attrunit) > 0)">
         <tr>
           <td valign="top">Units of Measure:</td>
           <td>
             <xsl:value-of
               select="$rdomNode/attrunit" />
           </td>
         </tr>
        </xsl:if>
        <xsl:if test="(count ($rdomNode/attrmres) > 0)">
         <tr>
           <td valign="top">Measurement Resolution:</td>
           <td>
             <xsl:value-of
               select="$rdomNode/attrmres"  />
           </td>
         </tr>    
        </xsl:if>
    </table>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Codesetd" >
    <xsl:param name="data" />
    <xsl:variable name="codesetdNode"
                 select="$data/codesetd" />
    <xsl:if test="(count ($codesetdNode) > 0)">
      <table style="margin-left: 30px;">
        <tr>
          <td style="font-weight:bold;">Codeset Domain:</td>
          <td><xsl:value-of
            select="$codesetdNode/codesetn" /></td>
        </tr>
     <tr>
        <td valign="top">Source:</td>
        <td>
          <xsl:value-of
            select="$codesetdNode/codesets" />
        </td>
      </tr>
    </table>
   </xsl:if>
  </xsl:template>
  <xsl:template name="Attrvai" >
    <xsl:param name="data" />
    <xsl:variable  name="attrvaiNode"
                  select="$data/attrvai" />
    <xsl:if test="(count ($attrvaiNode) > 0)">
      <h4 style="margin-left: 30px;">Accuracy Information</h4>
      <table style="margin-left: 30px;">
      <tr>
        <td valign="top">Accuracy:</td>
        <td>
          <xsl:value-of
             select="$attrvaiNode/attrva" />
        </td>
      </tr>
      <tr>
        <td valign="top">Accuracy Explanation:</td>
        <td>
          <xsl:value-of
            select="$attrvaiNode/attrvae" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <!-- OVERVIEW -->
  <xsl:template name="Overview" >
    <xsl:param name="data" />
    <xsl:variable name="overviewNode"
                 select="$data/overview" />
    <xsl:if test="(count ($overviewNode) > 0)">
    <h3>Overview</h3>
    <xsl:for-each select="$overviewNode">
    <table>
      <tr>
        <td>
          <xsl:value-of
             select="./eaover" />
        </td>
      </tr>
      <tr>
        <td valign="top">Detailed Citation:</td>
        <td>
          <xsl:for-each select="./eadetcit">
          <xsl:value-of
            select="." />
          </xsl:for-each>
        </td>
      </tr>
    </table>
      <br/>
    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <!--End of EAINFO-->

  <!--Distribution Information (DISTINFO)-->
  <xsl:template  name="DistInfo">
    <xsl:param name="data" />
    <xsl:variable name="distInfoNode"
        select="$data/distinfo" />
    <xsl:if test="(count ($distInfoNode) > 0)">
    <h2>Distribution Information</h2>
    <xsl:for-each select="$data/distinfo">
      <xsl:if test="(count (./distrib) > 0)">
      <h3>Distributor</h3>
      <xsl:call-template name="Cntinfo">
        <xsl:with-param name="data" select="./distrib"/>
      </xsl:call-template>
      </xsl:if>
      <table>
      <xsl:if test="(count (./resdesc) > 0)">
        <tr>
          <td valign="top" style="font-weight:bold">Resource Description:</td>
          <td>
        <xsl:value-of
          select="./resdesc" />
          </td>
        </tr>
      </xsl:if>
   
      <tr>
        <td valign="top" style="font-weight:bold">Distribution  Liability:</td>
        <td>
          <xsl:value-of
            select="./distliab" />
        </td>
      </tr>
      
        <xsl:if test="(count (./custom) > 0)">
        <tr>
          <td valign="top" style="font-weight:bold">Custom Order Process:</td>
        <td>
          <xsl:value-of
             select="./custom" />
        </td>
        </tr>
        </xsl:if>
        <xsl:if test="(count (./techpreq) > 0)">
         <tr>
           <td valign="top" style="font-weight:bold">Technical  Prerequisites:</td>
           <td>
             <xsl:value-of
               select="./techpreq" />
           </td>
         </tr>
        </xsl:if>
    </table>
    
      <xsl:call-template name="Stdorder">
        <xsl:with-param name="data" select="."/>
      </xsl:call-template>
      <xsl:if test="(count (./availabl) > 0)">
        <h3>Available Time period</h3>
        <xsl:call-template name="Timeinfo">
          <xsl:with-param name="data"
           select="./availabl" />
        </xsl:call-template>
      </xsl:if>

    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <!--End of Distribution Information-->
  <!--Standard Order Process-->
  <xsl:template name="Stdorder">
    <xsl:param name="data" />
    <xsl:variable name="stdorderInfoNode"
      select="$data/stdorder" />
    <xsl:if test="(count ($stdorderInfoNode) > 0)">
      <xsl:if test="(count ($stdorderInfoNode) > 1)">
      <h3>Standard Order Processes</h3>
      </xsl:if>
      <xsl:if test="(count ($stdorderInfoNode) = 1)">
        <h3>Standard Order Process</h3>
      </xsl:if>
      <xsl:for-each select="$data/stdorder">    
        <xsl:choose>
          <xsl:when
            test="count(./nondig) = 1">
            
            <table >
              <tr>
                <td style="font-weight:bold;">Non-digital Order:</td>
                <td>
                  <xsl:value-of
                    select="./nondig" />
                </td>
              </tr>
            </table>
            
          </xsl:when>
          <xsl:otherwise>
            
            <xsl:call-template name="Digform">
              <xsl:with-param name="data"
                select="$stdorderInfoNode" />
            </xsl:call-template>
            
          </xsl:otherwise>
        </xsl:choose>
        <table style="margin-left: 30px">
          <tr>
            <td  valign="top">Fees:</td>
            <td>
              <xsl:value-of
                select="./fees" />
            </td>
          </tr>
          <xsl:if test="(count (./ordering) > 0)">
          <tr>
            <td valign="top">Ordering Instructions:</td>
            <td>
              <xsl:value-of
                 select="./ordering" />
            </td>
          </tr>
          </xsl:if>
          <xsl:if test="(count (./turnarnd) > 0)">
          <tr>
            <td  valign="top">Turnaround:</td>
            <td>
              <xsl:value-of
                select="./turnarnd"  />
            </td>
          </tr>
          </xsl:if>
        </table>
        <br/>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <!--End of Standard Order Process-->
  <!--digform-->
  <!--digtinfo-->
  <xsl:template name="Digform">
    <xsl:param name="data" />
    <xsl:variable name="digformNode"
         select="$data/digform" />
    <xsl:if test="(count ($digformNode) > 0)">
  
    <xsl:for-each select="$digformNode">
      <xsl:call-template name="Digtinfo">
        <xsl:with-param name="data" select="."/>
      </xsl:call-template>
      
      <xsl:call-template name="Digtopt">
        <xsl:with-param name="data" select="."/>
      </xsl:call-template>
      
    </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!--digtinfo-->
  <xsl:template name="Digtinfo">
    <xsl:param name="data" />
    <xsl:variable name="digInfoNode"
     select="$data/digtinfo" />
    <xsl:if test="(count ($digInfoNode) > 0)">
     
      <table>
      <tr>
      <th>Digital Order Transfer Information</th>
      </tr>
      <tr>
        <td valign="top">Format:</td>
        <td>
          <xsl:value-of
            select="$digInfoNode/formname" />
        </td>
      </tr>
      <xsl:if test="(count ($digInfoNode/formcont) > 0)">
         <tr>
           <td  valign="top">Format Information:</td>
           <td>
             <xsl:value-of
               select="$digInfoNode/formcont" />
           </td>
         </tr>
      </xsl:if>
      <xsl:if test="(count ($digInfoNode/filedec) > 0)">
       <tr>
         <td valign="top">File Decompression Technique:</td>
         <td>
           <xsl:value-of
              select="$digInfoNode/filedec" />
         </td>
       </tr>
      </xsl:if>
      <xsl:if test="(count ($digInfoNode/transize) > 0)">
        <tr>
          <td valign="top">Transfer Size:</td>
          <td>
            <xsl:value-of
              select="$digInfoNode/transize" />
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="(count ($digInfoNode/formspec) > 0)">
        <tr>
          <td valign="top">Format Specification:</td>
          <td>
            <xsl:value-of
               select="$digInfoNode/formspec" />
          </td>
        </tr>
      </xsl:if>
      <xsl:choose>
        <xsl:when
         test="count($digInfoNode/formvern) = 1">

          <tr>
            <td valign="top">Format Version Number:</td>
            <td>
              <xsl:value-of
                select="$digInfoNode/formvern" />
            </td>
          </tr>

        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="(count ($digInfoNode/formverd) > 0)">
           <tr>
             <td valign="top">Format Version Date:</td>
             <td>
               <xsl:value-of
                  select="$digInfoNode/formverd" />
             </td>
           </tr>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose> 
    </table>
  </xsl:if>
 </xsl:template>
  <!--end of diginfo-->
  <!--digtopt-->
  <xsl:template name="Digtopt">
    <xsl:param name="data" />
    <xsl:variable  name="digtoptNode"
    select="$data/digtopt" />
    <xsl:if test="(count ($digtoptNode) > 0)">
    
    <xsl:for-each select="$digtoptNode">
    
      <xsl:choose>
        <xsl:when
         test="count($digtoptNode/onlinopt) = 1">
          <xsl:call-template name="Onlinopt">
            <xsl:with-param name="data" select="."/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="Offoptn">
            <xsl:with-param name="data" select="."/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    
    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <!--onlinopt-->
  <xsl:template name="Onlinopt">
    <xsl:param name="data" />
    <xsl:variable  name="onlinoptNode"
    select="$data/onlinopt" />
    <xsl:if test="(count ($onlinoptNode) > 0)">
   
    <xsl:call-template name="Computer">
      <xsl:with-param name="data"
       select="$onlinoptNode" />
    </xsl:call-template>
  
    <table style="margin-left:30px;">
      <xsl:if test="(count ($onlinoptNode/accinstr) > 0)">
      <tr>
        <td valign="top">Access Instructions:</td>
        <td>
          <xsl:value-of
           select="$onlinoptNode/accinstr" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="(count ($onlinoptNode/oncomp) > 0)">
      <tr>
        <td  valign="top">Online Computer and Operating System:</td>
        <td>
          <xsl:value-of
            select="$onlinoptNode/oncomp" />
        </td>
      </tr>
      </xsl:if>
    </table>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Computer" >
    <xsl:param name="data" />
    <xsl:variable  name="computerNode"
    select="$data/computer" />
    <xsl:if test="(count ($computerNode) > 0)">
      
    <xsl:for-each select="$data/computer">
    
      <xsl:choose>
        <xsl:when test="count(./networka) =1">
          <xsl:call-template name="Networka">
            <xsl:with-param name="data"
             select="." />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="Dialinst">
            <xsl:with-param name="data"
             select="." />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>   
    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Networka" >
    <xsl:param name="data" />
    <xsl:variable  name="networkaNode"
    select="$data/networka" />
    <xsl:if test="(count ($networkaNode) > 0)">
    
    
      <table style="margin-left:30px;">
      <tr>
        <td valign="top" style="font-weight:bold;">Network Transfer Resource:</td>
        <td>
          <xsl:for-each select="$networkaNode/networkr">
          <xsl:value-of
            select= "."/>
            <br/>
          </xsl:for-each>
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Dialinst">
    <xsl:param name="data" />
    <xsl:variable  name="dialinstNode"
    select="$data/dialinst" />
    <xsl:if test="count($dialinstNode)>0">
  
      <table style="margin-left:30px;">
      <tr>
        <tr>
          <td valign="top" style="font-weight:bold;">
            Dialup Transfer Instructions
          </td>
        </tr>
        <td valign="top">Lowest BPS:</td>
        <td>
          <xsl:value-of
            select="$dialinstNode/lowbps" />
        </td>
      </tr>
      <xsl:if test="count($dialinstNode/highbps)>0">
        <tr>
          <td valign="top">Highest BPS:</td>
          <td>
            <xsl:value-of
               select="$dialinstNode/highbps" />
          </td>
        </tr>
      </xsl:if>
      <tr>
        <td valign="top">Number DataBits:</td>
        <td>
          <xsl:value-of
            select="$dialinstNode/numdata" />
        </td>
      </tr>
      <tr>
        <td valign="top">Number StopBits:</td>
        <td>
          <xsl:value-of
             select="$dialinstNode/numstop" />
        </td>
      </tr>
      <tr>
        <td valign="top">Parity:</td>
        <td>
          <xsl:value-of
            select="$dialinstNode/parity" />
        </td>
      </tr>
      <xsl:if test="count($dialinstNode/compress)>0">
        <tr>
          <td valign="top">Compression Support:</td>
          <td>
            <xsl:value-of
               select="$dialinstNode/compress" />
          </td>
        </tr>
      </xsl:if>
      <tr>
        <td valign="top">Dialup Telephone:</td>
        <td>
          <xsl:for-each select="$dialinstNode/dialtel">
          <xsl:value-of
            select="." />
            <br/>
          </xsl:for-each>
        </td>
      </tr>
      <tr>
        <td valign="top">Dialup File Name:</td>
        <td>
          <xsl:for-each select="$dialinstNode/dialfile">
          <xsl:value-of 
             select="." />
          </xsl:for-each>
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <xsl:template  name="Offoptn" >
    <xsl:param name="data" />
    <xsl:variable name="offoptnNode"
    select="$data/offoptn" />
    <xsl:if test="count($offoptnNode)>0">
      
   <table style="margin-left:30px;">
      <tr>
        <td valign="top" style="font-weight:bold;">Offline Transfer Media:</td>
        <td>
          <xsl:value-of
             select="$offoptnNode/offmedia" />
        </td>
      </tr>
      <tr>
        <td valign="top">Recording Format:</td>
        <td>
          <xsl:for-each select="$offoptnNode/recfmt">
            <xsl:value-of
              select="." />
            <br/>
          </xsl:for-each>
        </td>
      </tr>
     <xsl:if test="count($offoptnNode/compat)>0">
      <tr>
        <td valign="top">Compatibility Information:</td>
        <td>
          <xsl:value-of
            select="$offoptnNode/compat" />
        </td>
      </tr>
     </xsl:if>
    </table>
    <xsl:call-template name="Reccap">
      <xsl:with-param name="data"
       select="$offoptnNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="Reccap">
    <xsl:param name="data" />
    <xsl:variable name="reccapNode"
     select="$data/reccap" />
    <xsl:if test="count($reccapNode)>0">
   
      <table style="margin-left:30px;">
      <tr>
        <td valign="top">Recording Density:</td>
        <td>
          <xsl:for-each select="$reccapNode/recden">

            <xsl:value-of
             select="." />
            <br/>
          </xsl:for-each>

        </td>
      </tr>
      <tr>
        <td  valign="top">Density Units:</td>
        <td>
          <xsl:value-of
            select="$reccapNode/recdenu" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>


 
  <!--End of DISTINFO-->
  <!--Metadata Reference  Information-->
  <xsl:template name="MetaInfo">
    <xsl:param name="data" />
    <xsl:variable name="metaInfoNode"
        select="$data/metainfo" />
    <xsl:if test="(count ($data/metainfo) > 0)">
    <h2>Metadata Reference  Information</h2>
    <table>
      <tr>
        <td valign="top">Metadata Date:</td>
        <td>
          <xsl:value-of
             select="$data/metainfo/metd" />
        </td>
      </tr>
      <xsl:if test="(count ($data/metainfo/metrd) > 0)">
      <tr>
        <td  valign="top">Review Date for metadata:</td>
        <td>
          <xsl:value-of
            select="$data/metainfo/metrd" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="(count ($data/metainfo/metfrd) > 0)">
      <tr>
        <td valign="top">Future Review Date for metadata:</td>
        <td>
          <xsl:value-of
             select="$data/metainfo/metfrd" />
        </td>
      </tr>
      </xsl:if>
    
      <tr>
        <td valign="top">Metadata Standard:</td>
        <td>
          <xsl:value-of
             select="$data/metainfo/metstdn" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Metadata Standard Version:</td>
        <td>
          <xsl:value-of
             select="$data/metainfo/metstdv" />
        </td>
      </tr>
      <xsl:if test="(count ($data/metainfo/mettc) > 0)">
      <tr>
        <td  valign="top">Time Convention:</td>
        <td>
          <xsl:value-of
            select="$data/metainfo/mettc" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="(count ($data/metainfo/metac) > 0)">
      <tr>
        <td valign="top">Access Constraints:</td>
        <td>
          <xsl:value-of
             select="$data/metainfo/metac" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="(count ($data/metainfo/metuc) > 0)">
      <tr>
        <td  valign="top">Use Constraints:</td>
        <td>
          <xsl:value-of
            select="$data/metainfo/metuc" />
        </td>
      </tr>
     </xsl:if>
    </table>
      <xsl:if test="(count ($metaInfoNode/metc) > 0)">
        <h3>Metadata Contact</h3>
        <xsl:call-template name="Cntinfo">
          <xsl:with-param name="data"
            select ="$data/metainfo/metc"/>
        </xsl:call-template>
      </xsl:if>
    <!--subconcepts-->
      <xsl:if test="(count ($data/metainfo/metsi) > 0)">
        <h3>Metadata Security Information</h3>
        <table>
          <tr>
            <td  valign="top">Security Classification System:</td>
            <td>
              <xsl:value-of
                 select="$metaInfoNode/metsi/metscs" />
            </td>
          </tr>
          <tr>
            <td valign="top">Security Classification:</td>
            <td>
              <xsl:value-of
                 select="$metaInfoNode/metsi/metsc"/>
            </td>
          </tr>
          <tr>
            <td valign="top">Security Information:</td>
            <td>
              <xsl:value-of
                select="$metaInfoNode/metsi/metshd"  />
            </td>
          </tr>
        </table>
      </xsl:if>
      <xsl:if test="(count ($metaInfoNode/metextns) > 0)">
        <h3>Metadata Extensions</h3>
        <xsl:for-each select="$metaInfoNode/metextns">
          <table>
            <tr>
              <td style="font-weight:bold;">Extension:</td>
            </tr>
            <tr>
              <td  valign="top">Online Linkage:</td>
              <td>
                <xsl:for-each select="./onlink">
                  <xsl:value-of
                    select="."  />
                  <br/>
                </xsl:for-each>
              </td>
            </tr>
            <tr>
              <td valign="top">Profile Name:</td>
              <td>
                <xsl:value-of
                 select="./metprof"/>
              </td>
            </tr>
          </table>
        </xsl:for-each>
      </xsl:if>
    
      </xsl:if>
  </xsl:template>
  <!--End of Metainfo-->

  <!-- DATAQUAL -->
  <xsl:template name="Dataqual" >
    <xsl:param name="data" />
    <xsl:variable name="dataqualNode"
    select="$data/dataqual" />
    <xsl:if test="count($dataqualNode)>0">
    <h2>Data Quality Information</h2>

    <xsl:call-template name="Attracc">
      <xsl:with-param name="data"
        select="$dataqualNode" />
    </xsl:call-template>
    <table>
      <tr>
        <td valign="top" style="font-weight:bold;">Logical Consistency Report:</td>
        <td>
          <xsl:value-of
            select="$dataqualNode/logic" />
        </td>
      </tr>
      <tr>
        <td valign="top" style="font-weight:bold;">Completeness Report:</td>
        <td>
          <xsl:value-of
             select="$dataqualNode/complete" />
        </td>
      </tr>
    
    
      <xsl:if test="count($dataqualNode/cloud)>0">
      <tr>
        <td  valign="top" style="font-weight:bold;">Cloud Cover:</td>
        <td>
          <xsl:value-of
            select="$dataqualNode/cloud" />
        </td>
      </tr>
      </xsl:if>
    </table>
   
   
    <xsl:call-template name="Posacc">
      <xsl:with-param name="data"
       select="$dataqualNode" />
    </xsl:call-template>

    <xsl:call-template name="Lineage">
      <xsl:with-param name="data"
       select="$dataqualNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template name="Attracc" >
    <xsl:param  name="data" />
    <xsl:variable name="attraccNode"
    select="$data/attracc" />
    <xsl:if test="count($attraccNode)>0">
    <h3>Attribute  Accuracy</h3>
    <table>
      <tr>
        <td valign="top">Accuracy Report:</td>
        <td>
          <xsl:value-of
           select="$attraccNode/attraccr" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Qattracc">
      <xsl:with-param name="data"
       select="$attraccNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Qattracc" >
    <xsl:param  name="data" />
    <xsl:variable name="qattraccNode"
    select="$data/qattracc" />
    <xsl:if test="count($data/qattracc)>0">
    
    	<xsl:for-each select="$qattraccNode">
      <table>
          <tr>
            <td valign="top">Accuracy Value:</td>
            <td>
              <xsl:value-of
                select="./attraccv" />
            </td>
          </tr>
          <tr>
            <td valign="top">Accuracy Explanation:</td>
            <td>
              <xsl:value-of
               select="./attracce" />
            </td>
          </tr>
          <tr>
            <td></td>
          </tr>
       
      </table>
    	</xsl:for-each>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="Posacc" >
    <xsl:param  name="data" />
    <xsl:variable name="posaccNode"
    select="$data/posacc" />
    <xsl:if test="count($posaccNode)>0">
    <h3>Positional Accuracy</h3>
    <xsl:call-template name="Horizpa">
      <xsl:with-param name="data"
       select="$posaccNode" />
    </xsl:call-template>
    <xsl:call-template name="Vertacc">
      <xsl:with-param name="data"
       select="$posaccNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Horizpa" >
    <xsl:param name="data" />
    <xsl:variable  name="horizpaNode"
    select="$data/horizpa" />
    <xsl:if test="count($horizpaNode)>0">
    <table>
      <tr>
        <td valign="top">Horizontal Positional Accuracy Report:</td>
        <td>
          <xsl:value-of
           select="$horizpaNode/horizpar" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Qhorizpa">
      <xsl:with-param name="data"
       select="$horizpaNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Qhorizpa">
    <xsl:param name="data" />
    <xsl:variable name="qhorizpaNode"
     select="$data/qhorizpa" />
    <xsl:if test="count($data/qhorizpa)>0">
      <xsl:for-each select="$qhorizpaNode">
        <table>
        <tr>
          <td valign="top">Horizontal Positional Accuracy Value:</td>
          <td>
            <xsl:value-of
              select="$qhorizpaNode/horizpav" />
          </td>
        </tr>
        <tr>
          <td valign="top">Explanation:</td>
          <td>
            <xsl:value-of
               select="$qhorizpaNode/horizpae" />
          </td>
        </tr>
        <tr>
          <td></td>
        </tr>
        </table>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <xsl:template  name="Vertacc">
    <xsl:param name="data" />
    <xsl:variable name="vertaccNode"
     select="$data/vertacc" />
    <xsl:if test="count($vertaccNode)>0">
    <table>
      <tr>
        <td valign="top">Vertical Positional Accuracy Report:</td>
        <td>
          <xsl:value-of
             select="$vertaccNode/vertaccr" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Qvertpa">
      <xsl:with-param name="data"
       select="$vertaccNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Qvertpa" >
    <xsl:param name="data" />
    <xsl:variable name="qvertpaNode"
   select="$data/qvertpa" />
    <xsl:if test="count($data/qvertpa)>0">
      <xsl:for-each select="$qvertpaNode">
        <table>
        <tr>
          <td valign="top">Vertical Positional  Accuracy Value:</td>
          <td>
            <xsl:value-of
              select="$qvertpaNode/vertaccv" />
          </td>
        </tr>
        <tr>
          <td valign="top">Explanation:</td>
          <td>
            <xsl:value-of
             select="$qvertpaNode/vertacce" />
          </td>
        </tr>
        <tr>
          <td></td>
        </tr>
        </table>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Lineage" >
    <xsl:param name="data" />
    <xsl:variable name="lineageNode"
   select="$data/lineage" />
    <xsl:if test="count($lineageNode)>0">
    <h3>Lineage</h3>
    <xsl:call-template name="Srcinfo">
      <xsl:with-param name="data"
       select="$lineageNode" />
    </xsl:call-template>
    <xsl:call-template name="Procstep">
      <xsl:with-param name="data"
       select="$lineageNode" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Srcinfo">
    <xsl:param name="data" />
    <xsl:variable name="srcinfoNode"
     select="$data/srcinfo" />
    <xsl:if test="count($data/srcinfo)>0">
      <xsl:for-each select="$data/srcinfo">
        <h4>Source Information</h4>
        <xsl:call-template name="Srccite">
          <xsl:with-param name="data"
            select="." />
        </xsl:call-template>
        <table>
          <xsl:if test="count(./srcscale)>0">
          <tr>
            <td  valign="top" >Source Scale Denominator:</td>
            <td>
              <xsl:value-of
                 select="./srcscale" />
            </td>
          </tr>
          </xsl:if>
          <tr>
            <td valign="top" >Type of Source  Media:</td>
            <td>
              <xsl:value-of
                select="./typesrc" />
            </td>
          </tr>
          <tr>
            <td valign="top" >
              Source Citation Abbreviation:</td>
            <td>
            <xsl:value-of
              select="./srccitea" />
            </td>
          </tr>
          <tr>
            <td valign="top" >Source Contribution:</td>
            <td>
              <xsl:value-of
                 select="./srccontr" />
            </td>
          </tr>
         
        </table>
        
        <xsl:call-template name="Srctime">
          <xsl:with-param name="data"
           select="." />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="Srccite" >
    <xsl:param name="data" />
    <xsl:variable name="srcciteNode"
     select="$data/srccite" />
    <xsl:call-template name="Citeinfo">
      <xsl:with-param name="data"
       select="$srcciteNode" />
    </xsl:call-template>
    <!--/xsl:if>-->
  </xsl:template>
 

  <xsl:template name="Srctime">
    <xsl:param name="data" />
    <xsl:variable name="srctimeNode"
   select="$data/srctime" />
    <h4>Source Time  Period of Content</h4> 
    <table>
      <tr>
        <td valign="top">Source Currentness Reference:</td>
        <td>
          <xsl:value-of
            select="$srctimeNode/srccurr" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Timeinfo">
      <xsl:with-param name="data"
       select="$srctimeNode" />
    </xsl:call-template>
  </xsl:template>
  
  <!--TIMEINFO-->
  <xsl:template name="Timeinfo" >
    <xsl:param name="data" />
    <xsl:variable name="timeInfoNode"
     select="$data/timeinfo" />
    <xsl:if test="(count($timeInfoNode) > 0)">
    <table>
        <xsl:variable name="dateCnt"
	 				select="count($timeInfoNode/sngdate)" />
        <xsl:if test="$dateCnt > 0">
          <tr>
            <xsl:if test="$dateCnt = 1">
              <td>Date:</td>
            </xsl:if>
            <xsl:if test="$dateCnt > 1">
              <td valign="top">Dates:</td>
            </xsl:if>
            <td>
              <xsl:for-each
                select="$timeInfoNode/sngdate">
                <xsl:value-of select="caldate" />
                <xsl:if  test="count(time) > 0">
                  <xsl:text> at </xsl:text>
                  <xsl:value-of  select="time" />
                </xsl:if>
                <xsl:if test="position() != last()">
                  <br />
                </xsl:if>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:if>
      
      <xsl:variable name="multipleDateCnt"
        select="count($timeInfoNode/mdattim)" />
      <xsl:if test="$multipleDateCnt > 0">
        <tr>
          <td valign="top">Dates:</td>
          <td>
            <xsl:for-each
              select="$timeInfoNode/mdattim/sngdate">
              <xsl:value-of select="caldate" />
              <xsl:if  test="count(time) > 0">
                <xsl:text> at </xsl:text>
                <xsl:value-of  select="time" />
              </xsl:if>
              <xsl:if test="position() != last()">
                <br />
              </xsl:if>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      
        <xsl:if test="count($timeInfoNode/rngdates) > 0">
          <tr>
            <td valign="top">Date  Range:</td>
            <td>
              <xsl:text>Starting: </xsl:text>
              <xsl:value-of
		 						select="$timeInfoNode/rngdates/begdate" />
              <xsl:if
               test="count($timeInfoNode/rngdates/begtime) >  0">
                <xsl:text> at </xsl:text>
                <xsl:value-of
					 				select="$timeInfoNode/rngdates/begtime" />
              </xsl:if>
              <br />
              <xsl:text>Ending: </xsl:text>
              <xsl:value-of
					 			select="$timeInfoNode/rngdates/enddate" />
              <xsl:if
				 				test="count($timeInfoNode/rngdates/endtime) > 0">
                <xsl:text> at  </xsl:text>
                <xsl:value-of
									 select="$timeInfoNode/rngdates/endtime" />
              </xsl:if>
            </td>
          </tr>
        </xsl:if>
      </table>
    </xsl:if>
  </xsl:template>
  <!--End of TIMEINFO-->
  
  <xsl:template name="Procstep" >
    <xsl:param name="data" />
    <xsl:variable name="procstepNode"
    select="$data/procstep" />
    <xsl:if test="count($procstepNode)>0">
      <xsl:for-each select="$data/procstep"> 
      <table>
        <tr>
        	<td style="font-weight:bold;">Process:</td>
          <td>
            <xsl:value-of
             select="./procdesc" />
          </td>
        </tr>
      
      <xsl:if test="count(./srcused)>0">
        <tr>
          <td  valign="top">Source Used Citation Abbreviation:</td>
          <td>
            <xsl:for-each select="./srcused" >
            <xsl:value-of
              select="." />
              <br/>
          </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      <tr>
        <td valign="top">Process Date:</td>
        <td>
          <xsl:value-of
             select="./procdate" />
        </td>
      </tr>
      <xsl:if test="count(./proctime)>0">
        <tr>
          <td  valign="top">Process Time:</td>
          <td>
            <xsl:value-of
              select="./proctime" />
          </td>
        </tr>
      </xsl:if>
        <xsl:if test="count(./srcprod)>0">
      <tr>
        <td valign="top">Source Produced Citation Abbreviation:</td>
        <td>
          <xsl:for-each select="./srcprod">
          <xsl:value-of
             select="." />
            <br/>
          </xsl:for-each>
        </td>
      </tr>
      </xsl:if>
    </table>
      <xsl:call-template name="Proccont">
      <xsl:with-param name="data"
       select="$procstepNode" />
      
    </xsl:call-template>
    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Proccont" >
    <xsl:param name="data" />
    <xsl:variable name="proccontNode"
    select="$data/proccont" />
    <xsl:if test="count($proccontNode/proccont)>0">
      <h3>Contact person with Processing step Info</h3>
      <xsl:call-template name="Cntinfo">
        <xsl:with-param name="data"
         select="$proccontNode" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Cntinfo"  >
    <xsl:param name="data" />
    <xsl:if test="(count($data/cntinfo) > 0)">
   
      <xsl:variable  name="cntInfoNode"
				select="$data/cntinfo" />
      <table>

        <xsl:choose>
          <xsl:when
            test="count($cntInfoNode/cntperp) = 1">
            <xsl:variable name="cntPerp"
              select="count ($cntInfoNode/cntperp)" />
            <xsl:if test="$cntPerp > 0">
              <xsl:variable name="cntper"
                select="count($cntInfoNode/cntperp/cntper)" />
              <xsl:if test="$cntper > 0">                
                <tr>
                  <td>Primary Contact Person:</td>
                  <td>
                    <xsl:value-of select="$cntInfoNode/cntperp/cntper" />
                  </td>
                </tr>
              </xsl:if>
              <xsl:variable name="cntorg"
                select="count($cntInfoNode/cntperp/cntorg)"  />
              <xsl:if test="$cntorg > 0">
                <tr>
                  <td>Contact Organization:</td>
                  <td>
                    <xsl:value-of select="$cntInfoNode/cntperp/cntorg" />
                  </td>
                </tr>
              </xsl:if>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="cntOrgp"
              select="count($cntInfoNode/cntorgp)" />
            <xsl:if  test="$cntOrgp > 0">
              <xsl:variable name="cntorgPp"
                select="count($cntInfoNode/cntorgp/cntorg)" />
              <xsl:if test="$cntorgPp > 0">
                <tr>
                  <td>Primary Contact Organization:</td>
                  <td>
                    <xsl:value-of select="$cntInfoNode/cntorgp/cntorg" />
                  </td>
                </tr>
              </xsl:if>
              <xsl:variable  name="cntperPp"
                select="count($cntInfoNode/cntorgp/cntper)" />
              <xsl:if test="$cntperPp > 0">
                <tr>
                  <td>Contact Person:</td>
                  <td>
                    <xsl:value-of select="$cntInfoNode/cntorgp/cntper" />
                  </td>
                </tr>
              </xsl:if>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
        
        <xsl:variable name="cntPos"
         select="count($cntInfoNode/cntpos)" />
        <xsl:if test="$cntPos > 0">
          <tr>
            <td>Contact Position:</td>
            <td>
              <xsl:value-of select="$cntInfoNode/cntpos" />
            </td>
          </tr>
         
        </xsl:if>
      </table>
      
        <xsl:for-each
          select="$cntInfoNode/cntaddr">
          <xsl:variable name="cntAddr"
            select="count($cntInfoNode/cntaddr)" />
          <xsl:if test="$cntAddr > 0">
            <xsl:variable name="addrtype"
              select="count($cntInfoNode/cntaddr/addrtype)" />
            <table><tr><td style="font-weight:bold;">Address</td></tr>
            </table>
            <table style="margin-left:20px;">
            <xsl:if  test="$addrtype > 0">

                    <tr>
                      <td>
                  Address Type:</td>
                   <td>
                  <xsl:value-of select="./addrtype" />
                </td>
              </tr>
            </xsl:if>
            <xsl:for-each
              select="./address">
              <xsl:variable  name="address"
                select="count($cntInfoNode/cntaddr/address)" />
              <xsl:if test="$address > 0">
                <tr style="margin-left:20px;">
                  <td>Address:</td>
                  <td>
                    <xsl:value-of select="." />
                  </td>
                </tr>
              </xsl:if>
            </xsl:for-each>
            <xsl:variable name="city"
              select="count (./city)" />
            <xsl:if test="$city > 0">
              <tr style="margin-left:20px;">
                <td>City:</td>
                <td>
                  <xsl:value-of select="./city" />
                </td>
              </tr>
            </xsl:if>
            <xsl:variable name="state"
              select="count(./state)" />
            <xsl:if test="$state > 0">
              <tr style="margin-left:20px;">
                <td>State:</td>
                <td>
                  <xsl:value-of select="./state" />
                </td>
              </tr>
            </xsl:if>
            <xsl:variable  name="postal"
              select="count(./postal)" />
            <xsl:if test="$postal > 0">
              <tr style="margin-left:20px;">
                <td>Postal Code:</td>
                <td>
                  <xsl:value-of select="./postal" />
                </td>
              </tr>
            </xsl:if>
            <xsl:variable name="country"
              select="count (./country)" />
            <xsl:if test="$country > 0">
              <tr style="margin-left:20px;">
                <td >Country:</td>
                <td>
                  <xsl:value-of select="./country" />
                </td>
              </tr>
            </xsl:if>
            </table>
          </xsl:if>
        </xsl:for-each>
        
          <xsl:variable name="cntvoice"
            select="count($cntInfoNode/cntvoice)" />
          <xsl:if test="$cntvoice > 0">
          <tr>
            <td>Contact Voice Telephone:</td>
            <td>
              <xsl:for-each
              select="$cntInfoNode/cntvoice">
              <xsl:value-of select="." />
                <br/>
              </xsl:for-each>
            </td>
          </tr>
          </xsl:if>
        
     <table>
        <xsl:variable name="cnttdd"
        select="count($cntInfoNode/cnttdd)" />
        <xsl:if test="$cnttdd > 0">
          <tr>
            <td>Contact TDD/TTY Telephone:</td>
            <td>
              <xsl:for-each
          select="$cntInfoNode/cnttdd">
              <xsl:value-of select="." />
                <br/>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:if>
       
        <xsl:variable  name="cntfax"
               select="count($cntInfoNode/cntfax)" />
        <xsl:if test="$cntfax > 0">
        <tr>
          <td>Contact Facsimile Telephone:</td>
          <td>
        <xsl:for-each
            select="$cntInfoNode/cntfax">
              <xsl:value-of select="$cntInfoNode/cntfax" />
          <br/>
        </xsl:for-each>
            </td>
        </tr>
        </xsl:if>
         
        <xsl:variable name="cntemail"
        select="count($cntInfoNode/cntemail)" />
        <xsl:if test="$cntemail > 0">
          <tr>
            <td>Contact Electronic Mail Address:</td>
            <td>
              <xsl:for-each
            select="$cntInfoNode/cntemail">
              <xsl:value-of select="." />
                <br/>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:if>
         
        <xsl:variable name="hours"
         select="count($cntInfoNode/hours)" />
        <xsl:if test="$hours > 0">
          <tr>
            <td>Hours of Service:</td>
            <td>
              <xsl:value-of select="$cntInfoNode/hours" />
            </td>
          </tr>
        </xsl:if>
        <xsl:variable name="cntinst"
        select="count($cntInfoNode/cntinst)" />
        <xsl:if  test="$cntinst > 0">
          <tr>
            <td>Contact Instructions:</td>
            <td>
              <xsl:value-of  select="$cntInfoNode/cntinst" />
            </td>
          </tr>
        </xsl:if>
      </table>
    </xsl:if>
  </xsl:template>
  <!--End of Dataqual-->

  <!--SPDOINFO-->
  <xsl:template name="Spdoinfo"  >
    <xsl:param name="data"  />
    <xsl:variable name="spdoinfoNode"
    select="$data/spdoinfo" />
    <xsl:if test="count($spdoinfoNode)>0">
    <h2>
      Spatial Data Organization Information
    </h2>
    <table>
      <xsl:if test="count($spdoinfoNode/indspref)>0">
      <tr>
        <td valign="top">Indirect Spatial Reference:</td>
        <td>
          <xsl:value-of
             select="$spdoinfoNode/indspref" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="count($spdoinfoNode/direct)>0">
      <tr>
        <td valign="top">Direct Spatial Reference Method:</td>
        <td>
          <xsl:value-of
            select="$spdoinfoNode/direct" />
        </td>
      </tr>
      </xsl:if>
    </table>

    <xsl:choose>
      <xsl:when
       test="count($spdoinfoNode/ptvctinf) = 1">
        
        <xsl:call-template name="Ptvctinf">
          <xsl:with-param name="data"
           select="$spdoinfoNode" />
        </xsl:call-template>
      
      </xsl:when>
      <xsl:otherwise>

        <xsl:call-template name="Rastinfo">
          <xsl:with-param name="data"
           select="$spdoinfoNode" />
        </xsl:call-template>

      </xsl:otherwise>
    </xsl:choose>

    </xsl:if>
  </xsl:template>
  <xsl:template name="Ptvctinf" >
    <xsl:param name="data"  />
    <xsl:variable name="ptvctinfNode"
    select="$data/ptvctinf" />
    <xsl:if test="count($ptvctinfNode)>0">
    <h3>Point and Vector Object Information</h3>
    <xsl:choose>
      <xsl:when
       test="count($ptvctinfNode/sdtsterm) = 1">

        <xsl:call-template name="Sdtsterm">
          <xsl:with-param name="data"
           select="$ptvctinfNode" />
        </xsl:call-template>
        
      </xsl:when>
      <xsl:otherwise>

        <xsl:call-template name="Vpfterm">
          <xsl:with-param name="data"
           select="$ptvctinfNode" />
        </xsl:call-template>

      </xsl:otherwise>
    </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Sdtsterm" >
    <xsl:param name="data" />
    <xsl:variable  name="sdtstermNode"
    select="$data/sdtsterm" />
    <xsl:if test="count($sdtstermNode)>0">
    <xsl:for-each select="$sdtstermNode">
    <table> 
      <tr>
        <td valign="top" style="font-weight:bold;">SDTS Point and Vector Object:</td>
        <td>
          <xsl:value-of
             select="./sdtstype" />
        </td>
      </tr>
      <xsl:if test="count(./ptvctcnt)>0">
      <tr>
        <td valign="top">Point and Vector Object Count:</td>
        <td>
          <xsl:value-of
            select="./ptvctcnt" />
        </td>
      </tr>
      </xsl:if>
    </table>
    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template name="Vpfterm">
    <xsl:param name="data" />
    <xsl:variable name="vpftermNode"
   select="$data/vpfterm" />
    <xsl:if test="count($vpftermNode)>0">
  
    <xsl:call-template name="Vpfinfo">
      <xsl:with-param name="data"
       select="$vpftermNode" />
    </xsl:call-template>
      <table>
        <tr>
          <td valign="top">VPF Topology Level:</td>
          <td>
            <xsl:value-of
              select="$vpftermNode/vpflevel" />
          </td>
        </tr>
      </table>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Vpfinfo" >
    <xsl:param name="data" />
    <xsl:variable name="vpfinfoNode"
     select="$data/vpfinfo" />
    <xsl:if test="count($vpfinfoNode)>0">
  
    	<xsl:for-each select="$vpfinfoNode">
	   
	    <table>
	      
	      <tr>
	        <td  valign="top" style="font-weight:bold;">VPF Point and Vector Object:</td>
	        <td>
	          <xsl:value-of
	            select="./vpftype" />
	        </td>
	      </tr>
	      <xsl:if test="count(./ptvctcnt)>0">
	      <tr>
	        <td valign="top">Point and Vector Object Count:</td>
	        <td>
	          <xsl:value-of
	             select="./ptvctcnt" />
	        </td>
	      </tr>
	      </xsl:if>
	    </table>
    </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <xsl:template  name="Rastinfo" >
    <xsl:param name="data" />
    <xsl:variable name="rastinfoNode"
     select="$data/rastinfo" />
    <xsl:if test="count($rastinfoNode)>0">
    <h3>Raster Object Information</h3>
    <table>
      <tr>
        <td  valign="top">Raster Object Type:</td>
        <td>
          <xsl:value-of
            select="$rastinfoNode/rasttype" />
        </td>
      </tr>
      <xsl:if test="count($rastinfoNode/rowcount)>0">
      <tr>
        <td valign="top">Row Count:</td>
        <td>
          <xsl:value-of
             select="$rastinfoNode/rowcount" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="count($rastinfoNode/colcount)>0">
      <tr>
        <td  valign="top">Column Count:</td>
        <td>
          <xsl:value-of
            select="$rastinfoNode/colcount" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="count($rastinfoNode/vrtcount)>0">
      <tr>
        <td valign="top">Vertical Count:</td>
        <td>
          <xsl:value-of
             select="$rastinfoNode/vrtcount" />
        </td>
      </tr>
      </xsl:if>
    </table>
    </xsl:if>
  </xsl:template>
  <!--End of  SPDOINFO-->


  <!--SPREF-->
  <xsl:template name="Spref"  >
    <xsl:param name="data"  />
    <xsl:if test="count($data/spref)>0">
    <h2>Spatial Reference Information</h2>
    <xsl:choose>
      <xsl:when
       test="count($data/spref/horizsys/geograph) = 1">

        <xsl:call-template name="Geograph">
          <xsl:with-param name="data"
           select="$data/spref/horizsys" />
        </xsl:call-template>

      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when
           test="count($data/spref/horizsys/planar) >0">

            <xsl:call-template name="Planar">
              <xsl:with-param name="data"
              select="$data/spref/horizsys" />
            </xsl:call-template>

          </xsl:when>
          <xsl:otherwise>

            <xsl:call-template name="Local">
              <xsl:with-param name="data"
              select="$data/spref/horizsys" />
            </xsl:call-template>

          </xsl:otherwise>
        </xsl:choose>

      </xsl:otherwise>
    </xsl:choose>

    <xsl:call-template name="Geodetic">
      <xsl:with-param name="data"
      select="$data/spref/horizsys" />
    </xsl:call-template>
    <xsl:call-template name="Vertdef">
      <xsl:with-param name="data"
      select="$data/spref" />
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <!-- GEODETIC -->
  <xsl:template name="Geodetic"  >
    <xsl:param name="data"  />
    <xsl:variable name="geodeticNode"
                  select="$data/geodetic" />
    <xsl:if test="count($geodeticNode)>0">
    <h3>Geodetic Model</h3>
    <table>
      <xsl:if test="count($geodeticNode/horizdn)>0">
      <tr>
        <td valign="top">Horizontal Datum Name:</td>
        <td>
          <xsl:value-of
             select="$geodeticNode/horizdn" />
        </td>
      </tr>
      </xsl:if>
      <tr>
        <td  valign="top">Ellipsoid Name:</td>
        <td>
          <xsl:value-of
            select="$geodeticNode/ellips" />
        </td>
      </tr>
      <tr>
        <td valign="top">Semi-major Axis:</td>
        <td>
          <xsl:value-of
             select="$geodeticNode/semiaxis" />
        </td>
      </tr>
      <tr>
        <td valign="top">Denominator of Flattening Ratio:</td>
        <td>
          <xsl:value-of
            select="$geodeticNode/denflat" />
        </td>
      </tr>
    </table>
   </xsl:if>
  </xsl:template>
  <!-- GEOGRAPH -->
  <xsl:template name="Geograph"  >
    <xsl:param name="data" />
    <xsl:variable name="geographNode"
                 select="$data/geograph" />
    <h3> Geographic position on Earth</h3>
    <table>
      <tr>
        <td valign="top">Latitude Resolution:</td>
        <td>
          <xsl:value-of
             select="$geographNode/latres" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude Resolution:</td>
        <td>
          <xsl:value-of
            select="$geographNode/longres" />
        </td>
      </tr>
      <tr>
        <td valign="top">Coordinate Units:</td>
        <td>
          <xsl:value-of
             select="$geographNode/geogunit" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <!-- PLANAR -->
  <xsl:template name="Planar"  >
    <xsl:param name="data" />
    <xsl:variable name="planarNode"
                   select="$data/planar" />
    <xsl:for-each select="$planarNode">
    <xsl:call-template name="Planci">
      <xsl:with-param name="data"
       select="." />
    </xsl:call-template>
    <xsl:choose>
      <xsl:when test="count(mapproj)=1">
        <xsl:call-template name="Mapproj">
          <xsl:with-param name="data"
           select="." />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="count(gridsys)=1">
            <xsl:call-template name="Gridsys">
              <xsl:with-param name="data"
               select="." />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
             <xsl:call-template name="Localp">
              <xsl:with-param name="data"
               select="." />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each> 
  </xsl:template>
  <!-- Planar Coordinate Information -->
  <xsl:template name="Planci">
    <xsl:param name="data" />
    <xsl:variable name="planciNode"
                   select="$data/planci" />
    <h3>Planar Coordinate Information</h3>
    <table>
      <tr>
        <td valign="top">Encoding Method:</td>
        <td>
          <xsl:value-of
            select="$planciNode/plance" />
        </td>
      </tr>
      <tr>
        <td valign="top">Planar Distance Units:</td>
        <td>
          <xsl:value-of
             select="$planciNode/plandu" />
        </td>
      </tr>
    </table>
    <xsl:choose>
      <xsl:when test="count($planciNode/coordrep)=1">
        <xsl:call-template name="Coordrep">
          <xsl:with-param name="data"
           select="$planciNode" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="Distbrep">
          <xsl:with-param name="data"
           select="$planciNode" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>   
 </xsl:template>
  <xsl:template name="Coordrep" >
    <xsl:param name="data" />
    <xsl:variable  name="coordrepNode"
                  select="$data/coordrep" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Coordinate Representation</td>
      </tr>
      <tr>
        <td valign="top">Abscissa Resolution:</td>
        <td>
          <xsl:value-of
             select="$coordrepNode/absres" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Ordinate Resolution:</td>
        <td>
          <xsl:value-of
            select="$coordrepNode/ordres" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Distbrep" >
    <xsl:param  name="data" />
    <xsl:variable name="distbrepNode"
                   select="$data/distbrep" />
    
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Distance and Bearing Representation</td>
      </tr>
      <tr>
        <td valign="top">Distance Resolution:</td>
        <td>
          <xsl:value-of
             select="$distbrepNode/distres" />
        </td>
      </tr>
      <tr>
        <td valign="top">Bearing Resolution:</td>
        <td>
          <xsl:value-of
            select="$distbrepNode/bearres" />
        </td>
      </tr>
      <tr>
        <td valign="top">Bearing Units:</td>
        <td>
          <xsl:value-of
             select="$distbrepNode/bearunit" />
        </td>
      </tr>
      <tr>
        <td valign="top">Bearing Reference Direction:</td>
        <td>
          <xsl:value-of
            select="$distbrepNode/bearrefd" />
        </td>
      </tr>
      <tr>
        <td valign="top">Bearing Reference Meridian:</td>
        <td>
          <xsl:value-of
             select="$distbrepNode/bearrefm" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <!-- Map Projection -->
  <xsl:template  name="Mapproj">
    <xsl:param name="data" />
    <xsl:variable name="mapprojNode"
                   select="$data/mapproj" />
    <xsl:if test="count($mapprojNode)>0" >
      <table>
        <tr>
          <td style="font-weight:bold;">Map Projection</td>
        </tr>
        <tr>
          <td valign="top">Name:</td>
          <td>
            <xsl:value-of
              select="$mapprojNode/mapprojn"  />
          </td>
        </tr>
      </table>
      <xsl:choose>
        <!--1-->
        <xsl:when test="count($mapprojNode/albers)=1">
          <xsl:call-template name="Albers">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--2-->
        <xsl:when test="count($mapprojNode/azimequi)=1">
          <xsl:call-template name="Azimequi">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
       <!--3-->
      <xsl:when test="count($mapprojNode/equicon)=1">
        <xsl:call-template name="Equicon">
          <xsl:with-param name="data"
           select="$mapprojNode" />
        </xsl:call-template> 
      </xsl:when>
        <!--4-->
       <xsl:when test="count($mapprojNode/equirect)=1">
         <xsl:call-template name="Equirect">
           <xsl:with-param name="data"
            select="$mapprojNode" />
         </xsl:call-template>
      </xsl:when>
      <!--5-->
      <xsl:when test="count($mapprojNode/gvnsp)=1">
        <xsl:call-template name="Gvnsp">
          <xsl:with-param name="data"
           select="$mapprojNode" />
        </xsl:call-template>
      </xsl:when>
       <!--6-->
        <xsl:when test="count($mapprojNode/gnomonic)=1">
          <xsl:call-template name="Gnomonic">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--7-->
        <xsl:when test="count($mapprojNode/lamberta)=1">
          <xsl:call-template name="Lamberta">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--8-->
        <xsl:when test="count($mapprojNode/lambertc)=1">
          <xsl:call-template name="Lambertc">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--9-->
        <xsl:when test="count($mapprojNode/mercator)=1">
          <xsl:call-template name="Mercator">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--10-->
        <xsl:when test="count($mapprojNode/modsak)=1">
          <xsl:call-template name="Modsak">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--11-->
        <xsl:when test="count($mapprojNode/miller)=1">
          <xsl:call-template name="Miller">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--12-->
        <xsl:when test="count($mapprojNode/obqmerc)=1">
          <xsl:call-template name="Obqmerc">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--13-->
        <xsl:when test="count($mapprojNode/orthogr)=1">
          <xsl:call-template name="Orthogr">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--14-->
        <xsl:when test="count($mapprojNode/polarst)=1">
          <xsl:call-template name="Polarst">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--15-->
        <xsl:when test="count($mapprojNode/polycon)=1">
          <xsl:call-template name="Polycon">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>

        </xsl:when>
        <!--16-->
        <xsl:when test="count($mapprojNode/robinson)=1">
          <xsl:call-template name="Robinson">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>

        </xsl:when>
        <!--17-->
        <xsl:when test="count($mapprojNode/sinusoid)=1">
          <xsl:call-template name="Sinusoid">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>

        </xsl:when>
        <!--18-->
        <xsl:when test="count($mapprojNode/spaceobq)=1">
          <xsl:call-template name="Spaceobq">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>

        </xsl:when>
        <!--19-->
        <xsl:when test="count($mapprojNode/stereo)=1">
          <xsl:call-template name="Stereo">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>

        </xsl:when>
        <!--20-->
        <xsl:when test="count($mapprojNode/mapprojp)=1">
          <xsl:call-template name="Mapprojp">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--21-->
        <xsl:when test="count($mapprojNode/transmer)=1">
          <xsl:call-template name="transmer">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
        <!--22-->
        <xsl:when test="count($mapprojNode/vdgrin)=1">
          <xsl:call-template name="Vdgrin">
            <xsl:with-param name="data"
             select="$mapprojNode" />
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
   </xsl:template>
  <xsl:template name="Albers">
    <xsl:param name="data" />
    <xsl:variable name="albersNode"
                   select="$data/albers" />
   
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold">Albers Conical Equal Area</td>
      </tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
             select="$albersNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$albersNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$albersNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$albersNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$albersNode/fnorth" />
        </td>
      </tr>
    </table>


  </xsl:template>
  <xsl:template name="Azimequi">
    <xsl:param name="data" />
    <xsl:variable name="azimequiNode"
                 select="$data/azimequi" />
    
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Azimuthal Equidistant</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
           select="$azimequiNode/longcm" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$azimequiNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$azimequiNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$azimequiNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Equicon" >
    <xsl:param name="data" />
    <xsl:variable  name="equiconNode"
                  select="$data/equicon" />
    
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Equidistant Conic Projection</td></tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
            select="$equiconNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$equiconNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$equiconNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$equiconNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$equiconNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Equirect">
    <xsl:param name="data" />
    <xsl:variable name="equirectNode"
                   select="$data/equirect" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Equirectangular Projection</td></tr>
      
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
             select="$equirectNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$equirectNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$equirectNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$equirectNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="Gvnsp">
    <xsl:param name="data" />
    <xsl:variable name="gvnspNode"
                   select="$data/gvnsp" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">General Vertical Near-sided Perspective</td></tr>
      
      <tr>
        <td valign="top">Height of Perspective Point Above Surface:</td>
        <td>
          <xsl:value-of
             select="$gvnspNode/heightpt" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$gvnspNode/longpc" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$gvnspNode/latprjc" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$gvnspNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$gvnspNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Gnomonic" >
    <xsl:param name="data" />
    <xsl:variable  name="gnomonicNode"
                  select="$data/gnomonic" />
    <table style="margin-left:30px;">
    
      <tr>
        <td style="font-weight:bold;">Gnomonic parameters</td></tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
             select="$gnomonicNode/longpc" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Latitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$gnomonicNode/latprjc" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$gnomonicNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$gnomonicNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Lamberta" >
    <xsl:param name="data" />
    <xsl:variable  name="lambertaNode"
                  select="$data/lamberta" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Lambert Azimuthal Equal Area</td></tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
             select="$lambertaNode/longpc" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Latitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$lambertaNode/latprjc" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$lambertaNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$lambertaNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Lambertc">

    <xsl:param name="data" />
    <xsl:variable name="lambertcNode"
                 select="$data/lambertc" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Lambert Conformal Conic</td></tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
           select="$lambertcNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$lambertcNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$lambertcNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$lambertcNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$lambertcNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Mercator" >
    <xsl:param name="data" />
    <xsl:variable name="mercatorNode"
                   select="$data/mercator" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Mercator projection Parameters</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$mercatorNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$mercatorNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$mercatorNode/fnorth" />
        </td>
      </tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
             select="$mercatorNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Scale Factor at Equator:</td>
        <td>
          <xsl:value-of
            select="$mercatorNode/sfequat" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Modsak" >
    <xsl:param  name="data" />
    <xsl:variable name="modsakNode"
                   select="$data/modsak" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Modified Stereographic for Alaska</td></tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$modsakNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$modsakNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="Miller" >
    <xsl:param name="data" />
    <xsl:variable name="millerNode"
                   select="$data/miller" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Miller Cylindrical</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$millerNode/longcm" />
        </td>
      </tr>

      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$millerNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$millerNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Obqmerc" >
    <xsl:param  name="data" />
    <xsl:variable name="obqmercNode"
                   select="$data/obqmerc" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Oblique Mercator</td></tr>
      <tr>
        <td valign="top">Scale Factor at Center Line:</td>
        <td>
          <xsl:value-of
             select="$obqmercNode/sfctrlin" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$obqmercNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$obqmercNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$obqmercNode/fnorth" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Obqlazim">
      <xsl:with-param name="data"
       select="$obqmercNode" />
    </xsl:call-template>
    <xsl:call-template name="Obqlpt">
      <xsl:with-param name="data"
       select="$obqmercNode" />
    </xsl:call-template>

  </xsl:template>
  <xsl:template name="Obqlazim" >
    <xsl:param name="data" />
    <xsl:variable  name="obqlazimNode"
                  select="$data/obqlazim" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Oblique Line Azimuth</td></tr>
      <tr>
        <td valign="top">Azimuthal Angle:</td>
        <td>
          <xsl:value-of
            select="$obqlazimNode/azimangl" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Azimuth Measure Point Longitude:</td>
        <td>
          <xsl:value-of
            select="$obqlazimNode/azimptl" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Obqlpt">
    <xsl:param  name="data" />
    <xsl:variable name="obqlptNode"
                   select="$data/obqlpt" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Oblique Line Point</td></tr>
      <tr>
        <td valign="top">Oblique Line Latitude:</td>
        <td>
          <xsl:value-of
            select="$obqlptNode/obqllat"  />
        </td>
      </tr>
      <tr>
        <td valign="top">Oblique Line Longitude:</td>
        <td>
          <xsl:value-of
             select="$obqlptNode/obqllong" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Orthogr">
    <xsl:param name="data" />
    <xsl:variable name="orthogrNode"
                   select="$data/orthogr" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Orthographic Parameters</td></tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
             select="$orthogrNode/longpc" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$orthogrNode/latprjc" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$orthogrNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$orthogrNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="Polarst">
    <xsl:param name="data" />
    <xsl:variable name="polarstNode"
                   select="$data/polarst" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Polar Stereographic</td></tr>
      <tr>
        <td valign="top">Straight Vertical Longitude from Pole:</td>
        <td>
          <xsl:value-of
             select="$polarstNode/svlong" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$polarstNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$polarstNode/fnorth" />
        </td>
      </tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
             select="$polarstNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Scale Factor at Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$polarstNode/sfprjorg" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Polycon">
    <xsl:param  name="data" />
    <xsl:variable name="polyconNode"
                   select="$data/polycon" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Polyconic Parameters</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$polyconNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$polyconNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$polyconNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$polyconNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="Robinson" >
    <xsl:param name="data" />
    <xsl:variable name="robinsonNode"
                   select="$data/robinson" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Robinson Parameters</td></tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
             select="$robinsonNode/longpc" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$robinsonNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$robinsonNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Sinusoid">
    <xsl:param name="data" />
    <xsl:variable name="sinusoidNode"
                   select="$data/sinusoid" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Sinusoidal Parameters</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$sinusoidNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$sinusoidNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$sinusoidNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Spaceobq">
    <xsl:param name="data" />
    <xsl:variable name="spaceobqNode"
                   select="$data/spaceobq" />
     <table style="margin-left:30px;">
       <tr>
         <td style="font-weight:bold;">Space Oblique Mercator (Landsat)</td></tr>
       <tr>
        <td valign="top">Landsat Number:</td>
        <td>
          <xsl:value-of
             select="$spaceobqNode/landsat" />
        </td>
      </tr>
      <tr>
        <td valign="top">Path Number:</td>
        <td>
          <xsl:value-of
            select="$spaceobqNode/pathnum" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$spaceobqNode/feast"  />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$spaceobqNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="Stereo">
    <xsl:param name="data" />
    <xsl:variable name="stereoNode"
                   select="$data/stereo" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Stereographic Projection</td></tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
             select="$stereoNode/longpc" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$stereoNode/latprjc" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$stereoNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$stereoNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="transmer" >
    <xsl:param name="data" />
    <xsl:variable name="transmerNode"
                   select="$data/transmer" />
     <table style="margin-left:30px;">
       <tr>
         <td style="font-weight:bold;">Transverse Mercator</td></tr>
       <tr>
        <td valign="top">Scale Factor at Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$transmerNode/sfctrmer" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$transmerNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Vdgrin" >
    <xsl:param name="data" />
    <xsl:variable name="vdgrinNode"
                   select="$data/vdgrin" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Lambert Conformal Conic</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$vdgrinNode/longcm"  />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$vdgrinNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$vdgrinNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Mapprojp">
    <xsl:param name="data" />
    <xsl:variable  name="mapprojpNode"
                  select="$data/mapprojp" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Map Projection Parameters</td></tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/fnorth" />
        </td>
      </tr>
      <tr>
        <td valign="top">Scale Factor at Equator:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/sfequat" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Height of Perspective Point Above Surface:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/heightpt" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Projection Center:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/longpc" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Center:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/latprjc" />
        </td>
      </tr>
      <tr>
        <td valign="top">Scale Factor at Center Line:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/sfctrlin" />
        </td>
      </tr>

      <xsl:call-template name="ObqlazimD">
        <xsl:with-param name="data"
         select="$mapprojpNode" />
      </xsl:call-template>
      <xsl:call-template name="ObqlptD">
      <xsl:with-param name="data"
       select="$mapprojpNode" />
    </xsl:call-template>
    
      <tr>
        <td  valign="top">Straight Vertical Longitude from Pole:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/svlong" />
        </td>
      </tr>
      <tr>
        <td valign="top">Scale Factor at Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/sfprjorg" />
        </td>
      </tr>
      <tr>
        <td valign="top">Landsat Number:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/landsat" />
        </td>
      </tr>
      <tr>
        <td valign="top">Path Number:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/pathnum" />
        </td>
      </tr>
      <tr>
        <td valign="top">Scale Factor at Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$mapprojpNode/sfctrmer" />
        </td>
      </tr>
      <tr>
        <td valign="top">Other Projection's Definition:</td>
        <td>
          <xsl:value-of
             select="$mapprojpNode/otherprj" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template  name="ObqlazimD" >
    <xsl:param name="data" />
    <xsl:variable name="obqlazimNode"
                   select="$data/obqlazim" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Oblique Line Azimuth</td></tr>
      <tr>
        <td valign="top">Azimuthal Angle:</td>
        <td>
          <xsl:value-of
             select="$obqlazimNode/azimangl" />
        </td>
      </tr>
      <tr>
        <td valign="top">Azimuth Measure Point Longitude:</td>
        <td>
          <xsl:value-of
            select="$obqlazimNode/azimptl" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="ObqlptD" >
    <xsl:param name="data" />
    <xsl:variable name="obqlptNode"
                 select="$data/obqlpt" />
    <table style="margin-left:30px;">
      <tr>
        <td style="font-weight:bold;">Oblique Line Point</td></tr>
      <tr>
        <td valign="top">Oblique Line Latitude:</td>
        <td>
          <xsl:value-of
           select="$obqlptNode/obqllat" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Oblique Line Longitude:</td>
        <td>
          <xsl:value-of
            select="$obqlptNode/obqllong" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <!-- Grid Coordinate System -->
  <xsl:template name="Gridsys" >
    <xsl:param  name="data" />
    <xsl:variable name="gridsysNode"
                   select="$data/gridsys" />
    <table>
      <tr>
        <td style="font-weight:bold;">Grid Coordinate System</td></tr>
      <tr>
        <td valign="top">Name:</td>
        <td>
          <xsl:value-of
             select="$gridsysNode/gridsysn" />
        </td>
      </tr>

    </table>

    <xsl:choose>
      <xsl:when test="count($gridsysNode/othergrd)=1">
        <table>
          <tr>
            <td valign="top">Other Grid System's Definition:</td>
            <td>
              <xsl:value-of
                select="$gridsysNode/othergrd" />
            </td>
          </tr>
        </table>
      </xsl:when>
      <xsl:when test="count($gridsysNode/utm)=1">
        <xsl:call-template name="Utm">
          <xsl:with-param name="data"
           select="$gridsysNode" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($gridsysNode/ups)=1">
        <xsl:call-template name="Ups">
          <xsl:with-param name="data"
           select="$gridsysNode" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($gridsysNode/spcs)=1">
        <xsl:call-template name="Spcs">
          <xsl:with-param name="data"
           select="$gridsysNode" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($gridsysNode/arcsys)=1">
        <xsl:call-template name="Arcsys">
          <xsl:with-param name="data"
           select="$gridsysNode" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
 </xsl:template>
  <xsl:template name="Utm" >
    <xsl:param  name="data" />
    <xsl:variable name="utmNode"
                   select="$data/utm" />
    <table>
      <tr>
        <td style="font-weight:bold;"> Universal Transverse Mercator (UTM)</td></tr>
      <tr>
        <td valign="top">UTM Zone Number:</td>
        <td>
          <xsl:value-of
            select="$utmNode/utmzone" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="TransmerD">
      <xsl:with-param name="data"
       select="$utmNode" />
    </xsl:call-template>
  </xsl:template>
  <xsl:template name="TransmerD" >
    <xsl:param name="data" />
    <xsl:variable name="transmerNode"
                   select="$data/transmer" />
    <table>
      <tr>
        <td>Transverse Mercator</td></tr>
      <tr>
        <td valign="top">Scale Factor at Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$transmerNode/sfctrmer" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$transmerNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Ups" >
    <xsl:param  name="data" />
    <xsl:variable name="upsNode"
                   select="$data/ups" />
    <table>
      <tr>
        <td style="font-weight:bold;"> Universal Polar Stereographic (UPS)</td></tr>
      <tr>
        <td valign="top">UPS Zone Identifier:</td>
        <td>
          <xsl:value-of
            select="$upsNode/upszone" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Polarst_ups">
      <xsl:with-param name="data"
       select="$upsNode" />
    </xsl:call-template>
  </xsl:template>
  <xsl:template name="Polarst_ups">
    <xsl:param name="data" />
    <xsl:variable name="polarstNode"
                   select="$data/polarst" />
    <table>
      <tr>
        <td>Polar Stereographic</td></tr>
      <tr>
        <td valign="top">Straight Vertical Longitude from Pole:</td>
        <td>
          <xsl:value-of
             select="$polarstNode/svlong" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$polarstNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$polarstNode/fnorth" />
        </td>
      </tr>
      <xsl:if test="count($polarstNode/stdparll)>0">
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
             select="$polarstNode/stdparll" />
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="count($polarstNode/sfprjorg)>0">
      <tr>
        <td  valign="top">Scale Factor at Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$polarstNode/sfprjorg" />
        </td>
      </tr>
      </xsl:if>
    </table>
  </xsl:template>
  <xsl:template name="Spcs" >
    <xsl:param  name="data" />
    <xsl:variable name="spcsNode"
                   select="$data/spcs" />
    <table>
      <tr>
        <td style="font-weight:bold;">State Plane Coordinate System (SPCS)</td></tr>
      <tr>
        <td valign="top">SPCS Zone Identifier:</td>
        <td>
          <xsl:value-of
            select="$spcsNode/spcszone"  />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Lambertc_spcs">
      <xsl:with-param name="data"
       select="$spcsNode" />
    </xsl:call-template>
    <xsl:call-template name="Transmer_spcs">
      <xsl:with-param name="data"
       select="$spcsNode" />
    </xsl:call-template>
    <xsl:call-template name="Obqmerc_spcs">
      <xsl:with-param name="data"
       select="$spcsNode" />
    </xsl:call-template>
    <xsl:call-template name="Polycon_spcs">
      <xsl:with-param name="data"
       select="$spcsNode" />
    </xsl:call-template>
  </xsl:template>
  <xsl:template  name="Lambertc_spcs" >
    <xsl:param name="data" />
    <xsl:variable name="lambertcNode"
                   select="$data/lambertc" />
    <table>
      <tr>
        <td>Lambert Conformal Conic</td></tr>
      <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
             select="$lambertcNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$lambertcNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$lambertcNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$lambertcNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$lambertcNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Transmer_spcs">
    <xsl:param  name="data" />
    <xsl:variable name="transmerNode"
      select="$data/transmer" />
    <table>
      <tr>
        <td>Transverse Mercator</td></tr>
      <tr>
        <td valign="top">Scale Factor at Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$transmerNode/sfctrmer" />
        </td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
             select="$transmerNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$transmerNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Obqmerc_spcs">
    <xsl:param  name="data" />
    <xsl:variable name="obqmercNode"
      select="$data/obqmerc" />
    <table>
      <tr>
        <td>Oblique Mercator</td></tr>
      <tr>
        <td valign="top">Scale Factor at Center Line:</td>
        <td>
          <xsl:value-of
             select="$obqmercNode/sfctrlin" />
        </td>
      </tr>
      <tr>
        <td valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$obqmercNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
            select="$obqmercNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
             select="$obqmercNode/fnorth" />
        </td>
      </tr>
    </table>
    <xsl:call-template name="Obqlazim_obqmerc_spcs">
      <xsl:with-param name="data"
       select="$obqmercNode" />
    </xsl:call-template>
    <xsl:call-template name="Obqlpt_obqmerc_spcs">
      <xsl:with-param name="data"
       select="$obqmercNode" />
    </xsl:call-template>
  </xsl:template>
  <xsl:template name="Obqlazim_obqmerc_spcs">
    <xsl:param name="data" />
    <xsl:variable name="obqlazimNode"
                 select="$data/obqlazim" />
    <xsl:if test="count($obqlazimNode)>0">
    <table>
      <tr>
        <td>Oblique Line Azimuth</td></tr>
      <tr>
        <td valign="top">Azimuthal Angle:</td>
        <td>
          <xsl:value-of
           select="$obqlazimNode/azimangl" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Azimuth Measure Point Longitude:</td>
        <td>
          <xsl:value-of
            select="$obqlazimNode/azimptl" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Obqlpt_obqmerc_spcs">
    <xsl:param name="data" />
    <xsl:variable name="obqlptNode"
                   select="$data/obqlpt" />
    <xsl:if test="count($obqlptNode)>0">
    <table>
      <tr>
        <td>Oblique Line Point</td></tr>
      <tr>
        <td valign="top">Oblique Line Latitude:</td>
        <td>
          <xsl:value-of
             select="$obqlptNode/obqllat" />
        </td>
      </tr>
      <tr>
        <td valign="top">Oblique Line Longitude:</td>
        <td>
          <xsl:value-of
            select="$obqlptNode/obqllong" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <xsl:template name="Polycon_spcs">
    <xsl:param name="data" />
    <xsl:variable  name="polyconNode"
                  select="$data/polycon" />
    <table>
      <tr>
        <td>Polyconic Projection</td></tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
           select="$polyconNode/longcm" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$polyconNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$polyconNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$polyconNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Arcsys" >
    <xsl:param name="data" />
    <xsl:variable  name="arcsysNode"
                  select="$data/arcsys" />
    <table>
      <tr>
        <td style="font-weight:bold;">ARC Coordinate System</td></tr>
      <tr>
        <td valign="top">ARC System Zone Identifier:</td>
        <td>
          <xsl:value-of
             select="$arcsysNode/arczone" />
        </td>
      </tr>

    </table>
    <xsl:call-template name="Equirect_arcsys">
      <xsl:with-param name="data"
       select="$arcsysNode" />
    </xsl:call-template>
    <xsl:call-template name="Azimequi_arcsys">
      <xsl:with-param name="data"
       select="$arcsysNode" />
    </xsl:call-template>
   
  </xsl:template>
  <xsl:template name="Equirect_arcsys" >
    <xsl:param name="data" />
    <xsl:variable  name="equirectNode"
                  select="$data/equirect" />
    <table>
      <tr>
        <td >Equirectangular Projection</td></tr>
          <tr>
        <td valign="top">Standard Parallel:</td>
        <td>
          <xsl:value-of
           select="$equirectNode/stdparll" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
            select="$equirectNode/longcm" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$equirectNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$equirectNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="Azimequi_arcsys">
    <xsl:param name="data" />
    <xsl:variable  name="azimequiNode"
                  select="$data/azimequi" />
    <table>
      <tr>
        <td >Azimuthal Equidistant</td>
      </tr>
      <tr>
        <td valign="top">Longitude of Central Meridian:</td>
        <td>
          <xsl:value-of
             select="$azimequiNode/longcm" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Latitude of Projection Origin:</td>
        <td>
          <xsl:value-of
            select="$azimequiNode/latprjo" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Easting:</td>
        <td>
          <xsl:value-of
             select="$azimequiNode/feast" />
        </td>
      </tr>
      <tr>
        <td valign="top">False Northing:</td>
        <td>
          <xsl:value-of
            select="$azimequiNode/fnorth" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <!-- Local Planar -->
  <xsl:template name="Localp">
    <xsl:param name="data" />
    <xsl:variable  name="localpNode"
                  select="$data/localp" />
    <table>
      <tr>
        <td style="font-weight:bold;">Local Planar  Information</td>
      </tr>
      <tr>
        <td valign="top">Description:</td>
        <td>
          <xsl:value-of
             select="$localpNode/localpd" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Georeference Information:</td>
        <td>
          <xsl:value-of
            select="$localpNode/localpgi" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <!-- LOCAL -->
  <xsl:template name="Local"  >
    <xsl:param  name="data" />
    <xsl:variable name="localNode"
                  select="$data/local" />
    <h3>Local Information</h3>
    <table>
     
      <tr>
        <td valign="top">Description:</td>
        <td>
          <xsl:value-of
           select="$localNode/localdes" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Georeference Information:</td>
        <td>
          <xsl:value-of
            select="$localNode/localgeo" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <!--Vertdef-->
  <xsl:template name="Vertdef"  >
    <xsl:param  name="data" />
    <xsl:call-template name="Altsys">
      <xsl:with-param name="data" select="$data/vertdef"/>
    </xsl:call-template>
    <xsl:call-template name="Depthsys">
      <xsl:with-param name="data" select="$data/vertdef"/>
    </xsl:call-template>
  </xsl:template>
  <!-- ALTSYS -->
  <xsl:template name="Altsys"  >
    <xsl:param  name="data" />
    <xsl:variable name="altsysNode"
                  select="$data/altsys" />
    <xsl:if test="(count ($altsysNode) > 0)">
    <h3>Altitude System Definition</h3>
    <table>
      <tr>
        <td valign="top">Datum Name:</td>
        <td>
          <xsl:value-of
           select="$altsysNode/altdatum" />
        </td>
      </tr>
      <tr>
        <td  valign="top">Resolution:</td>
        <td>
          <xsl:for-each select="$altsysNode/altres">
          <xsl:value-of
            select="." />
            <br/>
          </xsl:for-each>
        </td>
      </tr>
      <tr>
        <td valign="top">Distance Units:</td>
        <td>
          <xsl:value-of
             select="$altsysNode/altunits" />
        </td>
      </tr>
      <tr>
        <td valign="top">Encoding Method:</td>
        <td>
          <xsl:value-of
            select="$altsysNode/altenc" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <!-- DEPTHSYS -->
  <xsl:template name="Depthsys"  >
    <xsl:param name="data" />
    <xsl:variable name="depthsysNode"
                 select="$data/depthsys"/>
    <xsl:if test="(count ($depthsysNode) > 0)">
    <h3>Depth System Definition</h3>
    <table>
      <tr>
        <td valign="top">Datum Name:</td>
        <td>
          <xsl:value-of
             select="$depthsysNode/depthdn" />
        </td>
      </tr>

      <tr>
        <td  valign="top">Resolution:</td>
        <td>
          <xsl:for-each select="$depthsysNode/depthres">
          <xsl:value-of
            select="." />
            <br/>
          </xsl:for-each>
        </td>
      </tr>
      <tr>
        <td valign="top">Distance Units:</td>
        <td>
          <xsl:value-of
             select="$depthsysNode/depthdu" />
        </td>
      </tr>
      <tr>
        <td valign="top">Encoding Method:</td>
        <td>
          <xsl:value-of
            select="$depthsysNode/depthem" />
        </td>
      </tr>
    </table>
    </xsl:if>
  </xsl:template>
  <!--End of SPREF-->
</xsl:stylesheet>
