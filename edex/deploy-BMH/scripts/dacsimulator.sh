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
# Start script for BMH DacSimulator application.
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    10/17/14        3688          dgilling       Initial Creation.
#    10/20/14        3688          dgilling       Add logging, fix dependencies.
#    01/09/15        3942          rjpeter        Set memory parameters.
#    02/25/16        5395          rjpeter        Removed incremental CMS.
##############################################################################

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

export BMH_HOME=$(dirname $dir)
awips_home=$(dirname $BMH_HOME)
export EDEX_HOME="${awips_home}/edex"

export JAVA_HOME="${awips_home}/java"

# set Java into the path
export PATH=${awips_home}/bin:${JAVA_HOME}/bin

DEPENDENCIES="ch.qos.logback org.apache.commons.cli org.slf4j org.apache.commons.collections com.google.guava"

ENTRY_POINT="com.raytheon.bmh.dacsimulator.DacSimulatorMain"
CLASSPATH="${EDEX_HOME}/lib/plugins/*"
for dependency in $DEPENDENCIES; do
  CLASSPATH="${CLASSPATH}:/awips2/edex/lib/dependencies/${dependency}/*"
done;

JVM_ARGS="-Xms16m -Xmx32m -XX:+UseConcMarkSweepGC -XX:NewSize=8m -XX:MaxNewSize=8m -XX:SurvivorRatio=6 -XX:MaxPermSize=24m -XX:ReservedCodeCacheSize=8m"
JVM_PROPS="-Duser.timezone=GMT -Dlogback.configurationFile=${BMH_HOME}/conf/logback-dacsimulator.xml"


java ${JVM_ARGS} ${JVM_PROPS} -classpath ${CLASSPATH} ${ENTRY_POINT} "$@"