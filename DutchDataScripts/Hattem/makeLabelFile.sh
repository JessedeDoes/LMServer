PAGEDIR=/datalokaal/tranScriptorium/SVNRepository/tranScriptorium/pub/wp2_DataManagement/T2.2_GT/Hattem/GTWithCorrectedLineShapes/
perl Scripts/PageToPlain.pl 1 $PAGEDIR/*xml
perl Scripts/PageToPlain.pl 0 $PAGEDIR/*xml
perl Scripts/makeLabelFile.pl  /datalokaal/Scratch/HTR-V2/HTR/HattemData/Partitions/TrainLines_0 TranscribedLinesForHTR/Expanded/ > TranscribedLinesForHTR/Train_0_Expanded.mlf
perl Scripts/makeLabelFile.pl /datalokaal/Scratch/HTR-V2/HTR/HattemData/Partitions/TrainLines_0 TranscribedLinesForHTR/Abbreviations/ > TranscribedLinesForHTR/Train_0_Abbreviations.mlf
