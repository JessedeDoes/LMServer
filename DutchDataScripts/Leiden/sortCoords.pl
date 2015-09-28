while(<>)
{
  s/&lt;i&gt;/&lt;choice&gt;&lt;expan&gt;/gi;
  s/&lt;\/i&gt;/&lt;\/expan&gt;&lt;\/choice&gt;/gi;
  s/<Baseline[^>]*>/fixBaseline($&)/eg;
  print;
}

sub fixBaseline
{
  my $x = shift;
  $x =~ s/points="(.*?)"/"points=\"" . fixCoords($1) . '"'/e;
  return $x;
}

sub fixCoords
{
  my $c  = shift;
  my @C = split(/\s+/,$c);
  my @X = sort byX @C;
  my $c1 = join(" ", @X);
  if ($c1 ne $c)
  {
    warn "$c -> $c1 at $ARGV\n\n";
  }
  return $c1;
}

sub byX
{
#  my ($a,$b) = @_;
  my $a1 = $a;
  my $b1 = $b;
  $a1 =~ s/,.*//;
  $b1 =~ s/,.*//;
#  warn "$a $b";
  return ($a1 <=> $b1);
}
