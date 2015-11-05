#!/bin/bash

# This delta script is for BMH DR 5068. This delta script will
# transfer the existing audio targets from db to amplitude.

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db} applying db to amplitude conversion"

   ${PSQL} -U awips -d ${bmh_db} -f DR5068_amplitudeUpdates.sql > /dev/null
   if [ $? -ne 0 ]; then
      echo "Update Failed!"
      exit 1
   fi

done

echo "Update Finished Successfully."
exit 0
