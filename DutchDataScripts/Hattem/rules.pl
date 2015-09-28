my %unKnown;
my $combcurl = "1dce"; # &er; teken is 035b
my $zwnj = "200c"; # zero-width non-joiner
our $DIRK = 1;
our $DRAM = $DIRK?"z":"&#xf2e6;";
our $RECIPE = $DIRK? "R&#xa76d;" :"&#x211e;";
our %rule2id;
our %id2rule;
our @rules = 
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
  ["", "", "onse",    "&#x2125;", "onse"],
  ["R", "", "ecipe",  $RECIPE, "Recipe"], # 2113 is ook leuk voor eventuele haal?
                                             # anders gewoon R + &#xa76d;?? (vindt Dirk)
  ["", "", "librae",  "&#xf2ea;", "librae"],
  ["S", "", "emis", "S&#x0306;", "semis"], # hoe doe je semis teken in mufi???   small ezh?
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


sub processText
{
   my $text = shift;
    $text =~ s/<choose[^<>]*><abbrev>(.*?)<\/abbrev><expan>(.*?)<\/expan><\/choose>/HTMLAbbrev($2,$1,$&)/eg;
    $text =~ s/<expan>(.*?)<\/expan>/<font size="+1" weight='bold' color='red'><u>$1<\/u><\/font>/gi;
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
  return $text;
}
1;
