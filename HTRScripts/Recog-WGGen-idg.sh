#!/bin/bash

PROG=${0##*/}

RFLG=0
AFLG=0

NAME=${0##*/}
if [ $# -ne 1 ]; then
  echo "Usage: $NAME ConfigFile"
  exit
fi
source $1

ATROS_FORMAT=${ATROS_FORMAT:-0}
TEST_GSF=${GSF:-15.0}
TEST_WIP=${WIP:--26.0}
TEST_NUM_CORES=${TEST_NUM_CORES:-1}
RECOGNITION_TYPE=${RECOGNITION_TYPE:-0}
LIST_HMMS=AuxHMM.lst


[ -e $TEST_HMMFILE ] || { echo "ERROR: File \"$TEST_HMMFILE\" does not exist \!"; exit 1; }
#[ -d $FEDIR ] || { echo "ERROR: Dir \"$FEDIR\" does not exist \!"; exit 1; }
[ -e $TEST_DIC ] || { echo "ERROR: File \"$TEST_DIC\" does not exist \!"; exit 1; }
[ -e $TEST_SLF ] || { echo "ERROR: File \"$TEST_SLF\" does not exist \!"; exit 1; }
[ -e $TEST_LST_SAMPLES ] || { echo "ERROR: File \"$TEST_LST_SAMPLES\" does not exist \!"; exit 1; }
[ -e $TEST_LST_HMMs ] || { echo "ERROR: File \"${TEST_LST_HMMs}\" does not exist \!"; exit 1; }

awk '{print $1}' $TEST_LST_HMMs > $LIST_HMMS

for f in $(<$TEST_LST_SAMPLES); do
 [ -e $f ] || { echo "ERROR: File \"$f\" does not exist \!"; exit 1; }
done



# Check for installed software
HVITE=$(which HVite)
[ -z "$HVITE" ] && { echo "ERROR: \"HVite\" soft is not installed/found "'!' 1>&2; exit 1; }
AT_C=$(which at)
[ -z "${AT_C}" ] && { echo "ERROR: \"at\" soft is not installed/found "'!' 1>&2; exit 1; }


EXFLGS=""
DRES="REC"
if [ "${RECOGNITION_TYPE}" -eq 1 ]; then
  EXFLGS="-m -o W"
elif [ "${RECOGNITION_TYPE}" -eq 2 ]; then
  EXFLGS="-n "${TEST_WG_IDG} " 1 -z lat -q Atvalr"
  DRES="WG_REC"
fi
[ -d $DRES ] && { echo -e "WARNING: Directory \"$DRES\" already exist... removing it "'!' 1>&2; rm -rf $DRES; }
mkdir -p $DRES

[ -d AuxProc ] && rm -rf AuxProc; mkdir AuxProc
LSAMPLES=`basename $TEST_LST_SAMPLES`

# Creating config file if necessary
if [ "${ATROS_FORMAT}" -eq 1 ]; then
  VEC_SIZE=$(zcat `head -1 $TEST_LST_SAMPLES` | grep "NumParam" | awk '{print $2}')
  {
    echo "HPARMFILTER    = \"gzip -d -c $.gz\""
    echo "SOURCEFORMAT   = ATROS" 
    echo "NUMCEPS        = ${VEC_SIZE}"
    echo "TARGETKIND     = USER"
  } > AuxProc/config_HVITE
  sed "s/\.gz$//" $TEST_LST_SAMPLES > AuxProc/$LSAMPLES   # Delete .gz extensions
  FLAGSHVITE="-A -T 1 -C AuxProc/config_HVITE $EXGFLGS"
else
  cp $TEST_LST_SAMPLES AuxProc/$LSAMPLES
  FLAGSHVITE="-A -T 1 $EXFLGS"
fi

# Split the feature file list into the number of cores 
split -a 3 -d -n r/${TEST_NUM_CORES} AuxProc/$LSAMPLES AuxProc/part-

# Perform decoding
for d in $(seq -f "%03.0f" 0 1 $[${TEST_NUM_CORES}-1]); do
  echo "Launching recognition for sub-partition $d ..." 1>&2
  echo "HVite $FLAGSHVITE \
              -s $TEST_GSF -p $TEST_WIP \
	      -H $TEST_HMMFILE \
	      -l $DRES \
	      -S AuxProc/part-$d \
	      -w $TEST_SLF \
	      $TEST_DIC \
	      ${LIST_HMMS} 2>LOG-Dec-$d 1>&2" |
  ${AT_C} now
done

exit 0
