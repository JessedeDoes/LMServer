source setClassPath.sh
java eu.transcriptorium.repository.RepositoryCL INVOKE BUILDLM '{script:"WebContent/LMServerScripts/basicModelBuilding.sh",conf:"TestScripts/test.settings.sh",languageModel:"languageModel.lm",dictionary:"dictionary.txt", latticeFile:"latticeFile.txt", OUTPUT:"veryUsefulLanguageModel"}'
