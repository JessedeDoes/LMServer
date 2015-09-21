MODEL1=$1
MODEL2=$2
MODEL3=$3
OUTPUT=$4
LAM1=$5
LAM2=$6
LAM3=$7
MINFREQ=$8
TEXT=$9

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/


java $OPT -jar WList.jar -i $OUTPUT/cleanedText.txt -o $OUTPUT/csWordList.txt -n $MINFREQ -s $OUTPUT/csSortedWordList.txt

java $OPT -jar WNorm.jar $OUTPUT/cleanedText.txt $OUTPUT/normalizedText.txt


perl -pe 's/.*/uc $&/eg' $OUTPUT/csWordList.txt | sort -u >  $OUTPUT/ciWordList.txt

echo "<s>" >> $OUTPUT/ciWordList.txt
echo "</s>" >> $OUTPUT/ciWordList.txt

echo "             =============== merging is started! ==============="

ngram -lm $MODEL1/languageModel.lm -order 2 -lambda $LAM1 -mix-lm $MODEL2/languageModel.lm  -mix-lm2 $MODEL3/languageModel.lm  -mix-lambda2 $LAM3 -write-lm $OUTPUT/interpolatedLM.lm -vocab $OUTPUT/ciWordList.txt -limit-vocab 


echo "             =============== merging is finished! ==============="

echo "             =============== testing is started! ==============="
#ngram-count -text $TEXT -write-vocab /tmp/vocabtest12.txt
ngram -debug 1 -order 2  -lm $OUTPUT/interpolatedLM.lm  -ppl $TEXT  > $OUTPUT/ppl.interpolatedThreeLMS.log

echo "             =============== testing is finished! ==============="

#echo "             =============== Making a propor word list for the interpolated models ==============="

#java $OPT -jar WMList.jar -i $MODEL2/In.out.ecco.txt -o $MODEL2/csWordList.txt -d $MODEL3/csWordList.txt -s $MODEL2/csSortedWordList.txt

java $OPT -jar WDic.jar  $OUTPUT/csWordList.txt $OUTPUT/csSortedWordList.txt $OUTPUT/dictionary.txt

echo "HBuild is runing \n"
HBuild -s '<s>' '</s>' -n $OUTPUT/interpolatedLM.lm $OUTPUT/ciWordList.txt $OUTPUT/latticeFile.txt 2>/tmp/hbuild.log
echo " End of the process!"
