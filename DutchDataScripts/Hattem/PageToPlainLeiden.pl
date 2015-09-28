# beter van de partities uitgaan....
# dat levert meer op
use utf8;

my $MODE_ABBREVIATIONS=0;
my $MODE_EXPANSIONS=1;
my $MODE_BOTH=2;
my $OUTPUTFOLDER_LINES="../TranscribedLinesForHTR";
my $MODE=0;
my $LEIDEN=0;

my $subFolder="Abbreviations";

my $imageDir="/datalokaal/Scratch/HTR/Leiden_HTR/Lines/";

sub readListFromFolder
{
  my $imageDir = shift;
  opendir(D,$imageDir);

  while(my $x = readdir(D))
  {
    $x =~ s/\.(jpg|png)$//i;
    if ($x =~ /(.*)_[0-9]{2}_[0-9]{2}_(.*)/)
    {
      my ($f,$l) = ($1,$2);
      $image{$f}{$l} = $x;
    }
  }
}

sub readList
{
  my $list = shift;
  open(L,$list);
  while (<L>)
  {
    chomp();
    my $x = $_;
    if ($x =~ /(.*)_[0-9]{2}_[0-9]{2}_(.*)/)
    {
      my ($f,$l) = ($1,$2);
      $trainingImage{$f}{$l} = $x;
    }
  }
}

my $useExpansion=0;

binmode(stdout, ":encoding(utf8)");

my $pilcrow = sprintf("%c", 182);

sub doFile
{
  my $file = shift;
  my $text="";
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
        $text =~ s/\[.*?\]//g;

        if ($MODE == $MODE_EXPANSIONS)
        {
          $text =~ s/<abbrev>(.*?)<\/abbrev>//g;
        } elsif ($MODE==MODE_ABBREVIATIONS)
        {
          $text =~ s/<expan>(.*?)<\/expan>//g;
        } else
        {
          $text =~ s/<abbrev>(.*?)<\/abbrev><expan>(.*?)<\/expan>/{{$1:::$2}}/g;  
        }
  
        ## warn $text;

        $text =~ s/<[^<>]*>//g;
        $text =~ s/$pilcrow/<ESP>/g;
        $text =~ s/§/<PARA>/g;
        $text =~ s/\/\//<HYPHEN>/g;
        $text =~ s/\//<SLASH>/g;
        
        if ($MODE == $MODE_BOTH) 
        {
          $text =~ s/\{\{(.*?):::(.*?)\}\}/<$1_$2>/g;
          $text =~ s/&#x([A-Z0-9]*);/X$1/ig;
        } else
        { 
          $text =~ s/&#x([A-Z0-9]*);/<X$1>/ig; 
        }
        $text =~ s/#//g; # onzichtbare hyphen, kan je niets mee
       
         
        my $img = $image{$pageNumber}{$rid . "_" . $lid};

        my $outFile = sprintf("%s_%02d_%02d.txt", $pageNumber,$rIndex,$lIndex);

        if ($LEIDEN)
        {
          $outFile = sprintf("%s_%s_%s.txt", $pageNumber,$rid,$lid);
        };

        if ($trainingImage{$pageNumber}{$rid . "_" . $lid})
        {
          my $label = toLabel($img,$text);
          print LABELS "$label\n";
        }

        if ($LEIDEN)
        {
          open(OUT,">:encoding(utf8)", "$OUTPUTFOLDER_LINES/$subFolder/$img.txt");
        } else
        {
           open(OUT,">:encoding(utf8)", "$OUTPUTFOLDER_LINES/$subFolder/$outFile");
        }

        print OUT "$text\n";
        close(OUT);
      } 
      $lIndex++;
      
    }
    $rIndex++;
  }
}

sub toLabel
{
  my ($name, $text) = @_;
  my $l = "\"*/$name.lab\"\n";
  $text =~  s/[ \t]+/@/g;
  my $z =  join("\n", split(//,$text));
  $z =~ s/<.*?>/my $x=$&; $x =~ s|\s*||gs; $x/egs;
  $z =~ s/(^|\n)([0-9]+)/$1"$2"/g;
  return $l . "@\n" . $z . "\n\@\n.";
}

$MODE = shift @ARGV;
if ($MODE eq $MODE_EXPANSIONS) { $useExpansions = 1; $subFolder="Expanded"; };
readListFromFolder($imageDir);
readList(shift @ARGV);
open(LABELS,">labels.out");
print LABELS "#!MLF!#\n";
map { doFile($_) } @ARGV;
