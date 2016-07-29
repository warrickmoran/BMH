#!/bin/bash

# This delta script is for BMH DR 5766. This delta script must
# be ran on dx1f. This delta script will update the msg_type,
# input_msg, and static_msg_type tables to support periodic
# message scheduling based on broadcast cycle.

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

PSQL=/awips2/psql/bin/psql
BMH_DBS=( 'bmh' 'bmh_practice' )

for bmh_db in ${BMH_DBS[*]}; do
   echo "Updating: ${bmh_db}"

   ${PSQL} -U awips -d ${bmh_db} -f ${dir}/DR5766.sql
   if [ $? -ne 0 ]; then
      echo "ERROR: The update of database: ${bmh_db} has failed."
      exit 1
   fi
done

echo "Update Finished Successfully."
exit 0
