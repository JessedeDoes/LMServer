my $resultados = shift @ARGV;

my @resultFiles = split(/\n/, `find $resultados -name "*.rec" | sort`);

my $flerrs=0;
my $fwerrs=0;
print "<table>";
foreach my $r (@resultFiles)
{
#  warn $r;
  open(R,$r);
  my $recTxt;
  while(<R>)
  {
    chomp();
    my ($t1, $t2, $w, $c) = split(/\s+/, $_);
    $recTxt .= $w . " ";
  }  
  close(R);
  $recTxt =~ s/\s+$//;
  $recTxt =~ s/^s+//;
  $r =~ s/\.rec$//;
  $r =~ s/.*\///;
  my $gtTxt;
  my $gt = "BenthamData/Transcription/$r.txt";
  open(R,$gt);
  while(<R>)
  {
    chomp();
    $gtTxt .= $_;
  }
  $gtTxt =~ s/^\s+//;
  $gtTxt =~ s/\s+$//;
  close(R);
  my $image = "BenthamData/Images/Lines/Test/$r.png";
  if (firstWord($gtTxt) ne firstWord($recTxt))
  {
    $fwerrs++;
  }
  if (firstLetter($gtTxt) ne firstLetter($recTxt))
  {
    $recTxt =~ s/./<font style='border-color:red; border-width:5px; border-style:solid' weight='bold' color='red'>$&<\/font>/;
    $flerrs++;
  }
  print "<tr><td>$gtTxt</td><td>$recTxt</td><td><img height='80px' src='$image'/></td></tr>\n" ; 
  $lines++; 
}

print "</table>";

my $flrate = $flerrs / $lines;
my $fwrate = $fwerrs / $lines;

warn "fl:$flrate fw:$fwrate";
sub lastLetter
{
  my $x = shift;
  $x =~ s/.*(.)$/$1/;
  warn $x;
  return $x;
}

sub firstLetter
{
  my $x = shift;
  $x =~ s/(.).*/$1/;
  warn $x;
  return $x;
}

sub firstWord
{
  my $x = shift;
  $x =~ s/\s+.*//;
  warn $x;
  return $x;
}

