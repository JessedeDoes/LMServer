source setClassPath.sh
java eu.transcriptorium.repository.RepositoryCL INVOKE BUILDLM_LIST '{script:"WebContent/LMServerScripts/basicModelBuildingFromList.sh",conf:"WebContent/LMServerScripts/basic.settings.sh",languageModel:"languageModel.lm",dictionary:"dictionary.txt",latticeFile:"latticeFile.txt",OUTPUT:"veryUsefulLanguageModel",CORPUS:"2489,2338,2300,2343,2502,2415,2290",CHARSET:"resources/CharacterSets/AuxHMMsList"}'