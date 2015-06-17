var Geometry = new Object();
var geometry_initialized = false;

function safari_version()
{
  var version = navigator.appVersion;
  var search = "Safari";
  var index = version.indexOf(search);
  if (index == -1) return "";
  return parseFloat(version.substring(index+search.length+1));
}

// TODO: if safari (??) welke versies precies, want 3 is OK), tel NIET de scroll op bij event.clientX, event.clientY
// 


var isIE = (navigator.appName == "Microsoft Internet Explorer");

function getOffset(e)
{   
  var p = e.offsetParent; // parentNode werkt niet goed!!
  var o;  
  if (!p)
  {
    o = new Object();
    o.X = e.offsetLeft;
    o.Y = e.offsetTop;
    o.path = e.tagName + ":" + "(0,0)";
  } else
  { 
    o = getOffset(p);
    //alert(p + " " + o.X + " " + o.Y);
    o.X += e.offsetLeft;
    o.Y += e.offsetTop;
    o.path += "/" + e.tagName + "." + e.id + ":(" + e.offsetLeft + "," + e.offsetTop + ")";
  } 
  return o;
}

function init_geometry()
{
  if (geometry_initialized)
  {
    return;
  } else
  {
    geometry_initialized = true;
  }

  if (window.screenLeft)
  {
    Geometry.getWindowX = function() {  return window.screenLeft; }
    Geometry.getWindowY = function() {  return window.screenTop; }
  } else if (window.screenX)
  {
    Geometry.getWindowX = function() {  return window.screenX; }
    Geometry.getWindowY = function() {  return window.screenY; }
  };
  
  if (window.innerWidth)
  {
    //alert('tikkie');
    Geometry.getViewportWidth = function() { return window.innerWidth; }
    Geometry.getViewportHeight = function() { return window.innerHeight; } 
    Geometry.getHorizontalScroll = function() { return window.pageXOffset; }
    Geometry.getVerticalScroll = function() { return window.pageYOffset; }
  } else if (document.documentElement && document.documentElement.clientWidth)
  {
    //alert('hola die boe');
    var de = document.documentElement;
    Geometry.getViewportWidth = function() { return de.clientWidth; }
    Geometry.getViewportHeight = function() { return de.clientHeight; }
    Geometry.getHorizontalScroll = function() { return de.scrollLeft; }
    Geometry.getVerticalScroll = function() { return de.scrollTop; }
  } else if (document.body && document.body.clientWidth)
  {
    //alert('blibber die blap');
    var b = document.body;
    Geometry.getViewportWidth = function() { return b.clientWidth; }
    Geometry.getViewportHeight = function() { return b.clientHeight; }
    Geometry.getHorizontalScroll = function() { return b.scrollLeft; }
    Geometry.getVerticalScroll = function() { return b.scrollTop; }
  } else
  {
    alert('niks wil ....');
  }
  
  
  
  if (document.documentElement && document.documentElement.scrollWidth)
  {
    var de = document.documentElement;
    Geometry.getDocumentWidth =   function() { return de.scrollWidth; }
    Geometry.getDocumentHeight =   function() { return de.scrollHeight; }
  } else if (document.body.scrollWidth)
  {
    var b = document.body;
    Geometry.getDocumentWidth =   function() { return b.scrollWidth; }
    Geometry.getDocumentHeight =   function() { return b.scrollHeight; }
  };

  Geometry.getDocumentX = function(evt) { return evt.clientX +  Geometry.getHorizontalScroll(); }
  Geometry.getDocumentY = function(evt) { return evt.clientY + Geometry.getVerticalScroll(); }

  // bugje in Safari <=2: clientX is eigenlijk al in documentcoordinaten, niet in viewportcoordinaten

  if (navigator.appVersion.indexOf("Safari") >= 0) // dit stukje eigenlijk in function Geometry.documentX(evt) , etc
  {
    if (safari_version() < 500)
    {
      Geometry.getDocumentX = function(evt) { return evt.clientX; };
      Geometry.getDocumentY = function(evt) { return evt.clientY; };
    }
  }
}


function DummyFetcher()
{
  this.fetch = function(p,f)
  {
    var txt = p + " ofzo ";
    f(txt);
  }
}

XML = new Object();

XML.newDocument = function(rootTagName, namespaceURL) {
    if (!rootTagName) rootTagName = "";
    if (!namespaceURL) namespaceURL = "";

    if (document.implementation && document.implementation.createDocument) {
        // This is the W3C standard way to do it
        return document.implementation.createDocument(namespaceURL,
                                                      rootTagName, null);
    }
    else { // This is the IE way to do it
        // Create an empty document as an ActiveX object
        // If there is no root element, this is all we have to do
        var doc = new ActiveXObject("MSXML2.DOMDocument");

        // If there is a root tag, initialize the document
        if (rootTagName) {
            // Look for a namespace prefix
            var prefix = "";
            var tagname = rootTagName;
            var p = rootTagName.indexOf(':');
            if (p != -1) {
                prefix = rootTagName.substring(0, p);
                tagname = rootTagName.substring(p+1);
            }

            // If we have a namespace, we must have a namespace prefix
            // If we don't have a namespace, we discard any prefix
            if (namespaceURL) {
                if (!prefix) prefix = "a0"; // What Firefox uses
            }
            else prefix = "";

            // Create the root element (with optional namespace) as a
            // string of text
            var text = "<" + (prefix?(prefix+":"):"") +  tagname +
                (namespaceURL
                 ?(" xmlns:" + prefix + '="' + namespaceURL +'"')
                 :"") +
                "/>";
            // And parse that text into the empty document
            doc.loadXML(text);
        }
        return doc;
    }
};

function parseXML(text) 
{
    if (typeof DOMParser != "undefined") 
    {
        // Mozilla, Firefox, and related browsers
        return (new DOMParser()).parseFromString(text, "application/xml");
    }
    else if (typeof ActiveXObject != "undefined") 
    {
        // Internet Explorer.
        var doc = XML.newDocument();  // Create an empty document
        doc.loadXML(text);            // Parse text into it
        return doc;                   // Return it
    } else 
    {
        // As a last resort, try loading the document from a data: URL
        // This is supposed to work in Safari.  Thanks to Manos Batsis and
        // his Sarissa library (sarissa.sourceforge.net) for this technique.
        var url = "data:text/xml;charset=utf-8," + encodeURIComponent(text);
        var request = new XMLHttpRequest();
        request.open("GET", url, false);
        request.send(null);
        return request.responseXML;
    }
};


function replaceAll( str, from, to )
{
    var idx = str.indexOf( from );

    while ( idx > -1 )
    {
        str = str.replace( from, to );
        idx = str.indexOf( from );
    }
    return str;
}

