source setClassPath.sh
CLASS=eu.transcriptorium.repository.RepositoryCL
PROPS='{language:spanish,year_from:"<1790",type:corpus_plaintext,"eu.transcriptorium.repository.DistanceTest(2268)":"<0.3"}'
java $CLASS SEARCH $PROPS 

