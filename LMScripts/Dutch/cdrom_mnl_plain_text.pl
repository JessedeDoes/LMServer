my $mnl = 1;
undef $/;
use utf8;
binmode(stdout,":encoding(utf8)");

while(<>)
{
  s/.*<\/teiHeader>//s;
  s/&para;/¶/g;
  s/&nbsp;/ /g;
  s/&gt;/>/g;
  s/&lt;/</g;
  s/&amp;/&/g;
  s/<[^<>]*>//g;

  s/-->//g;
  s/<!--//g;

  s/&\S+;//g;

  print if ($mnl);
}
