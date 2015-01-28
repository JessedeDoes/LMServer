BFB=/datalokaal/Scratch/HTR/BFBNew
UNI=$BFB/LanguageModels/Unigram/
LM1=$UNI/languageModel.lm
DIC1=$UNI/dictionary.txt
LAT1=$BFB/StoredExperiments/experimentWithUnigramModel/resultados/res-128gauss/
TEMP1=Temp/Temp1


DIR2=/mnt/Projecten/transcriptorium/Tools/languagemodeling/NewSetOfExperiment/2-gram/FinalInDomainBentham/OneLineINBentham/
DIC2=$DIR2/dictionary.txt
LM2=$DIR2/languageModel.lm
LAT2=/mnt/Projecten/transcriptorium/Tools/languagemodeling/NewSetOfExperiment/2-gram/FinalInDomainBentham/OneLineINBentham/resultados/res-128gauss/
TEMP2=Temp/Temp2

LAT2a=/mnt/Projecten/transcriptorium/Tools/languagemodeling/NewSetOfExperiment/CoTrainingSingelIterationNewCharacterSet/OneLineECCO/intOneLine/resultados/res-128gauss/
DIR2a=/mnt/Projecten/transcriptorium/Tools/languagemodeling/NewSetOfExperiment/CoTrainingSingelIterationNewCharacterSet/OneLineECCO/intOneLine/
LM2a=$DIR2a/interpolatedLM.lm
DIC2a=$DIR2a/dictionary.txt
TEMP2a=Temp/Temp2a


DIR3=/mnt/Projecten/transcriptorium/Tools/languagemodeling/NewSetOfExperiment/3-gram-oneline
LM3=$DIR3/int/interpolatedLM.lm
DIC3=$DIC2a
LAT3=$LAT2a
TEMP3=Temp/Temp3

test()
{
  LM1=$1
  DIC1=$2
  LAT1=$3
  TEMP=$4
#  echo "testing $LM1 $DIC1 $LAT1"
  java -cp ./bin/ eu.transcriptorium.lattice.LatticeListDecoder $LM1 $DIC1  $LAT1 > $TEMP/listDecoder.1.out  2>$TEMP/decoder.log
  perl scripts/makeResultsFileFrom1BestResult.pl  $TEMP/listDecoder.1.out > $TEMP/listDecoder.result.1.cs
  perl scripts/makeResultsFileFrom1BestResult.pl  -i $TEMP/listDecoder.1.out > $TEMP/listDecoder.result.1.ci
  perl -pe 's/\s*#.*//' $TEMP/listDecoder.result.1.cs > $TEMP/listDecoder.result.cs
  perl -pe 's/\s*#.*//' $TEMP/listDecoder.result.1.ci > $TEMP/listDecoder.result.ci
  CIWER=`tasas $TEMP/listDecoder.result.ci -ie -f "$" -s " "`
  CSWER=`tasas $TEMP/listDecoder.result.cs -ie -f "$" -s " "`
  DETAILS=`bash scripts/tasasWithOptions.sh $TEMP/listDecoder.result.cs`
  echo "CS: $CSWER, CI: $CIWER, DETAILED: $DETAILS"
}


echo "Test 1"
test $LM1 $DIC1 $LAT1 $TEMP1
echo "Test 2"
test $LM2 $DIC2 $LAT2 $TEMP2
echo "Test 2 adapted"
test $LM2a $DIC2a $LAT2a $TEMP2a
echo "Test 3 adapted"
test $LM3 $DIC3 $LAT3 $TEMP3


# without alejandro weighting
# Test 1 (unigram)
# CS: 24.555160, CI: 23.640061
# Test 2 (bigram in-domain)
# CS: 22.191154, CI: 21.174377
# Test 2 adapted (bigram adapted)
# CS: 17.577529, CI: 16.662430
# Test 3 adapted (trigram adapted)
# CS: 16.611591, CI: 15.645653

# with alejandro weighting
# Test 1
# CS: 24.300966, CI: 23.512964
# Test 2
# CS: 22.000508, CI: 21.098119
# Test 2 adapted
# CS: 17.463142, CI: 16.637011
# Test 3 adapted
# CS: 16.535333, CI: 15.683782

# comparison with non-paragraph results:

# Test 1 24.631418
# Test 2 22.572445
# Test 2a  18.734113
# Test 3 (ci) 15.976106


# Test 1
# CS: 24.300966, CI: 23.512964, DETAILED: all words: 24.389934; first: 29.767442; withoutfirst: 23.715753; last: 31.511628; without last: 23.601598; without both: 22.819978; without both,ci,nopunct:  16.719835
# Test 2
# CS: 22.000508, CI: 21.098119, DETAILED: all words: 22.089476; first: 26.860465; withoutfirst: 21.461187; last: 28.837209; without last: 21.418379; without both: 20.619881; without both,ci,nopunct:  15.331207
# Test 2 adapted
# CS: 17.463142, CI: 16.637011, DETAILED: all words: 17.552110; first: 24.883721; withoutfirst: 16.595320; last: 24.767442; without last: 16.738014; without both: 15.609443; without both,ci,nopunct:  9.382623
# Test 3 adapted
# CS: 16.535333, CI: 15.683782, DETAILED: all words: 16.624301; first: 23.720930; withoutfirst: 15.667808; last: 25.116279; without last: 15.639269; without both: 14.549542; without both,ci,nopunct:  8.425596
# 
