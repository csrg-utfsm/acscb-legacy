Summary:	OmniOrb for ACS
Name:		acsOmniOrb
Version:	%{acs_ver}
Release:	%{acs_rel}
License:	LGPL
Group:		ACS
Packager:	%{packager}
Source0:	%{name}-%{version}.tar.gz
URL:		http://www.eso.org/~almamgr/AlmaAcs/
BuildRequires:  acsPython
#####BuildRoot:	%{_tmppath}/%{name}-%{version}-root-%(id -u -n)
AutoReqProv:    no

%define __check_files %{nil}

%description
Test, nothing yet.

%prep
%setup -q

# Preparation of a FAKE /alma/ACS-X.Y directory as a compile farm.
TOOLS_INSTALL_DIR="$RPM_BUILD_ROOT/alma/%{group}-%{version}/Python/omni"
rm -rf $TOOLS_INSTALL_DIR
mkdir -p $TOOLS_INSTALL_DIR

%build
# Load the Environment vars.
source .bash_profile.acs

# Make the bootstrap (prepare target)
cd INSTALL/
./buildOmniORB


%clean

rm -rf $RPM_BUILD_ROOT/alma/%{group}-%{version}/Python/omni

%files 
%defattr(-,-,-)
/alma/%{group}-%{version}/Python/omni

#%define date	%(echo `LC_ALL="C" date +"%a %b %d %Y"`)
# This changelog is "sick-defined", please chage it!
#%changelog
#* %{date} ACS-UTFSM Team <acs@listas.inf.utfsm.cl>
#  First RPM Build release


