java -cp ./bin/ eu.transcriptorium.lattice.LatticeListDecoder  > Temp/listDecoder.1.out 2>/dev/null
perl scripts/makeResultsFileFrom1BestResult.pl -i Temp/listDecoder.1.out > Temp/listDecoder.result.1
perl -pe 's/#.*//' Temp/listDecoder.result.1 > Temp/listDecoder.result.ci
tasas Temp/listDecoder.result.ci -ie -f "$" -s " "
# 15.645653
java -cp ./bin/ eu.transcriptorium.lattice.LatticeListDecoder  ./Lattices data/dictionary.txt > Temp/listDecoder.2.out 2>/dev/null
perl scripts/makeResultsFileFrom1BestResult.pl Temp/listDecoder.2.out > Temp/listDecoder.result.2
perl -pe 's/#.*//' Temp/listDecoder.result.2 > Temp/listDecoder.result.cs
tasas Temp/listDecoder.result.cs -ie -f "$" -s " "
#16.611591
