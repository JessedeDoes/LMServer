#/bin/bash

# Copyright (C) 1997 by Pattern Recognition and Human Language
# Technology Group, Technological Institute of Computer Science,
# Valencia University of Technology, Valencia (Spain).
#
# Permission to use, copy, modify, and distribute this software and its
# documentation for any purpose and without fee is hereby granted, provided
# that the abocopyright notice appear in all copies and that both that
# copyright notice and this permission notice appear in supporting
# documentation.  This software is provided "as is" without express or
# implied warranty.

NAME=${0##*/}
if [ $# -ne 1 ]; then
  echo "Usage: $NAME ConfigFile"
  exit
fi

source $1

PATH=$PATH:$HTK_PATH

F=`head -1 $TRAIN_LST_SAMPLES`
Dim=`HList -h -z $F|awk '{if ($0 ~ /Num Comps:/) print $3}'`

[ -d AuxProc ] && rm -rf AuxProc; mkdir AuxProc

L_HMMS=AuxProc/AuxHMM.lst
SCRIPTS_PATH=`dirname $0`

awk '{print $1}' $TRAIN_LST_HMMs > $L_HMMS

ERR=0;
#checking files
[ -e $TRAIN_LST_SAMPLES ]   || { echo "ERROR: File \"${TRAIN_LST_SAMPLES}\" does not exist \!"  ; ERR=1; }
[ -d $TRAIN_DIRHMM ]   || { mkdir ${TRAIN_DIRHMM}; }
[ -e $TRAIN_LABELS ]   || { echo "ERROR: File \"${TRAIN_LABELS}\" does not exist \!"  ; ERR=1; }
[ -e $TRAIN_LST_HMMs ] || { echo "ERROR: File \"${TRAIN_LST_HMMs}\" does not exist \!"; ERR=1; }

#in case of err exit
[ $ERR -eq 1 ] && exit -1

#checking features files
for f in $(<$TRAIN_LST_SAMPLES); do
  [ -e $f ] || { echo "file \""$f"\" dosen't exists"; ERR=1; }
done

#in case of err exit
[ $ERR -eq 1 ] && exit -1

[ -e Finished_processes ] && rm Finished_processes

#FLAGSHEREST="-A -T 1 -v 0.01 -m 3"
FLAGSHEREST="-A -T 1 -m 3"

############################## Functions ########################
function CreaProto {
  echo "~o <VecSize> $1 <USER>"

  echo "~h \"proto\""
  echo "<BeginHMM>"
  echo "<NumStates> $[$2+2]"
  i=2
  while [ $i -le $[$2+1] ]; do
    echo "<State> $i"
    echo "<Mean> $1"
    j=2
    echo -n "0.0"
    while [ $j -lt $1 ]; do echo -n " 0.0"; j=$[j+1]; done
    echo " 0.0"
    echo "<Variance> $1"
    j=2
    echo -n "1.0"
    while [ $j -lt $1 ]; do echo -n " 1.0"; j=$[j+1]; done
    echo " 1.0"
    i=$[i+1]
  done
  echo "<TransP> $[$2+2]"
  i=1
  while [ $i -le $[$2+2] ]; do
    if [ $i -eq 1 ]; then
      echo -n "0.000e+0   1.000e+0"
      j=3
    fi
    if [ $i -gt 1 -a $i -le $[$2+1] ]; then
       echo -n "0.000e+0"
       j=2
       while [ $j -lt $i ]; do echo -n "   0.000e+0"; j=$[j+1]; done
       echo -n "   6.000e-1   4.000e-1"
       j=$[j+2]
    fi
    while [ $j -le $[$2+1] ]; do echo -n "   0.000e+0"; j=$[j+1]; done
    if [ $i -eq $[$2+1] ]; then
      echo ""
    fi
    if [ $i -le $[$2] ]; then
      echo "   0.000e+0"
    fi
    if [ $i -eq $[$2+2] ]; then
      echo -n "0.000e+0"
      j=2
      while [ $j -le $[$2+1] ]; do echo -n "   0.000e+0"; j=$[j+1]; done
      echo "   0.000e+0"
    fi
    i=$[i+1]
  done
  echo "<EndHMM>"
}


##################################################################
function DivListTrain()
{
  local FILE=$1
  local NP=$2
  local NM

  NM=`awk -v np=$NP 'END{printf("%d\n",NR/np)}' $FILE`

  for (( c=0; c<$NP; c++ )); do
    if [ $c -eq $[NP-1] ]; then 
      sed -n "$[NM*c+1],\$p" $FILE > ${FILE##*/}_$[c+1]
    else 
      sed -n "$[NM*c+1],$[NM*c+NM]p" $FILE > ${FILE##*/}_$[c+1]
    fi
  done
}
##################################################################
function waiting()
{
  local FILE="Finished_processes"
  local NM=$1 
  local NF

  while [ 1 ]; do
    if [ -f $FILE ]; then
      NF=`awk 'END{print NR}' $FILE`
      [ $NF -ge $NM ] && break
    fi
    sleep 5
  done
  rm $FILE
}
#which is the last trained model?
g=1

if [ -d $TRAIN_DIRHMM/hmm_0 ]; then
   g=1;
   while [ $g -le $TRAIN_NUM_GAUSS ]; do
         [ -d $TRAIN_DIRHMM/hmm_$g ] || break;
         g=$[g*2]
   done
else
   g=0;
fi

##################################################################
#            MAIN SCRIPT
##################################################################
#if the model are not initialized
if [ $g -eq 0 ];  then
   echo "Initialization..." 1>&2
   mkdir $TRAIN_DIRHMM/hmm_0
   CreaProto $Dim 1 > $TRAIN_DIRHMM/hmm_0/proto
   HCompV -A -T 1 -f 0.1 -m -S $TRAIN_LST_SAMPLES -M $TRAIN_DIRHMM/hmm_0 $TRAIN_DIRHMM/hmm_0/proto

   MEAN=$(sed -nr /\<MEAN\>/,+1p $TRAIN_DIRHMM/hmm_0/proto | tail -1)
   VARIA=$(sed -nr /\<VARIANCE\>/,+1p $TRAIN_DIRHMM/hmm_0/proto | tail -1)
   GCONF=$(sed -nr /\<GCONST\>/p $TRAIN_DIRHMM/hmm_0/proto | tail -1)

   head -3 $TRAIN_DIRHMM/hmm_0/proto > $TRAIN_DIRHMM/hmm_0/Macros_hmm
   cat $TRAIN_DIRHMM/hmm_0/vFloors >> $TRAIN_DIRHMM/hmm_0/Macros_hmm
set -f
   for m in $(<${L_HMMS}); do
        NS=$(awk -v N=$m 'N==$1 {print $2}' ${TRAIN_LST_HMMs})
        CreaProto $Dim $NS |
        sed 1d |
        awk -v N=$m -v M="$MEAN" -v V="$VARIA" -v G="$GCONF" '{
                Lin[NR]=$0
              }END{
                for (l=1;l<=NR;l++) {
                  if (Lin[l]~/proto/) sub(/proto/,N,Lin[l]);
                  if (toupper(Lin[l])~/MEAN/) {
                    print Lin[l];
                    print M;
                    l++;
                    continue;
                  }
                  if (toupper(Lin[l])~/VARIANCE/) {
                    print Lin[l];
                    print V;
                    print G;
                    l++;
                    continue;
                  }
                  print Lin[l]
                }
         }' | sed 's/proto/\&/g';
       done >> $TRAIN_DIRHMM/hmm_0/Macros_hmm

   g=1;
fi

set +f

# Split the feature file list into the number of cores 
N=`wc -l $TRAIN_LST_SAMPLES |awk -v cores=${TRAIN_NUM_NODES} '{printf("%i"), $1/cores+0.5 }'`
split -a 3 -d -l $N $TRAIN_LST_SAMPLES  AuxProc/part-

#train from g to TRAIN_NUM_GAUS
while [ $g -le $TRAIN_NUM_GAUSS ]; do
  mkdir $TRAIN_DIRHMM/hmm_$g
  echo "Creating hmm with $g gaussians ..." 1>&2
  if [ $g -eq 1 ]; then
      cp $TRAIN_DIRHMM/hmm_0/Macros_hmm $TRAIN_DIRHMM/hmm_1/Macros_hmm
  else
     [ -f mult_gauss_$g ] && rm mult_gauss_$g
     for m in $(<${L_HMMS}); do
        NS=$(awk -v N=$m 'N==$1 {print $2}' ${TRAIN_LST_HMMs})
        echo "MU $g {\"$m\".state[2-$[NS-1]].mix}" >> mult_gauss_$g
     done
     HHEd -A -H $TRAIN_DIRHMM/hmm_$[g/2]/Macros_hmm -M $TRAIN_DIRHMM/hmm_$g mult_gauss_$g $L_HMMS
     rm mult_gauss_$g
  fi
  echo "Re-estimation of hmm_$g with $g gaussians ..."  1>&2
  k=1
  while [ $k -le $TRAIN_NUM_ITER ]; do
    echo "Re-estimation $k of hmm_$g with $g gaussians ..."  1>&2

    for c in $(seq -f "%03.0f" 0 1 $[${TEST_NUM_NODES}-1]); do
      if [ $TRAIN_CLUSTER -eq 1 ] ;then  
       echo -e $SCRIPTS_PATH/HERest.sh  AuxProc/part-$c  $TRAIN_LABELS  $TRAIN_DIRHMM/hmm_$g $L_HMMS $(expr $c + 1)|qsub  -S /bin/bash -l h_vmem=1g,h_rt=44:00:00 -cwd -N "Train_$c" -o $TRAIN_DIRHMM/hmm_$g/log_$c -j y;
      else 
         ( $SCRIPTS_PATH/HERest.sh  AuxProc/part-$c  $TRAIN_LABELS  $TRAIN_DIRHMM/hmm_$g $L_HMMS $(expr $c + 1) ) 2> $TRAIN_DIRHMM/hmm_$g/log_$c &
      fi
    done

    waiting $TRAIN_NUM_NODES

    HERest $FLAGSHEREST -H $TRAIN_DIRHMM/hmm_$g/Macros_hmm -p 0 $L_HMMS $TRAIN_DIRHMM/hmm_$g/*.acc 1>&2

    cat -v  $TRAIN_DIRHMM/hmm_$g/log_* >>  $TRAIN_DIRHMM/hmm_$g/log
    rm  $TRAIN_DIRHMM/hmm_$g/log_* 
    rm  $TRAIN_DIRHMM/hmm_$g/*.acc

    k=$[k+1]
  done

  g=$[g*2]
done
rm ${TRAIN_LST_SAMPLES}_*
rm ${L_HMMS}
