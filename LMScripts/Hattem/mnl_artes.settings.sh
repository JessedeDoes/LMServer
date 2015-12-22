
#CLASS_CHARSET=eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization
CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels_hattem.txt
TRANSCRIPTION_DIR=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/Transcriptions
TRAINING_PARTITION=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/train.lst
TRAIN_LINES=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/Train-Lines.lst.x
BASIC_TRAINING_SOURCE=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TC


MNLDIR=/media/jesse/Data/tranScriptorium/NederlandseData/MNL-plaintext/Corpora
######### 
CORPUS=$MNLDIR/CDROM-artes.txt
OUTPUT=/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/MNL-ARTES
CHARSET=$BASIC_TRAINING_SOURCE/charset.txt
CUTOFF=0




### path and classpath settings

export CLASSPATH=/home/jesse/workspace/LMServer/build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin
