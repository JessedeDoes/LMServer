<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns:ns="http://www.namescape.nl/"
   version="2.0" xpath-default-namespace="http://www.tei-c.org/ns/1.0">

<xsl:output encoding="UTF-8" method="html"/>

<xsl:template match="TEI.2|TEI">
<html>
<head>
<style type="text/css">
p {margin-top: 0; margin-bottom: 0; text-align: justify; text-indent: 1em }
body { margin-left: 2em; margin-right: 2em; font-family: Palatino Linotype }
.stage { font-style: italic }
.note_anchor { vertical-align: super; color: red; font-size: 70% }
.contents_h3 { margin-left: 2em } 
.contents_h4 { margin-left: 4em }
.Xnymlist {
-moz-column-count: 4;
-webkit-column-count: 4;
column-count: 4
}
		#graph { display: block; position: relative; overflow: hidden; }
		.node-label { font: 11px sans-serif; }
</style>

<!--
<script type="text/javascript" src="/NER/JS/http.js"></script>
<script type="text/javascript" src="/NER/JS/Tooltip.js"></script>
http://www.clips.ua.ac.be/media/pattern-graph/random/graph.js
-->
 <script type="text/javascript" 
   src="http://www.clips.ua.ac.be/media/pattern-graph/random/graph.js"></script>
    <script type="text/javascriptje_oud">  
        function init_graph() {
           var myCanvas = document.getElementById("_ctx");
           var ctx = myCanvas.getContext("2d");
           ctx.scale(2,2);
            SHADOW = 0.65 // Slow... 
            g = new Graph(document.getElementById("_ctx"));
             var ctx = myCanvas.getContext("2d");
           ctx.scale(2,2);
           
            for (var i=0; i &lt; 50; i++) { // Random nodes.
                g.addNode(i+1);
            }
            for (var i=0; i &lt; 75; i++) { // Random edges.
                var node1 = Array.choice(g.nodes);
                var node2 = Array.choice(g.nodes);
                g.addEdge(node1, node2, {weight: Math.random()});
            }
            g.prune(0);
            g.betweennessCentrality();
            g.eigenvectorCentrality();
            g.loop({frames:500, fps:20, ipf:2, weighted:0.5, directed:true});
        }
 </script>
     <script type="text/javascript">  
        function init_graph() {
            SHADOW = 0.65 // Slow... 
            g = new Graph(document.getElementById("_ctx"), 50);
            <xsl:for-each select="//node">
            {
            	var a = {label:"<xsl:value-of select='./label'/>", fontsize:9,
                      href:"javascript:alert('<xsl:value-of select='./label'/>');"};
            	var newId = '<xsl:value-of select="./@xml:id"/>';
            	g.addNode(newId,a);
            	//var newnode = g.nodeset[newId];
            	//g.setLabel(newnode,"<xsl:value-of select='./label'/>");
            } 
            </xsl:for-each>
            var node1;
            var node2;
            <xsl:for-each select="//arc">
            {
              var id1 = "<xsl:value-of select='@from'/>";
              var id2 = "<xsl:value-of select='@to'/>";
              node1 = g.nodeset[id1];
              node2 = g.nodeset[id2];
              //alert(node1 + " " + node2);
              //  {weight:0, length:1, type:null, stroke:"rgba(0,0,0,0.5)", strokewidth:1}
              g.addEdge(node1,node2,{stroke:"rgba(100,100,100,0.5)", weight: <xsl:value-of select="@weight"/>});
             }
            </xsl:for-each>
            
            g.prune(0);
            g.betweennessCentrality();
            g.eigenvectorCentrality();
            g.loop({frames:500, fps:20, ipf:2, weighted:0.5, directed:true});
        }
 </script>
<script type="text/javascript">

var tooltip = new Tooltip();

function gloss(event,id)
{
  // hier gaat iets mis, soms, maar dan werkt ie alsnog??
  var e = document.getElementById(id);
  if (e)
  {
    var t = e.innerHTML; 
    tooltip.show(event,t);
  } else
  {
    alert("not found:" + id);
  }
}

