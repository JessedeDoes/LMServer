MODEL1=$1
MODEL2=$2
MODEL3=$3
OUTPUT=$4
MINFREQ=$5

TEXT=/datalokaal/Scratch/HTR/HTR/BenthamData/PlainText/test_0.txt.norm

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/

echo "             =============== Best merging parameters! ==============="

compute-best-mix $MODEL1/ppl.Bentham_In.log  $MODEL2/ppl.Bentham_Out.log $MODEL3/ppl.ECCO.log 

echo "             =============== Best merging parameters Process is finished! ==============="





java $OPT -jar WList.jar -i $OUTPUT/In.out.ecco.txt -o $OUTPUT/csWordList.txt -n $MINFREQ -s $OUTPUT/csSortedWordList.txt

perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt

echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

echo "             =============== merging is started! ==============="

ngram -lm $MODEL1/languageModel.lm -order 2 -lambda 0.668408 -mix-lm $MODEL2/languageModel.lm  -mix-lm2 $MODEL3/languageModel.lm  -mix-lambda2 0.00725181 -write-lm $OUTPUT/interpolatedLM.lm -vocab $OUTPUT/ciWordList.txt -limit-vocab 


echo "             =============== merging is finished! ==============="

echo "             =============== testing is started! ==============="
#ngram-count -text $TEXT -write-vocab /tmp/vocabtest12.txt
ngram -debug 1 -order 2  -lm $OUTPUT/interpolatedLM.lm  -ppl $TEXT  > $OUTPUT/ppl.interpolatedInOutECCO.log

echo "             =============== testing is finished! ==============="

#echo "             =============== Making a propor word list for the interpolated models ==============="

#java $OPT -jar WMList.jar -i $MODEL2/In.out.ecco.txt -o $MODEL2/csWordList.txt -d $MODEL3/csWordList.txt -s $MODEL2/csSortedWordList.txt

java $OPT -jar WDic.jar  $OUTPUT/csWordList.txt $OUTPUT/csSortedWordList.txt $OUTPUT/dictionary.txt

echo "HBuild is runing \n"
HBuild -s '<s>' '</s>' -n $OUTPUT/interpolatedLM.lm $OUTPUT/ciWordList.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log
echo " End of the process!"
