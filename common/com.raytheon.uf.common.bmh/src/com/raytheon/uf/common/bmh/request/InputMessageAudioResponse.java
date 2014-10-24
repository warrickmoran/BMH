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
package com.raytheon.uf.common.bmh.request;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Extends the existing {@link InputMessageResponse} so that the audio bytes
 * associated with a particular input message can be returned. Audio information
 * is returned as a {@link InputMessageAudioData} record. One record is created
 * for each Transmitter Group that audio was generated for.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2014 3748       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class InputMessageAudioResponse extends InputMessageResponse {

    @DynamicSerializeElement
    private List<InputMessageAudioData> audioDataList;

    /**
     * 
     */
    public InputMessageAudioResponse() {
    }

    /**
     * @return the audioDataList
     */
    public List<InputMessageAudioData> getAudioDataList() {
        return audioDataList;
    }

    /**
     * @param audioDataList
     *            the audioDataList to set
     */
    public void setAudioDataList(List<InputMessageAudioData> audioDataList) {
        this.audioDataList = audioDataList;
    }
}