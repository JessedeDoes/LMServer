#!/bin/bash

PROG=${0##*/}

BPL=0
PRJ=0
NCRS=1
IEXT="jpg"
EFILE=""

if [ $# -lt 1 ] || [ "${1%elp}" = "-h"  ] ||
   [ $# -gt 10 ] || [ "${1%elp}" = "--h" ]; then
   echo -e "
   Usage: $PROG [-h] [-b] [-p] [-n <+int>] [-e <ext>] <Corpus-ID-Str> <IMG-Dir> 
                             <PAGE-Dir> <TrainList-File|Proj-File>" 1>&2
   echo -e "\n\tOption:"
   echo -e "\t\t-b\t Use directly the bounding polygon of each"
   echo -e "\t\t\t line to extract it (def = $BPL)"
   echo -e "\t\t-p\t Last argument of this command corresponds"
   echo -e "\t\t\t to the projection file (def = TrainList)"
   echo -e "\t\t-n\t Number of cores to use (def = $NCRS)"
   echo -e "\t\t-e\t Extension of page images (def = $IEXT)"
   echo -e "\t\t-h\t This help\n"
   exit 0
fi

while [ $# -ge 1 ] && [ "$1" != "${1#-}" ]; do
  if [ "${1}" = "-b"  ] ; then BPL=1; shift ; fi
  if [ "${1}" = "-p"  ] ; then PRJ=1; shift ; fi
  if [ "${1}" = "-n"  ] ; then NCRS=$2; shift 2; fi
  if [ "${1}" = "-e"  ] ; then IEXT=$2; shift 2; fi
done

if [ $# -ne 4 ]; then
  echo "Usage: $PROG [-h] [-b] [-p] [-n <+int>] [-e <ext>] <Corpus-ID-Str> <IMG-Dir>
                                  <PAGEs-Dir> <TrainList-File|Proj-File>" 1>&2
  exit 1
fi

CRPSTR=$1
IDIR=$2
PDIR=$3
EFILE=$4

[ -z "$CRPSTR" ] && { echo "ERROR: Corpus ID string is empty \!"; exit 1; }
[ -d $IDIR ] || { echo "ERROR: Dir \"$IDIR\" does not exist \!"; exit 1; }
[ -d $PDIR ] || { echo "ERROR: Dir \"$PDIR\" does not exist \!"; exit 1; }
[ -e $EFILE ] || { echo "ERROR: File \"$EFILE\" does not exist \!"; exit 1; }

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
  for f in $IDIR/*.$IEXT; do
    (
      F=$(basename $f);
      echo -e "\nProcessing Image: $F"
      [ -e $PDIR/${F/\.$IEXT/.xml} ] ||
      {
        echo "WARNING: Image \"$F\" does not have corresponding PAGE XML file" >> LOG_Line-Extraction
        continue
      }
  
      # $CONVERT $f -resize $sfact AuxProc/${CRPSTR}.${F/\.$IEXT/.png} ||
      $CONVERT $f AuxProc/${CRPSTR}.${F/\.$IEXT/.png} ||
      {
        echo "ERROR: \"convert\" with sample ${F/\.$IEXT/.png}" >> ../LOG_Line-Extraction
        continue
      }

      # Edit XML files with the new image paths 
      cat $PDIR/${F/\.$IEXT/.xml} |
      # $XMLSIZE $sfact |
      $XMLSTARLET ed -P -u '//@imageFilename' -v "${CRPSTR}.${F/\.$IEXT/.png}" > AuxProc/${CRPSTR}.${F/\.$IEXT/.xml} ||
      { 
        echo "ERROR: \"xmlstrlet\" with sample ${F/\.$IEXT/.png}" >> ../LOG_Line-Extraction
        continue
      }
   
      if [ $BPL -eq 1 ]; then
        # Obtain new XML files with line bounding polygons computed from previous baselines
        $PCONTOUR -a 75 -d 25 -p AuxProc/${CRPSTR}.${F/\.$IEXT/.xml} -o AuxProc/aux_$n || \
        { 
          echo "ERROR: \"page_format_generate_contour\" with sample ${F/\.$IEXT/.png}" >> ../LOG_Line-Extraction
          continue
        }
        mv AuxProc/aux_$n AuxProc/${CRPSTR}.${F/\.$IEXT/.xml} # New edited XML
      fi

      # Perform line extraction itself
      cd AuxProc
      $PAGEFORMAT -i ${CRPSTR}.${F/\.$IEXT/.png} -l ${CRPSTR}.${F/\.$IEXT/.xml} -m FILE || \
      { 
        echo "ERROR: \"page_format_tool\" with sample ${F/\.$IEXT/.png}" >> ../LOG_Line-Extraction
        cd ..; continue
      }

      # Moving image and xml files to directory "Pages"
      mv ${CRPSTR}.${F/\.$IEXT/.png} ${CRPSTR}.${F/\.$IEXT/.xml} Pages
      mv ${CRPSTR}.${F/\.$IEXT}*.txt ../Transcriptions

      # Line image cropping and elimination of ascending and descending elements
      # of adjacent lines
      dilRcm=0.1; dilRpx=$(echo ${TARGET_DENS} $dilRcm | awk '{printf("%.0f",$1*$2)}');
      for l in ${CRPSTR}.${F/\.$IEXT}*.png; do
        echo "Cleaning line: $l" 1>&2

        # Perform background removal and noise reduction at line level
        $IMGTXTENH -d ${TARGET_DENS} -a -r 0.16 -w 10 -k 0.1 $l png:- |
        $IMGLINECLEAN -d ${TARGET_DENS} -m 99% - png:- |
        $CONVERT - +repage -flatten -deskew 40% -fuzz 5% -trim +repage \
                 -bordercolor white -border ${dilRpx}x Lines/$l
        rm $l
      done
    
      cd ..
    ) &
    [ $n -ge "${NCRS}" ] && { wait || exit 1; n=0; }
    n=$[n+1];
  done
  wait || exit 1
fi

# Perform feature extraction
if [ "$PRJ" -eq 0 ]; then
  for f in $(< $EFILE); do ls AuxProc/Lines/${CRPSTR}.$f*.png; done |
  sed -r "s/^.*\///; s/\.png$//" > AuxProc/trainList
  cd AuxProc
  $EXTRFEAT -n $NCRS Lines png 2 20 32 8 20 trainList
  cd ..
else
  cd AuxProc
  $EXTRFEAT -n $NCRS Lines png 2 20 32 8 20 - $EFILE
  cd ..
fi

# Creating useful links
WRK=$(find AuxProc -name "WORK_*")
PRJ=$(find $WRK -name "proj_*")
FEA=$(find AuxProc -name "FEA_*")
[ -n "$PRJ" ] && ln -fs $PRJ Proj-Mat.mat
[ -n "$FEA" ] && ln -fs $FEA Features


exit 0
