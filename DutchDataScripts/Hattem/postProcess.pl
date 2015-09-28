undef $/;
my $addStructure=1;
while(<>)
{
  s/<p>([0-9]+)<\/p>/<pb n="$1"\/>/g;
  s/<p>(.*?)<\/p>/&processP($&)/egs;
  if ($addStructure)
  {
    s/<pb[^<>]*n="([0-9]*)"\/>/<SPLIT>$&/gs;
    s/<pb[^<>]*n="([0-9]*)"\/>(.*?)(<SPLIT>|<\/body>)/<div type='page' n="$1">$2<\/div>\n$3/gs;
    s/<SPLIT>//g;
  }
  s/<hi[^<>]*>(.*?)<\/hi>/<expan>$1<\/expan>/gs;
  print;
}

sub processP
{
    my $p = shift;
    $p =~ s/<p>([0-9]+)\s*/<p><lb n="$1"\/>/g;
    $p =~ s/<lb\/>([0-9]+)\s*/<lb n="$1"\/>/g;
    if ($addStructure)
    {
      $p =~ s/<lb[^<>]*n="([0-9]*)"\/>/<SPLIT>$&/g;
      $p =~ s/<lb[^<>]*n="([0-9]*)"\/>(.*?)(<SPLIT>|<\/p>)/<l n="$1">$2<\/l>\n$3/gs;
      $p =~ s/<SPLIT>//g;
    }
    return $p;
}
