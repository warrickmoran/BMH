#! /bin/sh
#
# Script to reset default audio device to the Shure headset, after pausing
# after it is plugged in so system can recognize it.
# Typically called by /awips2/bmh/bin/onShurePlugin.sh
#
# Written by Jim Buchman, 2/2015
# Updated by bkowal, 10/27/2015 - improved setup validation, implemented client notification

# First parameter is the delay, in seconds; default is 5. It takes a few seconds
# after plugging the headset in before the pulseaudio daemon sees it.

# Used to send a notification to AlertViz provided that a working and fully configured
# fxaAnnounce is present.
function alertVizNotify() {
    local FXA_ANNOUNCE=/awips2/fxa/bin/fxaAnnounce
    
    if [ ! -f ${FXA_ANNOUNCE} ]; then
        return 1
    fi
    
    msg=${1}
    importance=${2}
    /bin/bash ${FXA_ANNOUNCE} "${msg}" LOCAL ${importance} > /dev/null 2>&1 &
    return $?
}

_max_attempts=3
_expected_source="alsa_input.usb-Shure_Incorporated_Shure_Digital-00-Digital.analog-stereo"
_expected_sink="alsa_output.usb-Shure_Incorporated_Shure_Digital-00-Digital.analog-stereo"

delay=$1
if [ -z "$delay" ]
then
	delay=5
fi

logger "$0 invoked with $delay second delay."
_attempt_count=0
_settings_updated=0

while [ ${_settings_updated} -eq 0 ]; do
    if [ ${_attempt_count} -eq ${_max_attempts} ]; then
         host=`hostname`
         logger "Failed to set the shure headset as the default audio device on: ${host}!"
         alertVizNotify "Failed to set the shure headset as the default audio device on: ${host}! Rebooting the workstation and/or using a different USB port is recommended." URGENT
         if [ $? -ne 0 ]; then
              logger "Failed to send AlertViz notification!"
         fi
         exit
    fi 
    let _attempt_count+=1
    sleep $delay
    
    # First, verify that pacmd is usable on this workstation.
    pacmd help > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        logger "Error: pacmd is not usable on this workstation!"
        continue
    fi
    # Next, verify that the expected source and sink exist.
    pacmd list-sinks | grep ${_expected_sink} > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        logger "Failed to find expected Shure sink: ${_expected_sink}!"
        continue     
    fi
    logger "Found expected Shure sink: ${_expected_sink}." 
    pacmd list-sources | grep ${_expected_source} > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        logger "Failed to find expected Shure source: ${_expected_source}!"
        continue
    fi
    logger "Found expected Shure source: ${_expected_sink}."
    
    # Attempt to set the source and sink.
    pacmd set-default-sink ${_expected_sink} > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        logger "Failed to set the default sink to: ${_expected_sink}!"
        continue
    fi
    pacmd set-default-source ${_expected_source} > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        logger "Failed to set the default source to: ${_expected_source}!"
        continue
    fi

    _settings_updated=1
    logger "Successfully set the shure headset as the default audio device."
    alertVizNotify "Successfully set the shure headset as the default audio device." ROUTINE
    if [ $? -ne 0 ]; then
        logger "Failed to send AlertViz notification!"
    fi
done

exit 0
