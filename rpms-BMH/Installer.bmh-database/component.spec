%define _component_name           awips2-bmh-database
%define _component_project_dir    Installer.bmh-database
#
# AWIPS II BMH Database Spec File
#
Name: %{_component_name}
Summary: AWIPS II BMH Database
Version: 1.2
Release: %{_component_release}
Group: AWIPSII
BuildRoot: /tmp
BuildArch: noarch
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

AutoReq: no
provides: awips2-bmh-database
requires: awips2-database

%description
AWIPS II BMH Database - includes the
bmh database (when awips2-postgresql is installed).

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "${RPM_BUILD_ROOT}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

%build

%install
STAGING_DESTINATION=${RPM_BUILD_ROOT}/awips2/database/sqlScripts/share/sql/bmh
mkdir -p ${STAGING_DESTINATION}
if [ $? -ne 0 ]; then
   exit 1
fi

PATH_TO_DDL="deploy.edex-BMH/opt/db/ddl/bmh"
cp -r %{_baseline_workspace}/${PATH_TO_DDL}/* ${STAGING_DESTINATION}
if [ $? -ne 0 ]; then
   exit 1
fi

SQL_LOG="${RPM_BUILD_ROOT}/awips2/database/sqlScripts/share/sql/bmh/bmh.log"
# So that it will automatically be removed when the rpm is removed.
touch ${SQL_LOG}

%pre
# Verify that one of the official AWIPS II PostgreSQL configuration files exist.
if [ ! -f /awips2/data/postgresql.conf ]; then
   echo "ERROR: /awips2/data/postgresql.conf does not exist. However, "
   echo "       the AWIPS II PostgreSQL Configuration RPM is installed. "
   echo "       If you recently uninstalled awips2-database and purged "
   echo "       the /awips2/data directory, you will need to re-install "
   echo "       the AWIPS II PostgreSQL configuration rpm so that the "
   echo "       postgresql.conf file will be restored."
   exit 1
fi

%post
if [ "${1}" = "2" ]; then
   # Take no action on upgrades
   exit 0
fi

POSTGRESQL_INSTALL="/awips2/postgresql"
PSQL_INSTALL="/awips2/psql"

POSTMASTER="${POSTGRESQL_INSTALL}/bin/postmaster"
PG_CTL="${POSTGRESQL_INSTALL}/bin/pg_ctl"
DROPDB="${POSTGRESQL_INSTALL}/bin/dropdb"
PSQL="${PSQL_INSTALL}/bin/psql"

# Determine who owns the PostgreSQL Installation
DB_OWNER=`ls -l /awips2/ | grep -w 'data' | awk '{print $3}'`
# Our log file
SQL_LOG="/awips2/database/sqlScripts/share/sql/bmh/bmh.log"

# Determine if PostgreSQL is running.
I_STARTED_POSTGRESQL="NO"
su ${DB_OWNER} -c \
   "${PG_CTL} status -D /awips2/data > /dev/null 2>&1"
if [ $? -ne 0 ]; then
   su ${DB_OWNER} -c \
      "${POSTMASTER} -D /awips2/data > /dev/null 2>&1 &"
   if [ $? -ne 0 ]; then
      echo "Failed to start the PostgreSQL Server."
      exit 1
   fi
   # Give PostgreSQL Time To Start.
   sleep 10
   I_STARTED_POSTGRESQL="YES"
else
   # Show The User.
   su ${DB_OWNER} -c \
      "${PG_CTL} status -D /awips2/data"
fi

BMH_DB_EXISTS="false"
BMH_DB=`${PSQL} -U awipsadmin -l | grep bmh | awk '{print $1}'`
if [ "${BMH_DB}" = "bmh" ]; then
   BMH_DB_EXISTS="true"
   # There is already a BMH database. 
fi

BMH_PRAC_DB_EXISTS="false"
BMH_PRAC_DB=`${PSQL} -U awipsadmin -l | grep bmh_practice | awk '{print $1}'`
if [ "${BMH_PRAC_DB}" = "bmh_practice" ]; then
   BMH_PRAC_DB_EXISTS="true"
   # There is already a BMH Practice database. 
fi

if [ "${BMH_DB_EXISTS}" = "false" ]; then
   # Create the bmh directory; remove any existing directories.
   if [ -d /awips2/data/bmh ]; then
      su ${DB_OWNER} -c "rm -rf /awips2/data/bmh"
   fi
   su ${DB_OWNER} -c "mkdir -p /awips2/data/bmh"

   hba_conf_=/awips2/data/pg_hba.conf

   # Update pg_hba.conf with the default information for the bmh database.
   cat >> "${hba_conf}" <<EOF

# TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD
# ===== BMH Configuration (Operational) =====
local   bmh         all                               trust

# hostnossl    bmh         all         127.0.0.1/32          md5
# hostnossl    bmh         all         147.18.136.0/24       md5
# hostnossl    bmh         all         147.18.139.0/24       md5
# hostnossl    bmh         all         162.0.0.0/8           md5
hostssl      bmh         all         147.18.136.0/24       cert clientcert=1
hostssl      bmh         all         147.18.139.0/24       cert clientcert=1
hostssl      bmh         all         162.0.0.0/8           cert clientcert=1
EOF
   if [ $? -ne 0 ]; then exit 1; fi

   # trigger a reload of the configuration in PostgreSQL
   su ${DB_OWNER} -c \
      "${POSTGRESQL_INSTALL}/bin/pg_ctl reload -D /awips2/data > /dev/null 2>&1"
   if [ $? -ne 0 ]; then
      exit 1
   fi

   # Give PostgreSQL time to reload
   sleep 5

   # Run the bmh SQL creation scripts
   su ${DB_OWNER} -c \
      "${PSQL} -U awipsadmin -d postgres -f /awips2/database/sqlScripts/share/sql/bmh/createBMHDB.sql" >> ${SQL_LOG} 2>&1
   if [ $? -ne 0 ]; then
      exit 1
   fi
fi

if [ "${BMH_PRAC_DB_EXISTS}" = "false" ]; then
   # Create the bmh_practice directory; remove any existing directories.
   if [ -d /awips2/data/bmh_practice ]; then
      su ${DB_OWNER} -c "rm -rf /awips2/data/bmh_practice"
   fi
   su ${DB_OWNER} -c "mkdir -p /awips2/data/bmh_practice"

   hba_conf_=/awips2/data/pg_hba.conf

   cat >> "${hba_conf}" <<EOF

# TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD
# ===== BMH Configuration (Practice) =====

local   bmh         all                               trust

# hostnossl    bmh_practice all         127.0.0.1/32          md5
# hostnossl    bmh_practice all         147.18.136.0/24       md5
# hostnossl    bmh_practice all         147.18.139.0/24       md5
# hostnossl    bmh_practice all         162.0.0.0/8           md5
hostssl      bmh_practice all         147.18.136.0/24       cert clientcert=1
hostssl      bmh_practice all         147.18.139.0/24       cert clientcert=1
hostssl      bmh_practice all         162.0.0.0/8           cert clientcert=1
EOF
   if [ $? -ne 0 ]; then exit 1; fi

   # trigger a reload of the configuration in PostgreSQL
   su ${DB_OWNER} -c \
      "${POSTGRESQL_INSTALL}/bin/pg_ctl reload -D /awips2/data > /dev/null 2>&1"
   if [ $? -ne 0 ]; then
      exit 1
   fi

   # Give PostgreSQL time to reload
   sleep 5

   su ${DB_OWNER} -c \
      "${PSQL} -U awipsadmin -d postgres -f /awips2/database/sqlScripts/share/sql/bmh/createBMHPracticeDB.sql" >> ${SQL_LOG} 2>&1
   if [ $? -ne 0 ]; then
      exit 1
   fi
fi

# Stop PostgreSQL if we started it.
if [ "${I_STARTED_POSTGRESQL}" = "YES" ]; then
   su ${DB_OWNER} -c \
      "${PG_CTL} stop -D /awips2/data"
   if [ $? -ne 0 ]; then
      echo "Warning: Failed to shutdown PostgreSQL."
      exit 0
   fi
   sleep 10
fi

%preun
if [ "${1}" = "1" ]; then
   # Do nothing on upgrades.
   exit 0
fi

# Remove the BMH Database and Tablespace.
POSTGRESQL_INSTALL="/awips2/postgresql"
PSQL_INSTALL="/awips2/psql"

POSTMASTER="${POSTGRESQL_INSTALL}/bin/postmaster"
PG_CTL="${POSTGRESQL_INSTALL}/bin/pg_ctl"
DROPDB="${POSTGRESQL_INSTALL}/bin/dropdb"
PSQL="${PSQL_INSTALL}/bin/psql"

if [ ! -f ${POSTMASTER} ]; then
   exit 0
fi
if [ ! -f ${PG_CTL} ]; then
   exit 0
fi
if [ ! -f ${DROPDB} ]; then
   exit 0
fi
if [ ! -f ${PSQL} ]; then
   exit 0
fi

# Determine who owns the PostgreSQL Installation
DB_OWNER=`ls -l /awips2/ | grep -w 'data' | awk '{print $3}'`
# Our log file
SQL_LOG="/awips2/database/sqlScripts/share/sql/bmh/bmh.log"

# start PostgreSQL if it is not running
I_STARTED_POSTGRESQL="NO"
su ${DB_OWNER} -c \
   "${PG_CTL} status -D /awips2/data > /dev/null 2>&1"

# Start PostgreSQL if it is not running.
if [ $? -ne 0 ]; then
   su ${DB_OWNER} -c \
      "${POSTMASTER} -D /awips2/data > /dev/null 2>&1 &"
   if [ $? -ne 0 ]; then
      echo "Failed to Start the PostgreSQL Server."
      exit 1
   fi
   # Give PostgreSQL time to start.
   sleep 10
   I_STARTED_POSTGRESQL="YES"
else
   # Show The User.
   su ${DB_OWNER} -c \
      "${PG_CTL} status -D /awips2/data"
fi

# Is there a bmh database?
BMH_DB=`${PSQL} -U awipsadmin -l | grep bmh | awk '{print $1}'`

if [ "${BMH_DB}" = "bmh" ]; then
   # drop the bmh database
   su ${DB_OWNER} -c \
      "${DROPDB} -U awipsadmin bmh" >> ${SQL_LOG}
fi

# Is there a bmh tablespace?
# ask psql where the bmh tablespace is ...
BMH_DIR=`${PSQL} -U awipsadmin -d postgres -c "\db" | grep bmh | awk '{print $5}'`

if [ ! "${BMH_DIR}" = "" ]; then
   # drop the bmh tablespace
   su ${DB_OWNER} -c \
      "${PSQL} -U awipsadmin -d postgres -c \"DROP TABLESPACE bmh\"" >> ${SQL_LOG}
fi

# stop PostgreSQL if we started it
if [ "${I_STARTED_POSTGRESQL}" = "YES" ]; then
   su ${DB_OWNER} -c \
      "${PG_CTL} stop -D /awips2/data"
   sleep 2
fi

%postun
if [ "${1}" = "1" ]; then
   exit 0
fi

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(666,awips,fxalpha,775)
%dir /awips2/database/sqlScripts/share/sql/bmh
/awips2/database/sqlScripts/share/sql/bmh/bmh.log

%defattr(755,awips,fxalpha,755)
/awips2/database/sqlScripts/share/sql/bmh/*.sql
