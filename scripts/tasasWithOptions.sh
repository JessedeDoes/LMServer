nofirst=`perl scripts/removeFirst.pl -r first $1`
nolast=`perl scripts/removeFirst.pl -r last $1`
noboth=`perl scripts/removeFirst.pl -r both $1`
plain=`perl scripts/removeFirst.pl -r none $1`
ainsens=`perl scripts/removeFirst.pl -a -i -s '%' -r none $1`
easy=`perl scripts/removeFirst.pl -a -i -p -r both $1`
first=`perl scripts/removeFirst.pl -i -p -r keepfirst $1`
last=`perl scripts/removeFirst.pl -i -p -r keeplast $1`
echo "all words: $plain; case and accent insensitive: $ainsens; first: $first; withoutfirst: $nofirst; last: $last; without last: $nolast; without both: $noboth; without both,ci,nopunct:  $easy"
