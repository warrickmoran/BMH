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
 * Aug 15, 2014 3515       rjpeter     Initial creation.
 * Aug 25, 2014 3558       rjpeter     Don't auto add dac_ports.
 **/

insert into bmh.dac_address (id, address, receiveport) values (1, '147.18.136.46', 21000);
insert into bmh.dac_address (id, address, receiveport) values (2, '147.18.136.47', 22000);
--insert into bmh.dac_ports (dac_id, dataport) values (1, 21002);
--insert into bmh.dac_ports (dac_id, dataport) values (1, 21004);
--insert into bmh.dac_ports (dac_id, dataport) values (1, 21006);
--insert into bmh.dac_ports (dac_id, dataport) values (1, 21008);
--insert into bmh.dac_ports (dac_id, dataport) values (2, 22002);
--insert into bmh.dac_ports (dac_id, dataport) values (2, 22004);
--insert into bmh.dac_ports (dac_id, dataport) values (2, 22006);
--insert into bmh.dac_ports (dac_id, dataport) values (2, 22008);
