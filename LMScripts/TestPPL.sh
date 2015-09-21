#MODEL=/datalokaal/Scratch/HTR/HTR/EccoDictionary/languageModel.lm
#MODEL=BenthamDictionary/languageModel.lm
#MODEL=InDomainLMs/train_0.lm
#MODEL=OutDomainLMs/bentham_0.lm

TEXT=$1
MODEL1=$2
MODEL2=$3



export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/



ngram -debug 2 -order 2  -lm $MODEL1/languageModel.lm  -cache 0 -ppl $TEXT > $MODEL2

