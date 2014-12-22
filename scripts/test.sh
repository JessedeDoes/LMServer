LATTICES=/datalokaal/Scratch/HTR/BFBNew/Lattices
LATTICES=/datalokaal/Scratch/HTR/BFBNew/StoredExperiments/NewModels
LATTICES=./Lattices
DICT=./data/dictionary.txt
java -cp ./bin eu.transcriptorium.lattice.LatticeDecoder $LATTICES $DICT > Temp/test.out 
perl scripts/makeResultsFileFrom1BestResult.pl  Temp/test.out > Temp/test.results.1
perl -pe 's/#.*//' Temp/test.results.1 > Temp/test.results
tasas Temp/test.results -ie -f '$' -s ' '
#17.170819

