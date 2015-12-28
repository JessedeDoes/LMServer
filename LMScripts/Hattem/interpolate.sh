COMPONENTS="/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/DBNL-ARTES\
	/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/HATTEM-LM\
	/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/MNL-ARTES\
	/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/MNL-PROZA\
	/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/MNL-RIJM\
	/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TRAINING-SET"

VALIDATION=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TEST-SET/normalizedText.txt
OUTPUT=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/INTERPOLATION

### path and classpath settings

export CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
export SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.txt
BASIC_TRAINING_SOURCE=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/PREBUILD
export CHARSET=$BASIC_TRAINING_SOURCE/charset.txt

export CLASSPATH=/home/jesse/workspace/LMServer/build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin

export CUTOFF=3
bash LMScripts/MultipleInterpolation.sh $OUTPUT $VALIDATION $COMPONENTS