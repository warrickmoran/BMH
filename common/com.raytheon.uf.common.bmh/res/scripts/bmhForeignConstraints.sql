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
 * Feb 23, 2014 4140       rjpeter     Updated constraint names, add Message Default Transmitter Groups cascades.
 * May 12, 2015 4248       rjpeter     Remove bmh schema, standardize foreign keys.
 * Jun 05, 2015 4482       rjpeter     Add cascade delete to message type defaults.
 **/

/**
 * Allow cascading delete of incoming message all the way through to the playlist
 **/

alter table validated_msg drop constraint fk_validated_msg_to_input_msg;
alter table validated_msg add constraint fk_validated_msg_to_input_msg
    foreign key (input_msg_id) references input_msg (id) on delete cascade;

alter table validated_msg_transmitter_groups drop constraint fk_valid_msg_tx_groups_to_tx_group;
alter table validated_msg_transmitter_groups add constraint fk_valid_msg_tx_groups_to_tx_group
    foreign key (transmitter_group_id) references transmitter_group (id) on delete cascade;

alter table validated_msg_transmitter_groups drop constraint fk_valid_msg_tx_groups_to_validated_msg;
alter table validated_msg_transmitter_groups add constraint fk_valid_msg_tx_groups_to_validated_msg
    foreign key (validated_msg_id) references validated_msg (id) on delete cascade;

alter table broadcast_msg drop constraint fk_broadcast_msg_to_input_msg;
alter table broadcast_msg add constraint fk_broadcast_msg_to_input_msg
    foreign key (input_message_id) references input_msg (id) on delete cascade;

alter table playlist_msg drop constraint fk_playlist_msg_to_broadcast_msg;
alter table playlist_msg add constraint fk_playlist_msg_to_broadcast_msg
    foreign key (message_id) references broadcast_msg (id) on delete cascade;

alter table broadcast_msg_contents drop constraint fk_broadcast_msg_contents_to_broadcast_msg;
alter table broadcast_msg_contents add constraint fk_broadcast_msg_contents_to_broadcast_msg
    foreign key (broadcast_id) references broadcast_msg (id) on delete cascade;

alter table broadcast_fragment drop constraint fk_broadcast_fragment_to_broadcast_msg_contents;
alter table broadcast_fragment add constraint fk_broadcast_fragment_to_broadcast_msg_contents
    foreign key (contents_broadcast_id, contents_timestamp) references broadcast_msg_contents (broadcast_id, "timestamp") 
    on delete cascade;

/**
 * Suite Message to Message Type
 **/
alter table suite_msg drop constraint fk_suite_msg_to_msg_type;
alter table suite_msg add constraint fk_suite_msg_to_msg_type
    foreign key (msgtype_id) references msg_type(id) on delete cascade;

/**
 * Suite to Suite Message 
 **/

alter table suite_msg drop constraint fk_suite_msg_to_suite;
alter table suite_msg add constraint fk_suite_msg_to_suite
    foreign key (suite_id) references suite(id) on delete cascade;

/**
 * Program Suite to Program
 **/
alter table program_suite drop constraint fk_program_suite_to_program;
alter table program_suite add constraint fk_program_suite_to_program
     foreign key (program_id) references program(id) on delete cascade;

/**
 * Program Suite to Suite
 **/
alter table program_suite drop constraint fk_program_suite_to_suite;
alter table program_suite add constraint fk_program_suite_to_suite
     foreign key (suite_id) references suite(id) on delete cascade;

/**
 * Program Trigger to Program Suite
 **/
alter table program_trigger drop constraint fk_program_trigger_to_program_suite;
alter table program_trigger add constraint fk_program_trigger_to_program_suite
     foreign key (program_id, suite_id) references program_suite (program_id, suite_id) 
     on delete cascade;

/**
 * Program Trigger to Message Type
 **/
alter table program_trigger drop constraint fk_program_trigger_to_msg_type;
alter table program_trigger add constraint fk_program_trigger_to_msg_type
     foreign key (msgtype_id) references msg_type(id) on delete cascade;

/**
 * Area/Zone join table cascade delete
 **/
alter table area_transmitter drop constraint fk_area_tx_to_area;
alter table area_transmitter add constraint fk_area_tx_to_area
    foreign key (areaid) references area(id) on delete cascade;

alter table area_transmitter drop constraint fk_area_tx_to_tx;
alter table area_transmitter add constraint fk_area_tx_to_tx
    foreign key (transmitterid) references transmitter(id) on delete cascade;

alter table zone_area drop constraint fk_zone_area_to_area;
alter table zone_area add constraint fk_zone_area_to_area
    foreign key (areaId) references area(id) on delete cascade;

alter table zone_area drop constraint fk_zone_area_to_zone;
alter table zone_area add constraint fk_zone_area_to_zone
    foreign key (zoneId) references zone(id) on delete cascade;

/**
 * Playlist to transmitter group
 */
