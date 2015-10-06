my %split;
binmode(stdout,':encoding(utf8)');
my $corpus = shift @ARGV;
open(C, "<:encoding(utf8)", $corpus);

while(<C>)
{
  while(/\p{L}+/g)
  {
    if (length($&) > 2)
    {
#      warn $&;
      $freq{$&}++;
    }
  }
}

close(C);

open(W, ">:encoding(utf8)","/tmp/wordlist.txt");
foreach my $w (keys %freq)
{
  print W "$w\n";
}
close(W);

open(X, "bash scripts/run.dutch.withargs.sh /tmp/wordlist.txt | ");
while(<X>)
{
  chomp();
  my ($w,$s) = split(/\t/, $_); 
  $s =~ s/\//_/g;
  $split{$w} = $s;
}
close(X);

sub splitWord
{
  my $w = shift;
  if ($split{$w}) { return $split{$w} };
  return $w;
}
open(C, "<:encoding(utf8)", $corpus);

while(<C>)
{
  s/\p{L}+/splitWord($&)/eg;
  print;
}

close(C);

 


