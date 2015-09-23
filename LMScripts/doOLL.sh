CORPUS=/mnt/Projecten/transcriptorium/Data/Corpora/Bentham/OLL/LPLNewPunctuation/all.txt
OUTPUT=BenthamNewTokenization/OLL_bigram
CLASSPATH=./bin

java -classpath $CLASSPATH eu.transcriptorium.jafar.FinalCleaningText $CORPUS resources/CharacterSets/AuxHMMsList  $OUTPUT/test.out.clean $OUTPUT/normalizedText.txt
java -classpath $CLASSPATH eu.transcriptorium.jafar.WordFrequencySort -i $OUTPUT/test.out.clean -o $OUTPUT/csWordList.txt -n 2 -s $OUTPUT/csSortedWordList.txt

# these steps should be inside java...

perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt
echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

java -classpath $CLASSPATH eu.transcriptorium.jafar.BuildDictionaryFromOriginalText resources/CharacterSets/AuxHMMsList $OUTPUT/csWordList.txt $OUTPUT/csSortedWordList.txt $OUTPUT/dictionary.txt


export SRILM_HOME=/home/jesse/Tools/srilm
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

echo "Start language modeling \n"

$SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz

echo "finished counting"

$SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $OUTPUT/languageModel.lm -limit-vocab -vocab $OUTPUT/ciWordList.txt -interpolate

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
export HTK=/usr/local/bin
$HTK/HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log
# ciWordList.txt
