# arguments: list directory
my ($list,$dir) = @ARGV;
open(L,$list) || die $list;

while(<L>)
{
  chomp();
  s/\r//;
  $inList{$_}++;
}


opendir(D,$dir)|| die $dir;
while(my $x = readdir(D))
{
  my $name = $x;
  $name =~ s/\..*//;
  if (-f "$dir/$x" && !$inList{$name})
  {
    open(F,"$dir/$x");
    $x =~ s/\..*//;
#   print("\"*/$x.lab\"\n");
    while(<F>)
    {
      print "$_\n";
    }
#    print ".\n";
  }
}
