#!/bin/bash

#PATH=$PATH:/h/mpastorg/bin.linux

NAME=${0##*/}
if [ $# -ne 5 ]; then
  echo "Usage: $NAME <train.lst> <labels> <hmm> <L_HMMS> <procesNumber>"
  exit
fi

LSTTRA=$1
LABELS=$2
HMM_DIR=$3
L_HMMS=$4
N=$5

FLAGSHEREST="-A -T 1 -v 0.01 -m 3"

HERest  ${FLAGSHEREST} -S $LSTTRA -I $LABELS -H ${HMM_DIR}/Macros_hmm -M ${HMM_DIR} -p $N $L_HMMS  1>&2

echo $N >> Finished_processes

