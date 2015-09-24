# usage: 
# arg 1: output folder
# arg 2: text to evaluate against
# arg 3-last: language model folders for to-be-interpolated models

TEXT=$2
DESTINATION=$1

echo "Text=$TEXT; Destination=$DESTINATION"

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/
export CLASSPATH=./build/classes
export HTK=/mnt/Projecten/transcriptorium/Tools/HTK-BIN-100k/GLIBC_2.14/

source LMScripts/LMBuildingFunctions.sh

# first step: evaluate text agains each LM
# should set OOV penalty higher....

for x in "${@:3}";
do
  LM="$x/languageModel.lm"
  echo "compute perplexity of text $TEXT with respect to LM $LM"
  ngram -debug 2 -order 2 -lm $LM -cache 0 -ppl $TEXT > $x/linePerplexities.txt
done

MIXARGS=`echo "${@:3}" | perl -pe 's/\S+/$&\/linePerplexities.txt'/g`

#echo $MIXARGS

compute-best-mix $MIXARGS > $DESTINATION/lambdas.txt
echo "interpolation parameters:"
cat $DESTINATION/lambdas.txt

# best lambda (0.267464 0.732536)
LAMBDAS=`grep "best lambda" $DESTINATION/lambdas.txt | perl -pe 's/.*\(([0-9 \.]+)\).*/$1/'`
echo "LAMBDAS: $LAMBDAS"

CONCATARGS1=`echo "${@:3}" | perl -pe 's/\S+/$&\/normalizedText.txt/g'`
CONCATARGS2=`echo "${@:3}" | perl -pe 's/\S+/$&\/cleanedText.txt/g'`
MODELARGS=`echo "${@:3}" | perl -pe 's/\S+/$&\/languageModel.lm/g'`

echo "Models to interpolate: <$MODELARGS>"
#cat $CONCATARGS1 > $DESTINATION/normalizedText.txt # do we need this one??
#cat $CONCATARGS2 > $DESTINATION/cleanedText.txt


#echo "$CONCATARGS1 $CONCATARGS2"

#now do the dictionary processing for the merged text

CHARSET=resources/CharacterSets/AuxHMMsList
CUTOFF=1


intertwine()
{
  ARGZ="${@:1}"
  PERL="@argz=split(/\s+/, '$ARGZ'); my \$newargs; my \$d=scalar(@argz)/2; warn \$d; for (\$i=0; \$i < \$d; \$i++) { \$newargs .= \"\$argz[\$i] \$argz[\$i+\$d] \"; }; print \$newargs;";
  echo `perl -e "$PERL"`;
}

INTERPOLATION_ARGS=`intertwine "$MODELARGS $LAMBDAS"`
echo "Intertwined args=$INTERPOLATION_ARGS"


LexicalProcessing $DESTINATION $CHARSET $CUTOFF

# destination directory, model1 lambda1 model2 lambda2 etc...
# destination directory should contain normalizedWordList.txt (as a result of LexicalProcessing

interpolate()
{
  DESTINATION=$1
  MIXARGS="${@:2}" #model1 lambda1 etc
  STARTARGS="-lm $2 -lambda $3 -mix-lm $4 "
  PERL="@argz=split(/\s+/,'$MIXARGS'); my \$newargs; for (\$i=4; \$i < @argz; \$i+=2) { my \$k=\$i/2; \$newargs .= \" -mix-lm\$k \$argz[\$i] -mix-lambda\$k \$argz[\$i+1]\"; }; print \$newargs;";  
  ## echo $PERL
  EXTRAARGS=`perl -e "$PERL"`
  COMMAND="ngram -order 2 $STARTARGS $EXTRAARGS -write-lm $DESTINATION/languageModel.lm -vocab $DESTINATION/normalizedWordList.txt -limit-vocab"
  echo "interpolation command: $COMMAND"
  $COMMAND
  ## for 2 lms
  ###ngram -lm $MODEL1 -order 2 -mix-lm $MODEL2  -lambda $LAMBDA -write-lm $DESTINATION/interpolatedLM.lm -vocab $DESTINATION/ciWordList.txt -limit-vocab
  ## for 3 lms
  ####ngram -lm $MODEL1/languageModel.lm -order 2 -lambda $LAM1 -mix-lm $MODEL2/languageModel.lm  -mix-lm2 $MODEL3/languageModel.lm  -mix-lambda2 $LAM3 -write-lm $OUTPUT/interpolatedLM.lm -vocab $OUTPUT/ciWordList.txt -limit-vocab
}

interpolate $DESTINATION $INTERPOLATION_ARGS
RunHBuild $DESTINATION
#interpolate "DEST_DIR_THREE" m1 0.3 m2 0.3 m3 0.4
#interpolate "DEST_DIR_TWO" m1 0.3 m2 0.7

##echo "             =============== merging is finished! ==============="
## echo "             =============== testing is started! ==============="
#ngram-count -text $TEXT -write-vocab /tmp/vocabtest12.txt
##ngram -debug 1 -order 2  -lm $MODEL3/interpolatedLM.lm  -ppl $TEXT  > $MODEL3/ppl.EvaluationOfIntrepolatedLMs.log

