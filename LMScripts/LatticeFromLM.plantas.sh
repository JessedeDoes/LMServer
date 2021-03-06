# this scripts produces the required language resources for running the tS HTR system

INPUT=$1 # the input text corpus
IMPORT=$2 # list of acceptable (recognizable) characters, one per line
OUTPUT=$3 # folder where LM and dictionary will be stored
MINFREQ=$4 # cutoff frequency

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH
export TOOLDIR=/datalokaal/tranScriptorium/SVNRepository/tranScriptorium/pub/deliverables/D4.1/D4.1.1


OPT="-Dfile.encoding=utf8"
# removes unrecognizable characters (which the HTR engine is not trained for)
java $OPT -jar $TOOLDIR/WProc.jar $INPUT $IMPORT $OUTPUT/cleanedText.txt 1>/tmp/hbuildCleaned.log
# capitalize (dont)

# just uppercase...
java $OPT -classpath $TOOLDIR UpperCasing $OUTPUT/cleanedText.txt $OUTPUT/normalizedText.txt
#cp $OUTPUT/cleanedText.txt $OUTPUT/normalizedText.txt

#word lists
java $OPT -jar $TOOLDIR/WList.jar -i $OUTPUT/cleanedText.txt -o $OUTPUT/csWordList.txt -n $MINFREQ -s $OUTPUT/csSortedWordList.txt

# uppercase word list
java $OPT -classpath $TOOLDIR UpperCasing $OUTPUT/csWordList.txt $OUTPUT/tmp.uc
sort -u $OUTPUT/tmp.uc > $OUTPUT/ciWordList.txt
rm $OUTPUT/tmp.uc

## cp $OUTPUT/csWordList.txt $OUTPUT/ciWordList.txt

#perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt

echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

echo "The process is running, language modeling \n"

$SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz 

echo "finished counting" 

$SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $OUTPUT/languageModel.lm -limit-vocab -vocab $OUTPUT/ciWordList.txt -interpolate 


echo "The LM building process is finished! \n"

echo "Build HTR dictionary" 
java $OPT -jar $TOOLDIR/WDic.jar  $OUTPUT/csWordList.txt $OUTPUT/csSortedWordList.txt $OUTPUT/dictionary.txt

echo "HBuild is running \n"
HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/ciWordList.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log

echo " End of the process!"
