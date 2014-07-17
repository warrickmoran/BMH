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
package com.raytheon.uf.common.bmh.legacy.ascii.data;

import java.util.Map;

/**
 * Container for information from the ascii file that is parsed before the rest
 * of the information is ready for the bmh object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3175      rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class Ascii {
    private Map<String, StationIdData> stationIdData;

    private String officeOrigin;

    private String timeZone;

    private Map<String, MessageGroupData> messageGroupData;

    public Map<String, StationIdData> getStationIdData() {
        return stationIdData;
    }

    public void setStationIdData(Map<String, StationIdData> stationIdData) {
        this.stationIdData = stationIdData;
    }

    public String getOfficeOrigin() {
        return officeOrigin;
    }

    public void setOfficeOrigin(String officeOrigin) {
        this.officeOrigin = officeOrigin;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Map<String, MessageGroupData> getMessageGroupData() {
        return messageGroupData;
    }

    public void setMessageGroupData(
            Map<String, MessageGroupData> messageGroupData) {
        this.messageGroupData = messageGroupData;
    }
}
