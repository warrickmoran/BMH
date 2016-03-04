#!/bin/bash

# This delta script is for BMH DR 5332. This delta script will
# drop the unused dac_ports table. This delta script must
# be ran on dx1f. This delta script can be used at any time
# due to the fact that the table it affects is no longer used.

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db}"

   ${PSQL} -U awips -d ${bmh_db} -c "DROP TABLE IF EXISTS dac_ports;"
done

echo "Update Finished Successfully."
exit 0