function glossXY(id)
{
  var e = document.getElementById("anchor_" + id);
  var e1 = document.getElementById(id); 

  if (e &amp;&amp; e1) 
  {
    var t = e1.innerHTML; 
    var o = getOffset(e);
    var x = o.X; var y = o.Y;
    tooltip.showXY(t, x, y);
    window.scrollTo(x, y-50);
  } else
  {
    alert("not found:" + id);
  }
}

function show_element(element_id)
{
  var h = document.getElementById(element_id);
  h.style.display = 'block';
}

function toggle_element(element_id)
{
  var h = document.getElementById(element_id);
  if (h.style.display == 'block')
  {
    h.style.display = 'none';
  } else
  {
    h.style.display = 'block';
  }
}

var previousId = 'hihi';

function toggle_element_hide_previous(element_id)
{

  if (previousId == element_id)
  {
    toggle_element(element_id);
  } else
  {
     var p = document.getElementById(previousId);
     if (p)
     {
       p.style.display='none';
     }
  }
  toggle_element(element_id); 
  previousId=element_id;
}

function toggle_searchresults(element_id)
{
  var e = document.getElementById(element_id);
  if (previousId == element_id)
  {
    
  } else
  {
    var p = document.getElementById(previousId);
    if (p)
    {
      p.style.display='none';
    }
  }
  toggle_element(element_id);
  previousId=element_id;
}

function searchNym(nymref, maxLines, extend)
{
  var spans = document.getElementsByTagName('span');
  var concordances = "&lt;ul&gt;";
  var i;
  var k=0;
  var hasMore =false;
  for (i=0; i &lt; spans.length; i++)
  {
    var span = spans[i];
    var nr = span.getAttribute("data-nymref");
    if (nr == nymref)
    {
       if (k &gt; maxLines)
       { 
        hasMore = true;
          break;
       }
      concordances += "&lt;li&gt;"+ span.parentNode.innerHTML + "&lt;/li&gt;";
      k++;
      concordances = concordances.replace(/data-nymref/g,"data-exnymref");
    }
  }
  concordances += "&lt;/ul&gt;";
  //alert(concordances);
 
  var context = document.getElementById('context-'  + nymref);
  context.innerHTML = concordances;
  //alert(k + " " + maxLines + " "+ hasMore);
  if (hasMore)
  { 
    //alert("Piep!");
    context.innerHTML += "&lt;a href=\"javascript:searchNym('" + nymref + "',1000, true)\"&gt;toon alle&lt;/a&gt;"; 
  }
  if (extend)
  {
    show_element('context-'  + nymref);
    
  } else
  {
  toggle_searchresults('context-'  + nymref);
  }
  return concordances;
}

</script>
</head>
<body onLoad="init_graph();">

  


<h1 style='text-align:center'>
<xsl:for-each select="//interpGrp[@type='title']">
   <xsl:value-of select="./interp/@value"/><xsl:value-of select="./interp//text()"/>
</xsl:for-each>
<br/>
  <xsl:for-each select="//interpGrp[@type='author']">
    <xsl:value-of select="./interp/@value"/><xsl:value-of select="./interp//text()"/>
  </xsl:for-each>
</h1>

<xsl:call-template name="metadata"/>

<!-- table of contents -->
<!--
<a onClick="javascript:toggle_element('contents');"><u>Toggle contents</u></a>
-->
<br/>
<div id='contents' class='contents' style='display:none'>
Inhoud:
<dl>
<xsl:for-each select="//head">
<dd>
<xsl:attribute name='class'>contents_<xsl:value-of select="@rend"/></xsl:attribute>
<xsl:apply-templates/>&#160;<a><xsl:attribute name="href">#<xsl:value-of select="generate-id(.)"/></xsl:attribute>&#8594;</a>
</dd>
</xsl:for-each>
</dl>
</div>
<!-- end TOC -->

<!-- nym list -->

<a onClick="javascript:toggle_element('nymlist');"><h3>Show/hide name information</h3></a>
<div id='nymlist'  style='display:block; background-color: #bbbbbb'>
<xsl:call-template name="listNym"/>

