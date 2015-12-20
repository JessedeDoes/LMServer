my $SRILM="/mnt/Projecten/transcriptorium/Tools/SRILM/bin/i686-m64";

my $SRILM_HOME="/home/jesse/Tools/srilm";
$SRILM="$SRILM_HOME/bin/i686-m64";

my $dir = shift @ARGV;;
# $SRILM/lattice-tool -read-htk -in-lattice Lattices/002_579_001_02_07.lattice -ref-file /tmp/phrase
opendir(D,$dir);

my $refDir= shift @ARGV;
while($x = readdir(D))
{
  if ($x =~ /lat$/)
  {
    my $ref = $x; $ref =~ s/.lat/.txt/i;
    my $refT = "$refDir/$ref";
#    warn "$x $ref";
#    system("tr a-z A-Z < $refT > Temp/$ref");    

    my $C =  "$SRILM/lattice-tool -read-htk -in-lattice $dir/$x -ref-file $refT" ;
    my $Z = `$C`;

    if ($Z =~ /sub ([0-9]+) ins ([0-9]+) del ([0-9]+) wer ([0-9]+) words ([0-9]+)/)
    {
      $sub += $1; $ins += $2; $del += $3; $words += $5;
      my $lineWER = $4;
      warn $lineWER;
      if ($lineWER == 0)
      {
        $goodLines++;
      }
    } else
    {
       warn "FAILURE: $Z"; 
    }
  }
}

my $WER = ($sub + $ins + $del) / $words;
my $recall = 1 - ($sub + $del) / $words;
print "sub=$sub ins=$ins del=$del words=$words\n";
print "Perfect lines; $goodLines\n";
print "word recall: $recall\n";
print "LATTICE WER: $WER\n";

