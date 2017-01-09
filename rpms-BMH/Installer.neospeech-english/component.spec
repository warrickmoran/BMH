#
# AWIPS II BMH spec file
#
%define _version 3.10
%define _neospeech_zip neospeech.zip

Name: awips2-neospeech-english
Summary: AWIPS II BMH Installation
Version: %{_version}
Release: 2
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: noarch
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

Provides: awips2-neospeech-english
Requires: awips2-neospeech
Conflicts: awips2-neospeech-english-and-spanish

%description
AWIPS II BMH - Installs the NeoSpeech English Voice License.

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
mkdir -p %{_build_root}/awips2/bmh/neospeech/verify/
if [ $? -ne 0 ]; then
   exit 1
fi

_english_license=%{_baseline_workspace}/neospeech/verification_paul_07102017.txt
cp -v ${_english_license} %{_build_root}/awips2/bmh/neospeech/verify/verification.txt

%pre
%post
%preun
%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/bmh
%defattr(774,root,root,775)
%dir /awips2/bmh/neospeech
%dir /awips2/bmh/neospeech/verify
/awips2/bmh/neospeech/verify/verification.txt
