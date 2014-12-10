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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages;

/**
 * 
 * Composite that contains all of the message audio control composites.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 26, 2014  #3728     lvenable     Initial creation
 * Oct 26, 2014  #3748     bkowal       Clear audio list on reset.
 * Nov 18, 2014  #3829     bkowal       Track all audio managed by the control. Added
 *                                      getAudioDataList.
 * Dec 10, 2014  #3883     bkowal       Handle dialog closure during audio playback.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageAudioComp extends Composite implements IAudioControlAction {

    /** Audio data list. */
    private List<InputMessageAudioData> audioDataList;

    /** Record images used for displaying on buttons, etc. */
    private RecordImages recordImages;

    /** List of message audio controls. */
    private List<MessageAudioControlComp> maccList = new ArrayList<MessageAudioControlComp>();

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite.
     * @param audioDataList
     *            List of audio data.
     * @param recordImages
     *            Record images used for displaying on buttons, etc.
     */
    public MessageAudioComp(Composite parent,
            List<InputMessageAudioData> audioDataList, RecordImages recordImages) {
        super(parent, SWT.NONE);
        if (audioDataList == null) {
            this.audioDataList = new ArrayList<>(1);
        } else {
            this.audioDataList = audioDataList;
        }
        this.recordImages = recordImages;

        init();

        this.layout();
    }

    /**
     * Initialize method.
     */
    private void init() {
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 1;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        for (InputMessageAudioData ad : audioDataList) {
            MessageAudioControlComp macc = new MessageAudioControlComp(this,
                    ad, recordImages, this);
            maccList.add(macc);
        }
    }

    /**
     * Remove all of the audio controls.
     */
    public void removeAllAudioControls() {
        if (maccList == null || maccList.isEmpty()) {
            return;
        }

        for (MessageAudioControlComp macc : maccList) {
            macc.dispose();
        }

        maccList.clear();
        this.audioDataList.clear();

        this.layout();
    }

    /**
     * Check if there are any message audio control present.
     * 
     * @return True if there are controls on the display.
     */
    public boolean hasAudioControls() {
        return !maccList.isEmpty();
    }

    /**
     * Add an audio control to he display.
     * 
     * @param audioData
     *            Audio data.
     */
    public void addAudioControl(InputMessageAudioData audioData) {
        this.audioDataList.add(audioData);
        MessageAudioControlComp macc = new MessageAudioControlComp(this,
                audioData, recordImages, this);
        maccList.add(macc);
        this.layout();
    }

    @Override
    public void enableAudioControls(boolean enable) {
        if (this.isDisposed()) {
            // user closes dialog during audio playback
            return;
        }
        /*
         * Loop over and disable the audio control buttons on each of the audio
         * controls.
         */
        for (MessageAudioControlComp macc : maccList) {
            macc.enableAudioControls(enable);
        }
    }

    /**
     * Return all audio records that are displayed in the composite.
     * 
     * @return all audio records that are displayed in the composite.
     */
    public List<InputMessageAudioData> getAudioDataList() {
        return audioDataList;
    }
}