use POSIX;
my $listFile = shift @ARGV;
my $nPortions = shift @ARGV;


open(l,$listFile) || die $listFile;
my @lines;
while(<l>)
{
  chomp();
  push(@lines,$_);
}

my $pSize = floor(@lines / $nPortions)+1;


for (my $i=0; $i < $nPortions; $i++)
{
  open(OUT,">$listFile.$i");
  for (my $j = ($i*$pSize); $j < ($i+1) * $pSize; $j++)
  {
    if ($j < @lines)
    {
      print OUT  $lines[$j] . "\n";
    }
  }
  close(OUT);
}
