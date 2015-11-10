#!/bin/bash

# This delta script is for BMH DR 5092. This delta script will
# transfer the existing dac configuration and dac port information
# to the new format.

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db}"

   ${PSQL} -U awips -d ${bmh_db} -f dr5092_address_updates.sql > /dev/null
   if [ $? -ne 0 ]; then
      echo "Update Failed!"
      exit 1
   fi

   ${PSQL} -U awips -d ${bmh_db} -f dr5092_channelUpdates.sql > /dev/null
   if [ $? -ne 0 ]; then
      echo "Update Failed!"
      exit 1
   fi
done

echo "Update Finished Successfully."
exit 0
