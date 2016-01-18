#!/bin/bash

# This delta script is for BMH DR 5241. This delta script will add
# additional dictionary words to the BMH national ENGLISH dictionary.
# This delta script should be executed on the database server: dx1f.

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db}"

   ${PSQL} -U awips -d ${bmh_db} -f dr5421_natl_dictionary_updates.sql
   if [ $? -ne 0 ]; then
      echo "Update Failed!"
      exit 1
   fi
done

echo "Update Finished Successfully."
exit 0
