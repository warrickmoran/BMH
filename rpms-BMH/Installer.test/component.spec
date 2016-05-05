#
# AWIPS II BMH Test spec file
#
Name: awips2-bmh-test
Summary: AWIPS II EDEX BMH Test Installation
Version: 1.2
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: noarch
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

provides: awips2-bmh-test
requires: awips2
requires: awips2-bmh
requires: awips2-edex-bmh

%description
AWIPS II BMH - Installs the AWIPS II BMH Test Suite.

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
mkdir -p %{_build_root}/awips2/bmh/testSuite
if [ $? -ne 0 ]; then
   exit 1
fi

# deploy the BMH test suite.
test_suite=%{_baseline_workspace}/bmh.testSuite

cp -rv ${test_suite}/* %{_build_root}/awips2/bmh/testSuite
if [ $? -ne 0 ]; then
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
%dir /awips2/bmh/testSuite
%dir /awips2/bmh/testSuite/src
/awips2/bmh/testSuite/src/*
%dir /awips2/bmh/testSuite/config
/awips2/bmh/testSuite/config/*

%defattr(664,awips,fxalpha,775)
%dir /awips2/bmh/testSuite/data
/awips2/bmh/testSuite/data/*

%defattr(755,awips,fxalpha,755)
/awips2/bmh/testSuite/scenariosLauncher.sh
