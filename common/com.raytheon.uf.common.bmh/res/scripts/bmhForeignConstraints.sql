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

/**
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 10, 2014 3175       rjpeter     Initial creation.
 * Jul 28, 2014 3175       rjpeter     Added on delete cascade for inputmessage through playlist.
 **/

/**
 * Allow cascading delete of incoming message all the way through to the playlist
 **/

alter table bmh.validated_msg drop constraint fkceb729d04a334ab9;
alter table bmh.validated_msg add constraint fkceb729d04a334ab9
    foreign key (input_msg_id) references bmh.input_msg (id) on delete cascade;

alter table bmh.validated_msg_transmitter_groups drop constraint fka166c131a4daf631;
alter table bmh.validated_msg_transmitter_groups add constraint fka166c131a4daf631
    foreign key (validated_msg_id) references bmh.validated_msg (id) on delete cascade;

alter table bmh.broadcast_msg drop constraint fkc71a77035d48c9f3;
alter table bmh.broadcast_msg add constraint fkc71a77035d48c9f3
    foreign key (input_message_id) references bmh.input_msg (id) on delete cascade;

alter table bmh.playlist_messages drop constraint fk86d39d1946dde881;
alter table bmh.playlist_messages add constraint fk86d39d1946dde881
    foreign key (message_id) references bmh.broadcast_msg (id) on delete cascade;

/**
 * Area/Zone join table cascade delete
 **/
alter table bmh.area_tx drop constraint fkd381bb7657342f33;
alter table bmh.area_tx add constraint fkd381bb7657342f33
    foreign key (areaId) references bmh.area(areaId) on delete cascade;

alter table bmh.area_tx drop constraint fkd381bb762d91c1bf;
alter table bmh.area_tx add constraint fkd381bb762d91c1bf
    foreign key (transmitterId) references bmh.transmitter(id) on delete cascade;
    
alter table bmh.zone_area drop constraint fk1feed14057342f33;
alter table bmh.zone_area add constraint fk1feed14057342f33
    foreign key (areaId) references bmh.area(areaId) on delete cascade;

alter table bmh.zone_area drop constraint fk1feed14081c289b1;
alter table bmh.zone_area add constraint fk1feed14081c289b1
    foreign key (zoneId) references bmh.zone(id) on delete cascade;

