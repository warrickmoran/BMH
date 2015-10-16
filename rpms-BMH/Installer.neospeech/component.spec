#
# AWIPS II BMH spec file
#
%define _version 3.10
%define _neospeech_zip neospeech.zip

Name: awips2-neospeech
Summary: AWIPS II BMH Installation
Version: %{_version}
Release: 1
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: x86_64
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon

Provides: awips2-neospeech

%description
AWIPS II BMH - Installs the NeoSpeech TTS Server with the Paul voice.

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
mkdir -p %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi
mkdir -p %{_build_root}/etc/init.d
if [ $? -ne 0 ]; then
   exit 1
fi

neospeech_package=%{_awipscm_share}/awips2-static/neospeech/%{_version}/%{_neospeech_zip}
if [ ! -f ${neospeech_package} ]; then
   file ${neospeech_package}
   exit 1
fi

unzip ${neospeech_package} -d %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi

# deploy the NeoSpeech service scripts
cp -v %{_baseline_workspace}/rpms-BMH/Installer.neospeech/scripts/init.d/neospeech_tts %{_build_root}/etc/init.d
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
%defattr(774,root,root,775)
%dir /awips2/bmh/neospeech
/awips2/bmh/neospeech/

/etc/ttssrv.ini
/etc/voiced.conf
/etc/vtpath.ini

%defattr(774,root,root,775)
%dir /awips2/bmh/neospeech/bin
/awips2/bmh/neospeech/bin/*

%attr(744,root,root) /etc/init.d/* 
