my $HTML=0;
my $printExamples=1;
my $plainText = 0;
if ($HTML)
{
print <<END;
<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8"></meta>
<style type="text/css">
body { font-family: Junicode }
.pb { page-break-before: always }
</style>
</head>
<body>
END
}

# a76b --> a7b6

my %unKnown;
my $combcurl = "1dce"; # &er; teken is 035b
my $zwnj = "200c"; # zero-width non-joiner
my $DIRK = 1;
my $DRAM = $DIRK?"z":"&#xf2e6;";
my $RECIPE = $DIRK? "R&#xa76d;" :"&#x211e;";

my @rules = 
(
# macron rules
  ["e", "", "n", "&#x0113;", "en"],
  ["a", "", "n", "&#x0101;", "an"],
  ["a", "", "m", "&#x0101;", "am"],
  ["o", "", "n", "&#x014d;", "on"],
  ["o", "", "m", "&#x014d;", "om"],
  ["u", "", "m", "&#x016b;", "um"],
  ["e", "", "m", "&#x0113;", "em"],
  ["i", "", "n", "&#x012b;", "in"], 
  ["o", "", "m", "&#x014d;", "om"],
  ["u", "", "n", "&#x016b;", "un"], # simple macron rule

  # ende
  ["n", "", "de", "&#xe5dc;", "nde"],

  ["", "e", "er", "e&#x$combcurl;", "ere"], # ? of moeten we hier de "ur" aanhouden?? ? of &combcurl;=1dce? combhook=0309
  ["", "n", "re", "n&#x$combcurl;", "ren"], 
  ["v", "", "er", "v&#x$combcurl;", "ver"], # caution: er + lage letter moet eerst! (nee niet zo duidelijk)
  ["h", "", "er", "h&#x$combcurl;", "her"], # er staat er eigenlijk na (herdicheit) 
  ["u", "", "er", "u&#x$combcurl;", "uer"],
  ["c", "", "re", "c&#x$combcurl;", "cre"], # lecressie 
  ["", "cond:[\\s<\$]", "er", "&#x$zwnj;&#x$combcurl;", "er"], # loshangende er als in kinder
  ["e", "", "re", "e&#x$combcurl;", "ere"], # caution: other order first 
  ["", "n", "ri", "n&#x$combcurl;", "rin"], # restringeert
  ["i", "", "r", "i&#x$combcurl;", "ir"],
  ["", "e", "r", "e&#x$combcurl;", "re"], # 'ware' geval, misschien ook op te lossen als waere
  ["", "a", "r", "a&#x$combcurl;", "ra"], # scafisagra
  
  ["ij", "", "n", "&#x0133;&#x0304;", "ijn"], # kijnder

  ["d", "", "en", "&#xf2e5;", "den"], # identiek aan deniers teken, te lezen als den.?
  ["d", "", "eniers", "&#xf2e5;", "deniers"],
  ["d", "", "enier", "&#xf2e5;", "denier"],

  ["", "", "us", $DIRK?"&#xa76f;":"&#xf1a6;", "us"], # &usbase; (dirk vindt dit gelijk aan de "con"-toestand)

 
  ["", "", "dragmen", $DRAM, "dragmen"], # Dirk vindt dit gewoon een "z"
  ["", "", "dragme",  $DRAM, "dragme"],
  ["", "", "dragma",  $DRAM, "dragma"],
  ["", "", "onse",    "&#x2125;", "onse"],
  ["R", "", "ecipe",  $RECIPE, "Recipe"], # 2113 is ook leuk voor eventuele haal?
                                             # anders gewoon R + &#xa76d;?? (vindt Dirk)
  ["", "", "librae",  "&#xf2ea;", "librae"],
  ["S", "", "emis", "S&#x0306;", "semis"], # hoe doe je semis teken in mufi??? small ezh?
  ["s", "", "emis", "S&#x0306;", "semis"],
  
  ["h", "", "eit",    "h&#xa76b;", "heit"], # etfin = f155?
  ["h", "", "et",     "h&#xa76b;", "het"],
  ["s", "", "eiden",  $DIRK?"&#x00df;":"s&#xa76b;", "seiden"], # vorseiden (dirk vindt dit sz, net als paralisis)
  ["m", "", "et",     "m&#xa76b;", "met"], # met (CHECK THIS!)
  
  ["n", "", "a", "n&#x1dd3;", "na"], # ana-gevalletje

  # de 'polyvalente haal'

  ["s", "", "is", "&#x00df;", "sis"], # Paralisis
  ["", "", "is", "&#xa76d;", "is"], # Calament_is_
  ["t", "", "en", "t&#xa76d;", "ten"], # de telwoord-haal
  ["", "", "etera", "&#xa76d;", "etera"], # etcetera (polyvalente haal = &is;?, niet in junicode font)
  ["", "", "reuen", "&#xa76d;", "reuen"],
  ["", "cond:c", "et", "&#x204a;", "et"], # eerste stukje van etcetera

  # paralisis ? combining flourish? f1c6?
  # a76d = is sign

  ["p", "", "ur", "p&#x1dd1;", "pur"], # purgiert (?goede ur?)
  ["t", "", "ur", "t&#x1dd1;", "tur"], # turbiet (?goede ur?)

  # promitta?
  # mirabilanen:

  ["", "", "con",  "&#xa76f;", , "con"], # dirk vindt: con = usbase (dan nemen we con want binnen gewone unicode)
  ["sup", "", "er", "su&#xa751;", "super"], # $pbardes, superfluiteit
  ["prop", "", "er", "pro&#xa751;", "proper"], 
  ["p", "", "ro", "&#xa751;", "pro"], # ook prouicie

);

