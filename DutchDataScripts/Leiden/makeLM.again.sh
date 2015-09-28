#DATA=NotSelectedPlusTrain0.expansions.txt.cleaner
DATA=Temp/train.txt.clean
#CHARLIST=/datalokaal/Scratch/HTR-V2/HTR/TrainingSets/Abbreviations/HMMList
CHARLIST=/datalokaal/Scratch/HTR/Leiden_HTR/EXP-AbbrevExpand/Models/Lista_Modelos_Train.abbr
#CHARLIST=/datalokaal/Scratch/HTR/Leiden_HTR/EXP-AbbrevExpand_noSlant/Modelos/Lista_Modelos_Train
bash /datalokaal/tranScriptorium/SVNRepository/tranScriptorium/pub/deliverables/D4.1/D4.1.1/LatticeFromLM.sh $DATA $CHARLIST `pwd` 0
perl fixdict.pl dictionary.txt > OUT/dictionary.txt
perl fixlm.pl languageModel.lm > OUT/languageModel.lm
cd OUT
HBuild -s '<s>' '</s>' -n languageModel.lm dictionary.txt latticeFile.txt

