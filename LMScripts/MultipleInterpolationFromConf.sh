# usage:
# arg 1: output folder
# arg 2: text to evaluate against
# arg 3-last: language model folders for to-be-interpolated models


source $1

echo "charset type: $CLASS_CHARSET $CHARSET"
TEXT=$VALIDATION_TEXT
DESTINATION=$MODEL_DESTINATION_DIR
SUBMODEL_DIRS=$COMPONENT_MODEL_DIRS

bash LMScripts/MultipleInterpolation.sh  $TEXT $DESTINATION $SUBMODEL_DIRS
