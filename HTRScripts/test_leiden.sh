################################################
# Recognition Phase and Word-Graph generation
################################################

### avoid relative path stuff.....

source $1

cd $EXP_DIR/TEST

# Set PATH to include the "bin" and "scripts" dirs
export PATH=$(realpath ../../bin):$(realpath ../../scripts):$PATH


ln -s ../PROC/Features
ln -s ../TRAIN/hmms/hmm_32/Macros_hmm
ln -s ../../conf/Leiden.conf

ln -s ../TRAIN/$TEST_DIC
ln -s ../TRAIN/Leiden-HMMs.lst
ln -s ../TRAIN/$TEST_SLF

# Filter-out paths and extensions from the list of page IDs
for f in `sed -e 's/^/Features\//'  -e 's/.jpg/\*.fea.gz/g'  ../../corpora/Leiden/test.lst`; do ls ${f}*;  done > Test-List

# Launch recognition step parallelized among x cores
Recog-WGGen.sh Leiden.conf


# Replace the WG words by the corresponding real pronunciation
#PutRealPronInWG.sh Leiden.dic WG_REC WG_REC_ED

# As output, at the end of the execution of "Recog-WGGen.sh", a directory
# named REC (or WG_REC in case -w option was set) containing all corresponding 
# recognized hypotheses (and in WG_REC all corresponding word-graphs).
