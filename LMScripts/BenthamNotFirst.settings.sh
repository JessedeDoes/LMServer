CHARSET=resources/CharacterSets/AuxHMMsList
CORPUS=/mnt/Projecten/transcriptorium/Data/Corpora/Bentham/LPL/NotBatch1.lpl.newtok.txt
OUTPUT=BenthamNewTokenization/Bentham_bigram
CUTOFF=1

export CLASSPATH=./build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
export HTK=/usr/local/bin
export HTK=/mnt/Projecten/transcriptorium/Tools/HTK-BIN-100k/GLIBC_2.14/
