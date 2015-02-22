<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   version="1.0">



<xsl:template match="bibl">
<span>
<xsl:attribute name='class'><xsl:value-of select="name()"/></xsl:attribute>
&#160;
<xsl:choose>
<xsl:when test="./idno/@n!=''">
<a>
<xsl:attribute name='onClick'>tooltip.showFetchedText(event,bronfetcher,'<xsl:value-of select="./idno/@n"/>');</xsl:attribute><xsl:apply-templates/>
</a>
</xsl:when>
<xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
</xsl:choose>
</span>
</xsl:template>

<xsl:template match="biblScope[@type='pages']/span">
p.
<xsl:choose>
<xsl:when test="@from=@to or @to='p'">
<xsl:value-of select="@from"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@from"/>-<xsl:value-of select="@to"/>
</xsl:otherwise>
</xsl:choose>, 
</xsl:template>


<xsl:template match="biblScope[@type='lines_xx']/span">
<a target="_text">
<xsl:attribute name='href'><xsl:value-of select='$contextapplicatie'/>?doc=<xsl:value-of select="../../idno"/>&amp;page=<xsl:value-of select="../../biblScope[@type='pages']/span/@from"/>&amp;line=<xsl:value-of select="@from"/></xsl:attribute>
r.
<xsl:choose>
<xsl:when test="@from=@to or @to='l'">
<xsl:value-of select="@from"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@from"/>-<xsl:value-of select="@to"/>
</xsl:otherwise>
</xsl:choose></a>, </xsl:template>

<xsl:template match="biblScope[@type='lines']/span">
r.
<xsl:choose>
<xsl:when test="@from=@to or @to='l'">
<xsl:value-of select="@from"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@from"/>-<xsl:value-of select="@to"/>
</xsl:otherwise>
</xsl:choose>, </xsl:template>

<xsl:template match="placeName">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="placeName/region">
<xsl:apply-templates/>,
</xsl:template>

<xsl:template match="placeName/settlement">
<xsl:apply-templates/>,
</xsl:template>

<!-- VMNW anders -->

<xsl:template match="dateRange">
<xsl:if test="$dictionary='VMNW'">
<xsl:choose>
<xsl:when test="@from=@to">
<xsl:value-of select="@from"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@from"/>-<xsl:value-of select="@to"/>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
</xsl:template>


<xsl:template match="title"><i><xsl:apply-templates/></i></xsl:template>



<xsl:template match='author'><span class='author'><xsl:apply-templates/></span></xsl:template>

<xsl:template match="idno"/>
</xsl:stylesheet>
