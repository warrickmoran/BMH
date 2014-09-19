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
 * Jul 28, 2014 3175       rjpeter     Initial creation.
 * Sep 16, 2014 3587       bkowal      Updated to reference the new program suite table.
 **/
CREATE OR REPLACE VIEW bmh.program_message_view AS 
 SELECT p.name AS program, s.name AS suite, s.type, m.afosid
   FROM bmh.program p, bmh.program_suite ps, bmh.suite s, bmh.suite_message sm, 
    bmh.message_type m
  WHERE p.id = ps.program_id AND s.id = ps.suite_id AND s.id = sm.suite_id AND m.id = sm.msgtype_id;

