COMPONENTS=\
	/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/DBNL-ARTES\
	/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/HATTEM-LM\
	/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/MNL-ARTES\
	/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/MNL-PROZA\
	/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/MNL-RIJM\
	/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/TC
VALIDATION=/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/TEST/normalizedText.txt
OUTPUT=/home/jesse/TUTORIAL-HTR/EXP-LEIDEN/TRAIN/LM/INTERPOLATION

bash LMScripts/MultipleInterpolation.sh $OUTPUT $VALIDATION $COMPONENTS
