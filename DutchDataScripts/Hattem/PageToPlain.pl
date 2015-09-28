my $useExpansion=0;

binmode(stdout, ":encoding(utf8)");
my $pilcrow = sprintf("%c", 182);

sub doFile
{
  my $file = shift;
  my $text="";
  my $L=0;
  open(F,"<:encoding(utf8)", $file) || die $file;
  my $pageNumber = $file;
  $pageNumber =~ s/.*\///;
  $pageNumber =~ s/\..*//;
  while(<F>)
  {
    $text .= $_;
  }
  my $rIndex = 1;
  while ($text =~ /<TextRegion.*?<\/TextRegion/gs)
  {
    my $region = $&;
    my $rid;
    if ($region =~ /id=["'](.*?)['"]/)
    {
      $rid=$1;
    }
    my $lIndex = 1;
    while ($region =~ /<TextLine.*?<\/TextLine>/gs)
    {
      my $line = $&;
      my $lid;
      if ($line =~ /id=["'](.*?)['"]/)
      {
        $lid=$1;
      }

#   <choose n="1"><abbrev>&#x0113;</abbrev><expan>en</expan></choose>
      if ($line =~ /<Unicode>(.*?)<\/Unicode>/)
      {
        my $text = $1;
        $text =~ s/<!\[CDATA\[//;
        $text =~ s/\]\]>//;
        $text =~  s/&lt;/</g;
        $text =~ s/&gt;/>/g;

        $text =~ s/&amp;/&/g;

        $text =~ s/\[.*?\]//g;
        if ($useExpansion)
        {
          $text =~ s/<abbrev>(.*?)<\/abbrev>//g;
        } else
        {
          $text =~ s/<expan>(.*?)<\/expan>//g;
        }
        # warn $text;
        $text =~ s/<[^<>]*>//g;
        $text =~ s/$pilcrow/<ESP>/g;
        $text =~ s/\//<SLASH>/g;
        $text =~ s/&#x([A-Z0-9]*);/<X$1>/ig; 

        warn $L++ . ": " . $text;
        $text =~ s/#//g;
        my $outFile = sprintf("%s_%02d_%02d.txt", $pageNumber,$rIndex,$lIndex);
        ## $outFile = sprintf("%s_%s_%s.txt", $pageNumber,$rid,$lid);
        my $outFolder = $useExpansion?"Expanded":"Abbreviations";
        open(OUT,">:encoding(utf8)", "TranscribedLinesForHTR/$outFolder/$outFile");
        print OUT "$text\n";
        close(OUT);
      } 
      $lIndex++;
      
    }
    $rIndex++;
  }
}

$useExpansion = shift @ARGV;
warn join("\n", @ARGV);
map { doFile($_) } @ARGV;
