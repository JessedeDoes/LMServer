#!/bin/bash

export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -ne 2 ]; then
 echo "Uso: ${0##*/} <Directorio-resultados> <Directorio-Labels>" 
 exit
fi

for file in `find $1 -name "*.rec"`; 
do


   NAME=$2$(basename $file | sed 's/rec/txt/g')

   awk '{if (NR>=1) printf("%s",$0);}END{printf(" $ ")}' $NAME
   awk '{if (NR>=1) printf("%s ",$3); else printf("%s ",$3)}END{printf("\n")}' $file

done |sed 's/\\302\\266/¶/g' | sed 's/\\302\\243/£/g' | sed 's/\x22\x27\x22/\x27/g' | sed 's/\x27\x22\x27/\x22/g'  > fich_results.tmp

perl -pe 's/(\S)(\S)/$1 $2/g' fich_results.tmp | perl -pe 's/(\S)(\S)/$1 $2/g' > fich_results.cer

tasas fich_results.cer -ie -s " "  -f "$" 
