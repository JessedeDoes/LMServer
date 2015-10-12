#!/bin/bash

usage="Usage: ${0##*/} {cropWidth}x{cropHeight}+{offsetX}+{offsetY} < XML_PAGE_FILE";

if [ "$#" != 1 ]; then
  echo "$usage" 1>&2;
  exit 1;
elif [ $(echo "$1" | grep -P '^[0-9]+x[0-9]+[+-][0-9]+[+-][0-9]+$' | wc -l) = 1 ]; then
  cropWidth=$(echo "$1" | sed 's|^\([0-9]*\)x\([0-9]*\)+*\(-*[0-9]*\)+*\(-*[0-9]*\)$|\1|');
  cropHeight=$(echo "$1" | sed 's|^\([0-9]*\)x\([0-9]*\)+*\(-*[0-9]*\)+*\(-*[0-9]*\)$|\2|');
  offsetX=$(echo "$1" | sed 's|^\([0-9]*\)x\([0-9]*\)+*\(-*[0-9]*\)+*\(-*[0-9]*\)$|\3|');
  offsetY=$(echo "$1" | sed 's|^\([0-9]*\)x\([0-9]*\)+*\(-*[0-9]*\)+*\(-*[0-9]*\)$|\4|');
else
  echo "${0##*/}: error: unexpected input argument" 1>&2;
  echo "$usage" 1>&2;
  exit 1;
fi

XSLT='<?xml version="1.0"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:str="http://exslt.org/strings"
  xmlns="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15"
  xmlns:_="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15"
  xmlns:DEFAULT="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15"
  extension-element-prefixes="str"
  version="1.0">

  <xsl:output method="xml" indent="yes" encoding="utf-8" omit-xml-declaration="no"/>

  <xsl:variable name="cropWidth" select="'${cropWidth}'"/>
  <xsl:variable name="cropHeight" select="'${cropHeight}'"/>
  <xsl:variable name="offsetX" select="number(-1)*number('${offsetX}')"/>
  <xsl:variable name="offsetY" select="number(-1)*number('${offsetY}')"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//_:Page">
    <xsl:copy>
      <xsl:attribute name="imageWidth">
        <xsl:value-of select="$cropWidth"/>
      </xsl:attribute>
      <xsl:attribute name="imageHeight">
        <xsl:value-of select="$cropHeight"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*[local-name() != '"'imageWidth'"' and local-name() != '"'imageHeight'"'] | node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//*[@points]">
    <xsl:copy>
      <xsl:attribute name="points">
        <xsl:for-each select="str:tokenize(@points,'"', '"')">
          <xsl:choose>
            <xsl:when test="position() = 1">
              <xsl:value-of select="number($offsetX)+number(.)"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 0">
              <xsl:text>,</xsl:text><xsl:value-of select="number($offsetY)+number(.)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text> </xsl:text><xsl:value-of select="number($offsetX)+number(.)"/>
            </xsl:otherwise>
          </xsl:choose> 
        </xsl:for-each>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
';

cat /dev/stdin | xmlstarlet tr <( echo "$XSLT" );
