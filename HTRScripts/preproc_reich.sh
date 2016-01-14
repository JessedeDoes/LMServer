############################
# Create a work directory
############################
mkdir -p EXP-REICH/{PROC,TEST,TRAIN,CATTI}



############################################
# Processing and Feature Extraction steps
############################################
#Meermanno=/media/jesse/Data/tranScriptorium/Meermanno/Meermanno_GT_selection/
cd EXP-REICH/PROC

# Useful links
#ln -s $Meermanno Images
#ln -s $Meermanno/page PAGE
ln -s ../../conf/Reichsgericht.conf

# Filter-out paths and extensions from the list of page IDs 
sed "s/^.*\///; s/\.jpg$//" ../../corpora/Reichsgericht/partitions/train.lst > train.lst

# Set PATH to include the "bin" and "scripts" dirs
export PATH=$(realpath ../../bin):$(realpath ../../scripts):$PATH

# Launch processing and feature extraction steps parallelized among 4 cores
#ProcAndFeatExtr.sh -n 4 Reichsgericht Images/ PAGE/ Train-List

ProcAndFeatExtr.sh Reichsgericht.conf

# As output, at the end of the execution of "ProcAndFeatExtr.sh", two symbolic-links
# are created:
#
#     Features: pointing to the directory where are stores all feature ectraction files
#               corresponding to the extracted text lines stored in Auxproc/Lines dir.
#   
# Proj-Mat.mat: projection matrix employed in the PCA for dimensionality reduction.

#We remove the lines that do not have transcrition and that have eluded words ([...])
#BlackList.sh 

