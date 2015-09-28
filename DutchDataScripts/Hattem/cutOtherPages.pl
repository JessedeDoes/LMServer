my $selectedPages = shift;

open(S,$selectedPages) || die $selectedPages;
while(<S>)
{
  chomp();
  $selected{$_}=1;  
}

my $file = shift;

open(f,$file) || die $file;

$/ = "</div>";

while(<f>)
{
  s/.*(<div type=.page)/$1/;
  if (/n=["'](.*?)["']/)
  {
    my $n=$1;
    if (!$selected{$n})
    {
      my $file= sprintf("156730%03d.tei",$n);
      open(OUT,">NotSelectedPages/$file");
      print OUT $_;
      close(OUT);
    }
  }
}
