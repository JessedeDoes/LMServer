################################################
# Recognition Phase and Word-Graph generation
################################################
cd EXP-RESOLUTIONS/TEST

# Set PATH to include the "bin" and "scripts" dirs
export PATH=$(realpath ../../bin):$(realpath ../../scripts):$PATH

ln -s ../PROC/Features
ln -s ../TRAIN/hmms/hmm_32/Macros_hmm
ln -s ../../conf/Resolutions.conf
ln -s ../TRAIN/Resolutions.dic
ln -s ../TRAIN/Resolutions-HMMs.lst
ln -s ../TRAIN/Resolutions-LM.slf

# Filter-out paths and extensions from the list of page IDs
for f in `sed -e 's/^/Features\//'  -e 's/.jpg/\*.fea/g'  ../../corpora/Resolutions/test.lst`; do ls ${f};  done > Test-List


# Launch recognition step parallelized among x cores
Recog-WGGen.sh Resolutions.conf


# Replace the WG words by the corresponding real pronunciation
PutRealPronInWG.sh Resolutions.dic WG_REC WG_REC_ED

# As output, at the end of the execution of "Recog-WGGen.sh", a directory
# named REC (or WG_REC in case -w option was set) containing all corresponding 
# recognized hypotheses (and in WG_REC all corresponding word-graphs).
