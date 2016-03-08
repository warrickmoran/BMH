#!/bin/bash

# This delta script is for BMH DR 5241. This delta script will
# trigger the regeneration of certain messages that include the
# word(s) that was/were added to the national dictionary. This
# script should be executed on a BMH EDEX server: px1 or px2.
# Root privileges will be required to run this script.

MAX_EDEX_ATTEMPTS=180
SLEEP_DELAY_CHECK=5
EDEXUSER=awips

getEdexBMHPid() {
   _camel_pid=`pgrep -f  -u $EDEXUSER "java -Dedex.run.mode=bmh "`
   _camel_pid_found=$?
}

getEdexBMHUptime() {
   _pid=${1}

   _camel_runtime=`ps -p ${_pid} -oetime= | tr '-' ':' | awk -F: '{ total=0; m=1; } { for (i=0; i < NF; i++) {total += $(NF-i)*m; m *= i >= 2 ? 24 : 60 }} {print total}'`
}

if [ "${USER}" != "root" ]; then
   echo "Root privileges are required to run this script!"
   exit 1
fi

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

# the qpid server is required, determine where qpid is running by
# checking the EDEX setup.env script.
source /awips2/edex/bin/setup.env
if [ $? -ne 0 ]; then
   echo "Update Failed: failed to source EDEX setup.env."
   exit 1
fi

# ensure that the AWIPS II Python is the default
source /etc/profile.d/awips2Python.sh
if [ $? -ne 0 ]; then
   echo "Update Failed: failed to source the AWIPS II Python environment script."
   exit 1
fi

# this update will utilize dynamicserialize. so, it will be necessary
# to link the object that this script will serialize to the dynamicserialize
# python site-package.
link_loc=/awips2/python/lib/python2.7/site-packages/dynamicserialize/dstypes/com/raytheon/uf/common
pushd . > /dev/null 2>&1
cd ${link_loc}
if [ $? -ne 0 ]; then
   echo "Update Failed: Unable to access ${link_loc}."
   popd > /dev/null 2>&1
   exit 1
fi
ln -sf ${dir}/bmh bmh
if [ $? -ne 0 ]; then
   echo "Update Failed: Unable to create the link."
   popd > /dev/null 2>&1
   exit 1
fi
popd > /dev/null 2>&1

# need to verify that EDEX BMH is running.
_current_attempt=0
_ready=1
while [ ${_ready} -eq 1 ]; do
   let _current_attempt+=1
   # always verify that EDEX is still running every attempt because EDEX may
   # start successfully, run for a while, and then shutdown due to a configuration issue.
   getEdexBMHPid
   if [ ${_camel_pid_found} -eq 0 ]; then
      echo "Verified that EDEX BMH is running (pid ${_camel_pid})."
   else
      # let the operator know that the script is still running/waiting
      if [ ${_current_attempt} -gt ${MAX_EDEX_ATTEMPTS} ]; then
         echo "Update Failed: Failed to verify that EDEX BMH has started."
         exit 1
      fi
      log_timestamp=`date`
      echo "${log_timestamp}: Waiting for EDEX BMH to start (Attempt ${_current_attempt} of ${MAX_EDEX_ATTEMPTS}) ... "
      sleep ${SLEEP_DELAY_CHECK}
      continue
   fi

   # verify that EDEX BMH has been running for at least a minute.
   getEdexBMHUptime ${_camel_pid}
   log_timestamp=`date`
   if [ "${_camel_runtime}" = "" ]; then
      if [ ${_current_attempt} -gt ${MAX_EDEX_ATTEMPTS} ]; then
         echo "Update Failed: Failed to verify that EDEX BMH has started."
         exit 1
      fi
      echo "${log_timestamp}: Unable to determine runtime of EDEX BMH. Process may no longer be running? (Attempt ${_current_attempt} of ${MAX_EDEX_ATTEMPTS}) ..."
      sleep ${SLEEP_DELAY_CHECK}
      continue
   elif [ ${_camel_runtime} -lt 60 ]; then
      if [ ${_current_attempt} -gt ${MAX_EDEX_ATTEMPTS} ]; then
         echo "Update Failed: Script Timeout - maximum attempts reached before EDEX reached 1 minute of runtime. Immediately rerun the script."
         exit 1
      fi
      echo "${log_timestamp}: EDEX BMH has only been running for ${_camel_runtime} second(s). Waiting for 1 minute (Attempt ${_current_attempt} of ${MAX_EDEX_ATTEMPTS}) ..."
      sleep ${SLEEP_DELAY_CHECK}
      continue
   else
      echo "Verified that EDEX BMH has been running for at least 1 minute."
      _ready=0
   fi
done

# execute the update
/awips2/python/bin/python ${dir}/trigger_msg_regen.py ${BROKER_ADDR}
RC=$?

# cleanup the dynamicserialize link
rm -f ${link_loc}/bmh
if [ $? -ne 0 ]; then
   echo "Failed to remove the bmh python link. Manually remove: ${link_loc}/bmh"
fi

if [ ${RC} -ne 0 ]; then
   echo "Update Failed: trigger_msg_regen.py did not finish successfully."
   exit 1
fi

echo "Update Finished Successfully."
exit 0
