DESTINATION=/media/jesse/Data/tranScriptorium/NederlandseData/MNL-plaintext/L-artes
DIR1=/media/jesse/Data/tranScriptorium/NederlandseData/CdromtekstenMetBrill/Artes
DIR2=/media/jesse/Data/tranScriptorium/NederlandseData/CdromtekstenMetBrill/prozaMetBrill
DIR3=/media/jesse/Data/tranScriptorium/NederlandseData/CdromtekstenMetBrill/rijm

DEST1=/media/jesse/Data/tranScriptorium/NederlandseData/MNL-plaintext/Artes/CDROM-artes
DEST2=/media/jesse/Data/tranScriptorium/NederlandseData/MNL-plaintext/CDROM-proza
DEST3=/media/jesse/Data/tranScriptorium/NederlandseData/MNL-plaintext/CDROM-rijm 

convert()
{
  SRC=$1
  DEST=$2
  for x in `ls $SRC/*xml`;
  do
    y=`basename $x .xml`;
    echo $y;
    perl cdrom_mnl_plain_text.pl $x > $DEST/$y.txt;
  done
}

convert $DIR1 $DEST1
convert $DIR2 $DEST2
convert $DIR3 $DEST3

