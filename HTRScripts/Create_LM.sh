#!/bin/bash

# export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -lt 3 ]; then
 echo "Uso: ${0##*/} <Text-file> <HTK-Dic-file> <LM-file>" 
 exit
fi

TEXT=$1
DIC=$2
LM=$3

file $TEXT | awk '{if(($2!~"UTF-8")&&($2!~"ASCII")) print "WARNING: The input file is not in UTF-8 or ASCII format"}'

[ -e $TEXT ] || { echo "ERROR: File \"${TEXT}\" does not exist \!"; exit 1; }
[ -e $DIC ] || { echo "ERROR: File \"${DIC}\" does not exist \!"; exit 1; }


sed -e 's/ [ ]*/ /g'  $TEXT | awk '{print toupper($0)}' > /tmp/$TEXT

#Para hacer el modelo de lenguaje

awk '{if($1=="\"\\\"\"") print "\\\""; else{ gsub("\"","",$1); print $1}}' $DIC | LC_ALL=C sort -u > /tmp/vocab

ngram-count -text /tmp/$TEXT  -vocab /tmp/vocab -lm ${LM}.arpa -order 2 -ukndiscount1 -ukndiscount2;    

HBuild -n ${LM}.arpa -s "<s>" "</s>" $DIC ${LM}.slf;  

rm /tmp/$TEXT
rm /tmp/vocab


