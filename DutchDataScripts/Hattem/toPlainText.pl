my $file = shift;

open(f,"<:encoding(utf8)", $file) || die $file;

$/ = "</div>";

while(<f>)
{
  s/.*(<div type=.page)/$1/;
  if (/n=["'](.*?)["']/)
  {
    my $n=$1;
	s/<expan[^<>]*>.*?<\/expan>//g;
	s/<[^<>]*>//g;
	s/&#x(\S+?);/noEntity($1)/eg;
    $page{$n} = $_;
  }
}

$/="\n";

foreach my $n (sort {$a <=> $b} keys %page)
{ 
  my $file= sprintf("156730%03d.txt",$n);
  open(OUT,">:encoding(utf8)", "PlainText/$file");
  print OUT $page{$n};
  close(OUT);
}

sub noEntity
{
  my $x = shift;
  my $n = eval("0x$x");
  return sprintf("%c",$n);
}
