#MODEL=/datalokaal/Scratch/HTR/HTR/EccoDictionary/languageModel.lm
#MODEL=BenthamDictionary/languageModel.lm
#MODEL=InDomainLMs/train_0.lm
#MODEL=OutDomainLMs/bentham_0.lm
MODEL=$1
INPUT=$2
OUTPUT=$3


export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/


#ngram-count -text $INPUT -write-vocab /tmp/vocabtest.txt
ngram -debug 0 -order 2  -lm $MODEL/languageModel.lm  -cache 0 -ppl $INPUT > $OUTPUT

