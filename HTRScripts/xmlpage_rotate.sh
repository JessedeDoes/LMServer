#!/bin/bash

usage="Usage: ${0##*/} {rotAngle(degrees)} < XML_PAGE_FILE";

if [ "$#" != 1 ]; then
  echo "$usage" 1>&2;
  exit 1;
fi

XSLT='<?xml version="1.0"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:str="http://exslt.org/strings"
  xmlns:math="http://exslt.org/math"
  xmlns="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15"
  xmlns:_="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15"
  xmlns:DEFAULT="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15"
  extension-element-prefixes="str math"
  version="1.0">

  <xsl:output method="xml" indent="yes" encoding="utf-8" omit-xml-declaration="no"/>

  <xsl:variable name="width" select="//_:Page/@imageWidth" />
  <xsl:variable name="height" select="//_:Page/@imageHeight" />
  <xsl:variable name="cX" select="number($width) div 2" />
  <xsl:variable name="cY" select="number($height) div 2" />
  <xsl:variable name="cosA" select="math:cos(number('${1}')*number(3.14159265359) div 180)" />
  <xsl:variable name="sinA" select="math:sin(number('${1}')*number(3.14159265359) div 180)" />

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//*[@points]">
    <xsl:copy>
      <xsl:attribute name="points">
        <xsl:for-each select="str:tokenize(@points,'"' '"')">
          <xsl:variable name="pX" select="number(substring-before(.,'"','"'))" />
          <xsl:variable name="pY" select="number(substring-after(.,'"','"'))" />
          <xsl:if test="position() &gt; 1">
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:value-of select="round( $cX + ( $pX - $cX ) * $cosA - ( $pY - $cY )*$sinA )"/>
          <xsl:text>,</xsl:text>
          <xsl:value-of select="round( $cY + ( $pX - $cX ) * $sinA + ( $pY - $cY )*$cosA )"/>
        </xsl:for-each>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
';

cat /dev/stdin | xmlstarlet tr <( echo "$XSLT" );
