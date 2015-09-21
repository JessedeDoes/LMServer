
export PATH=/opt/jdk1.7.0/bin/:$PATH
export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM

#$SRILM_HOME/bin/i686-m64/ngram-count -order 3 -kndidiscount -text  --input $1 


COMMAND=train-lm



$SRILM_HOME/bin/i686-m64/lattice-tool -in-lattice in/test.lat -read-htk   -lm traininglm.lm -out-lattice out/test.lat -write-htk  -posterior-prune 0.001

#$SRILM_HOME/bin/i686-m64/ngram -debug 1 -order 2 -l -write-lm -make-ngram-pfsg add-pauses-to-pfsg version=1 pauselast=_pau top_level_name=.Top_LEVEL >LM.pfsg


