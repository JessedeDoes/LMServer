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

echo "Hallo? Zie je me nog? charset type: $CLASS_CHARSET $CHARSET"
TEXT=$VALIDATION_FILE
DESTINATION=$MODEL_DESTINATION_DIR
SUBMODEL_DIRS=$COMPONENT_MODEL_DIRS
echo "SUBMODEL_DIRS=$SUBMODEL_DIRS, DESTINATION=$MODEL_DESTINATION_DIR, TEXT=$VALIDATION_FILE"

CMD="bash $LM_SCRIPT_PATH/MultipleInterpolation.sh  $DESTINATION $TEXT $SUBMODEL_DIRS"
echo "CMD: $CMD"
$CMD
