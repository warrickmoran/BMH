#!/bin/bash
# AWIPS2 #5114
#
# Updates the bmh comms.xml and comms-practice.xml.
#
ext=dr5114
file=/awips2/bmh/conf/comms.xml
if [ -f $file ]; then
   echo "Updating $file"
   sed 's/<commsConfig[^>]*>/<commsConfig port="18000">/' -i $file
fi

file=/awips2/bmh/conf/comms-practice.xml
if [ -f $file ]; then
   echo "Updating $file"
   sed 's/<commsConfig[^>]*>/<commsConfig port="18500">/' -i $file
fi
