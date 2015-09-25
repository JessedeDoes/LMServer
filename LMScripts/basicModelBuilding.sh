source $1

rm -rf $OUTPUT/*
source LMScripts/LMBuildingFunctions.sh

TextAndLexicalProcessing $CORPUS $CHARSET $OUTPUT $CUTOFF

LanguageModelingWithoutBigLM $OUTPUT/normalizedText.txt $OUTPUT/normalizedWordList.txt $OUTPUT/languageModel.lm

echo "start HBuild"

$HTK/HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log
