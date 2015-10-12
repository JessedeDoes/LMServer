#!/bin/bash

# export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -lt 2 ]; then
 echo "Uso: ${0##*/} <Text-file> <HMM-list> <NumStates>[<Special labels>]" 
 exit
fi

TEXT=$1
OUTPUT=$2
N_STATES=$3
LABELS=$4; 

file -L $TEXT | awk '{if(($2!~"UTF-8")&&($2!~"ASCII")) print "WARNING: The input file is not in UTF-8 or ASCII format"}' 

[ -e $TEXT ] || { echo "ERROR: File \"${TEXT}\" does not exist \!"; exit 1; }


#First the text is tokenized 

cat $TEXT |  # We filter the special labels
awk -v n=$LABELS 'BEGIN{
        cont=0;
	if(n!=""){
		while(getline < n){
			cont++; 
			v[cont]=$1
		}
	}
}{
	for(i=1;i<=cont;i++){
		gsub(v[i],"",$0);
	} 
	print $0;
}' | # We compute the HMMs
awk 'BEGIN{
 	FS=""; 
}{
	for(i=1;i<=NF; i++){
		if($i~"—"){
			print "<DASH>" 
		}else print $i
	}
}' |
awk -v numStates=$N_STATES '{if(NF>0) print $0 "    " numStates}' | LC_ALL=C sort -u  > $OUTPUT


cat $TEXT |  # We add the special labels
awk -v n=$LABELS -v numStates=$N_STATES 'BEGIN{
        cont=0;
        if(n!=""){
	        while(getline < n){
        	        cont++; 
	                v[cont]=$1;
	                v2[$1]=$2;
	        }
	}
}{
        for(i=1;i<=cont;i++){
            if(($0~v[i])&&(v2[v[i]]!=""))  print v2[v[i]] "    " numStates;     
        } 
}' | LC_ALL=C sort -u >> $OUTPUT

echo "<SP>  " $N_STATES >> $OUTPUT

sed -e 's/\x27/<quote>/g' -e 's/"/<dquote>/g' -e 's/\//<SLASH>/g' -e 's/\#/<ALM>/g' -i $OUTPUT 

