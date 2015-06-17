<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   version="1.0">

<xsl:template name="JavaScript">

<!--
<script type="text/javascript" src="http://www.xs4all.nl/~dedoes/niftycube.js"></script>
-->

<script type="text/javascript" src="/iWNTLINKS/http.js"></script>
<script type="text/javascript" src="/iWNTLINKS/Tooltip.js"></script>
<script type="text/javascript">
<xsl:attribute name="src">http://pc-taalbank.inl.nl:8080/WAC/links?lemma_id=<xsl:value-of select="//entry/@id"/>&amp;action=make_links</xsl:attribute></script>
<script type="text/javascript" language="JavaScript">
var tooltip = new Tooltip();
</script>

<script type="text/javascript" src="niftycube.js"></script>

<script language="JavaScript">

var do_nifty = true;
var divjes = new Array();
var citatenblokken = new Array();
var schrapjes = new Array();
var verbindingen = new Array();
var subsenseblokken = new Array();
var subsenseknoppen = new Array();

var klap_me_uit = null;
var koppelingen_gemaakt = false;

function make_links_b()
{
  var r = "";
  //alert('blarp');
  if (!window.groups)
  {
    return;
  }
  for (var i=0; i &lt; groups.length; i++)
  {
    var category = groups[i].brontype;
    var b = groups[i].bronnen[0];
    var link = b.links[0];
    r += "&lt;a target='_blank' href='http://tst-dev1.inl.nl/iWNTLINKS/DATADIR/paginazy.html?" + category +  "+" + b.frametype + "+" + b.broncode;
    r += "+" + link.woordenboeklemma_id;
    r += "+" + link.woordenboeklemma;
    r += "+" + escape(link.linktitel);
    r += "+" + escape(link.linkid);
    r += escape(link.extra_info_verwijzing);
    r += "'&gt;";
    r += category;
    r += "&lt;/a&gt;\n";
    //alert(r);
    if (i &lt; groups.length - 1) r += "&lt;br&gt;";
  }
  document.getElementById('externe_koppelingen').innerHTML += r;
}

function plaatskoppelingen()
{
   if (navigator.appName == "Microsoft Internet Explorer")
   {
     var koppelingen = document.getElementById('koppelingen');
     //alert(document.documentElement.scrollTop);
     var scrolltop = 0;
     if (document.documentElement != null)
     {
       scrolltop = document.documentElement.scrollTop;
     }
     if (scrolltop == 0)
     {
       scrolltop = document.body.scrollTop;
     }
     //if (koppelingen.style.display == 'block')
     {
       koppelingen.style.top = scrolltop +  <xsl:value-of select='$koppelingen_top'/>;
     }
   } else
   {
     var koppelingen = document.getElementById('koppelingen');
     koppelingen.style.position = 'fixed'; // dit werkt niet in IE
   }
   if (!koppelingen_gemaakt)
   {
     make_links_b();
     koppelingen_gemaakt = true;
   }
}

document.plaatskoppelingen = plaatskoppelingen;

window.onscroll = function(e)
{
  plaatskoppelingen();
}

function browser_dependent_stuff()
{
  var m = document.getElementById('menu');
  return;
  //alert(m + " " + m.style.position);
  //alert(navigator.appName);
  if (navigator.appName != "Microsoft Internet Explorer")
  {
     //var r = document.getElementById('docroot');
     //r.style.overflow = 'auto';
     m.style.position='fixed';
  } else
  {
     //m.style.position='absolute';
  }
}

function getKnop(x)
{
  var theid = x.getAttribute('id');
  var knopid = "SUBKNOP_" + theid.substring(9); // BOE lelijk++
  return document.getElementById(knopid);

  var p = x.previousSibling;

  if (p != null &amp;&amp; p.tagName != 'DIV') // en nodetype is textnode .....
  {
    p = p.previousSibling; 
  }

  if (p == null)
  {
    return null;
  }
  //alert(p.tagName + ' ' + p.getAttribute('className'));
  var c0 = p.firstChild;
  if (c0)
  {
    var c1 = c0.firstChild;
    if (c1)
    {
      var c2 = c1.firstChild; //
      //alert(c2.tagName);
      if (c2 &amp;&amp; (c2.getAttribute('id') == knopid))
      {
        //alert(c2.tagName);
        return c2;
      }
    }
  }
  return null;
}

