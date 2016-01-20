#!/bin/sh
# DR 5127 - Adds an insert time column to input_msg that is auto populated by the database on insert of the row

PSQL="/awips2/psql/bin/psql"

databases=( "bmh" "bmh_practice" )

for database in ${databases[@]} ; do 
    echo "INFO: Updating $database input_msg"
    ${PSQL} -U awips -d $database -c "ALTER TABLE IF EXISTS input_msg DROP COLUMN if exists inserttime;"
    ${PSQL} -U awips -d $database -c "ALTER TABLE IF EXISTS input_msg ADD COLUMN inserttime timestamp without time zone not null default now();"
    ${PSQL} -U awips -d $database -c "ALTER TABLE IF EXISTS input_msg RENAME COLUMN updatedate to lastupdatetime;"
done

