#!/bin/bash

CURR_DIR=`pwd`
NAME=${0##*/}
#TMP=${CURR_DIR}/${0##*/}_$$; mkdir $TMP
#trap "rm -rf $TMP 2>/dev/null" EXIT

NCRS=1
AFLG=0
while [ $# -ge 1 ] && [ "$1" != "${1#-}" ]; do
  if [ ${1} = "-n" ]; then NCRS=$2; shift 2; fi
  if [ ${1} = "-a" ]; then AFLG=1; shift; fi
done
if [ $# -lt 8 -o $# -gt 9 ]; then
  echo "Usage: $NAME [-n <+int>] [-a] <Dir-source-pgm> <img-ext> <shift> <win-size> <height> <width> <Dim-proj> <Train-list-file> [<PCA-Proj-File>]" 1>&2
  echo -e "\n\tOption:"
  echo -e "\t\t-n\t Number of cores to use"
  echo -e "\t\t-a\t ATROS output format"
  exit 1
fi

IMG_DIR=$1
IMG_EXT=$2
FEEX_SHIFT=$3
FEEX_WIN_SIZE=$4
FEEX_HEIGHT=$5
FEEX_WIDTH=$6
NUM_DIM_START=$(expr $FEEX_HEIGHT '*' $FEEX_WIDTH)
FEEX_NUM_DIM_PROJ=$7
NUM_DIM_END=$(expr $FEEX_NUM_DIM_PROJ '+' 4)
LIST_TRAIN_PAGES=$8
if [ "${LIST_TRAIN_PAGES}" != "-" ]; then PROJ_PCA="-"
else PROJ_PCA=${9:-"null"}; fi
if [ "${PROJ_PCA}" = "null" ]; then
  echo "<PCA-Proj-File> muest be especified" 1>&2
  exit 1
fi

echo "PROJ_PCA is now $PROJ_PCA!!!"
# Check for installed software
DOTMATRIX=$(which dotmatrix)
[ -z "$DOTMATRIX" ] && { echo "ERROR: \"dotmatrix\" soft is not installed/found "'!' 1>&2; exit 1; }
PCA=$(which fast_pca)
[ -z "$PCA" ] && { echo "ERROR: \"pca\" soft is not installed/found "'!' 1>&2; exit 1; }
PFL2HTK=$(which pfl2htk)
[ -z "$PFL2HTK" ] && { echo "ERROR: \"pfl2htk\" soft is not installed/found "'!' 1>&2; exit 1; }
GZIP=$(which gzip)
[ -z "$GZIP" ] && { echo "ERROR: \"gzip\" soft is not installed/found "'!' 1>&2; exit 1; }


MAT_FILE="proj_${IMG_DIR}_SH${FEEX_SHIFT}_W${FEEX_WIN_SIZE}_OD${NUM_DIM_START}_PRD${FEEX_NUM_DIM_PROJ}_FD${NUM_DIM_END}.mat"
WORK_DIR="WORK_${IMG_DIR}_SH${FEEX_SHIFT}_W${FEEX_WIN_SIZE}_OD${NUM_DIM_START}_PRD${FEEX_NUM_DIM_PROJ}_FD${NUM_DIM_END}"
DEST_DIR="FEA_${IMG_DIR}_SH${FEEX_SHIFT}_W${FEEX_WIN_SIZE}_OD${NUM_DIM_START}_PRD${FEEX_NUM_DIM_PROJ}_FD${NUM_DIM_END}"

function cabecera {
echo "Name        Bentham
DataType     CepstrumCoef
Structure    PCA  + 4 aux momentum"
}

if [ "${PROJ_PCA}" = "-" ]; then
  echo $MAT_FILE
else
  echo ${PROJ_PCA}
fi
echo $WORK_DIR
echo $DEST_DIR

# Generate pixel and momentum features for each line
if [ ! -d ${WORK_DIR} ]; then
  mkdir -p ${WORK_DIR}
  n=1
  for f in ${IMG_DIR}/*.${IMG_EXT}; do
    (
      echo "Feaure extraction in file: $f" 1>&2
      F=$(basename $f .${IMG_EXT})
      # Obtain 256 dimensional features
      #$DOTMATRIX -SXg --height $FEEX_HEIGHT --width $FEEX_WIDTH --shift $FEEX_SHIFT --win-size ${FEEX_WIN_SIZE} --slant -i $f > ${WORK_DIR}/$F.raw
      $DOTMATRIX -S --slant -i $f > ${WORK_DIR}/$F.raw
      [ $? -ne 0 ] && { rm ${WORK_DIR}/$F.raw; echo $f >> ${WORK_DIR}/Failed_Samples.lst; continue; }
      # Obtain 4 momentum components
      #$DOTMATRIX -M -SXg --height $FEEX_HEIGHT --width $FEEX_WIDTH --shift $FEEX_SHIFT --win-size ${FEEX_WIN_SIZE} --slant -i $f > ${WORK_DIR}/$F.aux
      $DOTMATRIX -MS --slant -i $f > ${WORK_DIR}/$F.aux
    ) &
    [ $n -ge "${NCRS}" ] && { wait || exit 1; n=0; }
    n=$[n+1];
  done
  wait || exit 1
fi

# Calculate projection matrix
if [ "${PROJ_PCA}" = "-" ]; then
  if [ ! -e ${WORK_DIR}/${MAT_FILE} ]; then
    echo "Building matrix of training vectors ..." 1>&2
    for f in $(< ${LIST_TRAIN_PAGES}); do
      cat ${WORK_DIR}/$f.raw
    done > ${WORK_DIR}/All_vect
    #rm ${TMP}/All_vect
    echo "Computing PCA projection matrix ..." 1>&2
    #$PCA -o PCA -i ROWS -v 1 -p ${WORK_DIR}/${MAT_FILE} -d ${TMP}/TrainVectors.dat > ${WORK_DIR}/LOG_PCA
    $PCA -C -f ascii -p 256  -q 20  ${WORK_DIR}/All_vect  > ${WORK_DIR}/${MAT_FILE} 2> ${WORK_DIR}/LOG_PCA_allvect
  fi
  PROJ_PCA=${WORK_DIR}/${MAT_FILE}
fi

echo "PROJ_PCA should be in $PROJ_PCA WORK DIR = ${WORK_DIR}, MAT_FILE = ${MAT_FILE}"
ls $PROJ_PCA

if [ ! -d ${DEST_DIR} ]; then
  mkdir -p ${DEST_DIR}
  n=1
  for f in ${WORK_DIR}/*.raw; do
    (
      echo "Projecting file: $f" 1>&2
      [ -e ${f/\.raw/.aux} ] || { echo "Corresponding file: ${f/\.raw/.aux} does not exist ..." 1>&2; continue; }
      F=$(basename $f .raw)
      { awk '{print NF}' $f |
        uniq -c; cat $f;
      } | sed -r "s/^ +//; s/ +/ /g; s/ +$//" > ${WORK_DIR}/vects_$n.tmp

      # Generates .pixel_proj for each line
      #$PCA -o PROJ -i ROWS -e ROWS -p ${PROJ_PCA} -d ${WORK_DIR}/vects_$n.tmp -x ${WORK_DIR}/proj_$n.tmp
      $PCA -P -f ascii -m ${WORK_DIR}/${MAT_FILE} -p 256 -q 20  -o ${WORK_DIR}/proj_$n.tmp $f 2> ${WORK_DIR}/LOG_PCA

      #tail -n +2 ${WORK_DIR}/proj_$n.tmp |
      #awk -v numComp=${FEEX_NUM_DIM_PROJ} '{for (i=1;i<=numComp;i++) printf $i" "; print ""}' |
      #sed -r "s/^ +//; s/ +/ /g; s/ +$//" |
      # Adds .momentum to each .pixel_proj generating .vec
      paste -d " " ${WORK_DIR}/proj_$n.tmp ${WORK_DIR}/$F.aux > ${WORK_DIR}/pfl2htk_$n

      if [ "$AFLG" -eq 1 ]; then
        # Adding ATROS headers for creating a .fea for each line 
        declare -a A=($(head -1 ${WORK_DIR}/proj_$n.tmp))
        { cabecera; echo "NumParam     $[FEEX_NUM_DIM_PROJ+4]"; echo "NumVect  ${A[0]}"; echo "Data"; } > ${DEST_DIR}/$F.fea
        cat ${WORK_DIR}/pfl2htk_$n >> ${DEST_DIR}/$F.fea
        $GZIP -9 ${DEST_DIR}/$F.fea
      else
        $PFL2HTK ${WORK_DIR}/pfl2htk_$n ${DEST_DIR}/$F.fea
      fi
    ) &
    [ $n -ge "${NCRS}" ] && { wait || exit 1; n=0; }
    n=$[n+1];
  done
  wait || exit 1
  #rm ${WORK_DIR}/*.aux ${WORK_DIR}/*.raw
fi

if [ "${PROJ_PCA}" = "-" ]; then
  echo " Computing Global Std-Dev ..." 1>&2
  for f in $(< ${LIST_TRAIN_PAGES}); do
    echo "Processing file: $f" 1>&2
    zcat ${DEST_DIR}/$f.fea.gz | tail -n +7
  done |
  awk '{for (i=1;i<=NF;i++) {sum[i]+=$i; sum2[i]+=$i*$i}}\
       END{for (i=1;i<=NF;i++) {u[i]=sum[i]/NR; s[i]=sqrt(sum2[i]/NR-u[i]); print NR,u[i],s[i]}}' > ${WORK_DIR}/Desv.inf
fi
