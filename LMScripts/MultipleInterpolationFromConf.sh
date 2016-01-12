# usage:
# arg 1: output folder
# arg 2: text to evaluate against
# arg 3-last: language model folders for to-be-interpolated models


source $1

COMPONENT_MODEL_DIRS=$COMPONENT_0
arg="dummy"
i=1
while [ -n  "$arg" ] ;
do
  echo "i=$i"
  eval arg=\${COMPONENT_$i}
  i=$[ $i+1 ]
  if [ -n "$arg" ] ;
  then
    echo "|$arg|"
    COMPONENT_MODEL_DIRS="$COMPONENT_MODEL_DIRS $arg"
  fi
done

echo "Component model dirs: 
echo "charset type: $CLASS_CHARSET $CHARSET"
TEXT=$VALIDATION_TEXT
DESTINATION=$MODEL_DESTINATION_DIR
SUBMODEL_DIRS=$COMPONENT_MODEL_DIRS
echo $SUBMODEL_DIRS

# bash /var/lib/tomcat7/webapps/LMServer/LMScripts/MultipleInterpolation.sh  $TEXT $DESTINATION $SUBMODEL_DIRS
