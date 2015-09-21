                # This code uses the cleaned Text and then trains a LM
INPUT=$1
OUTPUT=$2
MINFREQ=$3

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH



OPT="-Dfile.encoding=utf8"
# capitalize
java $OPT -jar WNorm.jar $INPUT $OUTPUT/normalizedText.txt

#
java $OPT -jar WList.jar -i $INPUT -o $OUTPUT/csWordList.txt -n $MINFREQ -s $OUTPUT/csSortedWordList.txt

perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt

echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

echo "The process is runing, language modeling "



$SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz 

echo "finished counting" 

$SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $OUTPUT/languageModel.lm -limit-vocab -vocab $OUTPUT/ciWordList.txt -interpolate 



echo "The process is finished! "



