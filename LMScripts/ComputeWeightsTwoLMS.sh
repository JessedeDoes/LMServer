MODEL1=$1
MODEL2=$2

TEXT=/datalokaal/Scratch/HTR/HTR/BenthamData/PlainText/test_0.txt.norm

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/

echo "             =============== Best merging parameters! ==============="

compute-best-mix  $MODEL1 $MODEL2 

echo "             =============== Best merging parameters Process is finished! ==============="


echo " End of the process!"
