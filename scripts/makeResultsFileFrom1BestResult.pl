# 115_009_002_02_16.fea <s> AS WELL AS FROM SUFFERING FROM THE INCLEMENCY OF </s>

while(<>)
{
  if (/^(.*)\.lattice.*<s>(.*)<\/s>(.*)/)
  {
    my ($line,$text,$extra) = ($1,$2,$3); 
    $line =~ s/.*\///;
    my $ref = "./data/Transcription/$line.txt";
    my $reftxt;
    open(R,$ref);
    while(<R>) { chomp(); $reftxt .=  $_; };
    print "$reftxt \$  $text # $extra\n";
    close(R);
  }
}
