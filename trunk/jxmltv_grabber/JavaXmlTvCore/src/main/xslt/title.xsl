<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" encoding="UTF-8"/>
  <!-- Version 1.2.6 -->
  <xsl:template match="/">
    <xsl:apply-templates select="//ul[@id='prog-info-content-colleft']/li[1]/text()"/>
  </xsl:template>
</xsl:stylesheet>