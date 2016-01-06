# usage: 
# arg 1: output folder
# arg 2: text to evaluate against
# arg 3-last: language model folders for to-be-interpolated models
# assumptions:

echo "charset type: $CLASS_CHARSET $CHARSET"

DESTINATION=$1
TEXT=$2
SUBMODEL_DIRS="${@:3}"
CLEANED_TEXTS=`echo "$SUBMODEL_DIRS" | perl -pe 's/\S+/$&\/cleanedText.txt/g'`
FREQUENCY_LISTS=`echo "$SUBMODEL_DIRS" | perl -pe 's/\S+/$&\/wordFrequencyList.txt/g'`
SUBMODEL_FILES=`echo "$SUBMODEL_DIRS" | perl -pe 's/\S+/$&\/languageModel.lm/g'`

#HACK
CLEANED_TEXTS=`echo $CLEANED_TEXTS | perl -pe 's/LMTrain/LMTrainFull/'`

echo "Text=$TEXT; Destination=$DESTINATION"

#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
#export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
#export PATH=$PATH:/opt/jdk1.7.0/bin/
#export CLASSPATH=./build/classes
# export HTK=/mnt/Projecten/transcriptorium/Tools/HTK-BIN-100k/GLIBC_2.14/
# CHARSET=resources/CharacterSets/Konzil.chars.txt # foei
# CUTOFF=0 # for lexical processing of the interpolation lexicon

source LMScripts/LMBuildingFunctions.sh

###### do something

## first evaluate each model against the validation text
ComputeComponentPerplexities $TEXT $SUBMODEL_DIRS
## then compute interpolation parameters
INTERPOLATION_LAMBDAS=`ComputeBestMix "$DESTINATION" $SUBMODEL_DIRS`
echo "computed interpolation parameters: $INTERPOLATION_LAMBDAS"
echo "Models to interpolate: <$SUBMODEL_FILES>"
cat $CLEANED_TEXTS > $DESTINATION/cleanedText.txt
INTERPOLATION_ARGS=`interleave "$SUBMODEL_FILES $INTERPOLATION_LAMBDAS"`
# HACK
INTERPOLATION_ARGS=`echo $INTERPOLATION_ARGS | perl -pe 's/LMTrain/LMTrainFull/'`
echo "Interleaved args=$INTERPOLATION_ARGS"

## build the combined HTR dictionary, and the vocabulary for the interpolation
## here we have one cutoff for the combined corpus, which is not what we want
## instead: per-component cutoff, build vocabulary, etc...

# compile the vocabulary for the joint dictionary

cat $FREQUENCY_LISTS | cut -f1 | sort -u > $DESTINATION/combinedVocabulary.txt

# $CLASS_CHARSET $CHARSET $CORPUS $CUTOFF $OUTPUT
LexicalProcessing2  $DESTINATION/cleanedText.txt $CLASS_CHARSET $CHARSET $CUTOFF $DESTINATION
## carry out the interpolation
interpolate $DESTINATION $INTERPOLATION_ARGS
## convert the resulting language model to an HTK lattice file
RunHBuild $DESTINATION


## interpolate "DEST_DIR_TWO" m1 0.3 m2 0.7
## echo "             =============== testing is started! ==============="
## ngram-count -text $TEXT -write-vocab /tmp/vocabtest12.txt
## ngram -debug 1 -order 2  -lm $MODEL3/interpolatedLM.lm  -ppl $TEXT  > $MODEL3/ppl.EvaluationOfIntrepolatedLMs.log
