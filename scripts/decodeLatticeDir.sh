LT=/home/jesse/Tools/srilm/bin/i686-m64/lattice-tool
LM=$1
INDIR=$2
OUTDIR=$3

ls $INDIR/*.lat > /tmp/inList
$LT -read-htk -in-lattice-list /tmp/inList -viterbi-decode -lm $LM


