source $1

rm -rf $OUTPUT/*

lexicalProcessing()
{
   CORPUS=$1
   CHARSET=$2
   OUTPUT=$3
   echo "text cleaning..."
   java -classpath $CLASSPATH eu.transcriptorium.jafar.FinalCleaningText $CORPUS $CHARSET  $OUTPUT/cleanedText.txt $OUTPUT/normalizedText.txt
   echo "build frequency list...."
   java -classpath $CLASSPATH eu.transcriptorium.jafar.WordFrequencySort -i $OUTPUT/cleanedText.txt -o $OUTPUT/not_used_list.txt -n $CUTOFF -s $OUTPUT/wordFrequencyList.txt
   echo "build HTR dictionary"
   java -classpath $CLASSPATH eu.transcriptorium.jafar.BuildDictionaryFromOriginalText $CHARSET $OUTPUT/wordFrequencyList.txt $OUTPUT/dictionary.txt $OUTPUT/normalizedWordList.txt
}


lexicalProcessing $CORPUS $CHARSET $OUTPUT


#############################################################################################################################


languageModeling()
{
  CORPUSFILE=$1
  VOCAB=$2
  MODEL=$3
  echo "Start language modeling...."
  $SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz
  echo "finished counting...."
  $SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $MODEL -limit-vocab -vocab $VOCAB -interpolate
}

languageModeling $OUTPUT/normalizedText.txt $OUTPUT/normalizedWordList.txt $OUTPUT/languageModel.lm

echo "start HBuild"

$HTK/HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log

