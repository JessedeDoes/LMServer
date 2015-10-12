#|/bin/bash

PROG=${0##*/}
TMP=/tmp/$PROG.$$
trap "rm $TMP* 2>/dev/null" EXIT

if [ $# -ne 3 ]; then
  echo "Usage: $PROG <HTK-Dictionary-file> <WG-Input-Dir> <WG-Output-Dir>" 1>&2
  exit 1
fi

DIC=$1
DWGIN=$2
DWGOUT=$3

[ -e $DIC ] || { echo "ERROR: File \"$DIC\" does not exist "'!' 1>&2; exit 1; }
[ -d $DWGIN ] || { echo "ERROR: Dir \"$DWGIN\" does not exist "'!' 1>&2; exit 1; }
#[ -d $DWGOUT ] || { echo "ERROR: Dir \"$DWGOUT\" does not exist "'!' 1>&2; exit 1; }

if [ -d $DWGOUT ]; then
  echo "WARNING: Dir \"$DWGOUT\" already exists "'!' 1>&2;
  else
  mkdir -p $DWGOUT 2>/dev/null
fi


awk '{
       la=length($1); lb=length($2); 
       A=substr($1,2,la-2); B=substr($2,2,lb-2);
       print A,B
     }' $DIC | sed -r "s/\\\//g" > $TMP

awk -v od=$DWGOUT -v id=$DWGIN -v fpron=$TMP \
  'BEGIN{while (getline < fpron > 0) {C[$1]++; D[$1,C[$1]]=$2}}
  {
    if (/^J=/) {
      wrd=substr($4,3);
      if (wrd!~/<s>|<\/s>|\!NULL/) {
        prn=substr($5,3);
        gsub("=","",D[wrd,prn]);
        if (length(D[wrd,prn])) $4="W="D[wrd,prn];
        $5="v=1";
      }
    }
    auxfname=FILENAME;
    sub(/^.*\//,"",auxfname);
    print >> od"/"auxfname;
    fflush(od"/"auxfname);
  }' $DWGIN/*.lat

