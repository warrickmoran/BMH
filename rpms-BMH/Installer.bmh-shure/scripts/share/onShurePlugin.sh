#! /bin/sh
#
# Script to reset default audio device to the Shure headset whenever it is plugged in.
# Called by rule set in /etc/udev/rules.d
#
# Written by Jim Buchman, 2/2015

if [ "$ACTION" = "add" -a \! -z "$DEVNUM" ]
then
	logger "SHURE Headset was plugged in."

	#
	# Get the name of the user running the pulseaudio daemon.
	#
	pulseUser=`ps uax |grep pulseaudio |grep -v grep |awk '{print $1}'`
	logger "pulseaudio process running as user $pulseUser"

	if [ -z "$pulseUser" ]
	then
		logger "The pulseaudio process is not running."
		exit 1
	fi

# DIAGNOSTICS - when first invoked, pulseaudio recognizes only 2 sinks; after a few seconds, there are 3.
#	su $pulseUser -c pacmd <<EOF >>$SHURELOG 2>&1
#list-sinks
#EOF

	# Issue the command to reset the default source/sink.
	nohup su $pulseUser /usr/share/shure/setShure.sh &

fi
exit 0