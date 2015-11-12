source $1

rm -rf $OUTPUT/*

source LMScripts/LMBuildingFunctions.sh


TextAndLexicalProcessing2 $CORPUS $CLASS_CHARSET $CHARSET $CUTOFF $OUTPUT $ORDER

LanguageModelingWithoutBigLM $OUTPUT/normalizedText.txt $OUTPUT/normalizedWordList.txt $OUTPUT/languageModel.lm $ORDER

echo "start HBuild"

$HTK/HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log

cat /tmp/hbuild.log
