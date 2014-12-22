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
    my %lwh;
    while ($fws =~ /[^\s,\]\[]+/g)
    {
      my $x = $&;
      $x =~ s/\\//g;
      $fwh{$x}++;
    } 
    while ($lws =~ /[^\s,\]\[]+/g)
    {
      $lwh{$&}++;
    }

    my ($gt,$htr) = split(/\s*\$\s*/,$f);

    my $F="-";    
    my $L="-";
    my $printMe=0;
    if (firstWord($gt) ne firstWord($htr)) 
    {
      my $P=0;
      $printMe=1;
      if (0 || firstWord($gt) =~ /[A-Z]/i)
      {
        $P = defined($fwh{firstWord($gt)});
        if ($P)
        {
          $re++;
        } else
        {
          $P=0;
        }
        $e++;
      }
      $F=$P;
    }
    if (lastWord($gt) ne lastWord($htr))
    {
      my $P;
      if (1 || lastWord($gt) =~ /[A-Z]/i)
      {
        $P = defined($lwh{lastWord($gt)});
        if ($P)
        {
          $rel++;
        } else
        {
          $P=0;
        }
        $el++;
      }
      $L=$P;
      $printMe=1;
#     print "$P // $_\n";
    }
    if ($printMe)
    {
      print "$F$L // $_\n";
    }
  }
}

warn "e=$e, re=$re el=$el rel=$rel\n";
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
