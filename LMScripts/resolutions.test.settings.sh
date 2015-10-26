CHARSET=resources/CharacterSets/resolutions.chars.txt
CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.txt
TRANSCRIPTION_DIR=/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Transcriptions
TRAINING_PARTITION=/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/train.lst
TRAIN_LINES=/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Train-Lines.lst.x
DESTINATION=/tmp/MMTest

CORPUS=/media/jesse/Data/tranScriptorium/Resoluties/PlainText/test.txt
OUTPUT=/home/jesse/TUTORIAL-HTR/EXP-RESOLUTIONS/TRAIN/LM/TEST
#CHARSET=$DESTINATION/charset.txt
CUTOFF=0

### path and classpath settings

export CLASSPATH=/home/jesse/workspace/LMServer/build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin
