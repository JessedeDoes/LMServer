#CLASS_CHARSET=eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization

CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.reichsgericht.txt
TRANSCRIPTION_DIR=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/Transcriptions
TRAINING_PARTITION=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/train.lst
TRAIN_LINES=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/Train-Lines.lst.x


CORPUSDIR=/media/jesse/Data/tranScriptorium/LRServerData/German

######### 
CORPUS=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/PREBUILD-TEST/testCorpus.txt
OUTPUT=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/TEST-SET
CHARSET=/home/jesse/workspace/LMServer/resources/CharacterSets/reichsgericht.chars
CUTOFF=0




### path and classpath settings

export CLASSPATH=/home/jesse/workspace/LMServer/build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin
