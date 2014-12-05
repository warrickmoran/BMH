#!/bin/bash

##
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
##

##############################################################################
# Start script for BMH CommsManager application.
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    07/28/14        3399          bsteffen       Initial Creation.
#    08/25/14        3558          rjpeter        Added qpid flag so queues auto created,
#                                                 redirect to log file, and background script.
#    09/09/14        3456          bkowal         Use yajsw to start.
#    10/17/14        3687          bsteffen       Practice mode.
#    12/04/14        3890          bkowal         Use centralized yajsw.
##############################################################################

CONF_FILE="wrapper.conf"

for arg in $@
do
  case $arg in
    -p) 
       CONF_FILE="wrapper_practice.conf"
       ;;
  esac
done

export BMH_DATA=/awips2/bmh/data

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

export BMH_HOME=$(dirname $dir)
awips_home=$(dirname $BMH_HOME)
export EDEX_HOME="${awips_home}/edex"

export JAVA_HOME="${awips_home}/java"
export JAVA=${JAVA_HOME}/bin/java
export CONSOLE_LOGLEVEL=DEBUG
comms_pid=`pgrep -f "java.*-c ${BMH_HOME}/conf/${CONF_FILE}"`
if [ $? -eq 0 ]; then
  echo "Comms manager (pid ${comms_pid}) is already running."
  exit 1
fi



# set Java into the path
export PATH=${awips_home}/bin:${JAVA_HOME}/bin:${PATH}

$JAVA -Xmx32m -XX:MaxPermSize=12m -XX:ReservedCodeCacheSize=4m -jar \
	${YAJSW_HOME}/wrapper.jar -c ${BMH_HOME}/conf/${CONF_FILE}
