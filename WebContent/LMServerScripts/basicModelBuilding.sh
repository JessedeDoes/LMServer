source $1

# rm -rf $OUTPUT/*

source /var/lib/tomcat7/webapps/LMServer/LMServerScripts/LMBuildingFunctions.sh

TextAndLexicalProcessing2 $CORPUS $CLASS_CHARSET $CHARSET $CUTOFF $OUTPUT $ORDER

LanguageModelingWithoutBigLM $OUTPUT/normalizedText.txt $OUTPUT/normalizedWordList.txt $OUTPUT/languageModel.lm $ORDER

echo "start HBuild for $OUTPUT ...."

HBuild -s '<s>' '</s>' -n $OUTPUT/languageModel.lm $OUTPUT/dictionary.txt $OUTPUT/latticeFile.txt

cat /tmp/hbuild.log
