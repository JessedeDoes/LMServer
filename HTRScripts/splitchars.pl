binmode(stdin,":encoding(utf8)");
binmode(stdout,":encoding(utf8)");


while(<>)
{
  s/(\S)(\S)/$1 $2/g;
  s/(\S)(\S)/$1 $2/g;
  print;
}
