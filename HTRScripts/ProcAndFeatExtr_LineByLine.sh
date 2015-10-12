#!/bin/bash

PROG=${0##*/}

TMP=${PWD}/${0##*/}_$$; mkdir $TMP
trap "rm -rf $TMP 2>/dev/null" EXIT

if [ $# -lt 1 ] || [ "${1%elp}" = "-h"  ] ||
   [ $# -gt 3 ] || [ "${1%elp}" = "--h" ]; then
   echo -e "
   Usage: $PROG [-h] <PROJ-File> <IN-LineImg-File> <OUT-FeatExtr-File>" 1>&2
   echo -e "\n\tOption:"
   echo -e "\t\t-h\t This help\n"
   exit 0
fi

if [ $# -ne 3 ]; then
  echo "Usage: $PROG [-h] <PROJ-File> <IN-LineImg-File> <OUT-FeatExtr-File>" 1>&2
  exit 1
fi

PROJF=$1
IIMGF=$2
OFEAF=$3

[ -e $PROJF ] || { echo "ERROR: File \"$PROJF\" does not exist \!"; exit 1; }
[ -e $IIMGF ] || { echo "ERROR: File \"$IIMGF\" does not exist \!"; exit 1; }
[ -e $OFEAF ] && { echo "ERROR: File \"$OFEAF\" does exist \!"; exit 1; }

# Check for installed software
IMGTXTENH=$(which imgtxtenh)
[ -z "$IMGTXTENH" ] && { echo "ERROR: \"imgtxtenh\" soft is not installed/found "'!' 1>&2; exit 1; }
IMGLINECLEAN=$(which imglineclean)
[ -z "$IMGLINECLEAN" ] && { echo "ERROR: \"imglineclean\" soft is not installed/found "'!' 1>&2; exit 1; }
CONVERT=$(which convert)
[ -z "$CONVERT" ] && { echo "ERROR: IMAGE-MAGICK package is not installed "'!' 1>&2; exit 1; }
EXTRFEAT=$(which extract_rwth_features.sh)
[ -z "$EXTRFEAT" ] && { echo "ERROR: \"extract_rwth_features.sh\" soft is not installed/found "'!' 1>&2; exit 1; }
DOTMATRIX=$(which dotmatrix)
[ -z "$DOTMATRIX" ] && { echo "ERROR: \"dotmatrix\" soft is not installed/found "'!' 1>&2; exit 1; }
PCA=$(which pca)
[ -z "$PCA" ] && { echo "ERROR: \"pca\" soft is not installed/found "'!' 1>&2; exit 1; }
PFL2HTK=$(which pfl2htk)
[ -z "$PFL2HTK" ] && { echo "ERROR: \"pfl2htk\" soft is not installed/found "'!' 1>&2; exit 1; }

SOURCE_DENS=118  # 300dpi / 2.54cm
TARGET_DENS=${SOURCE_DENS}
#sfact=$(echo ${SOURCE_DENS} ${TARGET_DENS} | awk '{printf("%g%%",100*$1/$2)}');

# Line image cropping and elimination of ascending and descending elements
# of adjacent lines
dilRcm=0.1; dilRpx=$(echo ${TARGET_DENS} $dilRcm | awk '{printf("%.0f",$1*$2)}');
# Perform background removal and noise reduction at line level
AUX=$(echo $IIMGF | sed -r "s/^.*\///"; s/\....$//)
$IMGTXTENH -d ${TARGET_DENS} -a -r 0.16 -w 10 -k 0.1 $IIMGF png:- |
$IMGLINECLEAN -d ${TARGET_DENS} -m 99% - png:- |
$CONVERT - +repage -flatten -deskew 40% -fuzz 5% -trim +repage \
         -bordercolor white -border ${dilRpx}x $TMP/$AUX.png

SHIFT=2
WIN_SIZE=20
HEIGHT=32
WIDTH=8
NUM_DIM_START=$(expr $HEIGHT '*' $WIDTH)
NUM_DIM_PROJ=20
NUM_DIM_END=$(expr $NUM_DIM_PROJ '+' 4)

# Generate pixel and momentum features for each line
$DOTMATRIX -SwNXg --height $HEIGHT --width $WIDTH --shift=$SHIFT --win-size=${WIN_SIZE} --slant $TMP/$AUX.png > $TMP/$AUX.raw
[ $? -ne 0 ] && { rm ${WORK_DIR}/$F.raw; echo $f >> ${WORK_DIR}/Failed_Samples.lst; continue; }
$DOTMATRIX -SwNXg --height $HEIGHT --width $WIDTH --shift=$SHIFT --win-size=${WIN_SIZE} --slant --aux $TMP/$AUX.png > $TMP/$AUX.aux

{ awk '{print NF}' $TMP/$AUX.raw |
  uniq -c; cat $TMP/$AUX.raw;
} | sed -r "s/^ +//; s/ +/ /g; s/ +$//" > ${TMP}/vects.tmp
# Generates .pixel_proj for each line
$PCA -o PROJ -i ROWS -e ROWS -p $PROJF -d ${TMP}/vects.tmp -x ${TMP}/proj.tmp

tail -n +2 ${TMP}/proj.tmp |
awk -v numComp=${NUM_DIM_PROJ} '{for (i=1;i<=numComp;i++) printf $i" "; print ""}' |
sed -r "s/^ +//; s/ +/ /g; s/ +$//" |
# Adds .momentum to each .pixel_proj generating .vec
paste -d " " - $TMP/$AUX.aux > ${TMP}/pfl2htk

$PFL2HTK ${TMP}/pfl2htk $OFEAF

exit 0
