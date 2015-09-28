
open(P,"paragraph_text.tab");

while(<P>)
{
  chomp();
  my ($f,$l) = split(/\t/,$_);
  $safe{$f}{$l}++;
}

while(<>)
{
  chomp();
  if (/(.*)_[0-9]{2}_[0-9]{2}_(.*)/)
  {
    my ($f,$l) = ($1,$2);
    if ($safe{$f}{$l})
    {
      print $_ . "\n";
    }
  }
}
