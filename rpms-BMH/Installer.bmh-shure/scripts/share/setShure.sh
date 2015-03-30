#! /bin/sh
#
# Script to reset default audio device to the Shure headset, after pausing
# after it is plugged in so system can recognize it.
# Typically called by /awips2/bmh/bin/onShurePlugin.sh
#
# Written by Jim Buchman, 2/2015

# First parameter is the delay, in seconds; default is 5. It takes a few seconds
# after plugging the headset in before the pulseaudio daemon sees it.

delay=$1
if [ -z "$delay" ]
then
	delay=5
fi

logger "$0 invoked with $delay second delay."
sleep $delay

pacmd <<EOF 2>&1 | logger
list-sinks
set-default-sink alsa_output.usb-Shure_Incorporated_Shure_Digital-00-Digital.analog-stereo
set-default-source alsa_input.usb-Shure_Incorporated_Shure_Digital-00-Digital.analog-stereo
EOF

exit 0
