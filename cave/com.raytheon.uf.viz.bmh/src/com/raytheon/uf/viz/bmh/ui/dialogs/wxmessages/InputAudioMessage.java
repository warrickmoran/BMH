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
package com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.request.InputMessageAudioData;

/**
 * Data class holding the input message and list of audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 26, 2014  #3728     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class InputAudioMessage {

    /** Input Message. */
    private InputMessage inputMessage = null;

    /** List of audio. */
    private List<InputMessageAudioData> audioDataList;

    /**
     * Constructor.
     */
    public InputAudioMessage() {

    }

    /**
     * Constructor.
     * 
     * @param inputMessage
     * @param audioDataList
     */
    public InputAudioMessage(InputMessage inputMessage,
            List<InputMessageAudioData> audioDataList) {
        this.inputMessage = inputMessage;
        this.audioDataList = audioDataList;
    }

    public InputMessage getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    public List<InputMessageAudioData> getAudioDataList() {
        return audioDataList;
    }

    public void setAudioDataList(List<InputMessageAudioData> audioDataList) {
        this.audioDataList = audioDataList;
    }
}
