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
 * Nov 11, 2014 3803       bkowal      Added on delete cascade for ldad config and ldad msg type.
 * Nov 26, 2014 3613       bkowal      Added cascade for broadcast_fragment
 * Dec 09, 2014 3603       bsteffen    Add cascade for transmitter language
 * Dec 08, 2014 3864       bsteffen    Add a PlaylistMsg class.
 * Jan 21, 2015 3960       bkowal      Added on delete cascade for foreign key fk_1xd877wghwn3fnawv8d7u6xrx.
 **/

/**
 * Allow cascading delete of incoming message all the way through to the playlist
 **/

alter table bmh.validated_msg drop constraint fk_jvvhk5vu0m3kcq59vuekvi745;
alter table bmh.validated_msg add constraint fk_jvvhk5vu0m3kcq59vuekvi745
    foreign key (input_msg_id) references bmh.input_msg (id) on delete cascade;

alter table bmh.validated_msg_transmitter_groups drop constraint fk_1xd877wghwn3fnawv8d7u6xrx;
alter table bmh.validated_msg_transmitter_groups add constraint fk_1xd877wghwn3fnawv8d7u6xrx
    foreign key (transmitter_group_id) references bmh.transmitter_group (id) on delete cascade;

alter table bmh.validated_msg_transmitter_groups drop constraint fk_2owgxs947t60l18wt6fpgrjgc;
alter table bmh.validated_msg_transmitter_groups add constraint fk_2owgxs947t60l18wt6fpgrjgc
    foreign key (validated_msg_id) references bmh.validated_msg (id) on delete cascade;

alter table bmh.broadcast_msg drop constraint fk_p29wngg6tdbtcr7ukef334213;
alter table bmh.broadcast_msg add constraint fk_p29wngg6tdbtcr7ukef334213
    foreign key (input_message_id) references bmh.input_msg (id) on delete cascade;

alter table bmh.playlist_msg drop constraint fk_n7wab174o6f0cmvw3cetrtsl8;
alter table bmh.playlist_msg add constraint fk_n7wab174o6f0cmvw3cetrtsl8
    foreign key (broadcast_msg_id) references bmh.broadcast_msg (id) on delete cascade;

alter table bmh.broadcast_fragment drop constraint fk_m87w4p7uk2l0vyjb1es2xs4vm;
alter table bmh.broadcast_fragment add constraint fk_m87w4p7uk2l0vyjb1es2xs4vm
    foreign key (message_id) references bmh.broadcast_msg (id) on delete cascade;


/**
 * Suite Message to Message Type
 **/
alter table bmh.suite_message drop constraint suite_message_to_message_type;
alter table bmh.suite_message add constraint suite_message_to_message_type
    foreign key (msgtype_id) references bmh.message_type(id) on delete cascade;

/**
 * Suite to Suite Message 
 **/

alter table bmh.suite_message drop constraint fk_955qyy9ujl5rdokt9vfikixc1;
alter table bmh.suite_message add constraint fk_955qyy9ujl5rdokt9vfikixc1
    foreign key (suite_id) references bmh.suite(id) on delete cascade;

/**
 * Program Suite to Program
 **/
alter table bmh.program_suite drop constraint program_suite_to_program;
alter table bmh.program_suite add constraint program_suite_to_program
     foreign key (program_id) references bmh.program(id) on delete cascade;

/**
 * Program Suite to Suite
 **/
alter table bmh.program_suite drop constraint program_suite_to_suite;
alter table bmh.program_suite add constraint program_suite_to_suite
     foreign key (suite_id) references bmh.suite(id) on delete cascade;

/**
 * Program Trigger to Program Suite
 **/
alter table bmh.program_trigger drop constraint program_trigger_to_program_suite;
alter table bmh.program_trigger add constraint program_trigger_to_program_suite
     foreign key (program_id, suite_id) references bmh.program_suite (program_id, suite_id) 
     on delete cascade;

/**
 * Program Trigger to Message Type
 **/
alter table bmh.program_trigger drop constraint program_trigger_to_message_type;
alter table bmh.program_trigger add constraint program_trigger_to_message_type
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
alter table bmh.playlist drop constraint playlist_to_tx_group;
alter table bmh.playlist add constraint playlist_to_tx_group
    foreign key (transmitter_group_id) references bmh.transmitter_group(id) on delete cascade;

