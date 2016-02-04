#!/bin/bash
# AWIPS2 #5308
# 
# This script will alter all currently stored playlist and message
# files to force EDEX to regenerate newer versions of the files
# in the new format. This script will need to be run on the
# BMH EDEX server (px1 or px2).
#

if [ "${USER}" != "root" -a "${USER}" != "awips" ]; then
   echo "Only awips or a user with root privileges can run this script!"
   exit 1
fi

_bmh_data=/awips2/bmh/data
if [ ! -d ${_bmh_data} ]; then
   echo "WARN: No bmh data directory exists."
   exit 1
fi

if [ -d ${_bmh_data}/playlist ]; then
   echo "Cleaning ${_bmh_data}/playlist ..."
   rm -rf ${_bmh_data}/playlist
   if [ $? -ne 0 ]; then
      echo "ERROR: failed to remove the ${_bmh_data}/playlist directory."
      exit 1
   fi
fi
if [ -d ${_bmh_data}/practice ]; then
   echo "Cleaning ${_bmh_data}/practice ..."
   rm -rf ${_bmh_data}/practice
   if [ $? -ne 0 ]; then
      echo "ERROR: failed to remove the ${_bmh_data}/practice directory."
      exit 1
   fi
fi

echo "INFO: Update Successful"
exit 0
