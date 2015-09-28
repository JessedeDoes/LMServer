while(<>)
{
  s/^ngram 1=([0-9]+)/"ngram 1=" . ($1-4)/e;
  next if (/!ENTER|!EXIT|<S>|<\/S>/);
  print;
}
