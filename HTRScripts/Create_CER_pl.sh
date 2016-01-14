#!/bin/bash

export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -ne 2 ]; then
 echo "Uso: ${0##*/} <Directorio-resultados> <Directorio-Labels>" 
 exit
fi

perl scripts/process_rec.pl $* > fich_results.tmp

perl scripts/splitchars.pl <  fich_results.tmp  > fich_results.cer

tasas fich_results.cer -ie -s " "  -f "$"
