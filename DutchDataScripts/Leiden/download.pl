my $k=1;

while(<>)
{
  while (/<a .*?<\/a>/gs)
  {
    my $a = $&;
    my $title;
    if ($a =~ /title="(.*?)"/)
    {
      $title = $1;
    }
     if ($a =~ m|https://nl.yousendit.com/directDownload\?phi_action=app/directDownload.*?experience=bas|s)
    {
      my $link = $&;
      $link =~ s/&amp;/&/g;
      warn "($k): $title --> $link\n"; 
      $k++;
      if ($title =~ /tif/i)
      {
        system("wget -O /mnt/Scratch/Jesse/LeidenImages/$title '$link'");
      }
    }
  }
}
