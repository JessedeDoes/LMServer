CORPUS=/mnt/Projecten/transcriptorium/Data/Corpora/Bentham/OLL/LPLNewPunctuation/all.txt
OUTPUT=BenthamNewTokenization/OLL_bigram
CHARSET=resources/CharacterSets/AuxHMMsList
CUTOFF=1

export CLASSPATH=./build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
export HTK=/usr/local/bin
