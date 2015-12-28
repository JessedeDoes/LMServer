for x in `ls ../Hattem/*`; do echo $x; y=`basename $x`; echo $y; perl -pe 's/HATTEM/MEERMANNO/g' $x > $y; done
