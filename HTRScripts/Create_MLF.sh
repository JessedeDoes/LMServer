#!/bin/bash

# export PATH=$PATH:export PATH=$PATH:$HOME/HTR/bin:.

if [ $# -lt 2 ]; then
 echo "Uso: ${0##*/} <Train-list> <MLF-file> [<Special labels>]" 
 exit
fi

TRAIN=$1
OUTPUT=$2
LABELS=$3;

[ -e $TRAIN ] || { echo "ERROR: File \"${TRAIN}\" does not exist \!"; exit 1; }
for f in $(<$TRAIN); do 
 [ -e $f ] || { echo "ERROR: File \"$f\" does not exist \!"; exit 1; }
done

echo \#\!MLF\!# > $OUTPUT

for i in $(<${TRAIN}); do
        
        k=`basename $i`;
        sed -e s'/^/ /g' \
            -e s'/$/ \n/g' \
            -e 's/ [ ]*/ /g'  \
            -e 's/ /<SP>/g' $i > /tmp/$k; 
         
       
	f=${k/txt/lab};

        awk -v n=$LABELS -v t=$f 'BEGIN{
		print "\"*/"t"\"";
		cont=0;
	        if(n!=""){
	                while(getline < n){
                        	cont++; 
	                        v[cont]=$1;
	                        v2[$1]=$2;
                            
                	}
                        close(n);
     
        	}
	}{              
	        for(i=1;i<=cont;i++){
                        if(v2[v[i]]!=""){ gsub(v[i]," "v2[v[i]]" ",$0)}
                        else { gsub(v[i],"",$0);}
                } 

		a=length($0);                                   

 		emp="";                                         

	        for(i=1;i<=a;i++) {                              
		        emp=substr($0,i,1)
	                if((emp==".")||(emp=="0")||(emp=="1")||(emp=="2")||(emp=="3")||(emp=="4")||(emp=="5")||(emp=="6")||(emp=="7")||(emp=="8")||(emp=="9"))   print "\""emp"\"";
                        else 
	                if(emp=="/"){print "<SLASH>";fflush();} 
	                else
	     	        	if(emp=="#"){ print "<ALM>"; fflush();}
				else
                                    if(emp=="—"){print "<DASH>"; fflush();} 
				    else	
					if(emp=="\x27"){ print "<quote>";fflush();}
			                else
                				if(emp=="\x22"){ print "<dquote>";fflush();}
						else
							if(emp==" "){
					                   l=1;
					                   i++;
					                   emp=substr($0,i,1);
						           while(emp!=" "){
						                 i++;
							         l++
						                 emp=substr($0,i,1);
					                   }
					                   emp=substr($0,i-l+1,l-1);
					                   print emp;
                                                           fflush(); 
							}
					                else  
								if(emp=="<"){
                                                                    emp=substr($0,i,4);
								    if(emp=="<SP>"){
                                                                        print emp; 
                                                                        fflush();
									i=i+3;
								    }
                                                                    else{
									emp=substr($0,i,1);
                                                                        print emp;
                                                                        fflush();
								    }
								}else {
								    print emp;
								    fflush();	
								}
        
	     }
	   }                                                                                                           
	   END{print "."}' /tmp/$k >> $OUTPUT

           rm /tmp/$k;

done 









