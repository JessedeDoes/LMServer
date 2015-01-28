# 115_009_002_02_16.fea <s> AS WELL AS FROM SUFFERING FROM THE INCLEMENCY OF </s>

use Getopt::Std;
our $opt_i=0;
getopts('i');
my $insens=$opt_i;

while(<>)
{
  if (/^(.*)\.(lattice|fea).*<s>(.*)<\/s>(.*)/)
  {
    my ($line,$text,$extra) = ($1,$3,$4); 
    $line =~ s/.*\///;
    my $ref = "./data/Transcription/$line.txt";
    my $reftxt;
    open(R,$ref);
    while(<R>) { chomp(); $reftxt .=  $_; };

    $reftxt = trim($reftxt);
    $text = trim($text);
    if ($insens)
    {
       $reftxt = uc $reftxt;
       $text = uc $text;
    }
    if ($extra)
    {
      print "$reftxt \$ $text # $extra\n";
    } else
    {
       print "$reftxt \$ $text\n";
    }
    close(R);
  }
}

sub trim
{
  my $x = shift;
  $x =~ s/^\s+|\s+$//g;
  $x =~ s/\s+/ /g;
  return $x;
}
