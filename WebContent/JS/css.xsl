<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   version="1.0">

<xsl:template name="CSS">

<style type='text/css'>
<!-- uiteindelijk extern neerzetten; twee varianten definieren: 1 met, 1 zonder font properties, center etc -->

div.content, div.entry { margin-left: 3em; margin-right: 2em; margin-bottom: 4em;}

div.body
{
  margin-left: 0em;
  margin-right: 0em
}

html
{
  margin-right: 0;
  margin-left: 0;
}

div.menu
{ background-color: gray; position: absolute; top: 0; left: 0; height: 2em; width: 100% }

body
{
  font-family: Verdana;
  font-size: 11pt;
  margin-right: 0; margin-left: 0;
  margin-top: 2em;
  background-color: <xsl:value-of select="$light_background"/>;
}

.entry
{
  margin-right: 3em;
  margin-left: 3em;
}

#menu
{
  display: none;
  position: fixed;
  width: 100%;
  top:0; left:0;
  margin-left: 0;
  margin-bottom: 0;
  margin-top: 0 ;
  z-index: 100;
}
</style>
<style type="text/css">


td.broncel
{
  padding-left:10px;
  padding-right:10px;
  padding-top: 4px;
  padding-bottom: 4px;
  border-color:#e7d7b6;
}

div#koppelingen
{
  width:250px;
  background-color:  <xsl:value-of select="$dark_background"/>;
  background:  <xsl:value-of select="$dark_background"/>;
<xsl:choose>
<xsl:when test="$dictionary='MNW'">display: block;</xsl:when>
<xsl:otherwise>display: none;</xsl:otherwise>
</xsl:choose>
  padding: 10px 10px;
  position: absolute;
  top: <xsl:value-of select='$koppelingen_top'/>;
  right:20px;
  z-index: 200;
  font-size: 10pt;
  opacity: .9;
  filter:alpha(opacity=90);
}

div#koppelparent
{
  display:block;
  margin-left: 0;
  margin-right: 0;
  margin-top: 0;
  margin-bottom: 0;
}

.num
{
  vertical-align: super
}

