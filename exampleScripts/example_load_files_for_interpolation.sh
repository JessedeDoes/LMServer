source setClassPath.sh
CLASS=eu.transcriptorium.repository.RepositoryCL
PROPS='{language:dutch}'
java $CLASS CLEAR
java $CLASS STORE WebContent/LMServerScripts/MultipleInterpolationFromConf.sh
java $CLASS STORE WebContent/LMServerScripts/Settings/hattem.interpolate.settings.sh
java $CLASS STORE resources/CharacterSets/hattem.charset.txt
java $CLASS STORE /home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TEST-SET/normalizedText.txt
java $CLASS STORE_COLLECTION "/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/HATTEM-LM" $PROPS 
java $CLASS STORE_COLLECTION "/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TC" $PROPS
java $CLASS REPLACE_METADATA '{filename:"~\.lm$"}' '{type:lm}'
java $CLASS SEARCH '{type:lm}'

