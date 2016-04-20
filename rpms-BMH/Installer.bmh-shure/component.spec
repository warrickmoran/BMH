#
# AWIPS II BMH Shure spec file
#
Name: awips2-bmh-shure
Summary: AWIPS II BMH Shure Utility Installation
Version: 1.1
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: noarch
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

provides: awips2-bmh-shure

%description
AWIPS II BMH Shure - Installs system rules and utility scripts to autoset the shure adapter
as the headset and mic source whenever it is plugged into the PC this package has been installed on.

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
# Create the destination directories.
mkdir -p %{_build_root}/etc/udev/rules.d
if [ $? -ne 0 ]; then
   exit 1
fi

mkdir -p %{_build_root}/usr/share/shure
if [ $? -ne 0 ]; then
   exit 1
fi

_package_scripts_dir=%{_baseline_workspace}/rpms-BMH/Installer.bmh-shure/scripts
# Copy the rules to the destination.
cp -v ${_package_scripts_dir}/rules.d/99-shure.rules %{_build_root}/etc/udev/rules.d/99-shure.rules
if [ $? -ne 0 ]; then
   exit 1
fi
# Copy the utility scripts to the destination.
cp -v ${_package_scripts_dir}/share/onShurePlugin.sh %{_build_root}/usr/share/shure/onShurePlugin.sh
if [ $? -ne 0 ]; then
   exit 1
fi 
cp -v ${_package_scripts_dir}/share/setShure.sh %{_build_root}/usr/share/shure/setShure.sh
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
/etc/udev/rules.d/99-shure.rules

%defattr(755,awips,fxalpha,755)
%dir /usr/share/shure
/usr/share/shure/onShurePlugin.sh
/usr/share/shure/setShure.sh