function BronFetcher()
{
  this.makeURLfake = function(p)
  {
    return "http://pc-taalbank.inl.nl:8090/bronnen/dabron.xml";
  }

  this.makeURLx = function(idz)
  {
    idz = replaceAll(idz," ","|");
    return "/iWDB/search?wdb=WNTBRONNEN&actie=results&zoekopid=" + idz;
  }

  this.makeURLy = function(au)
  {
    var theurl = "/iWDB/search?wdb=WNTBRONNEN&actie=results&bronauteur=" + au;
    //alert(theurl);
    return theurl;
  }

  this.transform = function(x,p) // doe maar liever geen client side XSLT
  {
    //return "dit moet nog gemaakt!";
    var xmldoc = parseXML(x);
    //alert(xmldoc.prototype);    
    var results = xmldoc.getElementsByTagName("result");
    if (results == null || results.length == 0)
    {
      alert("he, niks!");
      return "geen resultaten voor " + p;
    }

    var transformed = "<table border=1 style='font-size: 9pt; padding: 4px; border-style:solid; border-width:1pt; border-collapse:collapse; border-color:#e2decb'>";
    for (var i=0; i < results.length; i++)
    {
      var r = results[i];
      var id = r.getAttribute("id");
      var title = r.getAttribute("titel"); 
      var author = r.getAttribute("auteur");
      var van = r.getAttribute("van"); 
      var tot = r.getAttribute("tot");
      var date = van + "-" + tot;
      if (van == tot)
      {
        date=van;
      }
      var linkstart = "<a target='_blank' href='/iWDB/search?wdb=WNTBRONNEN&amp;actie=article&amp;id=" + id + "'>";
      var linkend = "</a>";
      //alert(linkstart);      
      var linked = function(x) { return linkstart + x + linkend };

      transformed += "<tr><td  class='broncel' style='white-space:nowrap'>" + linked(author) + "<td class='broncel'>" + linked(title) + "<td class='broncel' style='white-space:nowrap'>" + linked(date)  + "</tr>";
      //alert(results[i].getAttribute("titel"));
    }
    transformed += "</table>";
    return transformed;
  }

  this.fetch = function(p,callback)
  {
    var url = this.makeURLx(p);
    //alert(url);
    var self = this;
    //alert(HTTP);
    HTTP.getText(url, function(x) { callback(self.transform(x,p)); }) ; 
  }
  return this;
}

var df = new DummyFetcher();
var bronfetcher = new BronFetcher();

function show1(e)
{
  e.style.visibility = 'visible';
}
function hide1(e)
{
  e.style.visibility = 'hidden';
}


function show2(e)
{
  e.style.display = 'block';
}
function hide2(e)
{
  e.style.display = 'none';
}

function Tooltip()
{
  this.tooltip = document.createElement("div");
  this.tooltip.style.position = "absolute";
  this.tooltip.style.visibility = "hidden";

  this.content = document.createElement("div");
  this.content.style.position = "relative";
  this.content.style.left = "-3px";
  this.content.style.top =  "5px";
  this.content.style.backgroundColor = "white"; // "#f2eedb"; // WAS d7c7a6
  this.content.style.borderStyle = "solid";
  this.content.style.borderColor = "#d7c7a6";
  this.content.style.padding = "5px";
  //this.content.style.opacity = ".9";
  //this.content.style.filter = "alpha(opacity=90)";
  this.content.className = 'tooltip';
  this.content.id = 'tooltip';

  // to nifty or not to nifty...
  this.tooltip.appendChild(this.content);
  this.count = 0;
 
  this.showXY = function(text,x,y)
  {
    this.count++;
    this.content.innerHTML = text;
    var w = Geometry.getViewportWidth();
    //alert(x + "/" + w);
    if (Geometry.getViewportWidth() > 2 * x)
    {
      this.tooltip.style.right = null;
      this.tooltip.style.left = x  + "px";
    } else
    {
      //alert(x);
      this.tooltip.style.left = null;
      this.tooltip.style.right =  Math.round((Geometry.getViewportWidth() - x)/2) + "px";
    }
    this.tooltip.style.top = y  + "px";
    show1(this.tooltip);
    var self = this; 
    this.tooltip.onclick = function(e) { self.hide() ; };
    if (this.tooltip.parentNode != document.body)
    {
      document.body.appendChild(this.tooltip);
      //Nifty('div#tooltip','big');
    }
    this.content.id = "tooltip" + this.count;
    //alert(this.content.id);
    //Nifty('div#tooltip' + this.count,'big');
  };
 
  this.show  = function(event,text)
  {
    this.hide();
    var x = Geometry.getDocumentX(event);
    var y = Geometry.getDocumentY(event);
    this.showXY(text,x,y);
  };

  this.hide = function()
  {
    hide1(this.tooltip);
  };

  this.showFetchedText = function(evt, fetcher, fetch_param)
  {  
    init_geometry();
    var self = this;
    this.hide();
    var x = Geometry.getDocumentX(evt);
    var y = Geometry.getDocumentY(evt);
    fetcher.fetch(fetch_param, function (txt) {  self.showXY(txt,x,y) ; } );
  }
  return this;
}
