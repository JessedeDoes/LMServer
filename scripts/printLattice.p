my %word;
while(<>)
{
  if (/I=\s*(.*?)\s*W=(.*?)\s*$/)
  {
#    warn "$1 -> $2";
    $word{$1}=$2;
  } else
  {
    s/([SE])=([0-9]+)/$1=$word{$2}/g;
    print;
  }
}
