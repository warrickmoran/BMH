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

alter table bmh.area_tx drop constraint fkd381bb7657342f33;
alter table bmh.area_tx add constraint fkd381bb7657342f33
    foreign key (areaId) references bmh.area(areaId) on delete cascade;
    
alter table bmh.area_tx drop constraint fkd381bb76f6502a8e;
alter table bmh.area_tx add constraint fkd381bb76f6502a8e
    foreign key (id) references bmh.transmitter(id) on delete cascade;
    
alter table bmh.zone_area drop constraint fk1feed14057342f33;
alter table bmh.zone_area add constraint fk1feed14057342f33
    foreign key (areaId) references bmh.area(areaId) on delete cascade;

alter table bmh.zone_area drop constraint fk1feed140ab43a105;
alter table bmh.zone_area add constraint fk1feed140ab43a105
    foreign key (id) references bmh.zone(id) on delete cascade;

