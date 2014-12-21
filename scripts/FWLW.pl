while(<>)
{
  chomp();
  $_ = uc $_;
  if (/(.*?)#(.*)/)
  {
    my ($f,$s) = ($1,$2);
    $s =~ s/#//;
    my ($fws,$lws) = split(/#/,$s);
    my %fwh;
    while ($fws =~ /[^\s,\]\[]+/g)
    {
      $fwh{$&}++;
    } 
    my ($gt,$htr) = split(/\s*\$\s*/,$f);
    if (firstWord($gt) ne firstWord($htr)) 
    {
      my $P = defined($fwh{firstWord($gt)});
      if ($P)
      {
        $re++;
      } else
      {
        $P=0;
      }
      $e++;
      print "$P // $_\n";
    }
  }
}

warn "e=$e, re=$re\n";
sub firstWord
{
  my $x = shift;
  $x =~ s/^\s*//;
  $x =~ s/\s+.*//;
  return $x;
}

sub lastWord
{
  my $x = shift;
  $x =~ s/\s*$//;
  $x =~ s/.*\s+//;
  return $x;
}
