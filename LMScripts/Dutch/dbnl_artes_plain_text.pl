my $mnl = 0;
use utf8;
binmode(stdout,":encoding(utf8)");

while(<>)
{
  s/&para;/¶/g;
  s/&nbsp;/ /g;

  if (/<!--[^<>]*begin[^<>]*mnl/i) 
  {
    warn $&;
    $mnl =1;
  } elsif (/<!--[^<>]*eind[^<>]*mnl/i)
  {   
    warn $&;
    $mnl=0;
  }

  s/<[^<>]*>//g;
  print if ($mnl);
}
