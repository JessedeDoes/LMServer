binmode(stdin, ":encoding(utf8)");
binmode(stdout, ":encoding(utf8)");

my $pilcrow = sprintf("%c", 182);

my $useExpansion=0;
while(<>)
{
   while (/<l.*?<\/l>/gs)
   {
       my $text = $_;
       chomp($text);
       $text =~ s/<!\[CDATA\[//;
       $text =~ s/\]\]>//;
       $text =~  s/&lt;/</g;
       $text =~ s/&gt;/>/g;
       $text =~ s/\[.*?\]//g;
       if ($useExpansion)
       {
         $text =~ s/<abbrev>(.*?)<\/abbrev>//g;
       } else
       {
          $text =~ s/<expan>(.*?)<\/expan>//g;
       }
       $text =~ s/<[^<>]*>//g;
       $text =~ s/$pilcrow/<ESP>/g;
       $text =~ s/\//<SLASH>/g;
       $text =~ s/&#x([A-Z0-9]*);/<X$1>/gi;
#       $text =~ s/#//g;
       print "$text\n";
    }
}
