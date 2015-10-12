#!/bin/bash

# Copyright (C) 1997 by Pattern Recognition and Human Language
# Technology Group, Technological Institute of Computer Science,
# Valencia University of Technology, Valencia (Spain).
#
# Permission to use, copy, modify, and distribute this software and its
# documentation for any purpose and without fee is hereby granted, provided
# that the above copyright notice appear in all copies and that both that
# copyright notice and this permission notice appear in supporting
# documentation.  This software is provided "as is" without express or
# implied warranty.


NAME=${0##*/}
if [ $# -ne 1 ]; then
  echo "Usage: $NAME ConfigFile"
  exit 0
fi
source $1

PROC_USE_BOUNDING_POLYGONS=${PROC_USE_BOUNDING_POLYGONS:-0}
PROC_IS_PCA_PROJECTION=${PROC_IS_PCA_PROJECTION:-0}
PROC_NUM_CORES=${PROC_NUM_CORES:-1}
PROC_IMAGE_EXTENSION=${PROC_IMAGE_EXTENSION:-jpg}
ATROS_FORMAT=${ATROS_FORMAT:-0}

[ -z "${CRP_ID}" ] && { echo "ERROR: Corpus ID string is empty \!"; exit 1; }
[ -d ${PROC_IMAGE_DIR} ] || { echo "ERROR: Dir \"${PROC_IMAGE_DIR}\" does not exist \!"; exit 1; }
[ -d ${PROC_XMLPAGE_DIR} ] || { echo "ERROR: Dir \"${PROC_XMLPAGE_DIR}\" does not exist \!"; exit 1; }
[ -e $PROC_EFILE ] || { echo "ERROR: File \"$PROC_EFILE\" does not exist \!"; exit 1; }

# Check for installed software
IMGTXTENH=$(which imgtxtenh)
[ -z "$IMGTXTENH" ] && { echo "ERROR: \"imgtxtenh\" soft is not installed/found "'!' 1>&2; exit 1; }
XMLSTARLET=$(which xmlstarlet)
[ -z "$XMLSTARLET" ] && { echo "ERROR: \"xmlstarlet\" soft is not installed/found "'!' 1>&2; exit 1; }
IMGLINECLEAN=$(which imglineclean)
[ -z "$IMGLINECLEAN" ] && { echo "ERROR: \"imglineclean\" soft is not installed/found "'!' 1>&2; exit 1; }
PAGEFORMAT=$(which page_format_tool)
[ -z "$PAGEFORMAT" ] && { echo "ERROR: \"page_format_tool\" soft is not installed/found "'!' 1>&2; exit 1; }
PCONTOUR=$(which page_format_generate_contour)
[ -z "$PCONTOUR" ] && { echo "ERROR: \"page_format_generate_contour\" soft is not installed/found "'!' 1>&2; exit 1; }
CONVERT=$(which convert)
[ -z "$CONVERT" ] && { echo "ERROR: IMAGE-MAGICK package is not installed "'!' 1>&2; exit 1; }
EXTRFEAT=$(which extract_rwth_features.sh)
[ -z "$EXTRFEAT" ] && { echo "ERROR: \"extract_rwth_features.sh\" soft is not installed/found "'!' 1>&2; exit 1; }
XMLSIZE=$(which xmlpage_resize.sh)
[ -z "$XMLSIZE" ] && { echo "ERROR: \"xmlpage_resize.sh\" soft is not installed/found "'!' 1>&2; exit 1; }

SOURCE_DENS=118  # 300dpi / 2.54cm
TARGET_DENS=${SOURCE_DENS}
#sfact=$(echo ${SOURCE_DENS} ${TARGET_DENS} | awk '{printf("%g%%",100*$1/$2)}');

if [ ! -d AuxProc ]; then
  
  [ -d Transcriptions ] && rm -rf Transcriptions;  mkdir Transcriptions
  [ -e LOG_Line-Extraction ] && rm LOG_Line-Extraction
  mkdir -p AuxProc/{Pages,Lines}

  n=1
  for f in ${PROC_IMAGE_DIR}/*.${PROC_IMAGE_EXTENSION}; do
    (
      F=$(basename $f);
      echo -e "\nProcessing Image: $F"
      [ -e ${PROC_XMLPAGE_DIR}/${F/\.${PROC_IMAGE_EXTENSION}/.xml} ] ||
      {
        echo "WARNING: Image \"$F\" does not have corresponding PAGE XML file" >> LOG_Line-Extraction
        continue
      }
  
      # Perform background removal and noise reduction at page level
      # $CONVERT $f -resize $sfact png:- ||
      { $CONVERT $f png:- ||
      {
        echo "ERROR: \"convert\" with sample ${F/\.${PROC_IMAGE_EXTENSION}/.png}" >> ../LOG_Line-Extraction
        continue
      }; } |
      $IMGTXTENH -d ${TARGET_DENS} -r 0.16 -w 10 -k 0.1 - AuxProc/${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.png} || \
      { 
        echo "ERROR: \"imgtxtenh\" with sample ${F/\.${PROC_IMAGE_EXTENSION}/.png}" >> ../LOG_Line-Extraction
        continue
      }

      # Edit XML files with the new image paths 
      cat ${PROC_XMLPAGE_DIR}/${F/\.${PROC_IMAGE_EXTENSION}/.xml} |
      # $XMLSIZE $sfact |
      $XMLSTARLET ed -P -u '//@imageFilename' -v "${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.png}" > AuxProc/${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.xml} ||
      { 
        echo "ERROR: \"xmlstrlet\" with sample ${F/\.${PROC_IMAGE_EXTENSION}/.png}" >> ../LOG_Line-Extraction
        continue
      }
   
      if [ ${PROC_USE_BOUNDING_POLYGONS} -eq 1 ]; then
        # Obtain new XML files with line bounding polygons computed from previous baselines
        $PCONTOUR -a 75 -d 25 -p AuxProc/${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.xml} -o AuxProc/aux_$n || \
        { 
          echo "ERROR: \"page_format_generate_contour\" with sample ${F/\.${PROC_IMAGE_EXTENSION}/.png}" >> ../LOG_Line-Extraction
          continue
        }
        mv AuxProc/aux_$n AuxProc/${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.xml} # New edited XML
      fi

      # Perform line extraction itself
      cd AuxProc
      $PAGEFORMAT -i ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.png} -l ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.xml} -m FILE || \
      { 
        echo "ERROR: \"page_format_tool\" with sample ${F/\.${PROC_IMAGE_EXTENSION}/.png}" >> ../LOG_Line-Extraction
        cd ..; continue
      }

      # Moving image and xml files to directory "Pages"
      mv ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.png} ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}/.xml} Pages
      mv ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}}*.txt ../Transcriptions

      # Line image cropping and elimination of ascending and descending elements
      # of adjacent lines
      #mkdir LinesRaw 2>/dev/null
      dilRcm=0.1; dilRpx=$(echo ${TARGET_DENS} $dilRcm | awk '{printf("%.0f",$1*$2)}');
      for l in ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}}*.png; do
        echo "Cleaning line: $l" 1>&2
        $IMGLINECLEAN -d ${TARGET_DENS} -m 99% $l png:- |
        $CONVERT - +repage -flatten -deskew 40% -fuzz 5% -trim +repage \
                 -bordercolor white -border ${dilRpx}x Lines/$l
        rm $l
      done
      #mv ${CRP_ID}.${F/\.${PROC_IMAGE_EXTENSION}}*.png LinesRaw
    
      cd ..
    ) &
    [ $n -ge "${PROC_NUM_CORES}" ] && { wait || exit 1; n=0; }
    n=$[n+1];
  done
  wait || exit 1
fi

FEFLG=""
[ ${ATROS_FORMAT} -eq 1 ] && FEFLG="-a"
# Perform feature extraction
if [ "${PROC_IS_PCA_PROJECTION}" -eq 0 ]; then
  for f in $(< $PROC_EFILE); do ls AuxProc/Lines/${CRP_ID}.$f*.png; done |
  sed -r "s/^.*\///; s/\.png$//" > AuxProc/trainList
  cd AuxProc
  echo $EXTRFEAT -n ${PROC_NUM_CORES} $FEFLG Lines png ${FEEX_SHIFT} ${FEEX_WIN_SIZE} ${FEEX_HEIGHT} ${FEEX_WIDTH} ${FEEX_NUM_DIM_PROJ} trainList
  $EXTRFEAT -n ${PROC_NUM_CORES} $FEFLG Lines png ${FEEX_SHIFT} ${FEEX_WIN_SIZE} ${FEEX_HEIGHT} ${FEEX_WIDTH} ${FEEX_NUM_DIM_PROJ} trainList
  cd ..
else
  cd AuxProc
  $EXTRFEAT -n ${PROC_NUM_CORES} $FEFLG Lines png ${FEEX_SHIFT} ${FEEX_WIN_SIZE} ${FEEX_HEIGHT} ${FEEX_WIDTH} ${FEEX_NUM_DIM_PROJ} - $PROC_EFILE
  cd ..
fi

# Creating useful links
WRK=$(find AuxProc -name "WORK_*")
MATPRJ=$(find $WRK -name "proj_*")
FEA=$(find AuxProc -name "FEA_*")
[ -n "$MATPRJ" ] && ln -fs $MATPRJ Proj-Mat.mat
[ -n "$FEA" ] && ln -fs $FEA Features


exit 0
