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
 * Sep 16, 2014 3587       bkowal      Added on delete cascade for program suite and program trigger.
 * Oct 13, 2014 3654       rjpeter     Updated constraint names.
 * Oct 22, 2014 3746       rjpeter     Hibernate upgrade.
 **/

/**
 * Allow cascading delete of incoming message all the way through to the playlist
 **/

alter table bmh.validated_msg drop constraint fk_jvvhk5vu0m3kcq59vuekvi745;
alter table bmh.validated_msg add constraint fk_jvvhk5vu0m3kcq59vuekvi745
    foreign key (input_msg_id) references bmh.input_msg (id) on delete cascade;

alter table bmh.validated_msg_transmitter_groups drop constraint fk_2owgxs947t60l18wt6fpgrjgc;
alter table bmh.validated_msg_transmitter_groups add constraint fk_2owgxs947t60l18wt6fpgrjgc
    foreign key (validated_msg_id) references bmh.validated_msg (id) on delete cascade;

alter table bmh.broadcast_msg drop constraint fk_p29wngg6tdbtcr7ukef334213;
alter table bmh.broadcast_msg add constraint fk_p29wngg6tdbtcr7ukef334213
    foreign key (input_message_id) references bmh.input_msg (id) on delete cascade;

alter table bmh.playlist_messages drop constraint fk_dna1dgu6xkltlusrwgc8s913q;
alter table bmh.playlist_messages add constraint fk_dna1dgu6xkltlusrwgc8s913q
    foreign key (message_id) references bmh.broadcast_msg (id) on delete cascade;

/**
 * Message Type to Suite Message
 **/
alter table bmh.suite_message drop constraint fk_qv6vo0x051r12tpkng2obfvbe;
alter table bmh.suite_message add constraint fk_qv6vo0x051r12tpkng2obfvbe
    foreign key (msgtype_id) references bmh.message_type(id) on delete cascade;

/**
 * Suite to Suite Message 
 **/

alter table bmh.suite_message drop constraint fk_955qyy9ujl5rdokt9vfikixc1;
alter table bmh.suite_message add constraint fk_955qyy9ujl5rdokt9vfikixc1
    foreign key (suite_id) references bmh.suite(id) on delete cascade;

/**
 * Program to Program Suite
 **/
alter table bmh.program_suite drop constraint fk_9ba88xi2q8p647co876bk9jg4;
alter table bmh.program_suite add constraint fk_9ba88xi2q8p647co876bk9jg4
     foreign key (program_id) references bmh.program(id) on delete cascade;

/**
 * Suite to Program Suite
 **/
alter table bmh.program_suite drop constraint fk_r7hcgfv2a6kdnt77isgw5lnwk;
alter table bmh.program_suite add constraint fk_r7hcgfv2a6kdnt77isgw5lnwk
     foreign key (suite_id) references bmh.suite(id) on delete cascade;

/**
 * Program Suite to Program Trigger
 **/
alter table bmh.program_trigger drop constraint fk_2p8905whnpp27i9g9n5am8r0d;
alter table bmh.program_trigger add constraint fk_2p8905whnpp27i9g9n5am8r0d
     foreign key (program_id, suite_id) references bmh.program_suite (program_id, suite_id) 
     on delete cascade;

/**
 * Message Type to Program Trigger
 **/
alter table bmh.program_trigger drop constraint fk_2tpfod4yh42mhea4814j5836g;
alter table bmh.program_trigger add constraint fk_2tpfod4yh42mhea4814j5836g
     foreign key (msgtype_id) references bmh.message_type(id) on delete cascade;

/**
 * Area/Zone join table cascade delete
 **/
alter table bmh.area_tx drop constraint fk_nxlwu1vgxtuv1w1r8rpu5lk0s;
alter table bmh.area_tx add constraint fk_nxlwu1vgxtuv1w1r8rpu5lk0s
    foreign key (areaId) references bmh.area(areaId) on delete cascade;

alter table bmh.area_tx drop constraint fk_r4ohtb0vnul78bp0w2k64feqv;
alter table bmh.area_tx add constraint fk_r4ohtb0vnul78bp0w2k64feqv
    foreign key (transmitterId) references bmh.transmitter(id) on delete cascade;
    
alter table bmh.zone_area drop constraint fk_oc1q5or3b91kvqh5i9mnrbfv5;
alter table bmh.zone_area add constraint fk_oc1q5or3b91kvqh5i9mnrbfv5
    foreign key (areaId) references bmh.area(areaId) on delete cascade;

alter table bmh.zone_area drop constraint fk_f1enmjbmmqdm8ufueky0cop5a;
alter table bmh.zone_area add constraint fk_f1enmjbmmqdm8ufueky0cop5a
    foreign key (zoneId) references bmh.zone(id) on delete cascade;

/**
 * Playlist to transmitter group
 */
alter table bmh.playlist drop constraint fk_6kdc3pt6pa1xwcnsoj71ljcvw;
alter table bmh.playlist add constraint fk_6kdc3pt6pa1xwcnsoj71ljcvw
    foreign key (transmitter_group_name) references bmh.transmitter_group(id) on delete cascade;

/**
 * Playlist message to playlist
 **/
alter table bmh.playlist_messages drop constraint fk_m2ap9u9gtxgdj45jux5po6h6g;
alter table bmh.playlist_messages add constraint fk_m2ap9u9gtxgdj45jux5po6h6g
    foreign key (playlist_id) references bmh.playlist(id) on delete cascade;

