binmode(stdout,":encoding(utf8)");

my $RESULT_DIR = shift @ARGV;
my $TRANSCRIPTION_DIR = shift @ARGV;
my $LINE_DIR = shift @ARGV;
my $DIC = shift @ARGV;

if ($DIC)
{
  open(D,$DIC);
  while(<D>)
  {
    chomp();
    my ($W,$w,$prob,$letters) = split(/\s+/,$_);
    $w =~ s/^\[//;
    $w =~ s/\]$//;
    if ($w)
    {
      $known{$w}++;
#    warn "known: $w";
    } else
    {
#     my ($prob, $letters) = split(/\t/,$rest);
      $W =~ s/^"//; $W =~ s/"$//;
      $letters =~ s/ //g;
      $letters =~ s/\@//g;
#      warn "<$letters>";
      $known{$W}++;
    }
  }
}

my @resultFiles = split(/\n/, `find $RESULT_DIR -name "*.rec" | sort`);

my $flerrs=0;
my $fwerrs=0;
print <<END;
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8"></meta>
</head>
<body>
<table>
END
foreach my $r (@resultFiles)
{
#  warn $r;
  open(R,$r);
  my $recTxt;
  while(<R>)
  {
    chomp();
    my @f = split(/\s+/, $_);
    my  $w = $f[2];

    $w =~ s/\\([0-9]+)/chr(oct($1))/eg;
    utf8::decode($w);

    $recTxt .= $w . " ";
  }  
  close(R);


  $recTxt =~ s/\s+$//;
  $recTxt =~ s/^\s+//;

  warn $recTxt;

  $r =~ s/\.rec$//;
  $r =~ s/.*\///;

  my $gtTxt;
  my $gt = "$TRANSCRIPTION_DIR/$r.txt";
  open(R,"<:encoding(utf8)", $gt);

  while(<R>)
  {
    chomp();
    $gtTxt .= $_;
  }

  $gtTxt =~ s/^\s+//;
  $gtTxt =~ s/\s+$//;
  $gtTxt = markOOV($gtTxt);
  close(R);
  my $image = "$LINE_DIR/$r.png";
  if (firstWord($gtTxt) ne firstWord($recTxt))
  {
    $fwerrs++;
  }
  if (0 && (firstLetter($gtTxt) ne firstLetter($recTxt)))
  {
    $recTxt =~ s/./<font style='border-color:red; border-width:5px; border-style:solid' weight='bold' color='red'>$&<\/font>/;
    $flerrs++;
  }
  print "<tr><td>$gtTxt</td><td>$recTxt</td><td><img height='80px' src='$image'/></td></tr>\n" ; 
  $lines++; 
}

print "</table>";

my $flrate = $flerrs / $lines;
my $fwrate = $fwerrs / $lines;

warn "fl:$flrate fw:$fwrate";
sub lastLetter
{
  my $x = shift;
  $x =~ s/.*(.)$/$1/;
#  warn $x;
  return $x;
}

sub firstLetter
{
  my $x = shift;
  $x =~ s/(.).*/$1/;
#  warn $x;
  return $x;
}

sub firstWord
{
  my $x = shift;
  $x =~ s/\s+.*//;
#  warn $x;
  return $x;
}

sub markOOV
{
  my $txt = shift;
  $txt =~ s/\S+/my $w = $&; if (!$known{$w}) { "<b>$w<\/b>"} else { $w }/eg;
  return $txt;
}
