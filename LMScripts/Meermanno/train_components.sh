for x in `ls LMScripts/Meermanno/*.sh | grep -v interp | grep -v create | grep -v component `; do echo $x; bash LMScripts/basicModelBuilding.sh $x; done;
