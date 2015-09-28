my %lineHash;

sub getVerticalPositionOfLine
{
  my $l = shift;
  my ($y,$n) = (0,0);
  foreach my $p (@{$l->{points}})
  {
    $n++;
    $y += $p->{y}; 
  }
  my $v = $y/$n;
  warn "vertical: $v";
  return $v;
}

# <Coords points="87,197 1250,221 1241,2179 78,2156" />
# <Coords> <Point x="201" y="189"/> <Point x="347" y="190"/>

sub getVerticalPosition
{
   my $r = shift;
   if ($r =~ /<Coords[^<>]*?points="(.*?)"/)
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
#     warn "$pointz-->$ay"; 
     return $ay;
   }
}

sub coordsXML
{
  my $l = shift;
  my @pz;
  foreach my $p (@{$l->{points}})
  {
    push(@pz, "$p->{x},$p->{y}"); 
  } 
  my $px  = join(" ", @pz);
  return "<Coords points=\"$px\"/>";
}

sub getPoints
{
  my $r = shift;
  my @points;
  if ($r =~ /<Coords.*?<\/Coords>/s)
  {
    my $c = $&;
    while ($c =~ /<Point.*?>/g)
    {
      my $p = $&;
      if ($p =~ /x=["'](.*?)["']/)
      {
        my $x = $1;
        if ($p =~ /y=["'](.*?)["']/)
        {
          my $y = $1;
          my $point = (); $point->{x}=$x; $point->{y} = $y;
#         warn "New point ($point->{x},$point->{y})";
          push(@points,$point); 
        }
      }
    }
  }
  return \@points;
}

my $BasilisDir = shift;


undef $/;
opendir(B,$BasilisDir) || die $BasilisDir;
while (my $x = readdir(B))
{
  if (!($x =~ /xml/i))
  {
    next;
  } 
  my @linesInFile;
#  warn "Basilis lines for $x";
  open(X,"$BasilisDir/$x");
  my $n;
  if ($x =~ /[0-9]{4,100}/)
  {
    $n = $&;
  } 
  while (<X>)
  {
    while (/<TextLine.*?<\/TextLine>/gs)
    {
      my $line = $&;
#      warn $line;
      my $l = ();
      $l -> {xml} = $line;
      $l -> {points} = getPoints($line);
      $l -> {verticalPosition} = getVerticalPositionOfLine($l);
      $l -> {xml} = $line;
      push(@linesInFile,$l);
    }
  }
  $lineHash{$n} = \@linesInFile;
}

my $GTDir = shift;
opendir(GT,"$GTDir");

while (my $x = readdir(GT))
{
  if (!($x =~ /xml/i))
  {
    next;
  }
  warn "########################### Reading GT file $x";
  if (!(-f "$GTDir/$x"))
  {
    warn "Hm $x";
    next;
  }
  my $n;
  if ($x =~ /[0-9]{4,100}/)
  {
    $n = $&;
  }
  open(f,"$GTDir/$x") || die "$x not found";
  while(<f>)
  {
    my $newname = $x;
    $newname =~ s/.xml/.withBasisisLines.xml/;
    open(OUT,">Fixed/$newname");
    my $gtPage = $_;
    my $fixedPage  = fixPage($gtPage,$n);
    print OUT $fixedPage;
    close(OUT);
  }
}

sub fixPage
{
  my ($gtPage,$n) = @_;
  my $basilisLines = $lineHash{$n};
  $gtPage =~ s/<TextLine.*?<\/TextLine/fixLine($&,$basilisLines)/egs;
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
  my ($gtLine, $bLines) = @_;
  my $v = getVerticalPosition($gtLine);
  # now look for closest $bLine..
  my $dMin=10000;
  my $closestLine;
  foreach my $l (@$bLines)
  {
#   warn "Check line for $v, $l->{verticalPosition}";
    my $d = abs($l->{verticalPosition} - $v);
    if ($d < $dMin)
    {
      $dMin =$d;
      $closestLine = $l;
    }
  }
  warn "dMin for vPos $v: $dMin";
  $gtLine =~ s/<Coords[^<>]*>/coordsXML($closestLine)/e;
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
