perl ../clean.pl /datalokaal/Scratch/HTR/Leiden_HTR/EXP-AbbrevExpand/Models/Lista_Modelos_Train.expand <  train_0_expanded.txt > train_0.txt.clean
bash /datalokaal/tranScriptorium/SVNRepository/tranScriptorium/pub/deliverables/D4.1/D4.1.1/LatticeFromLM.sh train_0.txt.clean /datalokaal/Scratch/HTR/Leiden_HTR/EXP-AbbrevExpand/Models/Lista_Modelos_Train.expand `pwd` 0
perl ../fixdict.pl dictionary.txt > dictionary.fixed.txt