my $ruleId=1;
foreach my $rule (@rules)
{
  $rule2id{$rule} = $ruleId;
  $id2rule{$ruleId} = $rule;
  $ruleId++; 
}

#while(<>)
sub fixTextLine
{
  my $tl = shift;
  $tl =~ s/(<Unicode>)(.*?)(<\/Unicode>)/$1 . fixLine($2) . $3/egis;
  return $tl;
}

undef $/;
while(<>)
{
  s/<TextLine.*?<\/TextLine>/fixTextLine($&)/egis;
  print;
}
sub fixLine
{
  my $line = shift;
  $line =~ s/&lt;/</g;
  $line =~ s/&gt;/>/g;
  $line =~ s/<choice.*?>//g;
  $line =~ s/<\/choice>//g;
  $line =~ s/<\/?abbrev.*?>//g;

  warn "BEFORE FIX: $line";

  # corrections in transcription

  $line =~ s/<expan>once<\/expan>/<expan>onse<\/expan>/g;
  $line =~ s/ <\/expan>/<\/expan> /g;
  $line =~ s|\.</expan>|</expan>.|g;
  $line =~ s|em<expan>m</expan>|e<expan>m</expan>m|g;
  $line =~ s|l<expan>i</expan>b<expan>rae</expan>|<expan>librae</expan>|g;
  $line =~ s|hooftzwe<expan>ere</expan>|hooftzwe<expan>er</expan>e|g;
  $line =~ s|([Ee])n<expan>d</expan>e|$1n<expan>de</expan>|g;
  $line =~ s|van<expan>der</expan>|vand<expan>er</expan>|g;
  $line =~ s|some<expan>r</expan>|som<expan>er</expan>e|g;
  $line =~ s|<expan></expan>||g;
  $line =~ s|gade<expan>r</expan>e|gad<expan>er</expan>e|g; # pag. 135

  # rules
  foreach my $rule (@rules)
  {
    my $ruleId = $rule2id{$rule};
    if ($$rule[0]) # left context
    {
      $line =~ s|$$rule[0]<expan>$$rule[2]</expan>|<choose n="$ruleId"><abbrev>$$rule[3]</abbrev><exp>$$rule[4]</exp></choose>|g;
    } elsif ($$rule[1]) # right context
    {
      my $right = $$rule[1];
      if ($right =~ /cond:(.*)/)
      {
        my $rcond = $1;
        $line =~ s|<expan>$$rule[2]</expan>($rcond)|<choose n="$ruleId"><abbrev>$$rule[3]</abbrev><exp>$$rule[4]</exp></choose>$1|g;  
      } else
      {
        $line =~ s|<expan>$$rule[2]</expan>$$rule[1]|<choose n="$ruleId"><abbrev>$$rule[3]</abbrev><exp>$$rule[4]</exp></choose>|g;
      }
    } else # no context needed
    {
      $line =~ s|<expan>$$rule[2]</expan>|<choose n="$ruleId"><abbrev>$$rule[3]</abbrev><exp>$$rule[4]</exp></choose>|g;
    }
  }
  if (/<expan>/)
  {
    warn $_;
  }
  while (/<expan>(.*?)<\/expan>/g)
  {
    $unKnown{$1}++;
  } 
  if ($HTML)
  {
    s/<div[^<>]*n=["'](.*?)["']/doDiv($1,$&) . $&/egi;
    s|</div>|</td></tr></table></div>|i;
    s/<l[^<>]*n=["'](.*?)["'][^<>]*>/<div class='line'><span style='font-size:8px; color:#888888'>$1<\/span> /g;
    s/<\/l>/<\/div>/g;
    s/<choose[^<>]*><abbrev>(.*?)<\/abbrev><exp>(.*?)<\/exp><\/choose>/HTMLAbbrev($2,$1,$&)/eg;
    s/<expan>(.*?)<\/expan>/<font size="+1" weight='bold' color='red'><u>$1<\/u><\/font>/gi;
    if ($printExamples)
    {
      while (/data-rule=["'](.*?)["']/g)
      {
        my $ruleId = $1;
        if (!$examples{$ruleId}) { $examples{$ruleId} = (); };
        my $cleanedLine=$_;
        $cleanedLine =~ s/.*(<div class=.line)/$1/;
        push(@{$examples{$ruleId}},$cleanedLine);
      }
    }
  }
  $line =~ s/<expan>(.*?)<\/expan>/<choose n='-1'><expan>$1<\/expan><abbrev>UNKNOWN<\/abbrev><\/choose>/g;
  $line =~ s/<exp>/<expan>/g;
  $line =~ s/<\/exp>/<\/expan>/g;
  warn "Fixed: $line";
  return $line;  
}

foreach my $x (sort { $unKnown{$b} <=> $unKnown{$a} } keys %unKnown)
{
  warn "!!$x\t$unKnown{$x}\n";
}

if ($HTML && $printExamples)
{
  foreach my $ruleId (sort { $a <=> $b }  keys %examples)
  {
    my $rule = $id2rule{$ruleId};
    my @examplez = @{$examples{$ruleId}};
    if (@examplez > 5)
    {
      warn "Te groot: " + (@examplez + 0);
      @examples = @examples[0..4];
      warn @examples + 0;
    }
    my $list = join(" ", @examplez[0..4]);
    #warn $list;
    my @rTemp = @$rule;
    for (my $i=0; $i < @rTemp; $i++)
    {
      $rTemp[$i] = '"' . $rTemp[$i] . '"';
    }
    my $ruleAsString =  join(", ", @rTemp); 
    $ruleAsString =~ s/&/&amp;/g;

    print <<END;
<div style="font-size:14pt">Rule $ruleId:  ($$rule[3]-->$$rule[4]) <tt>[$ruleAsString]</tt></div>
$list
END
  }
}

sub HTMLAbbrev
{
  my ($expansion, $abbreviation,$chooseTag) = @_;
  my $xa = $abbreviation;
  $xa =~ s/&/&amp;/g;
  my $ruleId;
  my $rule;
  if ($chooseTag =~ /n="(.*?)"/)
  {
    $ruleId = $1;
    $rule = join(", ", @{$id2rule{$ruleId}});
  } 
return "<span data-rule=\"$ruleId\" title=\"$xa --> $expansion r$ruleId:($rule)\">$abbreviation<\/span>";
}

sub doDiv
{
  my ($n,$div) = @_;
  my $file = sprintf("156730%03d.png",$n);
  my $img = "<img width='600' src='SamplePNG/$file'>";
  return <<END;
  <hr class='pb'>
  <h2>Page $n</h2>
  <hr>
<table><tr valign="top"><td>$img</td><td>
END
}
