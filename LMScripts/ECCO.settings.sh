CHARSET=resources/CharacterSets/AuxHMMsList
CLASS_CHARSET=eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization
CORPUS=BenthamNewTokenization/newSelectedECCOtext.txt
OUTPUT=BenthamNewTokenization/ECCO_bigram
CUTOFF=1

export CLASSPATH=./build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
export HTK=/usr/local/bin
export HTK=/mnt/Projecten/transcriptorium/Tools/HTK-BIN-100k/GLIBC_2.14/