function transfer_menu()
{
  var p = parent;
  if (p != null)
  {
    try
    {
      var f0 = p.frames[0].document;
      var menu_in_topframe = f0.getElementById('menuutje');
      var menu_in_article = document.getElementById('menu');
      menu_in_topframe.innerHTML = menu_in_article.innerHTML;
    //menu_in_article.style.display='none';

      f0.subsenseblokken = subsenseblokken;
      f0.subsenseknoppen = subsenseknoppen;

      f0.toggle_subsenseblokken = function()
      {
        var p = parent;
        var f1 = p.frames[1].document;
       f1.toggle_subsenseblokken();
      }
    } catch (e)
    {
    }
  }
}

function transfer_koppelknop()
{
  var p = parent;
  if (p != null)
  {
    try
    {
      var f0 = p.frames[0].document;
      var koppelknop = document.getElementById('koppelknop');
      //alert(koppelknop);
      if (koppelknop != null) // er zijn koppelingen
      {
        f0.getElementById('koppelknop').style.display='block';
        var koppelingen =  document.getElementById('koppelingen');
        if (koppelingen != null)
        {
          koppelingen.style.display='block';
          var koppelingen2 =  f0.getElementById('koppelingen');
          if (koppelingen2)
          {
            koppelingen2.innerHTML = koppelingen.innerHTML;
          }
        }
      }
      //alert(document.title);
      p.document.title = document.title;
    } catch (e)
    {
    }
  }
}

function get_tagpath(e)
{
  var p = e.parentNode;
  var tp = e.tagName + '[@id' + e.id + ']';
  while (p != null)
  {
    tp = p.tagName + '[@id' + p.id + ']' + '/' + tp; 
    p = p.parentNode;
  }
  return tp;
}

