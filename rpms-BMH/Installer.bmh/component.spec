#
# AWIPS II BMH spec file
#
Name: awips2-bmh
Summary: AWIPS II BMH Installation
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: noarch
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: Bryan Kowal

BuildRequires: awips2-ant
provides: awips2-bmh
requires: awips2
requires: awips2-edex-bmh

%description
AWIPS II BMH - Installs AWIPS II BMH scripts and configuration.

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

%install
mkdir -p %{_build_root}/awips2/bmh
if [ $? -ne 0 ]; then
   exit 1
fi
mkdir -p %{_build_root}/etc/init.d
if [ $? -ne 0 ]; then
   exit 1
fi

# deploy the BMH scripts and configuration
deploy_xml=%{_baseline_workspace}/deploy-BMH/wa-deploy.xml

/awips2/ant/bin/ant -f ${deploy_xml} -Dbmh.root.directory=%{_build_root}/awips2/bmh
if [ $? -ne 0 ]; then
   echo "FAILED TO DEPLOY THE BMH SCRIPTS!"
   exit 1
fi

# deploy the BMH service scripts
cp -v %{_baseline_workspace}/deploy-BMH/service/comms_manager %{_build_root}/etc/init.d
if [ $? -ne 0 ]; then
   echo "FAILED TO DEPLOY THE BMH SERVICE SCRIPTS!"
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
%dir /awips2/bmh
/awips2/bmh/*
%dir /awips2/bmh/conf
/awips2/bmh/conf/*
%config(noreplace) /awips2/bmh/conf/comms.xml
%config(noreplace) /awips2/bmh/conf/notification.properties
%config(noreplace) /awips2/bmh/conf/unacceptableWords.eng.txt
%config(noreplace) /awips2/bmh/conf/unacceptableWords.spa.txt
%dir /awips2/bmh/logs

%defattr(755,awips,fxalpha,755)
%dir /awips2/bmh/bin
/awips2/bmh/bin/*

%attr(744,root,root) /etc/init.d/* 
