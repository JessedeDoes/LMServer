my $T = 0;
my $i=0;
my $N = 100;
my $SAMPLESIZE=100;
while ($i++ < $N)
{
  system("randlines.pl $SAMPLESIZE fich_results > fich_results.part");
  my $rate =  `./bin/tasas fich_results.part -ie -s '  ' -f '\$'`; 
  push(@rates,$rate);
  $T += $rate;
}

my $mean = $T / $N;

my $dev2 = 0;
foreach my $r (@rates)
{
  my $d = ($r - $mean) * ($r - $mean);
  $dev2 += $d;
}

my $dev = sqrt($dev2 / $N);
print <<END;
Mean: $mean
Standard deviation: $dev
END


