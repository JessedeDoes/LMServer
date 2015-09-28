DATA=/datalokaal/Scratch/HTR/Leiden_HTR/PlainText/Train_0_expanded.txt
CHARLIST=/datalokaal/Scratch/HTR/Leiden_HTR/EXP-AbbrevExpand_noSlant/Modelos/Lista_Modelos_Train


perl clean.pl $CHARLIST $DATA > Temp/cleaned_train.txt


bash /datalokaal/tranScriptorium/SVNRepository/tranScriptorium/pub/deliverables/D4.1/D4.1.1/LatticeFromLM.sh Temp/cleaned_train.txt $CHARLIST `pwd` 0
perl fixdict.pl dictionary.txt > OUT/dictionary.txt
perl fixlm.pl languageModel.lm > OUT/languageModel.lm
cd OUT
HBuild -s '<s>' '</s>' -n languageModel.lm dictionary.txt latticeFile.txt

