for f in `ls /datalokaal/Scratch/HTR/HTR/BenthamData/PlainText/train*.txt`;
do
  java -jar WProc.jar $f $f.cleaned;
  java -jar WNorm.jar $f.cleaned $f.norm;
done