<div id="graph" style="display:none; width:800px; height:500px; border-style:solid; border-color: blue; background-color: lightblue">
        <canvas id="_ctx" width="800" height="500"></canvas>
</div>
</div>
<!--
<xsl:call-template name="languages"/>
-->
<xsl:apply-templates/>
</body>
</html>
</xsl:template>
<xsl:template match="p|div">
<xsl:call-template name="anchor"/>
<xsl:copy><xsl:apply-templates/></xsl:copy>
</xsl:template>

<xsl:template match="teiHeader">
<!--
  <a onClick="javascript:toggle_element('header');"><u>Toggle header</u></a>
 -->
<div id='header' style='display:none'>
<span style='color: #666666'>
<xsl:apply-templates/>
</span>
</div>

</xsl:template>

<xsl:template match="head">
<h3>
<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
<a>
<xsl:attribute name="name"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
</a>
<xsl:apply-templates/>
</h3>
</xsl:template>

<xsl:template match="lg">
<xsl:call-template name="anchor"/>
<div style='margin-left:4em'>
<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="l">
<div class='l'><xsl:apply-templates/></div>
</xsl:template>

<xsl:template match="hi">
<!--
<xsl:element name="{@rend}">
<xsl:apply-templates/>
</xsl:element>
-->
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="name|ne|ns:ne">
<span style="color: pink">
<xsl:apply-templates/>
</span>
</xsl:template>

<xsl:template match="name[@type='person']|ns:ne[@type='person']">
<span style="color: red">
 <xsl:attribute name="data-nymref"><xsl:value-of select="@nymRef"/></xsl:attribute>
<xsl:apply-templates/>
</span>
</xsl:template>

<xsl:template match="name[@type='location']|ns:ne[@type='location']">
<span style="color: green">
  <xsl:attribute name="data-nymref"><xsl:value-of select="@nymRef"/></xsl:attribute>
<xsl:apply-templates/>
</span>
</xsl:template>

<xsl:template match="name[@type='organisation']|ns:ne[@type='organisation']">
<span style="color: blue">
  <xsl:attribute name="data-nymref"><xsl:value-of select="@nymRef"/></xsl:attribute>
<xsl:apply-templates/>
</span>
</xsl:template>

<xsl:template match="name[@type='misc']|ns:ne[@type='misc']">
<span style="color: orange">
  <xsl:attribute name="data-nymref"><xsl:value-of select="@nymRef"/></xsl:attribute>
<xsl:apply-templates/>
</span>
</xsl:template>

<xsl:template match="list">
<dl><xsl:apply-templates/></dl>
</xsl:template>

<xsl:template match="item">
<dd><xsl:apply-templates/></dd>
</xsl:template>

<xsl:template match="table">
<table>
<xsl:apply-templates/>
</table>
</xsl:template>

<xsl:template match="row">
<tr>
<xsl:apply-templates/>
</tr>
</xsl:template>

<xsl:template match="cell">
<td>
<xsl:apply-templates/>
</td>
</xsl:template>

<xsl:template match="sp">
<div class='sp'>
<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="speaker">
<b style='font-variant:small-caps'><xsl:apply-templates/></b>
</xsl:template>

<xsl:template match="stage">
<div class='stage'>
<xsl:apply-templates/>
</div>
</xsl:template>


<xsl:template match="note">
<xsl:variable name="id"><xsl:value-of select="generate-id(.)"/></xsl:variable>
<span class='note_anchor'><a>
<xsl:attribute name="onClick">gloss(event,'<xsl:value-of select="$id"/>')</xsl:attribute>
<xsl:value-of select="@n"/>
</a></span>
<span class='hidden_note' style='display:none'>
<xsl:attribute name='id'><xsl:value-of select="$id"/></xsl:attribute>
<xsl:apply-templates/>
</span>
</xsl:template>

