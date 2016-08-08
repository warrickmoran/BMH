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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.util.List;

/**
 * Identifies the message metadata fields that must be readily accessible.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 03, 2016 5308       bkowal      Initial creation
 * Mar 08, 2016 5382       bkowal      Added additional EDEX BMH-specific fields.
 * Aug 04, 2016 5766       bkowal      Added {@link #isTimePeriodic()} and {@link #isCyclePeriodic()}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IMessageMetadataAccess {

    public String getName();

    public String getMessageType();

    public String getSAMEtone();

    public boolean isAlertTone();

    public boolean isSAMETones();

    public boolean isPeriodic();

    public boolean isTimePeriodic();

    public boolean isCyclePeriodic();

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

    public boolean isConfirm();

    public boolean isWarning();

    public boolean isDynamic();

}