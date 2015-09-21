MODEL1=$1
MODEL2=$2
MODEL3=$3

export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$PATH:$SRILM_HOME/bin:$SRILM_HOME/bin/i686-m64/
export PATH=$PATH:/opt/jdk1.7.0/bin/

echo "             =============== Best merging parameters! ==============="

compute-best-mix $MODEL1  $MODEL2 $MODEL3 

echo "             =============== Best merging parameters Process is finished! ==============="


echo " End of the process!"
