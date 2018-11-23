---
-- #%L
-- Excel Report Format Application
-- %%
-- Copyright (C) 2016 - 2018 Emu Data Services
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
CREATE TABLE 'tblMember' (
'mem_no' TEXT(20) DEFAULT NULL PRIMARY KEY,
'client_ident' TEXT(15) NOT NULL  DEFAULT 'NULL' REFERENCES 'tblClient' ('client_ident'),
'mem_first_name' TEXT(20) DEFAULT NULL,
'mem_surname' TEXT(20) DEFAULT NULL,
'mem_position' TEXT(20) DEFAULT NULL
);

CREATE TABLE 'tlkpCallCategory' (
'call_cat_id' INTEGER NOT NULL  DEFAULT NULL PRIMARY KEY AUTOINCREMENT,
'call_cat' TEXT(20) NOT NULL  DEFAULT 'NULL',
'call_cat_desc' TEXT(100) DEFAULT NULL
);

CREATE TABLE 'tblCall' (
'call_id' INTEGER DEFAULT NULL PRIMARY KEY,
'mem_no' TEXT(20) NOT NULL  DEFAULT 'NULL' REFERENCES 'tblMember' ('mem_no'),
'caller_name' TEXT(50) DEFAULT NULL,
'call_cat_id' INTEGER DEFAULT NULL REFERENCES 'tlkpCallCategory' ('call_cat_id'),
'callsubcat_id' INTEGER DEFAULT NULL REFERENCES 'tlkpCallSubCategory' ('callsubcat_id'),
'call_date_start' TEXT NOT NULL ,
'call_note_start' TEXT DEFAULT NULL,
'call_date_end' TEXT NOT NULL ,
'call_duration' INTEGER DEFAULT NULL,
'callstatus_id' INTEGER NOT NULL  REFERENCES 'tlkpCallStatus' ('callstatus_id'),
'callaction_id' INTEGER NOT NULL  REFERENCES 'tlkpCallAction' ('callaction_id'),
'callres_id' INTEGER NOT NULL  REFERENCES 'tlkpCallResolution' ('callres_id'),
'call_note_res' TEXT DEFAULT NULL,
'call_location' TEXT(50) DEFAULT NULL,
'call_pilot_part' TEXT(15) DEFAULT NULL
);

CREATE TABLE 'tlkpCallSubCategory' (
'callsubcat_id' INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT,
'call_cat_id' INTEGER DEFAULT NULL REFERENCES 'tlkpCallCategory' ('call_cat_id'),
'call_sub_cat' TEXT(20) DEFAULT NULL,
'call_sub_cat_desc' TEXT(100) DEFAULT NULL
);

CREATE TABLE 'tlkpCallStatus' (
'callstatus_id' INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT,
'call_status' TEXT(20) NOT NULL ,
'call_stat_desc' TEXT(100) DEFAULT NULL
);

CREATE TABLE 'tlkpCallAction' (
'callaction_id' INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT,
'call_action' TEXT(20) NOT NULL ,
'call_action_desc' TEXT(100) DEFAULT NULL
);

CREATE TABLE 'tlkpCallResolution' (
'callres_id' INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT,
'call_res' TEXT(20) NOT NULL  DEFAULT 'NULL',
'call_res_desc' TEXT(100) DEFAULT NULL
);

CREATE TABLE 'tblClient' (
'client_ident' TEXT(15) NOT NULL  DEFAULT 'NULL' PRIMARY KEY,
'client_name' TEXT(50) NOT NULL  DEFAULT 'NULL',
'client_rpt_freq' TEXT(1) NOT NULL  DEFAULT 'NULL'
);

CREATE TABLE 'tblFile' (
'file_name' TEXT NOT NULL  PRIMARY KEY,
'date_processed' TEXT NOT NULL ,
'file_status' TEXT NOT NULL  DEFAULT 'blank',
'enquiries' INTEGER NOT NULL  DEFAULT 0,
'calls' INTEGER NOT NULL  DEFAULT 0,
'times_run' INTEGER NOT NULL  DEFAULT 0
);


CREATE TABLE 'tblMeta' (
'call_id' INTEGER NOT NULL  PRIMARY KEY REFERENCES 'tblCall' ('call_id'),
'file_name' TEXT NOT NULL  REFERENCES 'tblStatus' ('file_name'),
'line_no' INTEGER NOT NULL  DEFAULT NULL,
'duplicate' NUMERIC(1) DEFAULT 0
);

CREATE TABLE 'tblAnomalies' (
'file_name' TEXT NOT NULL  DEFAULT 'NULL' REFERENCES 'tblStatus' ('file_name'),
'line_no' INTEGER NOT NULL  DEFAULT NULL,
'col_no' INTEGER NOT NULL  DEFAULT NULL,
'attrib' TEXT NOT NULL  DEFAULT 'NULL',
'err_code' TEXT NOT NULL  DEFAULT 'NULL',
'descr' TEXT NOT NULL  DEFAULT 'NULL',
'accept' STRING NULL DEFAULT "#"
);



CREATE INDEX 'IDX_01' ON 'tblAnomalies' ('file_name', 'line_no', 'col_no');

CREATE TRIGGER
updateFileStatusToValid
AFTER UPDATE ON tblAnomalies
WHEN 0 = (SELECT COUNT(*) FROM tblAnomalies WHERE file_name = old.file_name  AND accept = '#')

BEGIN
	UPDATE tblFile
    SET file_status = 'VALID'
    WHERE file_name = old.file_name;
END;


CREATE TRIGGER
updateFileStatusToInvalid
AFTER UPDATE ON tblAnomalies
WHEN 1 = (SELECT COUNT(*) FROM tblAnomalies WHERE file_name = old.file_name  AND accept = '#')

BEGIN
	UPDATE tblFile
    SET file_status = 'INVALID'
    WHERE file_name = old.file_name;
END;