<xsl:template match="pb">
<div style='text-align: center; background-color: #eeeeee; border-style: solid; border-width: .5pt; border-color: #dddddd'>
<a><xsl:attribute name="name">p<xsl:value-of select="@n"/></xsl:attribute></a>
<xsl:value-of select="@n"/>
</div>
</xsl:template>


<xsl:template match="titleStmt">
<div style='margin-bottom: 1em'>
<h4>Title statement</h4>
<xsl:apply-templates/>
</div>
<div/>
</xsl:template>

<xsl:template match="titleStmt/title|titleStmt/author|titleStmt/editor">
<div>
<xsl:value-of select="name()"/>:&#160;<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="sourceDesc">
<div>
Source description: <xsl:apply-templates/>
</div>
</xsl:template>


<xsl:template name="languages">
<xsl:if test="//(p|lg|note)[@lang_lingua_ident!='DUTCH' and @lang_textcat=@lang_lingua_ident and string-length(.) > 100]">
<div><u>Passages in andere taal</u>:&#160;
<xsl:for-each select="//(p|lg|note)[@lang_lingua_ident!='DUTCH' and @lang_textcat=@lang_lingua_ident and string-length(.) > 100]">
<a>
<xsl:attribute name="href">#<xsl:value-of select="generate-id(.)"/></xsl:attribute><xsl:value-of select="generate-id(.)"/>
</a>:&#160;
<xsl:value-of select="@lang_lingua_ident"/> (<xsl:value-of select="name()"/>:<xsl:value-of select="string-length(.)"/>)
<xsl:if test="position()!=last()">,&#160; </xsl:if>
</xsl:for-each>
</div>
</xsl:if>
</xsl:template>

<xsl:template name="mark_language">
<xsl:if test="@lang_lingua_ident!='DUTCH' and @lang_textcat=@lang_lingua_ident and string-length(.) > 100">
<span style='color:blue'>[Taal van deze <xsl:value-of select="name()"/>: <xsl:value-of select="@lang_lingua_ident"/>]</span>
</xsl:if>
</xsl:template>

<xsl:template name="anchor">
<a>
<xsl:attribute name="name"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
</a>
<xsl:call-template name="mark_language"/>
</xsl:template>

<xsl:template match="milestone[@unit='bo']">
<a><xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute></a>
<h1 style='background-color:pink'>[Hier begint stukje origineel: <xsl:value-of select="@n"/>]</h1>
</xsl:template>

<xsl:template match="milestone[@unit='eo']">
<a><xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute></a>
<h1 style='background-color:pink'>[Hier eindigt stukje origineel <xsl:value-of select="@n"/>]</h1>
</xsl:template>

<xsl:template match="milestone[@unit='be']">
<a><xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute></a>
<h3 style='background-color:lightblue'>[Hier begint stukje editorial: <xsl:value-of select="@n"/>]</h3>
</xsl:template>

<xsl:template match="milestone[@unit='ee']">
<a><xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute></a>
<h3 style='background-color:lightblue'>[Hier eindigt stukje editorial <xsl:value-of select="@n"/>]</h3>
</xsl:template>



<xsl:template name="listNym">
<xsl:for-each select="//listNym">  
  <h4><a href="javascript:toggle_element('persons')">Persons</a>,
  <a href="javascript:toggle_element('locations')">Locations</a>,
  <a href="javascript:toggle_element('organisations')">Organisations</a>,
  <a href="javascript:toggle_element('misc')">Misc</a>,
    <a href="javascript:toggle_element('graph');">Cooccurrence graph</a></h4>
  <div style="display:none" class="nymlist" id='persons'>
<xsl:for-each select="./nym[@ns:type='person']">
  <xsl:sort select="./usg" data-type="number" order="descending"/>
  <xsl:call-template name="nym"/></xsl:for-each>
  </div>
  
  <div style="display:none"  class="nymlist" id='locations'>
<xsl:for-each select="./nym[@ns:type='location']">
  <xsl:sort select="./usg" data-type="number" order="descending"/>
  <xsl:call-template name="nym"/></xsl:for-each>
  </div>
  
  <div style="display:none"  class="nymlist" id='organisations'>
