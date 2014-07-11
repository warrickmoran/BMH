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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;

/**
 * LDAD Configuration Dialog's data manager class
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 11, 2014    3381    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class LdadConfigDataManager {
    /**
     * Get the available {@link TtsVoice} Text to Speech voices
     * 
     * @return List of available voices
     */
    public List<TtsVoice> getTtsVoices() {
        List<TtsVoice> voiceList = getTestData();

        return voiceList;
    }

    /**
     * Get available file encoding options
     * 
     * @return encoding options
     */
    public String[] getEncodingOptions() {
        return getTestEncodingOptions();
    }

    /**
     * Get existing configuration names.
     * 
     * @return List of existing configuration names
     */
    public List<String> getConfigurations() {
        List<String> configs = getTestConfigs();
        return configs;
    }

    /*
     * TEST DATA
     */
    private List<TtsVoice> getTestData() {
        List<TtsVoice> voiceList = new ArrayList<TtsVoice>();
        TtsVoice julie = new TtsVoice();
        julie.setLanguage(Language.ENGLISH);
        julie.setMale(false);
        julie.setVoiceName("Julie");
        julie.setVoiceNumber(103);
        voiceList.add(julie);
        return voiceList;
    }

    private String[] getTestEncodingOptions() {
        String[] options = new String[] { ".mp3", ".wav" };

        return options;
    }

    private List<String> getTestConfigs() {
        return Collections.emptyList();
    }
}
