use utf8;

binmode(stdout,":encoding(utf8)");

my $SUB=0;
my ($RESULTS,$GTDIR) = @ARGV;
opendir(D,$RESULTS);

while (my $x = readdir(D))
{
  open(X ,"$RESULTS/$x");
  my $HTR;
  my $GT;
  while(<X>)
  {
    chomp();
    my @f = split(/\s+/,$_);
    my $txt = $f[2];

    $txt =~ s/\\([0-9]+)/chr(oct($1))/eg;

    utf8::decode($txt);
    if ($SUB)
    {
      $txt =~ s/[↵↳]+/ /g;
      $txt =~ s/ +/ /g;
      $HTR .= $txt;
    } else
    { 
      $txt =~ s/[↵↳]+/ /g;
      $txt =~ s/ +/ /g;
      $HTR .= $txt . " ";
    }
  }
  $HTR =~ s/\s+/ /g;
  close(X);
  my $y = $x;
  $y =~ s/\.rec/\.txt/;
  open(Y,"<:encoding(utf8)", "$GTDIR/$y");
  while(<Y>)
  {
    $GT .= $_;
  }
  chomp($GT);
  close(Y);
  print "$GT \$ $HTR\n";
}