<xsl:for-each select="./nym[@ns:type='organisation']">
  <xsl:sort select="./usg" data-type="number" order="descending"/>
  <xsl:call-template name="nym"/></xsl:for-each>
    </div>
  
  <div style="display:none"  class="nymlist" id='misc'>
<xsl:for-each select="./nym[@ns:type='misc']">
  <xsl:sort select="./usg" data-type="number" order="descending"/>
  <xsl:call-template name="nym"/>
</xsl:for-each>

</div>
</xsl:for-each>
</xsl:template>

<!--

<nym ns:type="person" xml:id="nym.person.12"><form type="nym">AMILIVIA</form><usg type="frequency">1</usg><form type="witnessed"><usg type="frequency">1</usg><orth type="original">Amilivia</orth><orth type="normalized">AMILIVIA</orth></form></nym>

-->


<xsl:template name="nym">
<div>
<xsl:attribute name="id"><xsl:value-of select="./@xml:id"></xsl:value-of></xsl:attribute>
<b><a>
  <xsl:attribute name="href">javascript:searchNym("<xsl:value-of select="./@xml:id"></xsl:value-of>",10, false)</xsl:attribute>
  <xsl:value-of select="form[@type='nym']"/></a></b>
<xsl:text> </xsl:text>
(<xsl:value-of select="./usg"/>)
<xsl:apply-templates select="./form[@type='witnessed']"/>
</div>
  <div style="opacity: 1; background-color:pink"><xsl:attribute name="id">context-<xsl:value-of select="./@xml:id"/></xsl:attribute></div>
</xsl:template>

<xsl:template match="form[@type='witnessed']" priority="100">
<div style="margin-left: 1em">
<xsl:value-of select="orth[@type='original']"/><xsl:text> </xsl:text> 
  <span style="font-size:8pt">(<xsl:value-of select="orth[@type='normalized']"/>)</span> 
(<xsl:value-of select="./usg"/>)
</div>
</xsl:template>

<xsl:template match="teiHeader//*">
<div style='margin-left:2em'>
<b>&lt;<xsl:value-of select="name(.)"/>
<xsl:for-each select="@*">
<xsl:text> </xsl:text><xsl:value-of select="name(.)"/>="<i><xsl:value-of select="."/></i>"
</xsl:for-each>&gt;</b>
<xsl:apply-templates/>
<b>&lt;/<xsl:value-of select="name(.)"/>&gt;</b>
</div>
</xsl:template>

<xsl:template match="w">
  <xsl:choose>
    <xsl:when test="@lemma">
<span>
<xsl:attribute name="title">
<xsl:value-of select="@lemma"/>:<xsl:value-of select="@type"/>
</xsl:attribute>
<xsl:apply-templates/>
</span>
 
  </xsl:when>
  <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="s">
<xsl:variable name="id"><xsl:value-of select="generate-id(.)"/></xsl:variable>
  <span class="sentence">
    <xsl:if test=".//@lemma">
<a>
<xsl:attribute name="href">javascript:toggle_element_hide_previous('<xsl:value-of select="$id"/>')</xsl:attribute>
<span style="color:white; background-color:blue">&#x2193;</span>
</a>
    
<div style="display:none; position:absolute; background-color: pink; border-style:solid; opacity: 0.85">
<xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
<table>
<xsl:for-each select=".//w">
<tr>
  <td style='background-color:pink'><xsl:value-of select=".//text()"/></td>
  <td style='background-color:orange'><xsl:value-of select="@type"/></td>
  <td style='background-color:yellow'><xsl:value-of select="@lemma"/></td>
</tr>
</xsl:for-each>
</table>
</div>
    </xsl:if>
<xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template name="metadata">
<xsl:for-each select="//listBibl[@id='inlMetadata']">
<table>
  <xsl:for-each select="//interpGrp">
    <tr>
      <td><b><xsl:value-of select="./@type"/></b>:</td>
      <td><xsl:value-of select="./interp/@value"/><xsl:value-of select="./interp//text()"/></td>
    </tr>
   </xsl:for-each>
</table>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>

