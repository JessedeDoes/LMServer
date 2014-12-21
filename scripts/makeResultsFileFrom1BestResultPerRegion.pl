# 115_009_002_02_16.fea <s> AS WELL AS FROM SUFFERING FROM THE INCLEMENCY OF </s>

my $TDIR = "BenthamData/Transcription";
opendir(TRANS,$TDIR) || die $TDIR;

my @files = sort {$a cmp $b} readdir(TRANS);

foreach my $x (@files)
{
  my $line = $x; $line =~ s/.txt//;
  my @parts = split(/_/,$line);
  my $region = join("_", @parts[0..3]);
  if ($line =~ /_/)
  {
    open(L,"$TDIR/$x") ||  die $x;
    while(<L>)
    {
      chomp();
      $textOf{$region} .= " " . $_;
    }
    close(L);
    ## warn "$region ($line)" . '-->' . $textOf{$region};
  }
}

while(<>)
{
  if (/^(.*)\.fea.*<s>(.*)<\/s>/)
  {
    my ($line,$text) = ($1,$2); 
    my @parts = split(/_/,$line);
   my $region = join("_", @parts[0..3]);

    my $reftxt = uc $textOf{$region};
    $reftxt =~ s/\s+/ /g;
    if (!$reftxt)
    {
       die "No text for $region!!!";
    }
    print "$reftxt \$  $text\n";
    close(R);
  }
}
