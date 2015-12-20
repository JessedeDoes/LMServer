

#my $RESULT_DIR = shift @ARGV;
my $TRANSCRIPTION_DIR = shift @ARGV;
my $LINE_DIR = shift @ARGV;

warn $TRANSCRIPTION_DIR;

my @gtFiles = split(/\n/, `find $TRANSCRIPTION_DIR/ -name "*.txt" | sort`);

my $flerrs=0;
my $fwerrs=0;
print "<table>";
foreach my $gt (@gtFiles)
{
  my $r = $gt;
  $r =~ s/.*\///;
  $r =~ s/.txt$//;

  warn $r;
   my $gtTxt;
#  my $gt = "$TRANSCRIPTION_DIR/$r.txt";
  open(R,$gt);
  while(<R>)
  {
    chomp();
    $gtTxt .= $_;
  }
  $gtTxt =~ s/^\s+//;
  $gtTxt =~ s/\s+$//;
  close(R);
  my $image = "$LINE_DIR/$r.png";
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

