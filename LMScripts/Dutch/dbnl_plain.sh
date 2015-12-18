DESTINATION=/media/jesse/Data/tranScriptorium/NederlandseData/MNL-plaintext/DBNL-artes
for x in `ls /media/jesse/Data/tranScriptorium/NederlandseData/artestxten_DBNL/DBNL_artes-gemarkeerd/*txt`;
do
  y=`basename $x`;
  echo $y;
  perl LMScripts/Dutch/dbnl_artes_plain_text.pl $x > $DESTINATION/$y; 
done

