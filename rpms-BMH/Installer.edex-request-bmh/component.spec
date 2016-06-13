#
# AWIPS II Edex Request BMH spec file
#
%define __prelink_undo_cmd %{nil}
# Turn off the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-java-repack-jars[[:space:]].*$!!g')

Name: awips2-edex-request-bmh
Summary: AWIPS II EDEX BMH Installation
Version: 1.2
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

BuildRequires: awips2-ant
BuildRequires: awips2-java
provides: awips2-edex-request-bmh
requires: awips2
requires: awips2-edex-base
requires: awips2-edex
requires: awips2-python
requires: awips2-java
requires: awips2-psql
requires: awips2-common-bmh

%description
AWIPS II Edex BMH - Installs AWIPS II BMH Edex Request Plugins and Configuration.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "%{_build_root}" = "" ]
then
   echo "ERROR: The RPM Build Root has not been specified."
   exit 1
fi

if [ -d %{_build_root} ]; then
   rm -rf %{_build_root}
fi

%build
# determine if the EDEX BMH plugins need to be built.
if [ -f %{_baseline_workspace}/build.edex/edex/dist/edex-request-bmh.zip ]; then
   exit 0
fi

# build the EDEX Request BMH plugins. Based on the current state of the build,
# the whole of EDEX will also need to be built.
cd %{_baseline_workspace}/build.edex
/awips2/ant/bin/ant -f build.xml -Duframe.eclipse=%{_uframe_eclipse}
if [ $? -ne 0 ]; then
   echo "FAILED TO BUILD THE BMH EDEX PLUGINS!"
   exit 1
fi

%install
mkdir -p %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi

# prepare the BMH EDEX plugins
unzip %{_baseline_workspace}/build.edex/edex/dist/edex-request-bmh.zip \
   -d %{_build_root}
if [ $? -ne 0 ]; then
   echo "FAILED TO INSTALL THE BMH EDEX REQUEST PLUGINS!"
   exit 1
fi

%pre
%post

%preun
%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2
%dir /awips2/edex
/awips2/edex/*
