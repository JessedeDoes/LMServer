#!/bin/bash

source $1

export PATH=$PATH:$SRILM_PATH:.

TEXT=$TRAINING_TEXT
OUTPUT=$DICTIONARY
SPECIAL_LABELS=$3

file $TEXT | awk '{if(($2!~"UTF-8")&&($2!~"ASCII")) print "WARNING: The input file is not in UTF-8 or ASCII format"}'

[ -e $TEXT ] || { echo "ERROR: File \"${TEXT}\" does not exist \!"; exit 1; }


#First the text is tokenized 


sed -e 's/ /\n/g' $TEXT | LC_ALL=C sort | uniq -c > /tmp/$TEXT


awk -v n=$LABELS -v t=/tmp/$TEXT 'BEGIN{                                          
        while(getline < t){                        
                valor[toupper($2)]=valor[toupper($2)]+$1;
        }
	cont=0;
        if(n!=""){
                while(getline < n){
                        cont++; 
                        v[cont]=$1;
                        v2[$1]=$2;
                }
        }
     }{                                                  
        if(NF > 1){                                      
                emp=$2;
                if($2=="\x27") emp="\\\x27";
                if($2=="\x22") emp="\\\x22";  
                printf "\""toupper(emp)"\"";              
                printf "    [";                          
                printf emp;                               
                printf "]  "$1/valor[toupper($2)]"          ";

                for(i=1;i<=cont;i++){
                       gsub(v[i]," "v2[v[i]]" ",$2);
	        } 
                
                a=length($2);                                 
           
                for(i=1;i<=a;i++){                                 
                      emp=substr($2,i,1);
                      if(emp=="\x27") printf " <quote>";
                      else 
                        if(emp=="\x22") printf " <dquote>";
                        else
                            if(emp=="/") printf " <SLASH>";
                            else
                                 if(emp=="#") printf " <ALM>";
                                 else
				   if(emp=="—")
		                        printf " <DASH>" 
                                   else
                                  	if(emp==" "){
				          l=1;
				          i++;
				          emp=substr($2,i,1);
				          while(emp!=" "){
				               i++;
				               l++
				               emp=substr($2,i,1);
				          }
				          emp=substr($2,i-l+1,l-1);
                                          printf " "emp; 
			       		}
				        else    
	                                  printf " "emp; 
                   }
                   print " <SP>";  
             }
     }' /tmp/$TEXT > $OUTPUT 


echo "\"<s>\"  [] <SP>" >> $OUTPUT
echo "\"</s>\" [] <SP>" >> $OUTPUT

rm /tmp/$TEXT

