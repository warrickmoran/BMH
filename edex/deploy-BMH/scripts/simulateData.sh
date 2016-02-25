#!/bin/sh

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
# Start script for BMH Dataflow simulator
#
#    SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    08/15/14        3515          rjpeter        Initial Creation.
#    02/25/16        5395          rjpeter        Removed incremental CMS.
##############################################################################


# set true to copy in all files that would currently be effective to allow for initial start up of a clean system
INITIAL_LOAD_ALL_EFFECTIVE=true

INPUT_DIR=/awips2/bmh/data/nwr/simulate
OUTPUT_DIR=/awips2/bmh/data/nwr/ready

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

export BMH_HOME=$(dirname $dir)
awips_home=$(dirname $BMH_HOME)
export EDEX_HOME="${awips_home}/edex"

export JAVA_HOME="${awips_home}/java"

# set Java into the path
export PATH=${awips_home}/bin:${JAVA_HOME}/bin:${PATH}

ENTRY_POINT="com.raytheon.uf.edex.bmh.test.data.DataSimulator"
CLASSPATH="${EDEX_HOME}/lib/plugins/com.raytheon.uf.edex.bmh.test.jar"

JVM_ARGS="-Xms4m -Xmx32m -XX:+UseConcMarkSweepGC"
JVM_PROPS="-DdoInitialLoad=$INITIAL_LOAD_ALL_EFFECTIVE -DinputDir=$INPUT_DIR -DoutputDir=$OUTPUT_DIR -Duser.timezone=GMT"

java ${JVM_ARGS} ${JVM_PROPS} -classpath ${CLASSPATH} ${ENTRY_POINT} "$@"
