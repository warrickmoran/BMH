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
package com.raytheon.uf.edex.bmh.tts;

import com.raytheon.uf.edex.bmh.tts.TTSConstants.TTS_RETURN_VALUE;

/**
 * Wraps data returned by {@link TTSInterface}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSReturn {

    private final TTS_RETURN_VALUE returnValue;

    private byte[] voiceData;

    /**
     * 
     */
    public TTSReturn(TTS_RETURN_VALUE returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * @return the voiceData
     */
    public byte[] getVoiceData() {
        return voiceData;
    }

    /**
     * @param voiceData
     *            the voiceData to set
     */
    public void setVoiceData(byte[] voiceData) {
        this.voiceData = voiceData;
    }

    /**
     * @return the returnValue
     */
    public TTS_RETURN_VALUE getReturnValue() {
        return returnValue;
    }
}