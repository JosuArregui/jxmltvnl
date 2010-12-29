<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" encoding="UTF-8"/>
  <!-- Version 1.2.6 -->
  <xsl:template match="/">
    <xsl:apply-templates select="//div[@id='prog-info-content']/ul"/>
  </xsl:template>

  <xsl:template match="div[@class='info']"><xsl:value-of select="text()"/>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="li">
  |^|<xsl:apply-templates/>
  </xsl:template>  

  <xsl:template match="strong">
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template match="img">
  	|^|Kijkwijzer: <xsl:value-of select="./@src"/><xsl:text> </xsl:text><xsl:value-of select="./@alt"/> 
  </xsl:template>
  
</xsl:stylesheet>