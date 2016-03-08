#!/bin/bash

# This delta script is for BMH DR 5160. This delta script will
# remove DMO messages from suites.

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db} removing DMO messages from suites"

   ${PSQL} -U awips -d ${bmh_db} -c "delete from suite_msg where msgtype_id in (select id from msg_type where substring(afosid from 4 for 3) = 'DMO');"
   if [ $? -ne 0 ]; then
      echo "Update Failed!"
      exit 1
   fi

done

echo "Update Finished Successfully."
exit 0