.denom
{
   vertical-align: sub
}
.P  { margin-top: 0pt; margin-bottom: 0pt; text-align: justify; }
P { margin-top: 0pt; margin-bottom: 0pt; text-align: justify; }
.defaultdiv  { display: block; text-indent: 0pt; margin-top: 4pt; margin-bottom: 4pt; margin-left: 0pt }
.defaultargument { display: block; font-size: 9pt; margin-left: 5em; margin-right: 5em}
.defaulthead { display: block; font-weight: bold; margin-bottom: 2pt; text-align: center }
.defaultq { border-left-style: solid; border-left-color: #bbbbbb ; padding-left: 1em; display: block; margin-left: 5em }
.lg { display: block; margin-top: .5em; margin-bottom: .5em }
.q2 { display: inline; text-indent: 0pt; font-family: Verdana; color: #880000}
.h0  { display: block; margin-bottom: 20pt; }
.h1  { display: block; margin-bottom: 16pt; }
.h2  { display: block; margin-bottom: 12pt; }
.h3  { display: block; margin-bottom: 8pt; }
.h4  { display: block; margin-bottom: 4pt; }
.h5  { display: block; margin-bottom: 0pt; }
.h6  { display: block; margin-bottom: 0pt; } 
.h7  { display: block; margin-bottom: 0pt; }
.h8  { display: block; margin-bottom: 4pt }

<!-- in Safari werken de plusjes NIET met negatieve margin-left!! -->
<!-- Volgens websites moet je position:relative toevoegen en dan zou het WEL werken?? -->
<!-- position relative: GAAT VRESELIJK MIS IN IE 7 -->

.floatleft { float:left; clear:none; width: 2em; margin-left: -2em; }
.speaker { display: block; font-weight: bold } <!-- display block of niet ?? -->
.stage  {  display: block; margin-top: 0; margin-bottom: 0; font-style: italic }
.performance {  display: block; margin-top: 0; margin-bottom: 0; font-style: italic }
.set  {  display: block; margin-top: 0; margin-bottom: 0; font-style: italic }
.opener_closer { display: block; margin-top: 1em; margin-bottom: 1em; }
.byline { display: block; margin-top: 4pt; margin-bottom: 4pt; }
.trailer { display: block; text-align: center; }
.epigraph { display: block; margin-left: 5em; margin-right: 5em;}
.role { }

.klapfont_B { font-weight: bold;  border-style: solid; border-width:0.2pt; border-color: #aaaaaa; color: #707070; background-color:#e2decb }
.klapfont { font-family: Inl vmnw wnt; color:#996666 }

.sense { clear:both; margin-left: 2em; margin-top: .5em; margin-bottom: 0pt}
.kortebetekenis  { margin-left: 2em; margin-top: .5em; margin-bottom: 2em}
.subsense { clear:both; margin-left: 2em; margin-top: .3em; margin-bottom: 0pt}
.subsenseblock { margin-left: 2em; margin-top: 0pt; margin-bottom: 0pt }
.subsenseknop { margin-left : 2em; display: block }
.verbinding { clear:both; margin-left: 2em; margin-top: .3em; margin-bottom: 0pt}
.schrapje { clear:both; margin-left: 2em; margin-top: .3em; margin-bottom: 0pt}

.citatenblock { display: none; border-left-style:solid; border-left-color: #aa8888; border-left-width: 0.15em }

.eg  { margin-left: 1em; }
.xdef { font-style: italic ; }
.entry { display: block; margin-bottom: 1cm }
.bibl { display: inline ; text-align: right; }
.dictScrap { display: block; margin-top: 2em } 
.sensenumber { font-family: Verdana;  font-weight: bold; color: #505050 }
.orth { color: #880000 ; font-family: Verdana }
.highlight { font-weight: bold; color: red }
strong { font-weight: bold;  background-color: #e7d7b6; border-style: solid; border-color:  #c7b796; }
.itype { color: green; }
.hom   { color: blue; }
.colloc { color: red; font-family: Verdana }
.opnoemer { font-variant: small-caps; font-style:italic; color: #880000 }
.subtitle { font-size: 14pt; font-variant: small-caps }
.sublemma { color: #880000 }
b { font-weight: bold; color: #505050 }
.field { font-size: 11pt; font-weight: bold; color: #707070; text-decoration: none; }
.plusje { font-weight: bold; color: #909090; text-decoration: none; }
.klapuit { font-weight: normal; color: #bb9999; text-decoration: none; }
.hangindent { display: block; margin-top: 0pt; margin-bottom: 0pt; text-indent: -2em; margin-left: 2em }
.rf { color: #000088 }

<!-- de rest is onzin ? -->

a:link    { text-decoration: none; color: black }
a:visited { text-decoration: none; color: black }
a:hover   { text-decoration: underline; color: black }

.textfont  { font-family: Verdana }
.defaultl  {  display: block; margin-top: 0; margin-bottom: 0 }
.defaultp    { display: block; margin-top: 0; margin-bottom: 0 } 
.defaultblock {  display: block; margin-top: 0; margin-bottom: 0 }
.runin   { display: inline; float: left; clear: none }   
.add  { color: darkred; text-align: left;
        float: right; width: 3.5cm; clear: none; margin-left: 2em; margin-right: -4cm }

.addeditor { color: blue;
                     float: right; width: 3.5cm; clear: none; margin-left: 2em; margin-right: -4cm }

.footnote {  display: inline; font-weight: normal; font-size: 80%; text-align: left; }
.notenumber_editor { vertical-align: super; color: blue; font-size: .55em }
.notenumber { vertical-align: super; font-size: .55em }

.tag { color: gray; font-family: Times New Roman; font-size: 10pt; text-align: left; font-style: normal; font-weight: normal }
.speaker {  margin-top: 0; margin-bottom: 0; font-weight: bold }
.stage  {  margin-top: 0; margin-bottom: 0; font-style: italic }
.divnumber { display:block; font-weight:bold; text-align:center }
.linenumber { color: darkred }
.notepagenumber { display: block; margin-top: 10pt ; margin-bottom: 0pt; color: blue; font-weight: bold }
.editorial { font-family: Times New Roman; color: blue; }
         .supplied  { color:darkgreen; font-style:italic }       
         .titlepage { margin-top: 0; margin-bottom: 0; }

.author { text-transform: lowercase; font-variant: small-caps }
.etymtaal { font-style: italic}
</style>
</xsl:template>
</xsl:stylesheet>
