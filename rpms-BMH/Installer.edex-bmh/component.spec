#
# AWIPS II Edex BMH spec file
#
%define __prelink_undo_cmd %{nil}
# Turn off the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-java-repack-jars[[:space:]].*$!!g')

Name: awips2-edex-bmh
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
provides: awips2-edex-bmh
requires: awips2
requires: awips2-edex-base
requires: awips2-edex
requires: awips2-python
requires: awips2-java
requires: awips2-psql
requires: awips2-common-bmh

%description
AWIPS II Edex BMH - Installs AWIPS II BMH Edex Plugins and Configuration.

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
if [ -f %{_baseline_workspace}/build.edex/edex/dist/edex-bmh.zip ]; then
   exit 0
fi

# build the EDEX BMH plugins. Based on the current state of the build,
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
unzip %{_baseline_workspace}/build.edex/edex/dist/edex-bmh.zip \
   -d %{_build_root}
if [ $? -ne 0 ]; then
   echo "FAILED TO INSTALL THE BMH EDEX PLUGINS!"
   exit 1
fi

# prepare the BMH configuration
cp -rv %{_baseline_workspace}/deploy.edex-BMH/esb/* %{_build_root}/awips2/edex
if [ $? -ne 0 ]; then
   echo "FAILED TO COPY THE BMH EDEX CONFIGURATION!"
   exit 1
fi

# prepare the BMH EDEX services list
mkdir -p %{_build_root}/etc/init.d
if [ $? -ne 0 ]; then
   echo "FAILED TO COPY THE BMH SERVICE LIST!"
   exit 1
fi
cp -v %{_baseline_workspace}/rpms-BMH/Installer.edex-bmh/scripts/init.d/* \
   %{_build_root}/etc/init.d/edexServiceList-bmh
if [ $? -ne 0 ]; then
   echo "FAILED TO COPY THE BMH SERVICE LIST!"
   exit 1
fi

%pre
%post
# replace the existing service list with the BMH service list
if [ -f /etc/init.d/edexServiceList ]; then
   mv -f /etc/init.d/edexServiceList /etc/init.d/edexServiceList.orig
   if [ $? -ne 0 ]; then
      exit 1
   fi
fi
mv -f /etc/init.d/edexServiceList-bmh /etc/init.d/edexServiceList
if [ $? -ne 0 ]; then
   exit 1
fi

%preun
if [ "${1}" = "1" ]; then
   exit 0
fi

# replace the bmh service list with the original service list
if [ -f /etc/init.d/edexServiceList.orig ]; then
   mv -f /etc/init.d/edexServiceList.orig /etc/init.d/edexServiceList
   if [ $? -ne 0 ]; then
      exit 1
   fi
fi

%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2
%dir /awips2/edex
/awips2/edex/*
%config(noreplace) /awips2/edex/etc/bmh.sh

%attr(744,root,root) /etc/init.d/*
