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
       sed 's/<\/add>//g' |
       sed 's/<add>//g' |
       sed 's/<\/catchword>//g' |
       sed 's/<catchword>//g' |
       sed 's/<\/del>//g' |
       sed 's/<del>//g' |
       sed 's/<\/foreign>//g' |
       sed 's/<foreign>//g' |
       sed 's/<\/hi>//g' |
       sed 's/<pageNum[^>]*>//g' |
       sed 's/<\/pageNum>//g' |
       sed 's/<pageNum>//g'  |
       sed 's/<\/unclear>//g'|
       sed 's/<unclear>//g' | 
       sed 's/<choose n=[^>]*>//g' | 
       sed 's/<\/choose>//g' |
       sed 's/<lb\/>//g' | 
       sed 's/<choice>//g' |
       sed 's/<\/choice>//g' | 
       sed 's/<und>//g' | 
       sed 's/<\/und>//g' |
       sed 's/\&\#x2014;/_/g'|sed 's/\&amp;/\&/g' > ${OUT}/$n; 
done

