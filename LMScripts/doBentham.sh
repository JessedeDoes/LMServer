#FILE=/mnt/tsdata/INL/EnglishCorpora/BenthamTranscriptionsNotBatch1.lpl.txt
FILE=/mnt/Projecten/transcriptorium/Data/Corpora/Bentham/LPL/NotBatch1.lpl.newtok.txt
#FILE=Test/Input/BenthamTranscriptionsNotBatch1.lpl.txt
#FILE=Test/Input/justATest.txt
OUTPUT=./Test

java -classpath ./bin eu.transcriptorium.jafar.FinalCleaningText $FILE resources/CharacterSets/AuxHMMsList  Test/test.out.clean Test/normalizedText.txt
java -classpath ./bin eu.transcriptorium.jafar.WordFrequencySort -i Test/test.out.clean -o Test/csWordList.txt -n 0 -s Test/csSortedWordList.txt

perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt

echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

java -classpath ./bin eu.transcriptorium.jafar.BuildDictionaryFromOriginalText resources/CharacterSets/AuxHMMsList $OUTPUT/csWordList.txt $OUTPUT/csSortedWordList.txt $OUTPUT/dictionary.txt


export SRILM_HOME=/home/jesse/Tools/srilm
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

echo "Start language modeling \n"

$SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz

echo "finished counting"

$SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $OUTPUT/languageModel.lm -limit-vocab -vocab $OUTPUT/ciWordList.txt -interpolate

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/

$HTK/HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log
# ciWordList.txt
