use Getopt::Std;

my $args = join(" ", @ARGV);
our $opt_i=0;
our $opt_p=0;
our $opt_l=1;
our $our_c=0;
our  $opt_s='\$';

getopts('ipcs:l:');
my $insens=$opt_i;

my $dic = shift;
open(D,$dic) || die $dic;

my $noPunctuation  = $opt_p;
my $onlyPunctuation = 0;
my $minLength = 0;
my $maxLength = 1e6;
my $onlyLinesWithoutOOV = $opt_c;
my $caseInsensitive = $opt_i;
my $fixDuplicates = 1;
my $removeFunctionWords = 0;
my $firstWordGedoe = 0;
my $lumpFirstLast= $opt_l;


my ($errorsFirst, $errorsLast, $errorsMid, $errsNoFirst, $errorsNoLast);
my ($nFirst, $nLast, $nMid, $nNoFirst, $nNoLast);
my @fw = qw/which him let from up has had was were he is of a  in on me have the it their yes no on for their his her she hers might be/;

if ($removeFunctionWords)
{
 foreach my $z (@fw)
 {
   $isNonContent{$z}++; 
 }
}

while(<D>)
{
  chomp();
  my ($W,$w,$prob,$letters) = split(/\s+/,$_);
  $w =~ s/^\[//;
  $w =~ s/\]$//;
  if ($w)
  {
    $known{$w}++;
#    warn "known: $w";
  } else
  {
#   my ($prob, $letters) = split(/\t/,$rest);
    $W =~ s/^"//; $W =~ s/"$//;
    $letters =~ s/ //g;
    $letters =~ s/\@//g;
#    warn "<$letters>";
    $known{$W}++;
  }
}

my $N=0;
my $OOV=0;
my $T=0;
my $E=0;
my $errorFreeLines=0;
my $nLines=0;
my $firstLetterErrors=0;

while(<>)
{
  chomp();

  if ($caseInsensitive)
  {
    $_ = uc $_; # ahem ????
  }

  if ($noPunctuation)
  {
    s/(^|\s+)\p{P}+(\s+|\$|$)/$1$2/g;
    s/_//g;
  } elsif ($onlyPunctuation)
  {
    s/\p{L}//g;
  }
   
   
  my ($gt,$htr) = split(/\s*$opt_s\s*/,$_);
  $gt =~ s/^s+//; $htr =~ s/^s+//;
  if ($firstWordGedoe)
  {
    $gt =~ s/\s+.*//;
    $htr =~ s/\s+.*//;
    $gt = join(" ",split(//,$gt));
    $htr = join(" ",split(//,$htr));
  }
  next if (length($gt) < $minLength || length($gt) > $maxLength);

  my %h1;
  my %h2;
  my $lOOV=0;
  my $lN=0;
  my @unk;
  my $i=1;
  my %position;

  my @gtWords;
  my @htrWords;
  while ($gt =~ /\S+/g)
  {
    my $w = $&;
    push(@gtWords,$w); 
    my $k = $h1{$w};
    my $wx = $w;

    if ($k && $fixDuplicates)
    {
      $wx = $w . "_" . $k;
      $h1{$wx}++;
    }

    $h1{$w}++;

    if (!$known{$w})
    {
      push(@unk,$w);
      $OOV++;
      $lOOV++;
    }

    $N++ if (isWord($w)); 
    $lN++;
    $position{$wx}=$i;
    $i++;
  } 

  while ($htr =~ /\S+/g)
  {
    my $w = $&;
    push(@htrWords,$w);
    my $k = $h2{$w};
    if ($k && $fixDuplicates)
    {
      $h2{$w . "_" . $k}++;
    }
    $h2{$w}++;
  }

  if ($gtWords[0] ne $htrWords[0])
  {
    my ($wg,$wh) = ($gtWords[0], $htrWords[0]);
    my ($lg, $lh) = ($wg,$wh);
    $lg =~ s/(.).*/$1/;
    $lh =~ s/(.).*/$1/;
    if ($lg ne $lh)
    {
      warn "Ouch $wg $wh $lg $lh!";
      $firstLetterErrors++;
    }
  }
  my $e=0;

  my @missed;

  foreach my $w (keys %h1)
  {
    my $d = $lumpFirstLast?min($position{$w}, $i - $position{$w}):$position{$w};
    my $p = $position{$w};

    # $d = $position{$w};

    $nWordsAtPosition{$d}++;
    next if ($isNonContent{lc $w});
    if ($p ==1) { $nFirst++ } else { $nNoFirst++ } ;
    if ($p == $i-1)  { $nLast++ }  else { $nNoLast ++ };
    if ($p > $1 && $p < $i-1) { $nMid++ };

    if (isWord($w) && !($h2{$w}))
    {
      $e++;

      if ($h1{$w} == 1)
      {
        $errorsAtPosition{$d}++;
      }

      if ($p == $i-1)
      {
        $errorsLast++;
      }  else
      {
        $errorsNoLast++;
      }
      if ($p == 1)
      {
        $errorsFirst++;
      } else
      {
        $errorsNoFirst++;
      }
      if ($p > 1 && $p < $i-1)
      {
        $errorsMid++;
      } 
      push(@missed, $w . "/" . $d . "($i)");
    }
  }

  foreach my $w (keys %h2)
  {
    if (!($h1{$w}))
    {
#     $e++;
    }
  }

  my $m = join(",", @missed);
  my $u = "";
  if (@unk > 0) { $u = "Unknown: " . join(",",@unk); };
  if ($e > 0)
  {
    warn "$e errors <$m>\n\t$gt\n\t$htr\n$u\n\n";
  } else
  {
    $errorFreeLines++;
  }
  if (!$onlyLinesWithoutOOV  || !$lOOV)
  {
    $T += $lN;
    $E += $e;
  }
  $nLines++;
}

sub isWord
{
  my $w = shift;
  return 1;
  return w =~ /[a-z0-9]/i;
}

sub min
{
  my ($x,$y) = @_;
  return ($x,$y)[$x > $y];
}

my $er = $E / $T;
my $rate = $OOV  / $N;
my $flRate = $firstLetterErrors / $nLines;
my $dist="";
foreach my $x (sort {$a <=> $b} keys %errorsAtPosition)
{
  my $rap = sprintf("%0.2f", $errorsAtPosition{$x} / $nWordsAtPosition{$x});
  $dist .= "$x:$errorsAtPosition{$x} ($rap) ";
}

my $eFirst = $errorsFirst / $nFirst;
my $eLast = $errorsLast / $nLast;
my $eNoFirst = $errorsNoFirst / $nNoFirst;
my $eNoLast = $errorsNoLast / $nNoLast;
my $eMid = $errorsMid / $nMid;

warn "OOV rate: $rate ($OOV / $N); ER=$er ($E $T); First letter CER=$flRate\n";
warn "$nLines lines, $errorFreeLines without missed word\n";
warn "$dist\n";

print "######## Computed from $args #########\n";
print "OOV rate: $rate ($OOV / $N); ER=$er ($E $T); First letter CER=$flRate\n";
print "ER first: $eFirst; ER last: $eLast; ER without first: $eNoFirst; ER without last: $eNoLast; ER mid: $eMid\n";
print "$nLines lines, $errorFreeLines without missed word\n";
print "$dist\n";
