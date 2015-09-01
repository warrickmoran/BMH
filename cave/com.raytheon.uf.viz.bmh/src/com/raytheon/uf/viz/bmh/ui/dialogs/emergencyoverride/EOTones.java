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
package com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride;

import com.raytheon.uf.common.bmh.tones.GeneratedTonesBuffer;

/**
 * POJO used by the {@link EOBroadcastSettingsBuilder} to store the generated
 * {@link GeneratedTonesBuffer} as well as the SAME String that was used to
 * generate the tones.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 1, 2015  4825       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class EOTones {

    private final GeneratedTonesBuffer tonesBuffer;

    private final String sameText;

    public EOTones(GeneratedTonesBuffer tonesBuffer, String sameText) {
        this.tonesBuffer = tonesBuffer;
        this.sameText = sameText;
    }

    /**
     * @return the tonesBuffer
     */
    public GeneratedTonesBuffer getTonesBuffer() {
        return tonesBuffer;
    }

    /**
     * @return the sameText
     */
    public String getSameText() {
        return sameText;
    }
}