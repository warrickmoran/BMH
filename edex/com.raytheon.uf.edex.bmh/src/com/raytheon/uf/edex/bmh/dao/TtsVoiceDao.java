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
package com.raytheon.uf.edex.bmh.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;

/**
 * BMH DAO for {@link TtsVoice}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014  3302       bkowal      Initial creation
 * Aug 11, 2014 #3490      lvenable    Updated to get Voice information.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TtsVoiceDao extends AbstractBMHDao<TtsVoice, Integer> {

    public TtsVoiceDao() {
        super(TtsVoice.class);
    }

    /**
     * Get all of the available voices.
     * 
     * @return List of voices.
     */
    public List<TtsVoice> getVoices() {
        List<Object> allObjects = this.loadAll();
        if (allObjects == null) {
            return Collections.emptyList();
        }

        List<TtsVoice> voiceList = new ArrayList<TtsVoice>(allObjects.size());
        for (Object obj : allObjects) {
            TtsVoice voice = (TtsVoice) obj;
            voiceList.add(voice);
        }

        return voiceList;
    }
}