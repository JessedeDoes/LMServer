################################################
# Recognition Phase and Word-Graph generation
################################################

### avoid relative path stuff.....

source $1

cd $EXP_DIR/TEST

# Set PATH to include the "bin" and "scripts" dirs
export PATH=$(realpath ../../bin):$(realpath ../../scripts):$PATH


# Replace the WG words by the corresponding real pronunciation
PutRealPronInWG.sh $TEST_DIC WG_REC WG_REC_ED

# As output, at the end of the execution of "Recog-WGGen.sh", a directory
# named REC (or WG_REC in case -w option was set) containing all corresponding 
# recognized hypotheses (and in WG_REC all corresponding word-graphs).
