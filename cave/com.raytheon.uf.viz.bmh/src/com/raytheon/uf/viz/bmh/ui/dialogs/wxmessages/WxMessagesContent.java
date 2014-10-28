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

/**
 * Used to track / identify Weather Messages content.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 26, 2014 3748       bkowal      Initial creation
 * Oct 28, 2014 3759       bkowal      Added a validation mechanism.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class WxMessagesContent {

    public static enum CONTENT_TYPE {
        AUDIO, TEXT
    }

    private final CONTENT_TYPE contentType;

    private String text;

    private byte[] audio;

    /**
     * 
     */
    public WxMessagesContent(CONTENT_TYPE contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the audio
     */
    public byte[] getAudio() {
        return audio;
    }

    /**
     * @param audio
     *            the audio to set
     */
    public void setAudio(byte[] audio) {
        this.audio = audio;
    }

    /**
     * @return the contentType
     */
    public CONTENT_TYPE getContentType() {
        return contentType;
    }

    public boolean isComplete() {
        if (this.contentType == CONTENT_TYPE.TEXT) {
            return this.text != null && this.text.isEmpty() == false;
        } else {
            // audio
            return this.audio != null;
        }
    }
}