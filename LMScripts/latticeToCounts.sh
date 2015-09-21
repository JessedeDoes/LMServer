DIR=/datalokaal/Scratch/HTR/HTR/BenthamData/Models/
/mnt/Projecten/transcriptorium/Tools/SRILM/bin/i686-m64/lattice-tool  -read-htk -in-lattice $DIR/bgram_0.gram -write-ngrams /tmp/test.counts
/mnt/Projecten/transcriptorium/Tools/SRILM/bin/i686-m64/ngram-count -read /tmp/test.counts -order 2 -kndiscount -lm /tmp/test.lm
