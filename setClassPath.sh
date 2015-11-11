export CLASSPATH=$CLASSPATH:./build/classes
for x in `grep jar .classpath  | perl -pe 's/.*path="(.*)".*/$1/'`;
do
  export CLASSPATH=$CLASSPATH:$x;
done

echo $CLASSPATH
