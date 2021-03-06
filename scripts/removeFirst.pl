use Getopt::Std;
use utf8;
binmode(stdin,":encoding(utf8)");
my $args = join(" ", @ARGV);
our $opt_i=0;
our $opt_p=0;
our $opt_l=1;
our $our_c=0;
our $opt_a=0;
our  $opt_s='\$';
our $opt_r='first';
getopts('aipcs:l:r:');
my $insens=$opt_i;


my $noPunctuation  = $opt_p;
my $onlyPunctuation = 0;
my $minLength = 0;
my $maxLength = 1e6;
my $onlyLinesWithoutOOV = $opt_c;
my $caseInsensitive = $opt_i;
my $fixDuplicates = 1;
my $removeFunctionWords = 0;
my $firstWordGedoe = 0;
my $lumpFirstLast= $opt_l;
my $remove=$opt_r;

my %diacritics = 
(
  "Í"=>"I",
  "Ó"=>"O",
  "Ú"=>"U",
  "á"=>"a",
  "â"=>"a",
  "Ç"=>"C",
  "ç"=>"c",
  "è"=>"e",
  "é"=>"e",
  "ê"=>"e",
  "í"=>"i",
  "ñ"=>"n",
  "ó"=>"o",
  "ú"=>"u",
  "ü"=>"u"
);

my ($errorsFirst, $errorsLast, $errorsMid, $errsNoFirst, $errorsNoLast);
my ($nFirst, $nLast, $nMid, $nNoFirst, $nNoLast);
my @fw = qw/which him let from up has had was were he is of a  in on me have the it their yes no on for their his her she hers might be/;


my $N=0;
my $OOV=0;
my $T=0;
my $E=0;
my $errorFreeLines=0;
my $nLines=0;
my $firstLetterErrors=0;

open(OUT,">/tmp/simplified.txt");

while(<>)
{
  chomp();

  if ($opt_a)
  {
    $_ = removeDiacritics($_);
  }

  if ($caseInsensitive)
  {
    $_ = uc $_; # ahem ????
  }

  my ($gt,$htr) = split(/\s*$opt_s\s*/,$_);

  if ($noPunctuation)
  {
    $gt =~ s/(^|\s+)\p{P}+(\s+|\$|$)/$1$2/g;
    $gt =~ s/_//g;
    $htr =~ s/(^|\s+)\p{P}+(\s+|\$|$)/$1$2/g;
    $htr =~ s/_//g;

  } elsif ($onlyPunctuation)
  {
    $gt =~ s/\p{L}//g;
    $htr =~ s/\p{L}//g;
  }
   
  $gt =~ s/^\s+//; $htr =~ s/^\s+//;

  if ($firstWordGedoe)
  {
    $gt =~ s/\s+.*//;
    $htr =~ s/\s+.*//;
    $gt = join(" ",split(//,$gt));
    $htr = join(" ",split(//,$htr));
  }

  next if (length($gt) < $minLength || length($gt) > $maxLength);

  my %h1;
  my %h2;
  my $lOOV=0;
  my $lN=0;
  my @unk;
  my $i=1;
  my %position;

  my @gtWords;
  my @htrWords;

  while ($gt =~ /\S+/g)
  {
    my $w = $&;
    push(@gtWords,$w); 
  } 

  while ($htr =~ /\S+/g)
  {
    my $w = $&;
    push(@htrWords,$w);
  }

  if ($remove eq 'first' || $remove eq  'both')
  {
    shift @gtWords;
    shift @htrWords;
  }
  if ($remove eq 'last' || $remove eq  'both')
  {
    pop @gtWords;
    pop @htrWords;
  }

  if ($remove eq 'keepfirst')
  {
    @gtWords = @gtWords[0..0];
    @htrWords = @htrWords[0..0]; 
  }

  if ($remove eq 'keeplast')
  {
    @gtWords = (pop @gtWords);
    @htrWords = (pop @htrWords);
  }

  print OUT join(" ", @gtWords) . " $opt_s " . join(" ", @htrWords) . "\n";
}

close(OUT);

my $cmd = "tasas /tmp/simplified.txt -ie -s \" \"  -f \"$opt_s\"";
warn $cmd;
my $tasas = `tasas /tmp/simplified.txt -ie -s " "  -f "$opt_s"`;

print "$tasas\n";


sub noDiac
{
  my $x = shift;
  if ($diacritics{$x})
  {
    return $diacritics{$x};
  }
  return $x;
}

sub removeDiacritics
{
  my $x = shift;
  $x =~ s/./&noDiac($&)/eg;
  return $x;
}


