/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
\set ON_ERROR_STOP 1
DROP DATABASE IF EXISTS bmh_practice;
DROP TABLESPACE IF EXISTS bmh_practice;
CREATE TABLESPACE bmh_practice owner awipsadmin location '/awips2/data/bmh_practice';
CREATE DATABASE bmh_practice OWNER awipsadmin TABLESPACE bmh_practice;

\connect bmh_practice

BEGIN TRANSACTION;
GRANT CONNECT, TEMPORARY ON DATABASE bmh_practice TO awips;

GRANT USAGE ON SCHEMA public to awips; -- Don't grant create
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, TRUNCATE ON TABLES TO awips; -- Don't grant references
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO awips;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO awips;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO awips;
COMMIT TRANSACTION;
