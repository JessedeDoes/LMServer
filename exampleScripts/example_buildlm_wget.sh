EXTRA="--auth-no-challenge --http-user=jesse --http-password=dedoes"
wget $EXTRA -O- 'http://localhost:8080/LMServer/LMServer?action=INVOKE&command=BUILDLM&script=WebContent/LMServerScripts/basicModelBuilding.sh&conf=WebContent/LMServerScripts/basic.settings.sh&languageModel=languageModel.lm&dictionary=dictionary.txt&latticeFile=latticeFile.txt&OUTPUT=notVeryUsefulLanguageModel&CHARSET=resources/CharacterSets/AuxHMMsList&CORPUS=English/ECCO/plainText//5333.txt'

