while(<>)
{
  chomp();
  if (/\!(ENTER|EXIT)/)
  {
    next;
  }
  if (/<S>/)
  {
    next;
  }
  if (/<\/S>/)
  {
    next;
  }
  s/^"<s>"\t.*/<s>\t[]/;
  s/^"<\/s>"\t.*/<\/s>\t[]/;
  my ($n, $n2, $p, $chars) = split(/\t/,$_);
  $chars =~ s/<.*?>/my $x = $&; $x =~ s|\s+||g; $x/eg;
  print join("\t", $n,$n2,$p,$chars) . "\n";
}