/**
 * Playlist to suite
 */
alter table bmh.playlist drop constraint playlist_to_suite;
alter table bmh.playlist add constraint playlist_to_suite
    foreign key (suite_id) references bmh.suite(id) on delete cascade;


/**
 * Playlist message to playlist
 **/
alter table bmh.playlist_msg drop constraint fk_4xn6yclcydx4g61id81rdhuki;
alter table bmh.playlist_msg add constraint fk_4xn6yclcydx4g61id81rdhuki
    foreign key (playlist_id) references bmh.playlist(id) on delete cascade;

/**
 * Broadcast message to transmitter group
 */
alter table bmh.broadcast_msg drop constraint broadcast_msg_to_tx_group;
alter table bmh.broadcast_msg add constraint broadcast_msg_to_tx_group
    foreign key (transmitter_group_id) references bmh.transmitter_group (id) on delete cascade;

/**
 * Message Same Tx to Transmitter
 */
alter table bmh.message_same_tx drop constraint msg_same_tx_to_tx;
alter table bmh.message_same_tx add constraint msg_same_tx_to_tx
    foreign key (transmitter_id) references bmh.transmitter (id) on delete cascade;

/**
 * Ldad Config to Dictionary
 **/
alter table bmh.ldad_config drop constraint fk_5s2c03labr2wnrww2ek27ybne;
alter table bmh.ldad_config add constraint fk_5s2c03labr2wnrww2ek27ybne
    foreign key (dictionary_name) references bmh.dictionary(name) on delete set null;

/**
 * Ldad Config to Voice
 **/
alter table bmh.ldad_config drop constraint fk_ps1jemud6ob63lpsti6d91vq5;
alter table bmh.ldad_config add constraint fk_ps1jemud6ob63lpsti6d91vq5
    foreign key (voicenumber) references bmh.tts_voice(voicenumber) on delete cascade;
 	
/**
 * Ldad Config / Message Type Join Table Cascade Delete
 **/
alter table bmh.ldad_msg_type drop constraint fk_8o231lsdsmrvlnmw4p1rjr47t;
alter table bmh.ldad_msg_type add constraint fk_8o231lsdsmrvlnmw4p1rjr47t
    foreign key (ldad_id) references bmh.ldad_config(id) on delete cascade;
 
alter table bmh.ldad_msg_type drop constraint fk_orp1qpttjs7whbutxov3k370o;
alter table bmh.ldad_msg_type add constraint fk_orp1qpttjs7whbutxov3k370o
    foreign key (msg_type_id) references bmh.message_type(id) on delete cascade;
    
/**
 * Transmitter Language to Transmitter
 */
alter table bmh.transmitter_language drop constraint fk_ag6l0d2oqq951ku6rebv3k2he;
alter table bmh.transmitter_language add constraint fk_ag6l0d2oqq951ku6rebv3k2he
    foreign key (transmittergroup_id) references bmh.transmitter_group (id) on delete cascade;

/**
 * Voice to Dictionary
 */
alter table bmh.tts_voice drop constraint fk_nsj4b6uq3lxg3xlhfktiqgxk4;
alter table bmh.tts_voice add constraint fk_nsj4b6uq3lxg3xlhfktiqgxk4
    foreign key (dictionary_name) references bmh.dictionary (name) on delete set null;

/**
 * Input Msg Selected Transmitter to Input Msg
 */
alter table bmh.input_msg_selected_transmitters drop constraint msg_def_tx_to_input_msg;
alter table bmh.input_msg_selected_transmitters add constraint msg_def_tx_to_input_msg
    foreign key (input_msg_id) references bmh.input_msg (id) on delete cascade;

/**
 * Input Msg Selected Transmitter To Transmitter
 */
alter table bmh.input_msg_selected_transmitters drop constraint msg_def_tx_to_tx;
alter table bmh.input_msg_selected_transmitters add constraint msg_def_tx_to_tx
    foreign key (transmitter_id) references bmh.transmitter (id) on delete cascade;