function on_load()
{
  //alert('loaded!');
  transfer_koppelknop();
  plaatskoppelingen();
  //alert('hallo....');
  //window.focus();
  browser_dependent_stuff();
  divjes = document.getElementsByTagName('div');

  if (do_nifty) 
  {
    try
    {
      Nifty("div#koppelingen","big");
    } catch (e)
    {
      //alert(e);
    }
  }

  var k = 0;
  var n_verb = 0;
  var n_subsense=0;

  // deze loop is kennelijk langzaam op konqueror ...
  // alert(navigator.appName);

  var len  = divjes.length; // dit scheelt heel veel voor grote artikelen in IE!

  for (i=0; i &lt; len; i++)
  {
    var thediv = divjes[i];
    var cls = thediv.getAttribute('class');
    var clsnm = thediv.getAttribute('className'); // nodig voor IE!

    // TODO Safari: alle citatenblokken...

    if (navigator.appVersion.indexOf("Safari") >= 0)
    {
       //alert("safari");
       //alert(cls + " | " + clsnm + " | " + thediv.className);
       var spans = null;
       if (cls == 'subsense' || cls == 'sense' || cls == 'schrapje' || cls =='verbinding' || cls=='kortebetekenis')
       {
	  spans = thediv.getElementsByTagName('span'); // dit is kwadratisch voor een ingewikkeld artikel??
       } else if (cls == 'citatenblock')
       {
         var p = thediv.parentNode; // nu "div class='P'"

         if (p != null &amp;&amp; p.className == 'P')
         {
           spans =  p.getElementsByTagName('span');
         }
       }

       if (spans != null)
       {
         x = spans.item(0);
         if (x)
         {
           if (x.getAttribute('class') == 'floatleft')
           {
              x.style.position='relative';
           }
         }
       }
    }
    if (cls == 'citatenblock' || clsnm == 'citatenblock')
    {
      citatenblokken[k] = thediv;
      k++;
    } else if (cls == 'verbinding' || clsnm == 'verbinding' || 
               cls == 'schrapje' || clsnm == 'schrapje')
    {
      verbindingen[n_verb] = thediv;
      n_verb++;
    } else if (cls == 'subsenseblock' || clsnm == 'subsenseblock')
    {
       subsenseblokken[n_subsense] = thediv;  // NEE GEWOON ZELfDE ID idioot
       var theid = thediv.getAttribute('id');

       subsenseknoppen[n_subsense] = getKnop(thediv);

       // dit betekent toch flink wat getelementbyids voor 
       // een groot document - dit is langzaam in IE?

       subsenseblokken[n_subsense].knop = subsenseknoppen[n_subsense];

       //alert(subsenseknoppen[n_subsense]);
       n_subsense++;
    }
  }

  //transfer_menu();

  var highlights = new Array();
  highlights =  document.getElementsByTagName('strong');
  var nofhighlights=0;
  var theanchor;
  var thelement = null;
  for  (i=0; i &lt; highlights.length; i++)
  {
    var hi = highlights[i];
    var p = hi.parentNode;
    if (i==0)
    {
      theanchor=hi.id;
      theelement = hi;
    }
    while (p != null)
    {
      //alert(p.tagName);
      if (p.tagName != null)
      {
        //window.status = p.tagName;
        var cls = p.getAttribute('class');
        var clsnm = p.getAttribute('className');
        if (cls == 'subsenseblock' || clsnm == 'subsenseblock' || cls=='citatenblock' || clsnm == 'citatenblock')
        {
          p.style.display='block';
        }
        if (cls == 'subsenseblock' || clsnm == 'subsenseblock')
        { 
          p.style.display='block';
          // pas ook weer de knop aan (+ wordt -)
          var cls = p.getAttribute('class');
          var clsnm = p.getAttribute('className');
          if (cls == 'subsenseblock' || clsnm == 'subsenseblock')
          {
            p.knop.innerHTML = '<xsl:value-of select="$klappie_Bopen_js"/>';
          }
        }
      }
      p = p.parentNode;
    }
    nofhighlights++;
  }
  if ((klap_me_uit == null) &amp;&amp; nofhighlights > 0)
  {
    //document.location.hash = theanchor;
    theelement.scrollIntoView(false);
  }
  if (klap_me_uit != null)
  {
    var klapme = document.getElementById(klap_me_uit);
    //alert(klap_me_uit);
    if (klapme != null)
    {
      //alert(klap_me_uit);
      klapuit(klapme);
      klapme.scrollIntoView(false);
      // dit gaat fout met de 'srollable div'
      // implementatie in firefox!
      //document.location.hash = klap_me_uit;
      highlight_item(klapme);
      //alert(m);
    }
  }
  // alert(citatenblokken.length);
  // window.resizeTo(600,800);
}

function highlight_item(x)
{
  //x.style.fontWeight='bold';
  x.style.backgroundColor= '#e7d7b6'; 
  x.style.borderStyle =  'solid'
  x.style.borderColor =   '#c7b796';
}
function klapuit(x) // argument is element
{
  var p = x;
  while (p != null)
  {
    if (p.tagName != null)
    {
      //if (p.style.display == 'none' || p.tagName == 'div')
      {
        // pas ook weer de knop aan (+ wordt -)
        var cls = p.getAttribute('class');
        var clsnm = p.getAttribute('className');

        if (cls == 'subsenseblock' || clsnm == 'subsenseblock' || cls=='citatenblock' || clsnm == 'citatenblock')
        {
           p.style.display='block';
        }

        if (cls == 'subsenseblock' || clsnm == 'subsenseblock')
        {
          p.knop.innerHTML = '<xsl:value-of select="$klappie_Bopen_js"/>';
        }
      }
      //p.style.display='block';
    }
    p = p.parentNode;
  }
}

function toggle_subsense(x)
{
   var z = 'SUBSENSE_' + x;
   var s = document.getElementById('SUBSENSE_' + x);
   var k = document.getElementById('SUBKNOP_' + x);
   if (s.style.display == "block")
   {
     hide(z);
     // alert(k);
     k.innerHTML='<xsl:value-of select="$klappie_B_js"/>';
   } else
   {
     show(z);
     k.innerHTML='<xsl:value-of select="$klappie_Bopen_js"/>';
   }
}

