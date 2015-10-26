export CHARSET=resources/CharacterSets/resolutions.chars.txt
export CLASS_CHARSET=eu.transcriptorium.lm.charsets.SyllableTokenizationSimple

export CLASSPATH=/home/jesse/workspace/LMServer/build/classes

export SRILM_HOME=/home/jesse/Tools/srilm
#export SRILM_HOME=/mnt/Projecten/transcriptorium/Tools/SRILM
export PATH=$SRILM_HOME/bin:/$SRILM_HOME/bin/i686-m64/:/usr/bin/:$PATH

export HTK=/home/jesse/Tools/htk3-4-Atros/bin.linux/
#export HTK=/usr/local/bin


LMDIR=/home/jesse/TUTORIAL-HTR/EXP-RESOLUTIONS/TRAIN/LM
bash LMScripts/MultipleInterpolation.sh\
	 $LMDIR/INTERPOLATION_SYL\
	 $LMDIR/TEST_SYL/normalizedText.txt\
	 $LMDIR/STELLINGWERF_SYL/\
	 $LMDIR/TRAINING_SYL
