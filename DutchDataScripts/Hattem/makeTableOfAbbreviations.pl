while(<>)
{
  while(/<choose.*?<\/choose>/gs)
  {
    my $ch = $&;
    if ($ch =~ /<abbrev.*?>(.*?)<\/abbrev>/)
    {
      my $abbrev = $1;
      if ($ch =~ /<expan.*?>(.*?)<\/expan>/)
      {
         my $expan = $1;
         #print "$abbrev\t$expan\n";
         $expansion{$abbrev}{$expan}++;
         $charfreq{$abbrev}++;
      }
    }
  }
}

print "<table border=border>\n";

foreach my $c (sort {$charfreq{$b} <=> $charfreq{$a}}  (keys %charfreq))
{
  my $h = $expansion{$c};
  my $f = $charfreq{$c};
  $ce = $c; $ce =~ s/&/&amp;/;
  print "<tr><td>$c</td><td>$ce</td><td>$f</td><td></td></tr>\n";
  foreach my $e (sort keys %$h)
  {
     print "<tr><td><td>$e<td>$$h{$e}<td></tr>\n";
  }
}

print "</table>";
