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
# Start script for BMH DacTransmit application. Will not start DacTransmit if
# another is running for the same dac on the same port. To force this script to
# to kill any existing DacTransmit applications and start a new one pass in the
# -k option. All other options are parsed directly through to the DacTransmit
# java process.
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    07/15/14        3388          dgilling       Initial Creation.
#    07/17/14        3286          dgilling       Add google guava to classpath.
#    07/28/14        3399          bsteffen       Build CLASSPATH from DEPENDENCIES.
#    08/11/14        3286          dgilling       Remove unneeded dependencies.
#    08/18/14        3286          dgilling       Add org.apache.commons.lang.
#    09/01/14        3665          bsteffen       Dont allow multiple instances
#    10/21/14        3687          bsteffen       Log practice mode to a different file.
#    11/15/14        3630          bkowal         Allow for greater customization of log file name.
#    01/09/15        3942          rjpeter        Set memory parameters, added USE_POSITION_STREAM.
##############################################################################


path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

# As we process args, any that aren't the kill flag are accumulated in this
# variable and passed to the java process.
preservedArgs=()

#Disables use of position stream, set to true to enable, enabling may cause issues keeping jitter buffer loaded
USE_POSITION_STREAM=false

# This loop processes the command line args. We need to extract DAC_ADDRESS(-d)
# and DAC_PORT(-p). To make it easier to grab the argument to flags $prev will
# contain the previous argument. Also need to check for the kill flag(-k)
for arg in $@
do
  case $prev in
    -d) 
       DAC_ADDRESS=$arg
       ;;
    -p) 
       DAC_PORT=$arg
       ;;
  esac
  case $arg in
    -k) 
       KILL=true
       ;;
    *)
       preservedArgs=("${preservedArgs[@]}" "${arg}")
       ;;
  esac
  prev=$arg
done

# If these aren't set there is something very wrong. Rather than try to print
# usage here, just let all the args go to the java application and it will print
# pretty usage.
if [ -n "$DAC_ADDRESS" ] && [ -n "$DAC_PORT" ]; then
    if [ -n "$KILL" ]; then
    	pkill -f "java.*DacTransmitMain(.*-d ${DAC_ADDRESS}|.*-p ${DAC_PORT}){2}"
    else
		pid=`pgrep -f "java.*DacTransmitMain(.*-d ${DAC_ADDRESS}|.*-p ${DAC_PORT}){2}"`
		if [ $? -eq 0 ]; then
		    echo "Dac transmit is already running with pid of $pid"
		    exit 1
		fi
	fi
fi

if [[ -z "$BMH_LOG_BASE" ]]; then
  export logfile_base=dactransmit
else
export logfile_base=${BMH_LOG_BASE}
fi

export BMH_HOME=$(dirname $dir)
awips_home=$(dirname $BMH_HOME)
export EDEX_HOME="${awips_home}/edex"

export JAVA_HOME="${awips_home}/java"

# set Java into the path
export PATH=${awips_home}/bin:${JAVA_HOME}/bin

DEPENDENCIES="ch.qos.logback org.apache.commons.cli org.slf4j com.google.guava org.apache.thrift net.sf.cglib org.apache.commons.lang"

ENTRY_POINT="com.raytheon.uf.edex.bmh.dactransmit.DacTransmitMain"
CLASSPATH="${EDEX_HOME}/lib/plugins/*"
for dependency in $DEPENDENCIES; do
  CLASSPATH="${CLASSPATH}:/awips2/edex/lib/dependencies/${dependency}/*"
done;

JVM_ARGS="-Xms16m -Xmx48m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:NewSize=8m -XX:MaxNewSize=8m -XX:SurvivorRatio=6 -XX:MaxPermSize=24m -XX:ReservedCodeCacheSize=8m"
JVM_PROPS="-Dthrift.stream.maxsize=20 -Duser.timezone=GMT -Dlogback.configurationFile=${BMH_HOME}/conf/logback-dactransmit.xml -DusePositionStream=${USE_POSITION_STREAM}"


java ${JVM_ARGS} ${JVM_PROPS} -classpath ${CLASSPATH} ${ENTRY_POINT} "${preservedArgs[@]}"