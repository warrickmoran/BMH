#!/bin/bash
# AWIPS2 #5210
# 
# Add the comms_manager and neospeech init.d scripts to autostart.
# This script will need to be run on both px1 and px2.
#

if [ "${USER}" != "root" ]; then
   echo "Root privileges are required to run this script!"
   exit 1
fi

if [ -f /etc/init.d/comms_manager ]; then
   echo "Adding comms_manager to chkconfig ..."
   /sbin/chkconfig --add comms_manager
   if [ $? -ne 0 ]; then
      echo "Failed to add comms_manager to chkconfig!"
      exit 1
   fi
fi
if [ -f /etc/init.d/neospeech_tts ]; then
   echo "Adding neospeech_tts to chkconfig ..."
   /sbin/chkconfig --add neospeech_tts
   if [ $? -ne 0 ]; then
      echo "Failed to add neospeech_tts to chkconfig!"
      exit 1
   fi
fi

exit 0
