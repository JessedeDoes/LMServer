while(<>)
{
  while(/<w.*?>(.*?)<\/w>/g)
  {
    my $w=$1;
    my $w1=$w; my $w2=$w;
    $w1 =~ s/<abbrev>(.*?)<\/abbrev>//g;
    $w2 =~ s/<expan[^<>]*>.*?<\/expan>//g;
    $w1 =~ s/<.*?>//g; $w2 =~s/<.*?>//g;
    $w1 = normalize($w1);
    $count{$w1}++;
    $count1{$w1}{$w2}++;
#    print "$w1 -- $w2\n";
  }
}

foreach my $w (sort {$count{$b} <=> $count{$a}} keys %count)
{
  print "$w $count{$w}  / ";
  foreach my $w2 (keys %{$count1{$w}})
  {
    my $relFreq = $count1{$w}{$w2} / $count{$w};
    print "$w2 $relFreq ";
  }
  print "\n";
}

sub normalize
{
  my $w=shift;
  $w =~ s/ae/aa/g;
  $w =~ s/j/i/g;
  $w =~ s/u/v/g;
  $w = lc $w;
  $w =~ s/gh/g/g;
  $w =~ s/ck/k/g;
  return $w;
}
