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
##############################################################################

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

export BMH_HOME=$(dirname $dir)
awips_home=$(dirname $BMH_HOME)
export EDEX_HOME="${awips_home}/edex"

export JAVA_HOME="${awips_home}/java"

# set Java into the path
export PATH=${awips_home}/bin:${JAVA_HOME}/bin:${PATH}

DEPENDENCIES="ch.qos.logback org.slf4j org.geotools javax.measure org.apache.thrift net.sf.cglib org.apache.qpid javax.jms"

ENTRY_POINT="com.raytheon.uf.edex.bmh.comms.CommsManager"
CLASSPATH="${EDEX_HOME}/lib/plugins/*"
for dependency in $DEPENDENCIES; do
  CLASSPATH="${CLASSPATH}:/awips2/edex/lib/dependencies/${dependency}/*"
done;

JVM_ARGS="-Xms128m -Xmx256m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
JVM_PROPS="-Duser.timezone=GMT"

java ${JVM_ARGS} ${JVM_PROPS} -classpath ${CLASSPATH} ${ENTRY_POINT} "$@"

