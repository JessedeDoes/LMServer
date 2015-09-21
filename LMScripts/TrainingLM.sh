#this code first cleans and normalized the text and then trains a LM
INPUT=$1
IMPORT=$2
OUTPUT=$3
MINFREQ=$4


export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH
export TOOLDIR=/datalokaal/tranScriptorium/SVNRepository/tranScriptorium/pub/deliverables/D4.1/D4.1.1

OPT="-Dfile.encoding=utf8"
# removes unrecognizable (by the HTR system) characters
java $OPT -jar $TOOLDIR/WProc.jar $INPUT $IMPORT $OUTPUT/cleanedText.txt 1>/tmp/hbuildCleaned.log
# capitalize
java $OPT -jar $TOOLDIR/WNorm.jar $OUTPUT/cleanedText.txt $OUTPUT/normalizedText.txt

#
java $OPT -jar $TOOLDIR/WList.jar -i $OUTPUT/cleanedText.txt -o $OUTPUT/csWordList.txt -n $MINFREQ -s $OUTPUT/csSortedWordList.txt

perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt

echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

echo "The process is running, language modeling \n"

$SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz 

echo "finished counting" 
$SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $OUTPUT/languageModel.lm -limit-vocab -vocab $OUTPUT/ciWordList.txt -interpolate 

echo "The process is finished! \n"


