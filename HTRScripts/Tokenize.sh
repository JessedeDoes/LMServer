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
	sed -e 's/,/ , /g' \
	    -e 's/;/ ; /g' \
	    -e 's/:/ : /g' \
	    -e "s/\x27/ \x27 /g" \
	    -e 's/"/ " /g' \
	    -e 's/“/ “ /g' \
	    -e 's/„/ „ /g' \
	    -e 's/(/ ( /g' \
	    -e 's/)/ ) /g' \
	    -e 's/}/ } /g' \
	    -e 's/{/ { /g' \
	    -e 's/\./ \. /g' \
	    -e 's/_/ _ /g' \
	    -e 's/;$/ ;/g' \
	    -e 's/:$/ :/g' \
	    -e 's/!/ ! /g' \
	    -e 's/?/ ? /g' \
	    -e 's/\x22/ \x22 /g' \
	    -e 's/+/ + /g' \
	    -e 's/\.$/ \./g' \
	    -e 's/,$/ ,/g' \
	    -e 's/\]/ \] /g' \
	    -e 's/\[/ \[ /g' \
            -e 's/\t/ /g' \
	    -e 's/ [ ]*/ /g' $f > ${OUT}/$n; 
done




