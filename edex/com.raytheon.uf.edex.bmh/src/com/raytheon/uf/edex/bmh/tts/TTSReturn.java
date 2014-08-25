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

import java.io.IOException;

import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;

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
 * Aug 25, 2014 3538       bkowal      Added additional fields for error reporting
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSReturn {

    private boolean synthesisSuccess;

    private long bytesSythensized;

    private final TTS_RETURN_VALUE returnValue;

    private byte[] voiceData;

    private boolean ioFailed;

    private IOException ioFailureCause;

    /**
     * 
     */
    public TTSReturn(TTS_RETURN_VALUE returnValue) {
        this.returnValue = returnValue;
        this.synthesisSuccess = false;
    }

    public void synthesisIsComplete(final long bytesSynthesized) {
        this.synthesisSuccess = true;
        this.bytesSythensized = bytesSynthesized;
    }

    public void ioHasFailed(IOException ioFailureCause) {
        this.ioFailed = true;
        this.ioFailureCause = ioFailureCause;
    }

    /**
     * @return the synthesisSuccess
     */
    public boolean isSynthesisSuccess() {
        return synthesisSuccess;
    }

    /**
     * @return the bytesSythensized
     */
    public long getBytesSythensized() {
        return bytesSythensized;
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

    /**
     * @return the ioFailed
     */
    public boolean isIoFailed() {
        return ioFailed;
    }

    /**
     * @return the ioFailureCause
     */
    public IOException getIoFailureCause() {
        return ioFailureCause;
    }

    /**
     * @param ioFailureCause
     *            the ioFailureCause to set
     */
    public void setIoFailureCause(IOException ioFailureCause) {
        this.ioFailureCause = ioFailureCause;
    }
}