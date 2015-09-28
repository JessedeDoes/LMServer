# arguments: list directory
my ($list,$dir) = @ARGV;
open(L,$list) || die $list;

while(<L>)
{
  chomp();
  s/\r//;
  $inList{$_}++;
}

print "#!MLF!#\n";

opendir(D,$dir)|| die $dir;

while(my $x = readdir(D))
{
  my $name = $x;
  $name =~ s/\..*//;
  if ($inList{$name})
  {
    open(F,"$dir/$x");
    $x =~ s/\..*//;
    print("\"*/$x.lab\"\n");
    while(<F>)
    {
      chomp();
      s/ /@/g;
      my $z =  join("\n", split(//,$_));
      $z =~ s/<.*?>/my $x=$&; $x =~ s|\s*||gs; $x/egs;
      $z =~ s/(^|\n)([0-9]+)/$1"$2"/g;
      print $z . "\n";
    }
    print "@\n.\n";
  }
}
