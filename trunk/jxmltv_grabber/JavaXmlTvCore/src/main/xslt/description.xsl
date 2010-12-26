<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" encoding="UTF-8"/>
  <!-- template to extract description from body tag -->
  <xsl:template match="/">
    <xsl:apply-templates select="//div[@class='detailBox']/div[@class='description']/div[@class='text']"/>
  </xsl:template>

  <xsl:template match="div[@class='text']"><xsl:value-of select="text()"/>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="li"><xsl:text>
</xsl:text>-<xsl:value-of select="text()"/>
  </xsl:template>  
 
  <xsl:template match="br"><xsl:text>
</xsl:text></xsl:template>
   
  <xsl:template match="div[@class='type']"><xsl:value-of select="text()"/> </xsl:template>

  <xsl:template match="p"><xsl:apply-templates/></xsl:template>
  
  <xsl:template match="a"/>
</xsl:stylesheet>