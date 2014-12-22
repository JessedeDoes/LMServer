my $dic = shift;

open(D,$dic) || die $dic;

my $noPunctuation  = 1;
my $onlyPunctuation = 0;
my $minLength = 0;
my $maxLength = 1e6;
my $onlyLinesWithoutOOV = 0;
my $caseInsensitive = 0;
my $fixDuplicates = 1;
my $removeFunctionWords = 0;

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
  my ($W,$w,$rest) = split(/\s+/,$_);
  $w =~ s/^\[//;
  $w =~ s/\]$//;
  $known{$w}++;
}

my $N=0;
my $OOV=0;
my $T=0;
my $E=0;
my $errorFreeLines=0;
my $nLines=0;

while(<>)
{
  chomp();
  if ($caseInsensitive)
  {
    $_ = lc $_;
  }
  if ($noPunctuation)
  {
    s/(^|\s+)\p{P}+(\s+|\$|$)/$1$2/g;
    s/_//g;
  } elsif ($onlyPunctuation)
  {
    s/\p{L}//g;
  }

  my ($gt,$htr) = split(/\s*\$\s*/,$_);

  next if (length($gt) < $minLength || length($gt) > $maxLength);

  my %h1;
  my %h2;
  my $lOOV=0;
  my $lN=0;
  my @unk;
  my $i=1;
  my %position;

  while ($gt =~ /\S+/g)
  {
    my $w = $&;
    
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
    my $k = $h2{$w};
    if ($k && $fixDuplicates)
    {
      $h2{$w . "_" . $k}++;
    }
    $h2{$w}++;
  }

  my $e=0;

  my @missed;

  foreach my $w (keys %h1)
  {
    my $d = min($position{$w}, $i - $position{$w});
    $nWordsAtPosition{$d}++;
    next if ($isNonContent{lc $w});
    if (isWord($w) && !($h2{$w}))
    {
      $e++;
      if ($h1{$w} == 1)
      {
        $errorsAtPosition{$d}++;
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

my $dist="";
foreach my $x (sort {$a <=> $b} keys %errorsAtPosition)
{
  my $rap = sprintf("%0.2f", $errorsAtPosition{$x} / $nWordsAtPosition{$x});
  $dist .= "$x:$errorsAtPosition{$x} ($rap) ";
}

warn "OOV rate: $rate ($OOV / $N); ER=$er ($E $T)\n";
warn "$nLines lines, $errorFreeLines without missed word\n";
warn "$dist\n";

