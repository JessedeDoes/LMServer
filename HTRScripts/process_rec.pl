use utf8;

binmode(stdout,":encoding(utf8)");

foreach my $x (@ARGV)
{
  open(X ,$x);
  my $S;
  while(<X>)
  {
    chomp();
    my @f = split(/\s+/,$_);
    my $txt = $f[2];
    $txt =~ s/\\([0-9]+)/chr(oct($1))/eg;
    utf8::decode($txt);
    $txt =~ s/[↵↳]+/ /g;
    $txt =~ s/ +/ /g;
    $S .= $txt;
  }
  $S =~ s/\s+/ /g;
  close(X);
  print "$S\n";
}
