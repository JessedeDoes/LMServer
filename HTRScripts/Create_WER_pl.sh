#!/bin/bash

export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -ne 2 ]; then
 echo "Uso: ${0##*/} <Directorio-resultados> <Directorio-Labels>" 
 exit
fi

perl scripts/process_rec.pl $* > fich_results

tasas fich_results -ie -s " "  -f "$" 
