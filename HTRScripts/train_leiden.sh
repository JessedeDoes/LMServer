###################
# Training Phase
###################
STARTDIR=`pwd`
rm -rf EXP-LEIDEN/TRAIN/hmms
cd EXP-LEIDEN/TRAIN

#Useful links
ln -s ../PROC/Transcriptions/
ln -s ../PROC/train.lst .
ln -s ../../conf/Leiden.conf
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


defaultTextProcessing()
{
   TAGfilter.sh Transcriptions filter-trans

   #The transcriptions are tokenized

   Tokenize.sh filter-trans tokenized-trans
   rm -r filter-trans

   #Make the list of lines to train the models
   for f in `perl -pe 's/^/tokenized-trans\/Leiden./'  train.lst`; do ls ${f}*;  done > Train-lines.lst


   #Put the transcription lines in a file (for training the LM)
   for f in $(<Train-lines.lst); do 
	perl -pe 's/\\//g' $f; 
	echo ""  ;	
   done  > Train-text 


   #Make the HMM-list (6 states per model) using the train transcription samples

   Create_HMM_list.sh Train-text Leiden-HMMs.lst 6

   # The input of this script is the training text (Train-text), the name of the output file (Leiden-HMMs.lst)
   # and optionally a file containing the correspondence between special character sequences or simbols.
   # As output we will obtain a file with the list of HMMs to be trained.

  # Make the mlf file (this should be parametrized)

  Create_MLF.sh Train-lines.lst Leiden.mlf

  Create_DIC_fromconf.sh Leiden.conf
  Create_LM_fromconf.sh Leiden.conf
}

#defaultTextProcessing


otherTextProcessing()
{
  cd $STARTDIR;
  source conf/Leiden.conf

  # first step is to produce
  # - the training corpus
  # - the mlf file
  # - the HMM list
  # - and as a side product, the list of lines corresponding to the training partition pages

  source LMScripts/LMBuildingFunctions.sh;
  #CLASS_CHARSET=eu.transcriptorium.lm.charsets.DutchArtesTokenization
  CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
  SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.txt
  TRANSCRIPTIONS=/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/Transcriptions
 
  TRAINING_PARTITION=/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/train.lst
  TRAINING_LINES=/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/Train-Lines.lst
  BUILD_DIRECTORY=/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/PREBUILD

  TEST_PARTITION=/home/jesse/TUTORIAL-HTR/corpora/Leiden/test.noprefix.lst
  TEST_LINES=
  TextProcessingBeforeTraining  $CLASS_CHARSET $SPECIAL_LABELS $TRANSCRIPTIONS $TRAINING_PARTITION $TRAINING_LINES $BUILD_DIRECTORY
  

  # second step is to go for simple language modeling

  HMM_LIST=$BUILD_DIRECTORY/HMMs.list
  LM_TRAINING_CORPUS=$BUILD_DIRECTORY/trainingCorpus.txt
  CHARSET=$BUILD_DIRECTORY/charset.txt

  LM_OUTPUT=EXP-LEIDEN/TRAIN/LM/TC
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

#  cp $LM_OUTPUT/latticeFile.txt EXP-LEIDEN/TRAIN/Leiden-LM.slf
#  cp $LM_OUTPUT/dictionary.txt EXP-LEIDEN/TRAIN/Leiden.dic
#  cp $HMM_LIST EXP-LEIDEN/TRAIN/Leiden-HMMs.lst
#  cp $BUILD_DIRECTORY/labelFile.mlf EXP-LEIDEN/TRAIN/Leiden.mlf
}

(otherTextProcessing)

###### end of text processing 

# The input of this script is the list of the files containing the transcription of the training lines,
# the name of the output file (Leiden.mlf)
# and optionally a file containing the correspondence between special character sequences or simbols.
# As output we will obtain the mlf file requiered to carry out the HMM training

#Launch the HMM training.
#First we compute the list of samples to be used to train the HMMs
sed -e 's/tokenized-trans/Features/g' -e 's/txt/fea/g' Train-lines.lst > Train-lines-feas.lst


#Then we launch the HMM training (PARALLEL version)
# A new column with the number of states has to be added to the Leiden-HMMs.lst file
#echo "../Scripts/HMM-Train_parallel_var.sh Train-lines-feas.lst hmms/ Leiden.mlf  Leiden-HMMs-NumStates.lst 32 64" |qsub -l h_vmem=1g,h_rt=144:00:00 -cwd -N "REIG" -o eixida -j y


# HMM-Train_parallel_var.sh Leiden.conf

#or 

#echo "$AbsolutePath/HMM-Train_parallel_var.sh Leiden.conf" |qsub -l h_vmem=1g,h_rt=144:00:00 -cwd -N "REIG" -o eixida -j y

#if a cluster is used (switching the config variable TRAIN_CLUSTER to one in the config file)

# The input of this script is the list of files used to train the HMMs, the name of the outpud directory (hmms)  
# the mlf file, the list of pars HMM and the number of states of it,  the number of gaussians per state.
# As output we will obtain a directory (hmms) containing the trained HMMs. In this directory we will find
# several subdirectories containing HMMs trained with different number of Gaussians from 0 to the maximum
# defined number (32).
# the last parameter is the number of nodes to be used


############################
# Language modeling
############################

#Make the dictionary
#Create_DIC.sh Train-text Leiden.dic [<Transcription-HMM Correspondence file>]
#Create_DIC_fromconf.sh Leiden.conf

# The input of this script is the training text (Train-text), the name of the output file (Leiden.dic)
# and optionally a file containing the correspondence between special character sequences or simbols.
# As output we will obtain an HTK dictionary


#Make the Language Model

#Create_LM_fromconf.sh Leiden.conf

# The input of this script is the training text (Train-text),
# the dictionary in HTK format, and the output name of the language model.
# As output we will obtain the language model in ARPA (Leiden-LM.arpa) and in HTK format (Leiden-LM.slf).

