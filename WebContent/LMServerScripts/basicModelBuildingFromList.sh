source $1

# rm -rf $OUTPUT/*

source $LM_SCRIPT_PATH/LMBuildingFunctions.sh

cat $CORPUS > $OUTPUT/inputCorpus.txt

export CORPUS=$OUTPUT/inputCorpus.txt

TextAndLexicalProcessing2 $CORPUS $CLASS_CHARSET $CHARSET $CUTOFF $OUTPUT $ORDER

LanguageModelingWithoutBigLM $OUTPUT/normalizedText.txt $OUTPUT/normalizedWordList.txt $OUTPUT/languageModel.lm $ORDER

echo "start HBuild for $OUTPUT ...."

HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt

rm $CORPUS