alter table playlist drop constraint fk_playlist_to_tx_group;
alter table playlist add constraint fk_playlist_to_tx_group
    foreign key (transmitter_group_id) references transmitter_group(id) on delete cascade;

/**
 * Playlist to suite
 */
alter table playlist drop constraint fk_playlist_to_suite;
alter table playlist add constraint fk_playlist_to_suite
    foreign key (suite_id) references suite(id) on delete cascade;


/**
 * Playlist message to playlist
 **/
alter table playlist_msg drop constraint fk_playlist_msg_to_playlist;
alter table playlist_msg add constraint fk_playlist_msg_to_playlist
    foreign key (playlist_id) references playlist(id) on delete cascade;

/**
 * Broadcast message to transmitter group
 */
alter table broadcast_msg drop constraint fk_broadcast_msg_to_tx_group;
alter table broadcast_msg add constraint fk_broadcast_msg_to_tx_group
    foreign key (transmitter_group_id) references transmitter_group (id) on delete cascade;

/**
 * Message Type Same Transmitter to Transmitter
 */
alter table msg_type_same_transmitter drop constraint fk_msg_type_same_tx_to_tx;
alter table msg_type_same_transmitter add constraint fk_msg_type_same_tx_to_tx
    foreign key (transmitter_id) references transmitter (id) on delete cascade;

/**
 * Message Type Default Area to Area
 */
alter table msg_type_default_areas drop constraint fk_msg_type_def_areas_to_area;
alter table msg_type_default_areas add constraint fk_msg_type_def_areas_to_area
    foreign key (area_id) references area (id) on delete cascade;

/**
 * Message Type Default Zone to Zone
 */
alter table msg_type_default_zones drop constraint fk_msg_type_def_zones_to_zone;
alter table msg_type_default_zones add constraint fk_msg_type_def_zones_to_zone
    foreign key (zone_id) references zone (id) on delete cascade;

/**
 * Message Default Transmitter Groups to TransmitterGroup
 */
alter table msg_type_default_transmitter_groups drop constraint fk_msg_type_def_tx_groups_to_tx_group;
alter table msg_type_default_transmitter_groups add constraint fk_msg_type_def_tx_groups_to_tx_group
 foreign key (transmitter_group_id) references transmitter_group (id) on delete cascade;


/**
 * Ldad Config to Dictionary
 **/
alter table ldad_config drop constraint fk_ldad_config_to_dict;
alter table ldad_config add constraint fk_ldad_config_to_dict
    foreign key (dictionary_name) references dictionary(name) on delete set null;

/**
 * TTS Voice to Dictionary
 **/
alter table tts_voice drop constraint fk_tts_voice_to_dict;
alter table tts_voice add constraint fk_tts_voice_to_dict
     foreign key (dictionary_name) references dictionary(name) on delete set null;

/**
 * Ldad Config / Message Type Join Table Cascade Delete
 **/
alter table ldad_config_msg_type drop constraint fk_ldad_config_msg_type_to_ldad_config;
alter table ldad_config_msg_type add constraint fk_ldad_config_msg_type_to_ldad_config
    foreign key (ldad_id) references ldad_config(id) on delete cascade;
 
alter table ldad_config_msg_type drop constraint fk_ldad_config_msg_type_to_msg_type;
alter table ldad_config_msg_type add constraint fk_ldad_config_msg_type_to_msg_type
    foreign key (msg_type_id) references msg_type(id) on delete cascade;
    
/**
 * Transmitter Language to Transmitter
 */
alter table transmitter_language drop constraint fk_tx_language_to_tx_group;
alter table transmitter_language add constraint fk_tx_language_to_tx_group
    foreign key (transmittergroup_id) references transmitter_group (id) on delete cascade;

/**
 * Input Msg Selected Transmitter to Input Msg
 */
alter table input_msg_selected_transmitters drop constraint fk_selected_tx_to_input_msg;
alter table input_msg_selected_transmitters add constraint fk_selected_tx_to_input_msg
    foreign key (input_msg_id) references input_msg (id) on delete cascade;

/**
 * Input Msg Selected Transmitter To Transmitter
 */
alter table input_msg_selected_transmitters drop constraint fk_selected_tx_to_tx;
alter table input_msg_selected_transmitters add constraint fk_selected_tx_to_tx
    foreign key (transmitter_id) references transmitter (id) on delete cascade;

/**
  * Static Message Type to Transmitter Language
  */
alter table static_msg_type drop constraint fk_static_msg_type_to_tx_lang;
alter table static_msg_type add constraint fk_static_msg_type_to_tx_lang
    foreign key (language, transmittergroup_id) references transmitter_language (language, transmittergroup_id) on delete cascade;

/**
  * Static Message Type to Message Type
  */
alter table static_msg_type drop constraint fk_static_msg_type_to_msg_type;
alter table static_msg_type add constraint fk_static_msg_type_to_msg_type
    foreign key (msgtype_id) references msg_type (id) on delete cascade;

/**
 * Delete duplicate constraint
 */
alter table transmitter_group drop constraint fk_tx_group_to_program_delete_me;
