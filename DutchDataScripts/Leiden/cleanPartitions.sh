for file in `ls /datalokaal/Scratch/HTR/Leiden_HTR/partitionsBU/*`; do b=`basename $file`; echo $b; perl cleanFile.pl $file > /datalokaal/Scratch/HTR/Leiden_HTR/partitions/$b; done
