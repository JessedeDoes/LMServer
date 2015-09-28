undef $/;
while(<>)
{
  my $file = $ARGV;
  $file =~ s/\.xml//;
  $file =~ s/.*\///;
  while (/<TextRegion.*?<\/TextRegion>/gs)
  {
     my $tr = $&;
     my $tag = $tr; $tag =~ s/>.*/>/s;
     my $pid;
     if ($tag =~ /id=["'](.*?)["']/)
     {
       $pid = $1;
     } ;
      
     if ($tag =~ /=["']paragraph/)
     {
       while ($tr =~ /<TextLine[^<>]*>/gs)
       {
         my $line = $&;
         my $lid;
         if ($line =~ /id=["'](.*?)["']/)
         {
            $lid = $1;
         } ;

         print $file . "\t"  . $pid . _ .  $lid . "\n";
       }
     }
  }
}
