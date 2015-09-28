trainX()
{
  X=$1;
perl clean.pl /datalokaal/Scratch/HTR/Leiden_HTR/EXP-AbbrevExpand/Models/Lista_Modelos_Train.abbr /datalokaal/Scratch/HTR/Leiden_HTR/PlainText/Train_$X.txt > Temp/train.txt.clean;
  bash makeLM.again.sh;
  mv OUT OUT$X;
  mkdir OUT;
}

trainX 4
