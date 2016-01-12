
TextAndLexicalProcessing2()
{
   CORPUS=$1
   CLASS_CHARSET=$2
   CHARSET=$3
   OUTPUT=$5
   CUTOFF=$4
   java -classpath $CLASSPATH eu.transcriptorium.jafar.TextAndLexicalProcessing $CLASS_CHARSET $CHARSET $CORPUS $CUTOFF $OUTPUT
}

####################################################################################################

LexicalProcessing2()
{
   CORPUS=$1
   CLASS_CHARSET=$2
   CHARSET=$3
   OUTPUT=$5
   CUTOFF=$4
   VOCABULARY=$6
   echo "Vocabulary: $VOCABULARY"
   java -classpath $CLASSPATH eu.transcriptorium.jafar.LexicalProcessing $CLASS_CHARSET $CHARSET $CORPUS $CUTOFF $OUTPUT $VOCABULARY
}

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

#################################################################################################

TextProcessingBeforeTraining()
{
  CLASS_CHARSET=$1
  SPECIAL_LABELS=$2
  TRANSCRIPTION_DIR=$3
  TRAINING_PARTITION=$4
  TRAIN_LINES=$5
  DESTINATION=$6
  java -classpath $CLASSPATH eu.transcriptorium.jafar.TextProcessingBeforeTraining $CLASS_CHARSET $SPECIAL_LABELS $TRANSCRIPTION_DIR $TRAINING_PARTITION $TRAIN_LINES $DESTINATION
}

#############################################################################################################################

## oepsie CORPUSFILE not used!

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

##################################################################################################################################

LanguageModelingWithoutBigLM()
{
  CORPUSFILE=$1
  VOCAB=$2
  MODEL=$3
  ORDER=$4
  if [ -z "$ORDER" ] ; then
    ORDER=2;
  fi;
  ngram-count -lm $MODEL -limit-vocab -vocab $VOCAB -order $ORDER -ukndiscount 1 -ukndiscount 2 -gt1min 0 -gt2min 0 -text $CORPUSFILE
}
########################################################################################################################################


RunHBuild()
{
  FOLDER=$1
  echo "start HBuild for directory  $1"
  $HTK/HBuild -s '<s>' '</s>' -n $FOLDER/languageModel.lm $FOLDER/dictionary.txt $FOLDER/latticeFile.txt 2>/tmp/hbuild.log
  cat /tmp/hbuild.log
}


#########################################################################

ComputeComponentPerplexities()
{
  TEXT=$1;
  for x in "${@:2}";
  do
    LM="$x/languageModel.lm"
    echo "Compute perplexity of text $TEXT with respect to LM $LM"
    # syllable splitting of TEXT if LM is a syllable model / TODO remove hack
    if [[ $x == *"_syl"* ]]
    then
       OTHERTEXT=`echo $TEXT | perl -pe   's/bigram/bigram_syl/'`;
       ngram -debug 2 -order 2 -lm $LM -cache 0 -ppl $OTHERTEXT > $x/linePerplexities.txt
    else
    ngram -debug 2 -order 2 -lm $LM -cache 0 -ppl $TEXT > $x/linePerplexities.txt
    fi
  done
}

###################################################################################

ComputeBestMix()
{
   DESTINATION=$1
   #echo "DESTINATION=$DESTINATION"
   MIXARGS=`echo "${@:2}" | perl -pe 's/\S+/$&\/linePerplexities.txt'/g`
   #echo "<MIXARGS=$MIXARGS>"
   CMD="compute-best-mix $MIXARGS"
   echo "cmb command:<$CMD>" 1>&2
   $CMD > $DESTINATION/lambdas.txt
   LAMBDAS=`grep "best lambda" $DESTINATION/lambdas.txt | perl -pe 's/.*\(([0-9 \.]+)\).*/$1/'`
   echo $LAMBDAS
}

###########################################################################################

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
  if [ -z "$ORDER" ] ; then
    ORDER=2;
  fi;
  COMMAND="ngram -order $ORDER $STARTARGS $EXTRAARGS -write-lm $DESTINATION/languageModel.lm -vocab $DESTINATION/normalizedWordList.txt -limit-vocab"
  echo "interpolation command: $COMMAND"
  $COMMAND
  ## for 2 lms
  ###ngram -lm $MODEL1 -order 2 -mix-lm $MODEL2  -lambda $LAMBDA -write-lm $DESTINATION/interpolatedLM.lm -vocab $DESTINATION/ciWordList.txt -limit-vocab
  ## for 3 lms
  ####ngram -lm $MODEL1/languageModel.lm -order 2 -lambda $LAM1 -mix-lm $MODEL2/languageModel.lm  -mix-lm2 $MODEL3/languageModel.lm  -mix-lambda2 $LAM3 -write-lm $OUTPUT/interpolatedLM.lm -vocab $OUTPUT/ciWordList.txt -limit-vocab
}

##############################################################

interleave()
{
  ARGZ="${@:1}"
  PERL="@argz=split(/\s+/, '$ARGZ'); my \$newargs; my \$d=scalar(@argz)/2; warn \$d; for (\$i=0; \$i < \$d; \$i++) { \$newargs .= \"\$argz[\$i] \$argz[\$i+\$d] \"; }; print \$newargs;";
  echo `perl -e "$PERL"`;
}

######################################################################

#SYLDIR=/mnt/Projecten/Taalbank/CL-Tools/Lettergreepsplitsing
#SYLDIR=/home/jesse/Tools/Lettergreepsplitsing

syllableSplitting()
{
  INPUT=$1;
  SYL_OUTPUT=$2;
  (cd $SYLDIR; perl scripts/sylsplit_corpus.pl $INPUT > $SYL_OUTPUT);
}
