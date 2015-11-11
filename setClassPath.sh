export CLASSPATH=$CLASSPATH:./build/classes
for x in `grep jar .classpath  | grep -v 'ant.jar'  | perl -pe 's/.*path="(.*)".*/$1/' | perl -pe 's/N:/\/mnt\/Projecten\//'`;
do
  export CLASSPATH=$CLASSPATH:$x;
done

echo $CLASSPATH
