my $file = shift;

open(f,$file) || die $file;

$/ = "</div>";

while(<f>)
{
  s/.*(<div type=.page)/$1/;
  if (/n=["'](.*?)["']/)
  {
    my $n=$1;
    $page{$n} = $_;
  }
}

$/="\n";

while(<>)
{
  chomp();
  my $n=$_;
  my $file= sprintf("156730%03d.tei",$n);
  open(OUT,">SelectedPages/$file");
  print OUT $page{$n};
  close(OUT);
}
