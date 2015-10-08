CORPUS=BenthamNewTokenization/BenthamTrain/training.untokenized.txt
OUTPUT=BenthamNewTokenization/Bentham_train_bigram_char
#CLASS_CHARSET=eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization
CLASS_CHARSET=eu.transcriptorium.lm.charsets.ProcessingForCharacterLM

CHARSET=resources/CharacterSets/AuxHMMsList
CUTOFF=1

export CLASSPATH=./build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
export HTK=/usr/local/bin
