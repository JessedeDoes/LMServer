#!/bin/bash

# export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -ne 2 ]; then
 echo "Uso: ${0##*/} <Input-dir> <Output-dir>" 
 exit
fi

IN=$1
OUT=$2

[ -d $IN ] || { echo "ERROR: Directory \"${IN}\" does not exist \!"; exit 1; }
[ -d $OUT ] || mkdir $OUT


for f in ${IN}/*.txt; do 
       n=`basename $f`;
       sed 's/<hi[^>]*>//g'  $f | 
       perl -pe 's/<abbrev>.*?<\/abbrev>//g' |
       perl -pe  's/<.*?>//g' |
       sed 's/\&\#x2014;/_/g'|sed 's/\&amp;/\&/g' > ${OUT}/$n;
done

