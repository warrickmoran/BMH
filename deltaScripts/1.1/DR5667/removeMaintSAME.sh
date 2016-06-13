#!/bin/bash

# This delta script is for BMH DR 5667. This delta script must be 
# ran on px1 or px2. This delta script will remove the
# maintenance SAME tone audio file if it exists. The
# removal of the file will ensure that a new file will be generated
# during EDEX startup.

MAINT_AUDIO="maintenanceSame.ulaw"
MAINT_AUDIO_LOC=( '/awips2/bmh/data/audio/maintenance' '/awips2/bmh/data/practice/audio/maintenance' )

for maint_loc in ${MAINT_AUDIO_LOC[*]}; do
   maint_path=${maint_loc}/${MAINT_AUDIO}

   echo "Removing SAME maintenance file: ${maint_path} ..."
   rm -f ${maint_path}
   if [ $? -ne 0 ]; then
      echo "Failed to remove SAME maintenance file: ${maint_path}."
      echo "Update Failed!"
      exit 1
   fi
done

echo "Update Finished Successfully."
exit 0
