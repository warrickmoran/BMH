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
package com.raytheon.uf.common.bmh.datamodel.playlist.compatibility;

import java.util.List;

/**
 * Identifies the message metadata fields that must be readily accessible.
 * 
 * NOTE: Do not update. Only exists to allow message file compatibility from one
 * version to the next.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 03, 2016  5308       bkowal      Initial creation
 * Mar 08, 2016  5382       bkowal      Based on 16.1.3. Maintained for message file conversion.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@Deprecated
public interface IMessageMetadataAccess16_1_3 {

    public boolean isPeriodic();

    /**
     * If this message has a valid "periodicity" setting, this method calculates
     * the time (in ms) that should elapse between plays of this message based
     * on the periodicity setting (format is DDHHmm).
     * 
     * @return Number of milliseconds between plays, or, -1 if this message does
     *         not have a valid periodicity setting.
     */
    public long getPlaybackInterval();

    public List<String> getSoundFiles();

    public boolean isDynamic();

}