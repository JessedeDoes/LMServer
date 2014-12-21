my $TDIR = "BenthamData/Transcription";
while(<>)
{
  chomp(); s/\r//;
  my $line = $_;
  my @parts = split(/_/,$line);
  my $page = join("_", @parts[0..2]);
  $includePage{$page}++;
}

opendir(TRANS,$TDIR) || die $TDIR;

my @files = sort {$a cmp $b} readdir(TRANS);

foreach my $x (@files)
{
  my $line = $x; $line =~ s/.txt//;
  my @parts = split(/_/,$line);
  my $page = join("_", @parts[0..2]);
  if ($includePage{$page})
  {
    open(L,"$TDIR/$x") ||  die $x;
    while(<L>)
    {
      chomp();
      $textOf{$page} .= " " . $_;
    }
    close(L);
  }
}

foreach my $p (sort keys %textOf)
{
  my $t = $textOf{$p};
  $t =~ s/\s*//;
  print "$t\n";
}
