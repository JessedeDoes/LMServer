CLASS_CHARSET=eu.transcriptorium.lm.charsets.DutchArtesTokenization
SPECIAL_LABELS=/home/jesse/TUTORIAL-HTR/conf/DutchArtes.special.labels.txt
TRANSCRIPTION_DIR=/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Transcriptions
TRAINING_PARTITION=/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/train.lst
TRAIN_LINES=/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Train-Lines.lst.x
DESTINATION=/tmp/MMTest

export CLASSPATH=./build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin


