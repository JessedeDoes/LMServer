MODEL1=$1
MODEL2=$2
MODEL3=$3
LAMBDA=$4
CUTOFF=$5
TEXT=$6


export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/usr/bin/


java $OPT -jar WList.jar -i $MODEL3/cleanedText.txt -o $MODEL3/csWordList.txt -n $CUTOFF -s $MODEL3/csSortedWordList.txt

java $OPT -jar WNorm.jar $MODEL3/cleanedText.txt $MODEL3/normalizedText.txt


# build dictionary and vocab for combined text...
# which means you have to combine texts first.

perl -pe 's/.*/uc $&/eg' $MODEL3/csWordList.txt | sort -u >  $MODEL3/ciWordList.txt

echo "<s>" >> $MODEL3/ciWordList.txt
echo "</s>" >> $MODEL3/ciWordList.txt


echo "             =============== merging is started! ==============="

ngram -lm $MODEL1 -order 2 -mix-lm $MODEL2  -lambda $LAMBDA -write-lm $MODEL3/interpolatedLM.lm -vocab $MODEL3/ciWordList.txt -limit-vocab 


echo "             =============== merging is finished! ==============="

echo "             =============== testing is started! ==============="
#ngram-count -text $TEXT -write-vocab /tmp/vocabtest12.txt
ngram -debug 1 -order 2  -lm $MODEL3/interpolatedLM.lm  -ppl $TEXT  > $MODEL3/ppl.EvaluationOfIntrepolatedLMs.log

echo "             =============== testing is finished! ==============="

java $OPT -jar WDic.jar  $MODEL3/csWordList.txt $MODEL3/csSortedWordList.txt $MODEL3/dictionary.txt

echo "HBuild is runing \n"
HBuild -s '<s>' '</s>' -n $MODEL3/interpolatedLM.lm $MODEL3/ciWordList.txt $MODEL3/latticeFile.txt 2>/tmp/hbuild.log
echo " End of the process!"
