##################################################################################################

TextAndLexicalProcessing()
{
   CORPUS=$1
   CHARSET=$2
   OUTPUT=$3
   CUTOFF=$4
   echo "text cleaning..."
   java -classpath $CLASSPATH eu.transcriptorium.jafar.FinalCleaningText $CORPUS $CHARSET  $OUTPUT/cleanedText.txt $OUTPUT/normalizedText.txt
   echo "build frequency list...."
   java -classpath $CLASSPATH eu.transcriptorium.jafar.WordFrequencySort -i $OUTPUT/cleanedText.txt -o $OUTPUT/not_used_list.txt -n $CUTOFF -s $OUTPUT/wordFrequencyList.txt
   echo "build HTR dictionary"
   java -classpath $CLASSPATH eu.transcriptorium.jafar.BuildDictionaryFromOriginalText $CHARSET $OUTPUT/wordFrequencyList.txt $OUTPUT/dictionary.txt $OUTPUT/normalizedWordList.txt
}

#################################################################################################

LexicalProcessing()
{
  FOLDER=$1
  CHARSET=$2
  CUTOFF=$3
  echo "build frequency list...."
  java -classpath $CLASSPATH eu.transcriptorium.jafar.WordFrequencySort -i $FOLDER/cleanedText.txt -o $FOLDER/not_used_list.txt -n $CUTOFF -s $FOLDER/wordFrequencyList.txt
  echo "build HTR dictionary"
  java -classpath $CLASSPATH eu.transcriptorium.jafar.BuildDictionaryFromOriginalText $CHARSET $FOLDER/wordFrequencyList.txt $FOLDER/dictionary.txt $FOLDER/normalizedWordList.txt
}


#############################################################################################################################


LanguageModeling()
{
  CORPUSFILE=$1
  VOCAB=$2
  MODEL=$3
  echo "Start language modeling...."
  $SRILM_HOME/bin/i686-m64/ngram-count -sort -order 2 -text $OUTPUT/normalizedText.txt -write $OUTPUT/count.txt.gz
  echo "finished counting...."
  $SRILM_HOME/bin/make-big-lm -read $OUTPUT/count.txt.gz -name $OUTPUT/normalizedText.txt -order 2  -gt1min 0 -gt2min 0 -kndiscount -lm $MODEL -limit-vocab -vocab $VOCAB -interpolate
}

########################################################################################################################################


RunHBuild()
{
  FOLDER=$1
  echo "start HBuild for directory  $1"
  $HTK/HBuild -s '<s>' '</s>' -n $FOLDER/languageModel.lm $FOLDER/dictionary.txt $FOLDER/latticeFile.txt 2>/tmp/hbuild.log
}
