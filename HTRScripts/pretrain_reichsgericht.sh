###################
# Training Phase
###################
STARTDIR=`pwd`
#rm -rf EXP-REICH/TRAIN/hmms
cd EXP-REICH/TRAIN

#Useful links
ln -s ../PROC/Transcriptions/
ln -s ../PROC/train.lst .
ln -s ../../conf/Reichsgericht.conf
ln -s ../PROC/Features/

# Set PATH to include the "bin" and "scripts" dirs 

export PATH=$(realpath ../../bin):$(realpath ../../scripts):$PATH

#Filter-out TEI labels in the transcriptions


##### start of text processing
### Input is: raw transcription
### file with token mappings
### Output:
####  1) tokenized transcription
####  2) HMM list (character set info)
####  3) Label file for training the recognizer
### and after language modeling
### HTR dictionary
### language model and lattice file


otherTextProcessing()
{
  cd $STARTDIR;
  source conf/Reichsgericht.conf

  # first step is to produce
  # - the training corpus
  # - the mlf file
  # - the HMM list
  # - and as a side product, the list of lines corresponding to the training partition pages

  source LMScripts/LMBuildingFunctions.sh;
  #CLASS_CHARSET=eu.transcriptorium.lm.charsets.DutchArtesTokenization
  CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
  SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.reichsgericht.txt
  TRANSCRIPTIONS=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/Transcriptions
 
  TRAINING_PARTITION=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/train.lst
  TRAINING_LINES=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/Train-lines.lst.xout
  BUILD_DIRECTORY=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/PREBUILD

  TEST_PARTITION=/home/jesse/TUTORIAL-HTR/corpora/Reichsgericht/partitions/test.prebuild.lst
  TEST_LINES=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/Test-Lines.lst.xout
  TEST_BUILD_DIRECTORY=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/PREBUILD-TEST

  TextProcessingBeforeTraining  $CLASS_CHARSET $SPECIAL_LABELS $TRANSCRIPTIONS $TRAINING_PARTITION $TRAINING_LINES $BUILD_DIRECTORY
  TextProcessingBeforeTraining  $CLASS_CHARSET $SPECIAL_LABELS $TRANSCRIPTIONS $TEST_PARTITION $TEST_LINES $TEST_BUILD_DIRECTORY
  

  # second step is to go for simple language modeling

  HMM_LIST=$BUILD_DIRECTORY/HMMs.list
  LM_TRAINING_CORPUS=$BUILD_DIRECTORY/trainingCorpus.txt
  CHARSET=$BUILD_DIRECTORY/charset.txt

  LM_OUTPUT=EXP-REICH/TRAIN/LM/TC
  CUTOFF=0

#  syllableSplitting $LM_TRAINING_CORPUS $LM_TRAINING_CORPUS.syl
#  CLASS_CHARSET=eu.transcriptorium.lm.charsets.SyllableTokenization
  TextAndLexicalProcessing2 $LM_TRAINING_CORPUS $CLASS_CHARSET $CHARSET $CUTOFF $LM_OUTPUT

  LanguageModelingWithoutBigLM $LM_OUTPUT/normalizedText.txt $LM_OUTPUT/normalizedWordList.txt $LM_OUTPUT/languageModel.lm

  echo "start HBuild"

  $HTK/HBuild -s '<s>' '</s>' -n $LM_OUTPUT/languageModel.lm $LM_OUTPUT/dictionary.txt $LM_OUTPUT/latticeFile.txt 2>/tmp/hbuild.log

  cat /tmp/hbuild.log

  # DICTIONARY=Leiden.dic
  # LANGUAGE_MODEL=Leiden-LM
  # some copying to comply with the other variant

#  cp $LM_OUTPUT/latticeFile.txt EXP-REICH/TRAIN/Leiden-LM.slf
#  cp $LM_OUTPUT/dictionary.txt EXP-REICH/TRAIN/Leiden.dic
#  cp $HMM_LIST EXP-REICH/TRAIN/Leiden-HMMs.lst
#  cp $BUILD_DIRECTORY/labelFile.mlf EXP-REICH/TRAIN/Leiden.mlf
}

(otherTextProcessing)

