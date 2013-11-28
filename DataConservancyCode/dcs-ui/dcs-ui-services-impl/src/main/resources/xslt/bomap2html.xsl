<?xml version="1.0"?>

<!-- Convert the xml serialization of a business object map to simple html -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method='html' media-type='text/html'/>

  <xsl:template match="text()|@*">
  </xsl:template>

  <xsl:template match="/">
    <html>
      <head>
	<title>Business Object Map</title>
	<style>
	  .boheader {
	    font-size: larger;
	    font-weight:bold;
	  }

          .boitem {
	    margin-top: 1em;
          }
	</style>
      </head>
      <body>
	<h2>Business Objects Map</h2>
	<ul>
	  <xsl:apply-templates/>
	</ul>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="bo">
    <li class="boitem"><span class='boheader'>Business Object</span>

      <ul>
	<li><b>Name:</b><xsl:text> </xsl:text><xsl:value-of select="name"/></li>
	<li><b>Type:</b><xsl:text> </xsl:text><xsl:value-of select="type"/></li>
	<li><b>Deposit Status:</b><xsl:text> </xsl:text><xsl:value-of select="depositStatus"/></li>
	<li><b>Identifier:</b><xsl:text> </xsl:text><xsl:value-of select="id"/></li>
	
	<xsl:for-each select="alternateid">
	  <li><b>Alternate identifier:</b><xsl:text> </xsl:text><xsl:value-of select="."/></li>
	</xsl:for-each>
      </ul>
    </li>
    
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

</xsl:stylesheet>
