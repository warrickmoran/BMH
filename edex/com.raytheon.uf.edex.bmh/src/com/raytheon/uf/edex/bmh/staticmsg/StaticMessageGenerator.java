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
package com.raytheon.uf.edex.bmh.staticmsg;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.MessageTypeConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.IContextStateProcessor;

/**
 * Responsible for generating static time and station messages. When, a message
 * needs to be generated, it will create a @{link ValidatedMessage} and send it
 * through the camel pipeline.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 28, 2014 3568       bkowal      Initial creation
 * Sep 09, 2014 2585       bsteffen    Implement MAT
 * Sep 12, 2014 3588       bsteffen    Support audio fragments.
 * Sep 29, 2014 3673       bkowal      Fix existing msg validation.
 * Oct 2, 2014  3642       bkowal      Utilize the time audio generator to
 *                                     verify / generate time messages for a
 *                                     specific voice and timezone.
 * Oct 07, 2014 3687       bsteffen    Handle reset notification
 * Oct 17, 2014 3642       bkowal      Set input msg name for static messages.
 * Oct 28, 2014 3750       bkowal      Fix time messages. Support practice mode.
 * 
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StaticMessageGenerator implements IContextStateProcessor {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(StaticMessageGenerator.class);

    private final TimeMessagesGenerator tmGenerator;

    /*
     * Maximum allowed value for PostgreSQL year as documented at:
     * http://www.postgresql.org/docs/9.3/static/datatype-datetime.html
     * 
     * TODO: recommend supporting NULL to indicate no expiration.
     */
    private static final int MAX_YEAR = 294276;

    private final Calendar expire;

    private BroadcastMsgDao broadcastMsgDao;

    private TransmitterGroupDao transmitterGroupDao;

    private TransmitterLanguageDao transmitterLanguageDao;

    private MessageTypeDao messageTypeDao;

    private Map<Integer, MessageType> staticMessageTypesMap;

    private Object configurationLock = new Object();

    /**
     * 
     */
    public StaticMessageGenerator(final TimeMessagesGenerator tmGenerator) {
        this.tmGenerator = tmGenerator;
        this.expire = Calendar.getInstance();
        this.expire.set(Calendar.YEAR, MAX_YEAR);
    }

    public void initializeInternal() {
        this.staticMessageTypesMap = new HashMap<>();

        List<MessageType> stationMessageTypes = this
                .retrieveMsgTypes(StaticMessageIdentifierUtil.stationDesignation);
        if (stationMessageTypes != null) {
            for (MessageType stationMessageType : stationMessageTypes) {
                this.staticMessageTypesMap.put(stationMessageType.getId(),
                        stationMessageType);
            }
        }
        List<MessageType> timeMessageTypes = this
                .retrieveMsgTypes(StaticMessageIdentifierUtil.timeDesignation);
        if (timeMessageTypes != null) {
            for (MessageType timeMessageType : timeMessageTypes) {
                this.staticMessageTypesMap.put(timeMessageType.getId(),
                        timeMessageType);
            }
        }
    }

    private List<MessageType> retrieveMsgTypes(Designation designation) {
        List<MessageType> msgTypes;

        statusHandler.info("Retrieving the " + designation
                + " Message Type(s) ...");
        msgTypes = this.messageTypeDao
                .getMessageTypeForDesignation(designation);
        if (msgTypes != null && msgTypes.isEmpty() == false) {
            statusHandler.info("Successfully retrieved " + msgTypes.size()
                    + " message type(s) for the " + designation
                    + " designation.");
        } else {
            statusHandler.info("Unable to find any message type(s) for the "
                    + designation + " designation.");
        }

        return msgTypes;
    }

    public List<ValidatedMessage> process(Object notificationObject) {
        if (notificationObject instanceof MessageTypeConfigNotification) {
            MessageTypeConfigNotification notification = (MessageTypeConfigNotification) notificationObject;
            statusHandler.info("Processing "
                    + MessageTypeConfigNotification.class + " for id: "
                    + notification.getId());
            MessageType updatedMsgType = this.refreshMessageTypes(notification);
            if (updatedMsgType == null) {
                return Collections.emptyList();
            }
            return this.generateStaticMessages(updatedMsgType);
        } else if (notificationObject instanceof TransmitterLanguageConfigNotification) {
            TransmitterLanguageConfigNotification notification = (TransmitterLanguageConfigNotification) notificationObject;
            statusHandler.info("Processing "
                    + TransmitterLanguageConfigNotification.class
                    + " for key: " + notification.getKey());
            if (notification.getType() == ConfigChangeType.Delete) {
                /*
                 * Do Nothing.
                 */
                return Collections.emptyList();
            }
            TransmitterLanguage language = this.transmitterLanguageDao
                    .getByID(notification.getKey());
            if (language == null) {
                return Collections.emptyList();
            }
            return this.generateStaticMessages(language);
        } else if (notificationObject instanceof TransmitterGroupConfigNotification) {
            TransmitterGroupConfigNotification notification = (TransmitterGroupConfigNotification) notificationObject;
            statusHandler.info("Processing "
                    + TransmitterGroupConfigNotification.class + " for ids: "
                    + notification.getIds());
            if (notification.getType() == ConfigChangeType.Delete) {
                /*
                 * Do Nothing.
                 */
                return Collections.emptyList();
            }
            List<ValidatedMessage> generatedMsgs = new ArrayList<>();
            for (int id : notification.getIds()) {
                TransmitterGroup group = this.transmitterGroupDao.getByID(id);
                if (group == null) {
                    continue;
                }
                List<ValidatedMessage> msgs = this
                        .generateStaticMessages(group);
                if (msgs == null || msgs.isEmpty()) {
                    continue;
                }
                generatedMsgs.addAll(msgs);
            }
            return generatedMsgs;
        } else if (notificationObject instanceof ResetNotification) {
            initializeInternal();
            List<ValidatedMessage> generatedMsgs = new ArrayList<>();
            for (TransmitterGroup group : this.transmitterGroupDao
                    .getEnabledTransmitterGroups()) {
                List<ValidatedMessage> msgs = this
                        .generateStaticMessages(group);
                if (msgs == null || msgs.isEmpty()) {
                    continue;
                }
                generatedMsgs.addAll(msgs);
            }
            return generatedMsgs;

        }

        /*
         * Should never encounter this case.
         */
        return null;
    }

    public boolean checkSkipMessage(Object notificationObject) {
        return (notificationObject instanceof MessageTypeConfigNotification
                || notificationObject instanceof TransmitterLanguageConfigNotification
                || notificationObject instanceof TransmitterGroupConfigNotification || notificationObject instanceof ResetNotification) == false;
    }

    private MessageType refreshMessageTypes(
            MessageTypeConfigNotification notification) {
        synchronized (this.configurationLock) {
            int id = notification.getId();

            /*
             * is it a message type that we are interested in? No reason to
             * verify that the map actually contains the item that is being
             * removed (outright or replaced) because it will just be discarded
             * anyway.
             */
            this.staticMessageTypesMap.remove(id);

            if (notification.getType() == ConfigChangeType.Update) {
                /*
                 * is it a message type that we are interested in?
                 */
                // first, retrieve the current message type from the db.
                MessageType messageType = this.messageTypeDao.getByID(id);
                if (StaticMessageIdentifierUtil.isStaticMsgType(messageType)) {
                    /*
                     * Either a new or updated static message type.
                     */
                    this.staticMessageTypesMap.put(id, messageType);
                    return messageType;
                }

            }

            return null;
        }
    }

    private List<ValidatedMessage> generateStaticMessages(MessageType type) {
        /*
         * In this case, we need to examine all enabled transmitter groups and
         * every language associated with each transmitter group.
         */
        List<TransmitterGroup> groups = this.transmitterGroupDao
                .getEnabledTransmitterGroups();
        if (groups == null || groups.isEmpty()) {
            statusHandler
                    .info("Skipping message generation. No transmitter groups are currently enabled.");
            return Collections.emptyList();
        }

        synchronized (this.configurationLock) {
            List<MessageType> types = new ArrayList<>(1);
            types.add(type);
            List<ValidatedMessage> generatedMessages = new ArrayList<>();

            /* Iterate through the enabled transmitter groups. */
            for (TransmitterGroup group : groups) {
                /*
                 * Retrieve the Transmitter Language(s) associated with each
                 * group.
                 */
                List<TransmitterLanguage> languages = this.transmitterLanguageDao
                        .getLanguagesForTransmitterGroup(group);

                List<ValidatedMessage> msgs = this.generateStaticMessages(
                        group, languages, types);
                if (msgs.isEmpty() == false) {
                    generatedMessages.addAll(msgs);
                }
            }

            return generatedMessages;
        }
    }

    private List<ValidatedMessage> generateStaticMessages(
            TransmitterLanguage language) {
        TransmitterGroup group = language.getTransmitterGroup();
        if (group.getEnabledTransmitters() == null
                || group.getEnabledTransmitters().isEmpty()) {
            statusHandler
                    .info("Skipping message generation. Transmitter group "
                            + group.getId() + " is not currently enabled.");
            return Collections.emptyList();
        }

        List<TransmitterLanguage> languages = new ArrayList<>(1);
        languages.add(language);

        synchronized (this.configurationLock) {
            return this.generateStaticMessages(group, languages,
                    this.staticMessageTypesMap.values());
        }
    }

    private List<ValidatedMessage> generateStaticMessages(TransmitterGroup group) {
        /* Is the group currently enabled? */
        if (group.getEnabledTransmitters() == null
                || group.getEnabledTransmitters().isEmpty()) {
            statusHandler
                    .info("Skipping message generation. Transmitter group "
                            + group.getId() + " is not currently enabled.");
            return Collections.emptyList();
        }

        /* retrieve the transmitter language(s) associated with the group */
        List<TransmitterLanguage> languages = this.transmitterLanguageDao
                .getLanguagesForTransmitterGroup(group);

        synchronized (this.configurationLock) {
            return this.generateStaticMessages(group, languages,
                    this.staticMessageTypesMap.values());
        }
    }

    private List<ValidatedMessage> generateStaticMessages(
            TransmitterGroup group, List<TransmitterLanguage> languages,
            Collection<MessageType> types) {
        if (languages == null || languages.isEmpty()) {
            statusHandler
                    .info("Skipping message generation for transmitter group "
                            + group.getId()
                            + ". No associated transmitter languages were found.");
            return Collections.emptyList();
        }

        if (types.isEmpty()) {
            statusHandler
                    .info("Skipping message generation. No static message types have been defined.");
            return Collections.emptyList();
        }

        final Set<TransmitterGroup> groupSet = new HashSet<TransmitterGroup>(1);
        groupSet.add(group);

        List<ValidatedMessage> generatedMessages = new ArrayList<>();

        for (TransmitterLanguage language : languages) {
            /*
             * Create the static message when necessary.
             */
            for (MessageType messageType : types) {
                String text = StaticMessageIdentifierUtil.getText(messageType,
                        language);
                /*
                 * Do time message fragments need to be generated?
                 * 
                 * Another special case for static messages.
                 */
                if (messageType.getDesignation() == Designation.TimeAnnouncement) {
                    try {
                        BMHTimeZone tz = BMHTimeZone.getTimeZoneByID(group
                                .getTimeZone());
                        if (tz == null) {
                            statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                                    "Failed to find the BMHTimeZone associated with identifier: "
                                            + group.getTimeZone());

                            continue;
                        }

                        this.tmGenerator.process(language.getVoice(),
                                tz.getShortDisplayName());
                    } catch (StaticGenerationException e) {
                        statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                                "Failed to generate the static time message fragments for voice "
                                        + language.getVoice().getVoiceNumber()
                                        + "!", e);
                        continue;
                    }
                }
                ValidatedMessage msg = this.generateStaticMessage(group,
                        language, messageType, text, groupSet);
                if (msg != null) {
                    generatedMessages.add(msg);
                }
            }
        }

        return generatedMessages;
    }

    private ValidatedMessage generateStaticMessage(TransmitterGroup group,
            TransmitterLanguage language, MessageType messageType,
            final String text, final Set<TransmitterGroup> groupSet) {
        if (text == null || text.trim().isEmpty()) {
            StringBuilder logMsg = new StringBuilder(
                    "Skipping message generation for message type ");
            logMsg.append(messageType.getAfosid());
            logMsg.append(" associated with transmitter group ");
            logMsg.append(group.getId());
            logMsg.append(" and language ");
            logMsg.append(language.getLanguage().toString());
            logMsg.append(". No message has been specified.");

            statusHandler.info(logMsg.toString());
            return null;
        }

        /* determine if the static message needs to be created. */
        BroadcastMsg existingMsg = this.broadcastMsgDao.getMessageExistence(
                group, messageType.getAfosid(), language.getLanguage());
        /*
         * Does an associated broadcast message exist. And, if one does exist,
         * is it complete?
         */
        boolean complete = existingMsg != null && existingMsg.isSuccess()
                && existingMsg.getFragments() != null
                && !existingMsg.getFragments().isEmpty();
        if (complete) {
            for (BroadcastFragment fragment : existingMsg.getFragments()) {
                String output = fragment.getOutputName();
                complete &= output != null && !output.isEmpty()
                        && Files.exists(Paths.get(output));
            }
        }

        if (complete == false) {
            /*
             * an associated broadcast message does not exist or is incomplete.
             */
            return this.create(language, messageType, text, groupSet);
        }

        /*
         * If we reached, this point we know that there is already an existing
         * broadcast message. So, it is just a question of whether or not an
         * update to the message will need to be generated.
         */
        // compare the message text, periodicity, and voice.
        if (text.equals(existingMsg.getInputMessage().getContent()) == false
                || messageType.getPeriodicity().equals(
                        existingMsg.getInputMessage().getPeriodicity()) == false) {
            /*
             * The message text has been altered. New audio will need to be
             * generated.
             */
            return this.create(language, messageType, text, groupSet);
        }

        /*
         * No messages will need to be generated.
         */

        StringBuilder logMsg = new StringBuilder(
                "Found existing static message for transmitter group ");
        logMsg.append(group.getId());
        logMsg.append(" with afos id ");
        logMsg.append(messageType.getAfosid());
        logMsg.append(" and language ");
        logMsg.append(language.getLanguage().toString());
        logMsg.append(".");
        statusHandler.info(logMsg.toString());
        return null;
    }

    private ValidatedMessage create(TransmitterLanguage language,
            MessageType messageType, final String text,
            final Set<TransmitterGroup> groupSet) {
        Calendar now = TimeUtil.newCalendar();

        /* create an InputMessage */
        InputMessage inputMsg = new InputMessage();

        String inputMsgName = "StaticMsg-" + messageType.getAfosid();
        // TODO: use annotation scanning to get the max field length
        if (inputMsgName.length() > 40) {
            inputMsgName = inputMsgName.substring(0, 39);
        }
        inputMsg.setName(inputMsgName);

        inputMsg.setLanguage(language.getLanguage());
        inputMsg.setAfosid(messageType.getAfosid());
        inputMsg.setCreationTime(now);
        inputMsg.setEffectiveTime(now);
        inputMsg.setPeriodicity(messageType.getPeriodicity());
        inputMsg.setInterrupt(false);
        inputMsg.setAlertTone(false);
        inputMsg.setNwrsameTone(false);
        inputMsg.setExpirationTime(this.expire);
        inputMsg.setContent(text);
        inputMsg.setValidHeader(true);

        /*
         * Also create the validated message to ensure that the TransmitterGroup
         * is limited only to the transmitter group the static message(s) are
         * being generated for. If the normal flow were to create the validated
         * message, it is possible that additional transmitter groups would be
         * included based on the ugcs associated with the specific transmitter
         * group.
         */
        ValidatedMessage validMsg = new ValidatedMessage();
        validMsg.setInputMessage(inputMsg);
        validMsg.setLdadStatus(LdadStatus.ERROR);
        validMsg.setTransmissionStatus(TransmissionStatus.ACCEPTED);
        validMsg.setTransmitterGroups(groupSet);
        return validMsg;
    }

    /**
     * @return the broadcastMsgDao
     */
    public BroadcastMsgDao getBroadcastMsgDao() {
        return broadcastMsgDao;
    }

    /**
     * @param broadcastMsgDao
     *            the broadcastMsgDao to set
     */
    public void setBroadcastMsgDao(BroadcastMsgDao broadcastMsgDao) {
        this.broadcastMsgDao = broadcastMsgDao;
    }

    /**
     * @return the transmitterGroupDao
     */
    public TransmitterGroupDao getTransmitterGroupDao() {
        return transmitterGroupDao;
    }

    /**
     * @param transmitterGroupDao
     *            the transmitterGroupDao to set
     */
    public void setTransmitterGroupDao(TransmitterGroupDao transmitterGroupDao) {
        this.transmitterGroupDao = transmitterGroupDao;
    }

    /**
     * @return the transmitterLanguageDao
     */
    public TransmitterLanguageDao getTransmitterLanguageDao() {
        return transmitterLanguageDao;
    }

    /**
     * @param transmitterLanguageDao
     *            the transmitterLanguageDao to set
     */
    public void setTransmitterLanguageDao(
            TransmitterLanguageDao transmitterLanguageDao) {
        this.transmitterLanguageDao = transmitterLanguageDao;
    }

    /**
     * @return the messageTypeDao
     */
    public MessageTypeDao getMessageTypeDao() {
        return messageTypeDao;
    }

    /**
     * @param messageTypeDao
     *            the messageTypeDao to set
     */
    public void setMessageTypeDao(MessageTypeDao messageTypeDao) {
        this.messageTypeDao = messageTypeDao;
    }

    private void validateDaos() throws IllegalStateException {
        if (this.messageTypeDao == null) {
            throw new IllegalStateException(
                    "MessageTypeDao has not been set on the StaticMessageGenerator");
        } else if (this.transmitterLanguageDao == null) {
            throw new IllegalStateException(
                    "TransmitterLanguageDao has not been set on the StaticMessageGenerator");
        } else if (this.broadcastMsgDao == null) {
            throw new IllegalStateException(
                    "BroadcastMsgDao has not been set on the StaticMessageGenerator");
        } else if (this.transmitterGroupDao == null) {
            throw new IllegalStateException(
                    "TransmitterGroupDao has not been set on the StaticMessageGenerator");
        }
    }

    private void initialize() {
        this.validateDaos();

        this.initializeInternal();
        
        try {
            this.tmGenerator.initialize();
        } catch (BMHConfigurationException e) {
            statusHandler.fatal(BMH_CATEGORY.TTS_CONFIGURATION_ERROR,
                    "Time Messages Generator initialization failed!", e);
            /* Halt the context startup. */
            throw new RuntimeException(
                    "Time Messages Generator initialization failed!");
        }
    }

    @Override
    public void preStart() {
        this.initialize();
    }

    @Override
    public void postStart() {
        // Do Nothing
    }

    @Override
    public void preStop() {
        // Do Nothing
    }

    @Override
    public void postStop() {
        // Do Nothing
    }
}