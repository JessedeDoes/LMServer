require("rules.pl");

my $imageDir="/datalokaal/Scratch/HTR/Leiden_HTR/Lines/";
$imageDir="LeidenLines";
opendir(D,$imageDir);
while(my $x = readdir(D))
{
  if ($x =~ /(.*)_[0-9]{2}_[0-9]{2}_(.*)/)
  {
    my ($f,$l) = ($1,$2);
    my $lineId = "$1_$2";
    $lineId =~ s/\.png$//;
    $id2image{$lineId} = "$imageDir/$x";
  }
}

my $HTML=0;
my $printExamples=1;
my $plainText = 0;
if ($HTML)
{
print <<END;
<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8"></meta>
<style type="text/css">
body { font-family: Junicode }
.pb { page-break-before: always }
</style>
</head>
<body>
END
}

my $xmlDir="./TestLeidenAbbr";

undef $/;
opendir(D,$xmlDir);
print "<table>";
while (my $x = readdir(D))
{
  open(X,"$xmlDir/$x");
  $x =~ s/.xml$//i;
  while(<X>)
  {
    my $text = $_;
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
        if ($line =~ /<Unicode>(.*)<\/Unicode>/s)
        {
          my $linetext = $1;
          my $html = processText($linetext);
          my $id = $x . "_" . $rid . "_" . $lid;
          my $image = $id2image{$id};
          print "<tr><td>$html</td><td><img height='80px' src='$image'/></td></tr>\n" ;
        }
      }
    }
  } 
}

print "</table>";
