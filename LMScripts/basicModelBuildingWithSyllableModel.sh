source $1

rm -rf $OUTPUT/*
source LMScripts/LMBuildingFunctions.sh

SYLCORPUS=/tmp/split_corpus.txt
syllableSplitting $CORPUS $SYLCORPUS
TextAndLexicalProcessing2 $SYLCORPUS $CLASS_CHARSET $CHARSET $CUTOFF $OUTPUT

LanguageModelingWithoutBigLM $OUTPUT/normalizedText.txt $OUTPUT/normalizedWordList.txt $OUTPUT/languageModel.lm

echo "start HBuild"

$HTK/HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log

cat /tmp/hbuild.log
