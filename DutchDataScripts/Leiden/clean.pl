my $list = shift @ARGV;

open(L,$list);

while(<L>)
{
  chomp();
  $ok{$_}++;
}

while (<>)
{
  my @w;
  s/#//g;
  while (/\S+/g)
  {
     push(@w,$&);
  }
  for (my $i=0; $i < @w; $i++)
  {
    my $x = $w[$i];
    my $x0=$x;
    $x0 =~ s/<.*?>/if ($ok{$&}) { $&; } else { "" }/eg;
    my $x1 = $x;
    if ($x0 eq $x)
    {
    } else
    {
      $w[$i] = "NOPE:$w[$i]";
    }
    $x1 =~ s/<.*?>//g; 
    my $x2=$x1;
    $x2 =~ s/./if ($ok{$&}) { $&; } else { "" }/eg;
    if ($x2 eq $x1)
    {
    } else
    {
       $w[$i] = "NOPE:$w[$i]";
    }
  }
  my $line = join(" ", @w) . "\n";
  $line =~ s/NOPE:\S+//g;
  $line =~ s/ +/ /g;
  $line =~ s/<HYPHEN>/-/g;
  print $line;
}
