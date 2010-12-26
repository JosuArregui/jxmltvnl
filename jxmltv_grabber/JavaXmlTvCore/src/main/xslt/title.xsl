<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" encoding="UTF-8"/>
  <!-- template to extract description from body tag -->
  <xsl:template match="/">
    <xsl:apply-templates select="//div[@class='detailBox']/h2/span[@class='title']"/>
  </xsl:template>

  <xsl:template match="span[@class='title']"><xsl:value-of select="text()"/></xsl:template>
</xsl:stylesheet>