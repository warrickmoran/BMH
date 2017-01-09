#
# AWIPS II BMH spec file
#
%define _version 3.10
%define _neospeech_directory neospeech-spanish

Name: awips2-neospeech-english-and-spanish
Summary: AWIPS II BMH Installation
Version: %{_version}
Release: 3
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: x86_64
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

Provides: awips2-neospeech-english-and-spanish
Requires: awips2-neospeech
Conflicts: awips2-neospeech-english

%description
AWIPS II BMH - Enables both the English and Spanish NeoSpeech Voices.

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
neospeech_package=%{_baseline_workspace}/foss/neospeech-%{_version}/%{_neospeech_directory}
if [ ! -d ${neospeech_package} ]; then
   echo "Directory ${neospeech_package} not found!"
   exit 1
fi

cp -rv ${neospeech_package}/* %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi

mkdir -p %{_build_root}/awips2/bmh/neospeech/verify/
if [ $? -ne 0 ]; then
   exit 1
fi

_all_license=%{_baseline_workspace}/neospeech/verification_paulvioleta_07102017.txt
cp -v ${_all_license} %{_build_root}/awips2/bmh/neospeech/verify/verification.txt

%pre
%post
%preun
%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(774,root,root,775)
%dir /awips2/bmh/neospeech
/awips2/bmh/neospeech/*
