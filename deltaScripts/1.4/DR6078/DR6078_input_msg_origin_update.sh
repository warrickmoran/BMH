#!/bin/bash

# This delta script is for BMH DR 6078. This delta script will
# add a new origin column to the input_msg table.

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db}"

   ${PSQL} -U awipsadmin -d ${bmh_db} -f dr6078_input_msg_origin_update.sql > /dev/null
   if [ $? -ne 0 ]; then
      echo "Update Failed!"
      exit 1
   fi
done

echo "Update Finished Successfully."
exit 0
