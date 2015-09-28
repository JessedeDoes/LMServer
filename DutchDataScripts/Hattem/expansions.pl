while(<>)
{
  chomp();
  while (/[^<>\s]?<expan>(.*?)<\/expan>/g)
  {
    my $expan = $&;
    $expan =~ s/\s*//g;
    $count{$expan}++;
    $example{$expan} = $_;
    $file{$expan} = $ARGV;
  }
}

foreach my $x (sort {$count{$b} <=> $count{$a}} keys %count)
{
  print "$x\t$count{$x}\t$file{$x}\t$example{$x}\n";
}
