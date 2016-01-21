EXTRA="--auth-no-challenge --http-user=jesse --http-password=dedoes"
wget $EXTRA -O- 'http://localhost:8080/LMServer/LMServer?action=INVOKE&command=INTERPOLATE_TWO&params={script:"WebContent/LMServerScripts/MultipleInterpolationFromConf.sh",conf:"WebContent/LMServerScripts/Settings/hattem.interpolate.settings.sh",languageModel:"languageModel.lm",dictionary:"dictionary.txt", latticeFile:"latticeFile.txt", MODEL_DESTINATION_DIR:"veryUsefulInterpolatedLanguageModel", COMPONENT_0:"/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/HATTEM-LM", COMPONENT_1:"/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TC", CHARSET:"resources/CharacterSets/hattem.charset.txt", VALIDATION_FILE:"/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TEST-SET/normalizedText.txt"}'


# should be loaded:

# Webcontent/LMserverScripts/MultipleInterpolationFromConf.sh
# WebContent/LMServerScripts/Settings/hattem.interpolate.settings.sh
# resources/CharacterSets/hattem.charset.txt
# collection "/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/HATTEM-LM"
# collection "/home/jesse/TUTORIAL-HTR/EXP-HATTEM/TRAIN/LM/TC"



