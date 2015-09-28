my $file = shift;

open(f,$file) || die $file;

$/ = "</div>";

sub getTextKey
{
  my $gtLine = shift;
  if ($gtLine =~ /<Unicode>(.*?)<\/Unicode>/)
  {
    my $gtText = $1; 
    $gtText =~ s/<!\[CDATA\[//;
    $gtText =~ s/\]\]>//;
    my $text = $gtText;
    $text =~ s/<abbrev>(.*?)<\/abbrev>//g;
    $text =~ s/&gt;/>/g;
    $text =~ s/&lt;/</g;
    $text =~ s/<[^<>]*>//g;
    $text =~ s/\[.*?\]//g;
    $text =~ s/waterachjpge/waterachtige/;
    $text =~ s/almachjpghe/almachtighe/;
    $text =~ s/\s+//g;
    $text =~ s/ghelijkealsdesomer$/ghelijkealsdesomere/;
    $text = lc $text;
##    warn "GNA $text";
    return $text;
  }
}

# <Coords points="87,197 1250,221 1241,2179 78,2156" />

sub getVerticalPosition
{
   my $r = shift;
   if ($r =~ /<Coords.*?points="(.*?)"/)
   {
     my $ay = 0;
     my $pointz = $1;
     my @points = split(/\s+/,$pointz);
     foreach my $p (@points)
     {
        my ($x,$y) = split(/,/, $p);
        $ay += $y; 
     } 
     $ay = $ay / @points;
     warn "$pointz-->$ay"; 
     return $ay;
   }
}

while(<f>)
{
  s/.*(<div type=.page)/$1/;
  if (/n=["'](.*?)["']/)
  {
    my $n=$1;
    $page{$n} = $_;
    my $file= sprintf("156730%03d.xml",$n);
    $file2page{$file} = $n;
  }
}

$/="\n";

my $GTDir = shift;
opendir(GT,"$GTDir");

undef $/;
while (my $x = readdir(GT))
{
  warn "Reading GT file $x";
  my $n = $file2page{$x};
  if (!(-f "$GTDir/$x"))
  {
    warn "Hm $x";
    next;
  }
  if (!$n || !$page{$n})
  {
    die $x;
  }  
  warn "\n\n\nPAGE: $x\n";
  open(f,"$GTDir/$x") || die "$x not found";
  while(<f>)
  {
    my $newname = $x;
    $newname =~ s/.xml/.withAbbreviations.xml/;
    open(OUT,">Fixed/$newname");
    my $gtPage = $_;
    my $fixedPage  = fixPage($gtPage,$page{$n},$x);
    print OUT $fixedPage;
    close(OUT);
  }
}

sub fixPage
{
  my ($gtPage,$page, $file) = @_;
  my %lineHash;
  my %text2LineHash;
  my $k=0;
  while ($page =~ /<l [^<>]*>(.*?)<\/l>/gs)
  {
    
    $lineHash{$k++} = $&;
    my $text = $1;
    my $line = $text;
    $text =~ s/<abbrev>(.*?)<\/abbrev>//g;
    $text =~ s/<[^<>]*>//g;      
    $text =~ s/\s+//g;
    $text =~ s/\[.*?\]//g;
    $text = lc $text;
    $text2LineHash{$text} = $line;
    $text2LineNumber{$text} = $k;
#    warn "New key: $text ($k)\n";
  }
  warn "Lines in TEI: $k";
  $k=0;
  $gtPage =~ s/<TextLine.*?<\/TextLine>/fixLine($&, $lineHash{$k++},\%text2LineHash)/egs;
  warn "Lines in GT: $k";
  foreach my $txt (sort keys %text2LineHash) # why no more unused lines???
  {
     my $n = $text2LineHash{$txt};
     warn "At page: $file, unused line $n: ($txt) $lineHash{$n}\n";
  }
  $gtPage =~ s/<TextRegion.*?<\/TextRegion>/orderLinesInRegion($&,\%text2LineNumber)/egs;
  $gtPage = orderRegions($gtPage);
  return $gtPage;
}

sub orderLinesInRegion
{
   my ($region,$hash) = @_;
   my @lines;
   my @numbers;
   my %number2line;
   while ($region =~  /<TextLine.*?<\/TextLine>/gs)
   {
     my $line= $&;
     my $k = getTextKey($line);
     my $n =  $$hash{$k};
     push(@numbers,$n);
     $number2line{$n} = $line;
     if (!$n && !($k =~ /^\s*[0-9]+\s*/s))
     {
       die "HMPF ($line/$k)" . $$hash{getTextKey($line)};
     }
     push(@lines,$line);
   } 
   if (@numbers != @lines)
   {
     warn "Not able to sort lines in $region";
     return $region;
   }
   my @linesSorted = map { $number2line{$_} } sort { $a <=> $b } @numbers;
#  my @linesSorted = sort { $$hash{getTextKey($a)} <=> $$hash{getTextKey{$b}}  } @lines;
   my $newLines = join("\n", @linesSorted);
   $region =~ s/<TextLine.*<\/TextLine>/$newLines/s;
   return $region;
}


sub fixLine
{
  my ($gtLine, $line, $t2l) = @_;
  my %text2Line = %$t2l;

  if ($gtLine =~ /<Unicode>(.*?)<\/Unicode>/)
  {
    my $gtText = $1;
    my $text = getTextKey($gtLine);
    my $line2 = $text2Line{$text};
    if ($line2)
    {
      ## warn "Text match: $gtText ... --> ...  (key $text)\n\n";
      $gtLine =~ s/<Unicode>(.*?)<\/Unicode>/<Unicode><!\[CDATA\[$line2\]\]><\/Unicode>/;
##      warn "DELETE KEY FOR $text\n";
      delete $$t2l{$text};
    } else
    {
      warn "No text match: $gtText  #$text#";
    }
  }
  return $gtLine;
}


sub orderRegions
{
  my @regions;
  my $page = shift;
  while ($page =~ /<TextRegion.*?<\/TextRegion>/gs)
  {
    push(@regions,$&);
  }
  my @sortedRegions = sort { getVerticalPosition($a) <=> getVerticalPosition($b) } @regions;
  my $newRegions = join("\n", @sortedRegions);
  $page =~ s/<TextRegion.*<\/TextRegion>/$newRegions/s;
  return $page;
}
### the order of lines is actually wrong in the GT. We should fix it as well in this script ....
