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
package com.raytheon.uf.edex.bmh.test;

import java.util.Collection;
import java.util.Map;

import org.springframework.transaction.TransactionException;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.database.DataAccessLayerException;

/**
 * NOT OPERATIONAL CODE!. Handles common data access operations that are
 * utilized by multiple test cases.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 8, 2014  3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class TestDataUtil {

    private static final TtsVoiceDao ttsVoiceDao = new TtsVoiceDao();

    private static final InputMessageDao inputMessageDao = new InputMessageDao();

    private static final TransmitterGroupDao transmitterGroupDao = new TransmitterGroupDao();

    private static final TransmitterLanguageDao transmitterLanguageDao = new TransmitterLanguageDao();

    /**
     * 
     */
    protected TestDataUtil() {
    }

    /**
     * Creates an instance of a TTS Voice based on the NeoSpeech Julie voice.
     * Julie was used based on the current TTS Server setup in Omaha.
     * 
     * @return the TtsVoice that is constructed
     */
    public static TtsVoice constructDefaultTtsVoice() {
        /*
         * This could optionally be configurable via the test properties file.
         */
        TtsVoice voice = new TtsVoice();
        voice.setVoiceNumber(103);
        voice.setVoiceName("Julie");
        voice.setLanguage(Language.ENGLISH);
        voice.setMale(false);

        return voice;
    }

    public static void persistTtsVoice(TtsVoice ttsVoice)
            throws TestProcessingFailedException {
        try {
            ttsVoiceDao.persist(ttsVoice);
        } catch (TransactionException e) {
            throw new TestProcessingFailedException(
                    "Failed to create a test Tts Voice!!", e);
        }
    }

    public static InputMessage checkForExistingTestInputMessage(
            final String afosid) {
        Collection<?> results = null;
        try {
            results = inputMessageDao.queryBySingleCriteria("afosid", afosid);
        } catch (DataAccessLayerException e) {
            // Do Nothing.
        }

        if ((results == null) || results.isEmpty()) {
            return null;
        }

        Object result = results.iterator().next();
        if (result instanceof InputMessage) {
            return (InputMessage) result;
        }

        return null;
    }

    public static InputMessage persistInputMessage(final String afosid,
            final String content) throws TestProcessingFailedException {
        InputMessage inputMessage = new InputMessage();
        inputMessage.setAfosid(afosid);
        inputMessage.setContent(content);
        try {
            inputMessageDao.persist(inputMessage);
        } catch (TransactionException e) {
            throw new TestProcessingFailedException(
                    "Failed to create a test Input Message!", e);
        }

        return inputMessage;
    }

    public static TransmitterGroup persistTransmitterGroup(
            final String groupName, final String programName,
            Map<Language, TransmitterLanguage> languages)
            throws TestProcessingFailedException {
        TransmitterGroup transmitterGroup = new TransmitterGroup();
        transmitterGroup.setName(groupName);
        try {
            transmitterGroupDao.persist(transmitterGroup);
        } catch (TransactionException e) {
            throw new TestProcessingFailedException(
                    "Failed to create a test Transmitter Group!", e);
        }

        if ((languages != null) && !languages.isEmpty()) {
            try {
                for (TransmitterLanguage lang : languages.values()) {
                    lang.setTransmitterGroup(transmitterGroup);
                }

                transmitterLanguageDao.persistAll(languages.values());
            } catch (TransactionException e) {
                throw new TestProcessingFailedException(
                        "Failed to create a test Transmitter Group!", e);
            }
        }

        return transmitterGroup;
    }
}