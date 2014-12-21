my $train = "BenthamData/PlainText/Train.txt";
my $test = "BenthamData/PlainText/Test.txt";
my $validation = "BenthamData/PlainText/Validation.txt";

sub munchFile
{
  my $f = shift;
  my @lines;
  my $k=0;
  open(F,$f) || die $f;
  while(<F>)
  {
    chomp();
    s/\r//;
    my $line = $_;
    my %words;
    my $l = ();
    $l->{line} = $line;
    my $h = ();
    my  $L = 0;
    while ($line =~ /\S+/g)
    {
      $$h{$&}++; 
      $L++;
    }
    $l->{words} = $h;
    $l->{length} = $L;
    $l->{number} = $k;

    push(@lines,$l);
    $k++;
  }
  return @lines;
}

my @testLines = munchFile($test);
my @trainLines = munchFile($train);
my @validationLines = munchFile($validation);
my @trainVal = (@trainLines,@validationLines);
my $k=0;
foreach my $t (@testLines)
{
  next if ($t ->{length} < 5);
  my $h = $t->{words};
  
#  warn $t->{line};
#  warn join(" " , sort keys %$h);
  foreach my $t1 (@trainVal)
  {
    my $s = overlap($t->{words},$t1->{words});
    my $k1 = $t1->{number};
    if ($s > 0.5)
    {
       my $l = $t->{line};
       my $l1 = $t1->{line};
        
       warn "Test line $k / train line $k1: ($s) $l ==== $l1\n";
       last;
    }
  }
  $k++;
}

sub overlap
{
  my ($h1,$h2) = @_;
  my $N=0;  
  my $O=0;
  foreach my $k (keys %$h1)
  {
    $N++;
    if ($$h2{$k})
    {
      $O++;
    }
  } 
  foreach my $k (keys %$h2)
  {
    if (!$$h1{$k}) { $N++; }
  }
  return $O / $N;
}

