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
package com.raytheon.uf.edex.bmh.test.xform;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.TransactionException;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.test.AbstractBMHTester;
import com.raytheon.uf.edex.bmh.test.TestDataUtil;
import com.raytheon.uf.edex.bmh.test.TestProcessingFailedException;

/**
 * NOT OPERATIONAL CODE! Created to test the Message Transformer. Refer to the
 * data/xform directory in this plugin for a template input properties file.
 * 
 * NOTE: this test does not produce any output directly. This test encompasses:
 * TEST BEGIN -> Message Transformer -> TTS Manager -> TEST END
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

public class MessageTransformerTester extends AbstractBMHTester {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageTransformerTester.class);

    private static final String TEST_NAME = "Message Transformer";

    private static final String XFORM_DIRECTORY_INPUT_PROPERTY = "bmh.xform.test.directory.input";

    private static final String XFORM_TEST_AFOS_ID = "XFORMTST1";

    private static final String PROGRAM_NAME_PREFIX = "prgm";

    private static final String MSG_TYPE_TITLE = "XFORMTITLE";

    private static final String MSG_TYPE_DEFAULT_9 = "999999";

    private static final String TRANSMITTER_LANGUAGE_DICTIONARY = "XFormTestDict";

    private final InputMessageDao inputMessageDao;

    private final TtsVoiceDao ttsVoiceDao;

    private final MessageTypeDao messageTypeDao;

    private final TransmitterGroupDao transmitterGroupDao;

    private static final class INPUT_PROPERTIES {
        public static final String XFORM_AFOS_ID_PROPERTY = "xform.afosid";

        public static final String XFORM_MESSAGE_PROPERTY = "xform.message";

        public static final String XFORM_TRANSMITTER_GRP_PROPERTY_PREFIX = "xform.transmitter.group.";
    }

    /**
     * 
     */
    public MessageTransformerTester() {
        super(statusHandler, TEST_NAME);
        this.inputMessageDao = new InputMessageDao();
        this.ttsVoiceDao = new TtsVoiceDao();
        this.messageTypeDao = new MessageTypeDao();
        this.transmitterGroupDao = new TransmitterGroupDao();
    }

    @Override
    public void initialize() {
        statusHandler.info("Initializing the " + this.name + " Tester ...");
        if (this.validateTestInputs(XFORM_DIRECTORY_INPUT_PROPERTY) == false) {
            return;
        }
    }

    @Override
    protected Object processInput(Configuration configuration,
            String inputFileName) throws TestProcessingFailedException {
        final String message = super.getStringProperty(configuration,
                INPUT_PROPERTIES.XFORM_MESSAGE_PROPERTY, inputFileName);

        String afosid = null;
        try {
            afosid = super.getStringProperty(configuration,
                    INPUT_PROPERTIES.XFORM_AFOS_ID_PROPERTY, inputFileName);
        } catch (TestProcessingFailedException e) {
            statusHandler.info("The " + INPUT_PROPERTIES.XFORM_AFOS_ID_PROPERTY
                    + " property has not been set in input file: "
                    + inputFileName + "! Generating a default afos id.");
            afosid = XFORM_TEST_AFOS_ID;
        }

        /* Retrieve the transmitter groups from configuration. */
        List<String> namedTransmitterGroups = new LinkedList<String>();
        int index = 1;
        while (true) {
            final String transmitterProperty = INPUT_PROPERTIES.XFORM_TRANSMITTER_GRP_PROPERTY_PREFIX
                    + index;
            try {
                String transmitterGroup = super.getStringProperty(
                        configuration, transmitterProperty, inputFileName);
                if ((transmitterGroup == null)
                        || transmitterGroup.trim().isEmpty()) {
                    continue;
                }
                transmitterGroup = transmitterGroup.trim();
                if (transmitterGroup.length() > TransmitterGroup.NAME_LENGTH) {
                    transmitterGroup = transmitterGroup.substring(0,
                            TransmitterGroup.NAME_LENGTH);
                }
                namedTransmitterGroups.add(transmitterGroup);
            } catch (TestProcessingFailedException e) {
                /* we have finished reading the specified transmitter groups */
                break;
            }
            ++index;
        }

        if (namedTransmitterGroups.isEmpty()) {
            throw new TestProcessingFailedException(
                    "No transmitter groups were specified in input file: "
                            + inputFileName + "!");
        }

        /*
         * Create an InputMessage to associate with the test record.
         * 
         * Only setting the minimum required fields for the purposes of this
         * test.
         */
        InputMessage inputMessage = TestDataUtil
                .checkForExistingTestInputMessage(afosid);
        if (inputMessage == null) {
            /*
             * Create a new InputMessage for the purposes of this test.
             */
            inputMessage = TestDataUtil.persistInputMessage(afosid, message);
            statusHandler.info("Created Test Input Message with id: "
                    + inputMessage.getId());
        } else {
            if (inputMessage.getContent().equals(message) == false) {
                inputMessage.setContent(message);
                this.updateInputMessage(inputMessage);
            }
            statusHandler.info("Using existing Test Input Message with id: "
                    + inputMessage.getId());
        }
        /*
         * Need to ensure that there is also a {@link MessageType} associated
         * with the afos id.
         */
        MessageType messageType = this.messageTypeDao.getByAfosId(afosid);
        if (messageType == null) {
            /*
             * Create a Message Type to associate with the test record.
             * 
             * Only setting the minimum required fields for the purposes of this
             * test.
             */
            messageType = new MessageType();
            messageType.setAfosid(afosid);
            messageType.setTitle(MSG_TYPE_TITLE);
            messageType.setDuration(MSG_TYPE_DEFAULT_9);
            messageType.setDesignation(Designation.Warning);
            messageType.setDuration(MSG_TYPE_DEFAULT_9);
            messageType.setPeriodicity(MSG_TYPE_DEFAULT_9);
            messageType.setVoice(this.getAndInitDefaultTtsVoice());
            this.persistMsgType(messageType);
        }

        /*
         * Create and/or retrieve the transmitter groups that will be associated
         * with the Validated Message.
         */
        Set<TransmitterGroup> transmitterGroups = new LinkedHashSet<TransmitterGroup>(
                namedTransmitterGroups.size());
        int currentIndex = 1;
        for (String group : namedTransmitterGroups) {
            TransmitterGroup transmitterGroup = this
                    .saveOrRetrieveTransmitterGroup(group, currentIndex,
                            messageType.getVoice());
            transmitterGroups.add(transmitterGroup);
            ++currentIndex;
        }

        /*
         * Construct the Validated Message.
         */
        ValidatedMessage validatedMessage = new ValidatedMessage();
        validatedMessage.setInputMessage(inputMessage);
        validatedMessage.setTransmitterGroups(transmitterGroups);
        validatedMessage.setTransmissionStatus(TransmissionStatus.ACCEPTED);
        validatedMessage.setLdadStatus(LdadStatus.ACCEPTED);

        return validatedMessage;
    }

    private TransmitterGroup saveOrRetrieveTransmitterGroup(final String name,
            int currentIndex, TtsVoice ttsVoice)
            throws TestProcessingFailedException {
        TransmitterGroup transmitterGroup = this.transmitterGroupDao
                .getByGroupName(name);
        if (transmitterGroup != null) {
            return transmitterGroup;
        }

        Map<Language, TransmitterLanguage> languages = new HashMap<Language, TransmitterLanguage>();
        TransmitterLanguagePK transmitterLanguagePK = new TransmitterLanguagePK();
        transmitterLanguagePK.setLanguage(Language.ENGLISH);
        TransmitterLanguage transmitterLanguage = new TransmitterLanguage();
        transmitterLanguage.setId(transmitterLanguagePK);
        transmitterLanguage.setStationIdMsg(StringUtils.EMPTY);
        transmitterLanguage.setTimeMsg(StringUtils.EMPTY);
        // transmitterLanguage.setDictionaryName(TRANSMITTER_LANGUAGE_DICTIONARY);
        transmitterLanguage.setVoice(ttsVoice);
        languages.put(Language.ENGLISH, transmitterLanguage);

        return TestDataUtil.persistTransmitterGroup(name, PROGRAM_NAME_PREFIX
                + currentIndex, languages);
    }

    private TtsVoice getAndInitDefaultTtsVoice()
            throws TestProcessingFailedException {
        TtsVoice ttsVoice = TestDataUtil.constructDefaultTtsVoice();
        /*
         * Verify that the voice this test case will use is present in the
         * database.
         */
        if (this.ttsVoiceDao.getByID(ttsVoice.getVoiceNumber()) == null) {
            /*
             * Create a record for the specified test voice.
             */
            TestDataUtil.persistTtsVoice(ttsVoice);
            statusHandler.info("Created Test Tts Voice with id: "
                    + ttsVoice.getVoiceNumber());
        } else {
            statusHandler.info("Using existing Test Tts Voice with id: "
                    + ttsVoice.getVoiceNumber());
        }

        return ttsVoice;
    }

    private void updateInputMessage(InputMessage msg)
            throws TestProcessingFailedException {
        try {
            this.inputMessageDao.saveOrUpdate(msg);
        } catch (TransactionException e) {
            throw new TestProcessingFailedException(
                    "Failed to update the test Input Message with id: "
                            + msg.getId(), e);
        }
    }

    private void persistMsgType(MessageType messageType)
            throws TestProcessingFailedException {
        try {
            this.messageTypeDao.persist(messageType);
            statusHandler.info("Created Message Type with id: "
                    + messageType.getAfosid());
        } catch (TransactionException e) {
            throw new TestProcessingFailedException(
                    "Failed to create a test Message Type!", e);
        }
    }
}