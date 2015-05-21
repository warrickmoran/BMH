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
 * May 21, 2015 4397       bkowal      Initial creation.
 **/
---
--- This procedure will calculate the percentage of valid messages that were successfully
--- broadcast based on the total number of messages that the system has received.
---
DROP FUNCTION IF EXISTS messageDeliveryStat();
CREATE FUNCTION messageDeliveryStat() RETURNS decimal AS $$
DECLARE
	valid_msg_row validated_msg%ROWTYPE;
	valid_msg_trx_row validated_msg_transmitter_groups%ROWTYPE;

	expected_count decimal := 0;
	actual_count decimal := 0;
	message_broadcast boolean;
	calculated_percent decimal;
	final_result decimal;
	---
	--- Ensure that we only include messages that are due to be broadcast.
	---
	valid_msg_cursor CURSOR FOR SELECT v.* FROM validated_msg v
		INNER JOIN input_msg i
		ON v.transmissionstatus = 'ACCEPTED' AND
		i.id = v.input_msg_id AND i.effectivetime <= current_timestamp;
	---
	--- Ensure that we only include transmitter group(s) that are currently
	--- enabled.
	---
	valid_msg_trx_cursor CURSOR (v_id integer) FOR SELECT DISTINCT vg.* 
		FROM validated_msg_transmitter_groups vg
		INNER JOIN transmitter_group g ON g.id = vg.transmitter_group_id  
		AND vg.validated_msg_id = v_id
		INNER JOIN transmitter t ON t.transmittergroup_id = vg.transmitter_group_id AND
		t.txStatus = 'ENABLED';
BEGIN	
	FOR valid_msg_row IN valid_msg_cursor LOOP
		FOR valid_msg_trx_row IN valid_msg_trx_cursor(valid_msg_row.id) LOOP
			expected_count := expected_count + 1;

			SELECT bmsg.broadcast INTO message_broadcast FROM broadcast_msg bmsg WHERE
			bmsg.transmitter_group_id = valid_msg_trx_row.transmitter_group_id AND
			bmsg.input_message_id = valid_msg_row.input_msg_id;
			IF message_broadcast = true THEN
				actual_count := actual_count + 1;
			END IF;
		END LOOP;
	END LOOP;
	IF expected_count = 0 THEN
		return 0;
	END IF;
	calculated_percent = (actual_count / expected_count);
	final_result := calculated_percent * 100;
	RETURN final_result;
END;
$$ LANGUAGE plpgsql;
