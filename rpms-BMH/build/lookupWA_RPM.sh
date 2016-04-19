#!/bin/bash

function lookupWA_RPM()
{
	# Arguments
	#	1) name of the rpm to lookup
	#	2) WA RPM build directory root
	
	# lookup the rpm
	if [ "${1}" = "awips2-bmh-database" ]; then
		export RPM_SPECIFICATION="${2}/Installer.bmh-database"
		return 0	
	fi
	if [ "${1}" = "awips2-edex-bmh" ]; then
                # Remove conflicting version created by the EDEX RPM build.
                rm -f ${AWIPSII_TOP_DIR}/RPMS/x86_64/awips2-edex-bmh-${AWIPSII_VERSION}-${AWIPSII_RELEASE}.x86_64.rpm
		export RPM_SPECIFICATION="${2}/Installer.edex-bmh"
		return 0
	fi
	if [ "${1}" = "awips2-bmh" ]; then
		export RPM_SPECIFICATION="${2}/Installer.bmh"
		return 0
	fi
	if [ "${1}" = "awips2-bmh-test" ]; then
		export RPM_SPECIFICATION="${2}/Installer.test"
		return 0
	fi
	if [ "${1}" = "awips2-bmh-shure" ]; then
		export RPM_SPECIFICATION="${2}/Installer.bmh-shure"
		return 0
	fi
	if [ "${1}" = "awips2-edex-request-bmh" ]; then
                # Remove conflicting version created by the EDEX RPM build.
                rm -f ${AWIPSII_TOP_DIR}/RPMS/x86_64/awips2-edex-request-bmh-${AWIPSII_VERSION}-${AWIPSII_RELEASE}.x86_64.rpm
		export RPM_SPECIFICATION="${2}/Installer.edex-request-bmh"
		return 0
	fi
	if [ "${1}" = "awips2-neospeech" ]; then
		export RPM_SPECIFICATION="${2}/Installer.neospeech"
		return 0
	fi
	if [ "${1}" = "awips2-neospeech-english" ]; then
		export RPM_SPECIFICATION="${2}/Installer.neospeech-english"
		return 0
	fi
	if [ "${1}" = "awips2-neospeech-english-and-spanish" ]; then
		export RPM_SPECIFICATION="${2}/Installer.neospeech-english-and-spanish"
		return 0
	fi
        if [ "${1}" = "awips2-common-bmh" ]; then
                # Remove conflicting version created by the EDEX RPM build.
                rm -f ${AWIPSII_TOP_DIR}/RPMS/x86_64/awips2-common-bmh-${AWIPSII_VERSION}-${AWIPSII_RELEASE}.x86_64.rpm
                export RPM_SPECIFICATION="${2}/Installer.common-bmh"
                return 0
        fi
	
	return 1
}
