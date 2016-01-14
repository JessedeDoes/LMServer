COMPONENTS="/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/GUTENBERG-SAMPLE\
	/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/OCR-BOOK\
	/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/TRAINING-SET"

VALIDATION=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/TEST-SET/normalizedText.txt
OUTPUT=/home/jesse/TUTORIAL-HTR/EXP-REICH/TRAIN/LM/INTERPOLATION

### path and classpath settings

export CLASS_CHARSET=eu.transcriptorium.lm.charsets.SimpleTokenization
export SPECIAL_LABELS=/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.reichsgericht.txt
export CHARSET=/home/jesse/workspace/LMServer/resources/CharacterSets/reichsgericht.chars

export CLASSPATH=/home/jesse/workspace/LMServer/build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin

export CUTOFF=0
bash LMScripts/MultipleInterpolation.sh $OUTPUT $VALIDATION $COMPONENTS