function toggle_citaten()
{
  // alert('hoepsa');
  var cb = document.getElementById('citaten_check');
  //alert(cb);
  if (cb.checked)
  {
    //alert('hopsa');
    toon_citaten();
  } else
  {
    verberg_citaten();
  }
}


function toggle_verbindingen()
{
  var cb = document.getElementById('verbindingen_check');
  //alert(cb);
  if (cb.checked)
  {
    //alert('hopsa');
    toon_verbindingen();
  } else
  {
    verberg_verbindingen();
  }
}


function toggle_subsenseblokken()
{
  var cb = document.getElementById('subsense_check');
  //alert(cb);
  if (cb.checked)
  {
    //alert('hopsa');
    toon_subsenseblokken();
  } else
  {
    verberg_subsenseblokken();
  }
}

document.toggle_subsenseblokken = toggle_subsenseblokken;

function toon_citaten()
{
  for (i=0; i &lt; citatenblokken.length; i++)
  {
    showX(citatenblokken[i]);
  }
}

document.toon_citaten = toon_citaten;

function verberg_citaten()
{
  for (i=0; i &lt; citatenblokken.length; i++)
  {
    hideX(citatenblokken[i]);
  }
}

document.verberg_citaten = verberg_citaten;

function toon_verbindingen()
{
  for (i=0; i &lt; verbindingen.length; i++)
  {
    showX(verbindingen[i]);
  }
}

function verberg_verbindingen()
{
  for (i=0; i &lt; verbindingen.length; i++)
  {
    hideX(verbindingen[i]);
  }
}

function toon_subsenseblokken()
{
  for (i=0; i &lt; subsenseblokken.length; i++)
  {
    showX(subsenseblokken[i]);
    subsenseknoppen[i].innerHTML = '<xsl:value-of select="$klappie_Bopen_js"/>';
    // stuk om de knoppen te verbergen is eng en gaat even weg
    // hideX(subsenseblokken[i].previousSibling.previousSibling); // Hm ENG
  }
}

document.toon_subsenseblokken = toon_subsenseblokken;

function verberg_subsenseblokken()
{
  for (i=0; i &lt; subsenseblokken.length; i++)
  {
    hideX(subsenseblokken[i]);
    subsenseknoppen[i].innerHTML = '<xsl:value-of select="$klappie_B_js"/>';
    //showX(subsenseblokken[i].previousSibling.previousSibling); // Hm ENG
  }
}

document.verberg_subsenseblokken = verberg_subsenseblokken;

function showX(s)
{
  //s.style.visibility="visible";
  //s.style.backgroundColor="#f8f8f8";
  //s.style.position="static";
  //s.style.top=0;
  s.style.display="block";
}

function hideX(s)
{
  //s.style.position="absolute";
  //s.style.visibility="hidden";
  s.style.display="none";
}

function donthide(x)
{

}
function show(x)
{
  var s = document.getElementById(x);
  showX(s);
}

function hide(x)
{
  var s = document.getElementById(x);
  hideX(s);
}

function toggle(x)
{
   var s = document.getElementById(x);
   if (s.style.display == "block")
   {
     hide(x);
   } else
   {
     show(x);
   }
}

//Naam waardeparen zonder '?' in een array

if (document.location.search)
{
  var ParamArray = document.location.search.substr(1).split("&amp;");
  for (var i = 0; i &lt; ParamArray.length; i++) 
  {
    var name_value = ParamArray[i].split("=");
    var name = name_value[0].toLowerCase();
    var val = decodeURIComponent(name_value[1]);
    if (name == 'betekenis_id' || name == 'citaat_id' || 
        (name == 'id' &amp;&amp; val.indexOf('.re.') > 0 )) // gruwelik
    { 
      klap_me_uit = val;
    }
  }
} 

function showBronPopup(the_id)
{
  var thetitle = document.getElementById(the_id);
  //alert(thetitle.innerHTML);

  thetitle.title = "Hola: nu zit ie der wel";

  return;

  if (thetitle != null)
  {
    if (thetitle.brongedaan)
    {
    } else
    {
      thetitle.innerHTML = "&lt;div style='position:absolute'&gt;haha&lt;/div&gt;" + thetitle.innerHTML;
    }
  }
}
</script>
</xsl:template>
</xsl:stylesheet>
