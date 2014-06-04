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
package com.raytheon.uf.edex.bmh.msg;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * The header of a broadcast message. The header includes information about the
 * message, including the Specific Area Message Encoding (SAME) tones to be
 * issued before and after message broadcast and Alert tones; Comms Manager uses
 * this information to generate the sound bytes corresponding to these tones.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2014  3228       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

/*
 * TODO: define the message header. Contains tone information & primarily used
 * by the Comms Manager.
 */
@DynamicSerialize
public class BroadcastMsgHeader {

    /**
     * 
     */
    public BroadcastMsgHeader() {
        // TODO Auto-generated constructor stub
    }

}
