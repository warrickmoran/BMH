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
package com.raytheon.uf.common.bmh.legacy.ascii;

import com.raytheon.uf.common.bmh.datamodel.language.Language;

/**
 * POJO used to keep track of the time and station id messages associated with a
 * transmitter group and a specific {@link Language}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 10, 2015 4213       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterMessages {

    private final String transmitterGroupName;

    private final Language language;

    private final String stationIdMessage;

    private final String defaultTimeMessage;

    public TransmitterMessages(String transmitterGroupName, Language language,
            String stationIdMessage, String defaultTimeMessage) {
        this.transmitterGroupName = transmitterGroupName;
        this.language = language;
        this.stationIdMessage = stationIdMessage;
        this.defaultTimeMessage = defaultTimeMessage;
    }

    /**
     * @return the transmitterGroupName
     */
    public String getTransmitterGroupName() {
        return transmitterGroupName;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @return the stationIdMessage
     */
    public String getStationIdMessage() {
        return stationIdMessage;
    }

    /**
     * @return the defaultTimeMessage
     */
    public String getDefaultTimeMessage() {
        return defaultTimeMessage;
    }
}
