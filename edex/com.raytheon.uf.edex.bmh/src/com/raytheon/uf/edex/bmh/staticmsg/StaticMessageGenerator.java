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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.notify.config.AbstractDictionaryWordChangeNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.MessageActivationNotification;
import com.raytheon.uf.common.bmh.notify.config.MessageTypeConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.LanguageDictionaryConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.bmh.notify.config.StaticMsgTypeConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupIdentifier;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.bmh.schemas.ssml.Prosody;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SpeechRateFormatter;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.bmh.trace.TraceableId;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.StaticMessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.bmh.tts.NeoSpeechConstants;
import com.raytheon.uf.edex.core.IContextStateProcessor;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterLocker;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

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
 * Nov 3, 2014  3759       bkowal      Generate both dst and non-dst timezones.
 * Nov 5, 2014  3630       bkowal      Support maintenance audio generation.
 * Jan 19, 2015 4011       bkowal      Support transmitter language removal.
 * Jan 20, 2015 4011       bkowal      Static msg deactivation fixes.
 * Jan 22, 2015 4017       bkowal      Use {@link TransmitterGroupIdentifier}.
 * Feb 10, 2015 4085       bkowal      Updated how static message types are
 *                                     retrieved.
 * Feb 11, 2015 4116       bkowal      Include the name of the destination in the
 *                                     static message name.
 * Feb 19, 2015 4142       bkowal      Take the speech rate into account when determining
 *                                     whether or not a static message needs to be
 *                                     regenerated.
 * Mar 05, 2015 4222       bkowal      Use null for messages that never expire.
 * Mar 13, 2015 4213       bkowal      Support {@link StaticMessageType}s.
 * Mar 25, 2015 4213       bkowal      Update the generated messages in response to
 *                                     {@link Program} and {@link Suite} modifications.
 * Mar 26, 2015 4213       bkowal      Reuse existing usable static {@link InputMessage}s
 *                                     when only the contents or periodicity has changed.
 * Mar 27, 2015 4314       bkowal      {@link TimeMessagesGenerator} updates.
 * Mar 30, 2015 4314       bkowal      Cluster lock static message generation to ensure
 *                                     multiple cluster members do not generate messages.
 * Apr 07, 2015 4293       bkowal      Persist the generated static messages while within
 *                                     the cluster lock.
 * Apr 16, 2015 3809       bkowal      The time generator is now initialized by Spring.
 * Apr 27, 2015 4397       bkowal      Set the {@link InputMessage} update date.
 * Apr 29, 2015 4450       bkowal      Re-activate any inactive time messages that were
 *                                     previously generated successfully.
 * May 08, 2015 4465       bkowal      Generated time zone audio is no longer 
 *                                     Transmitter specific.
 * May 11, 2015 4476       bkowal      Inline replace {@link InputMessage}s that do not have an associated
 *                                     {@link BroadcastMsg} or are associated with failure.
 * May 20, 2015 4490       bkowal      Fixes for {@link TraceableId}.
 * May 28, 2015 4429       rjpeter     Add ITraceable.
 * Jun 11, 2015 4490       bkowal      {@link AlignmentTestGenerator} is now initialized by Spring.
 * Oct 06, 2015 4904       bkowal      Check the volume when determining if a static message will
 *                                     need to be regenerated.
 * Dec 03, 2015 5159       bkowal      Messages will now be regenerated when dictionary rules apply
 *                                     or no longer apply.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StaticMessageGenerator implements IContextStateProcessor {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(StaticMessageGenerator.class);

    private final boolean operational;

    private final TimeMessagesGenerator tmGenerator;

    private final AlignmentTestGenerator alignmentTestGenerator;

    private ProgramDao programDao;

    private ValidatedMessageDao validatedMessageDao;

    private BroadcastMsgDao broadcastMsgDao;

    private TransmitterGroupDao transmitterGroupDao;

    private TransmitterLanguageDao transmitterLanguageDao;

    private MessageTypeDao messageTypeDao;

    private InputMessageDao inputMessageDao;

    private StaticMessageTypeDao staticMessageTypeDao;

    private final ClusterLocker locker;

    private final Object configurationLock = new Object();

    /**
     * 
     */
    public StaticMessageGenerator(final TimeMessagesGenerator tmGenerator,
            final AlignmentTestGenerator alignmentTestGenerator,
            final boolean operational) {
        this.tmGenerator = tmGenerator;
        this.alignmentTestGenerator = alignmentTestGenerator;
        this.operational = operational;
        locker = new ClusterLocker(AbstractBMHDao.getDatabaseName(operational));
    }

    public List<TraceableId> process(Object notificationObject) {
        if (notificationObject instanceof MessageTypeConfigNotification) {
            MessageTypeConfigNotification notification = (MessageTypeConfigNotification) notificationObject;
            statusHandler.info("Processing "
                    + MessageTypeConfigNotification.class + " for id: "
                    + notification.getId());
            MessageType updatedMsgType = null;
            return this.generateStaticMessages(updatedMsgType, notification);
        } else if (notificationObject instanceof TransmitterLanguageConfigNotification) {
            TransmitterLanguageConfigNotification notification = (TransmitterLanguageConfigNotification) notificationObject;
            statusHandler.info("Processing "
                    + TransmitterLanguageConfigNotification.class
                    + " for key: " + notification.getKey());
            if (notification.getType() == ConfigChangeType.Delete) {
                synchronized (configurationLock) {
                    try {
                        this.deactivateTransmitterLanguageStaticMsgs(
                                notification.getTransmitterGroup(),
                                notification.getKey().getLanguage(),
                                notification.getStaticAfosIds(), notification);
                    } catch (Exception e) {
                        StringBuilder sb = new StringBuilder(
                                "Failed to purge the static message(s) for the deleted Language: ");
                        sb.append(notification.getKey().getLanguage()
                                .toString());
                        sb.append(" originally associated with Transmitter: ");
                        sb.append(notification.getTransmitterGroup().getName())
                                .append(".");
                        statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                                sb.toString(), e);
                    }
                }
                return Collections.emptyList();
            }
            TransmitterLanguage language = this.transmitterLanguageDao
                    .getByID(notification.getKey());
            if (language == null) {
                return Collections.emptyList();
            }
            return this.generateStaticMessages(language, notification);
        } else if (notificationObject instanceof TransmitterGroupConfigNotification) {
            TransmitterGroupConfigNotification notification = (TransmitterGroupConfigNotification) notificationObject;
            statusHandler.info("Processing "
                    + TransmitterGroupConfigNotification.class
                    + " for identifiers: " + notification.toString());
            if (notification.getType() == ConfigChangeType.Delete) {
                /*
                 * Do Nothing.
                 */
                return Collections.emptyList();
            }
            List<TraceableId> generatedMsgs = new ArrayList<>();
            for (TransmitterGroupIdentifier identifier : notification
                    .getIdentifiers()) {
                TransmitterGroup group = this.transmitterGroupDao
                        .getByID(identifier.getId());
                if (group == null) {
                    continue;
                }
                List<TraceableId> msgs = this.generateStaticMessages(group,
                        notification);
                if (msgs == null || msgs.isEmpty()) {
                    continue;
                }
                generatedMsgs.addAll(msgs);
            }
            return generatedMsgs;
        } else if (notificationObject instanceof ResetNotification
                || notificationObject instanceof LanguageDictionaryConfigNotification) {
            List<TraceableId> generatedMsgs = new ArrayList<>();
            for (TransmitterGroup group : this.transmitterGroupDao
                    .getEnabledTransmitterGroups()) {
                List<TraceableId> msgs = this.generateStaticMessages(group,
                        (ITraceable) notificationObject);
                if (msgs == null || msgs.isEmpty()) {
                    continue;
                }
                generatedMsgs.addAll(msgs);
            }
            return generatedMsgs;
        } else if (notificationObject instanceof StaticMsgTypeConfigNotification) {
            StaticMsgTypeConfigNotification notification = (StaticMsgTypeConfigNotification) notificationObject;

            if (notification.getType() == ConfigChangeType.Delete) {
                Set<String> staticAfosIds = new HashSet<>(1, 1.0f);
                staticAfosIds.add(notification.getAfosId());

                try {
                    this.deactivateTransmitterLanguageStaticMsgs(
                            notification.getTransmitterGroup(),
                            notification.getLanguage(), staticAfosIds,
                            notification);
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder(
                            "Failed to deactive the static message for:  ");
                    sb.append(notification.toString()).append(".");

                    statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                            sb.toString(), e);
                }
            } else if (notification.getType() == ConfigChangeType.Update) {
                final TransmitterGroup group = notification
                        .getTransmitterGroup();
                final Set<TransmitterGroup> groupSet = new HashSet<TransmitterGroup>(
                        1);
                groupSet.add(group);

                /*
                 * Retrieve the associated static message type.
                 */
                StaticMessageType staticMsgType = this.staticMessageTypeDao
                        .getStaticForMsgTypeAndTransmittergroup(
                                notification.getAfosId(), group);
                if (staticMsgType == null) {
                    StringBuilder sb = new StringBuilder(
                            "Unable to find a Static Message Type for Transmitter Group ");
                    sb.append(group.getName()).append(" and Message Type ")
                            .append(notification.getAfosId())
                            .append(" for notification: ");
                    sb.append(notification.toString()).append(".");

                    statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                            sb.toString());

                    return Collections.emptyList();
                }

                /*
                 * Generate a validated message (if applicable).
                 */
                TraceableId traceableId = this.generateStaticMessage(
                        staticMsgType.getTransmitterLanguage(), group,
                        staticMsgType, groupSet, notification);
                if (traceableId == null) {
                    return Collections.emptyList();
                }

                List<TraceableId> messageList = new ArrayList<>(1);
                messageList.add(traceableId);

                return messageList;
            }
        } else if (notificationObject instanceof SuiteConfigNotification) {
            SuiteConfigNotification notification = (SuiteConfigNotification) notificationObject;

            if (notification.getType() == ConfigChangeType.Update) {
                /*
                 * Need to determine which ENABLED Transmitter Group(s) the
                 * suite has been assigned to.
                 */
                return this.generateStaticMessage(this.programDao
                        .getSuiteEnabledGroups(notification.getId()),
                        notification);
            } else if (notification.getType() == ConfigChangeType.Delete) {
                this.generateStaticMessage(
                        notification.getAssociatedEnabledTransmitterGroups(),
                        notification);

                /*
                 * Messages will never be created in this case. Messages that
                 * are no longer assigned to a suite that is associated with the
                 * Transmitter Group will be deactivated.
                 */
                return Collections.emptyList();
            }
        } else if (notificationObject instanceof ProgramConfigNotification) {
            ProgramConfigNotification notification = (ProgramConfigNotification) notificationObject;

            if (notification.getType() == ConfigChangeType.Update) {
                return this.generateStaticMessage(this.programDao
                        .getProgramEnabledGroups(notification.getId()),
                        notification);
            } else if (notification.getType() == ConfigChangeType.Delete) {
                /*
                 * TODO? a Transmitter must be disabled before a Program can be
                 * deleted so there would not be any enabled associated
                 * Transmitters. All message de-activation would be initiated by
                 * the Transmitter Group Config notification.
                 */
            }
        }

        /*
         * Should never encounter this case.
         */
        return null;
    }

    private void deactivateTransmitterLanguageStaticMsgs(
            TransmitterGroup transmitterGroup, Language language,
            Set<String> staticAfosIds, ITraceable traceable) throws Exception {
        if (staticAfosIds.isEmpty()) {
            return;
        }

        List<InputMessage> disabledStaticMessages = new ArrayList<>();
        for (String afosId : staticAfosIds) {
            /**
             * Iterate through all static {@link MessageType}s and retrieve any
             * {@link BroadcastMsg}s associated with the
             * {@link TransmitterGroup} and {@link Language}.
             */
            BroadcastMsg msg = this.broadcastMsgDao.getMessageExistence(
                    transmitterGroup, afosId, language);
            if (msg == null) {
                continue;
            }
            InputMessage im = msg.getInputMessage();
            if (Boolean.FALSE.equals(im.getActive())) {
                /**
                 * The message is already inactive.
                 */
                continue;
            }
            StringBuilder sb = new StringBuilder(
                    "Disabling static message [id=");
            sb.append(im.getId());
            sb.append(", name=").append(im.getName());
            sb.append(", afosid=").append(im.getAfosid())
                    .append("] for Language: ");
            sb.append(language.toString()).append(" and Transmitter: ");
            sb.append(transmitterGroup.getName()).append(".");
            statusHandler.info(sb.toString());
            disabledStaticMessages.add(im);
            im.setActive(false);
        }

        if (disabledStaticMessages.isEmpty()) {
            /*
             * There is nothing to disable. So, playlists do not need to be
             * re-generated.
             */
            return;
        }

        /**
         * Attempt to persist all {@link InputMessage} changes at once.
         */
        this.inputMessageDao.persistAll(disabledStaticMessages);
        /**
         * Attempt to trigger a playlist re-generation without the
         * {@link InputMessage}s that have just been disabled.
         */
        BmhMessageProducer.sendConfigMessage(new MessageActivationNotification(
                disabledStaticMessages, false, traceable), this.operational);
    }

    public boolean checkSkipMessage(Object notificationObject) {
        return (notificationObject instanceof MessageTypeConfigNotification
                || notificationObject instanceof TransmitterLanguageConfigNotification
                || notificationObject instanceof TransmitterGroupConfigNotification
                || notificationObject instanceof ResetNotification
                || notificationObject instanceof StaticMsgTypeConfigNotification
                || notificationObject instanceof SuiteConfigNotification
                || notificationObject instanceof ProgramConfigNotification || notificationObject instanceof LanguageDictionaryConfigNotification) == false;
    }

    private List<TraceableId> generateStaticMessage(
            List<TransmitterGroup> transmitterGroups, ITraceable traceable) {
        if (transmitterGroups.isEmpty()) {
            return Collections.emptyList();
        }

        List<TraceableId> generatedMsgs = new ArrayList<>();
        for (TransmitterGroup group : transmitterGroups) {
            List<TraceableId> msgs = this.generateStaticMessages(group,
                    traceable);
            if (msgs == null || msgs.isEmpty()) {
                continue;
            }
            generatedMsgs.addAll(msgs);
        }

        return generatedMsgs;
    }

    private List<TraceableId> generateStaticMessages(MessageType type,
            ITraceable traceable) {
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

        synchronized (configurationLock) {
            List<MessageType> types = new ArrayList<>(1);
            types.add(type);
            List<TraceableId> generatedMessages = new ArrayList<>();

            /* Iterate through the enabled transmitter groups. */
            for (TransmitterGroup group : groups) {
                /*
                 * Retrieve the Transmitter Language(s) associated with each
                 * group.
                 */
                List<TransmitterLanguage> languages = this.transmitterLanguageDao
                        .getLanguagesForTransmitterGroup(group);

                List<TraceableId> msgs = this.generateStaticMessages(group,
                        languages, traceable);
                if (msgs.isEmpty() == false) {
                    generatedMessages.addAll(msgs);
                }
            }

            return generatedMessages;
        }
    }

    private List<TraceableId> generateStaticMessages(
            TransmitterLanguage language, ITraceable traceable) {
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

        synchronized (configurationLock) {
            return this.generateStaticMessages(group, languages, traceable);
        }
    }

    private List<TraceableId> generateStaticMessages(TransmitterGroup group,
            ITraceable traceable) {
        /* Is the group currently enabled? */
        if (CollectionUtils.isEmpty(group.getEnabledTransmitters())) {
            statusHandler
                    .info("Skipping message generation. Transmitter group "
                            + group.getId()
                            + " is not currently enabled. Generating / Verifying the existence of maintenance messages ...");
            try {
                this.alignmentTestGenerator.process();
            } catch (StaticGenerationException | BMHConfigurationException e) {
                statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                        "Failed to generate the maintenance message audio.", e);
            }
            return Collections.emptyList();
        }

        /* retrieve the transmitter language(s) associated with the group */
        List<TransmitterLanguage> languages = this.transmitterLanguageDao
                .getLanguagesForTransmitterGroup(group);

        synchronized (configurationLock) {
            return this.generateStaticMessages(group, languages, traceable);
        }
    }

    private List<TraceableId> generateStaticMessages(TransmitterGroup group,
            List<TransmitterLanguage> languages, ITraceable traceable) {
        if (languages == null || languages.isEmpty()) {
            statusHandler
                    .info("Skipping message generation for transmitter group "
                            + group.getId()
                            + ". No associated transmitter languages were found.");
            return Collections.emptyList();
        }

        final Set<TransmitterGroup> groupSet = new HashSet<TransmitterGroup>(1);
        groupSet.add(group);

        List<TraceableId> generatedMessages = new ArrayList<>();

        for (TransmitterLanguage language : languages) {
            /*
             * Have static message types been defined.
             */
            if (language.getStaticMessageTypes().isEmpty()) {
                continue;
            }

            /*
             * Create the static message when necessary.
             */
            for (StaticMessageType staticMsgType : language
                    .getStaticMessageTypes()) {
                TraceableId traceableId = this.generateStaticMessage(language,
                        group, staticMsgType, groupSet, traceable);
                if (traceableId != null) {
                    generatedMessages.add(traceableId);
                }
            }
        }

        return generatedMessages;
    }

    private TraceableId generateStaticMessage(TransmitterLanguage language,
            TransmitterGroup group, StaticMessageType staticMsgType,
            Set<TransmitterGroup> groupSet, ITraceable traceable) {
        String text = StaticMessageIdentifierUtil.getText(staticMsgType);

        if (staticMsgType.getMsgTypeSummary().getDesignation() == Designation.TimeAnnouncement) {
            try {
                this.tmGenerator.process(language.getVoice(),
                        language.getSpeechRate());
            } catch (StaticGenerationException e) {
                statusHandler
                        .error(BMH_CATEGORY.STATIC_MSG_ERROR,
                                "Failed to generate the static time message fragments for voice "
                                        + language.getVoice().getVoiceNumber()
                                        + "!", e);
                return null;
            }
        }

        /*
         * Ensure that a static message is only generated by a single cluster
         * member.
         */
        ClusterTask ct = null;
        do {
            ct = locker
                    .lock("static-message-gen",
                            this.getStaticMsgName(groupSet, staticMsgType),
                            30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        try {
            return this.generateStaticMessage(group, language, staticMsgType,
                    text, groupSet, traceable);
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
        }
    }

    private TraceableId generateStaticMessage(TransmitterGroup group,
            TransmitterLanguage language, StaticMessageType staticMsgType,
            final String text, final Set<TransmitterGroup> groupSet,
            ITraceable traceable) {
        if (text == null || text.trim().isEmpty()) {
            StringBuilder logMsg = new StringBuilder(
                    "Skipping message generation for message type ");
            logMsg.append(staticMsgType.getMsgTypeSummary().getAfosid());
            logMsg.append(" associated with transmitter group ");
            logMsg.append(group.getId());
            logMsg.append(" and language ");
            logMsg.append(language.getLanguage().toString());
            logMsg.append(". No message has been specified.");

            statusHandler.info(logMsg.toString());
            return null;
        }

        /*
         * Determine if the static message type can even be scheduled for the
         * associated Transmitter Group.
         */
        if (this.programDao.verifyMsgTypeHandledByTrxGroup(group, staticMsgType
                .getMsgTypeSummary().getAfosid()) == false) {
            StringBuilder logMsg = new StringBuilder(
                    "Skipping message generation for message type ");
            logMsg.append(staticMsgType.getMsgTypeSummary().getAfosid());
            logMsg.append(" associated with transmitter group ");
            logMsg.append(group.getId());
            logMsg.append(" and language ");
            logMsg.append(language.getLanguage().toString());
            logMsg.append(". Message type is not part of a suite associated with the Transmitter Group.");

            statusHandler.info(logMsg.toString());

            /*
             * Attempt to remove / deactivate any existing messages for the
             * transmitter group and message type. Handles message type removed
             * from suite scenario.
             */
            Set<String> staticAfosIds = new HashSet<>(1, 1.0f);
            staticAfosIds.add(staticMsgType.getMsgTypeSummary().getAfosid());
            try {
                this.deactivateTransmitterLanguageStaticMsgs(group,
                        staticMsgType.getTransmitterLanguage().getLanguage(),
                        staticAfosIds, traceable);
            } catch (Exception e) {
                logMsg = new StringBuilder(
                        "Failed to deactive any existing message(s) associated with transmitter group ");
                logMsg.append(group.getId()).append(" and message type ")
                        .append(staticMsgType.getMsgTypeSummary().getAfosid())
                        .append(".");

                statusHandler.warn(BMH_CATEGORY.STATIC_MSG_ERROR,
                        logMsg.toString());
            }

            return null;
        }

        /* determine if the static message needs to be created. */
        BroadcastMsg existingMsg = this.broadcastMsgDao.getMessageExistence(
                group, staticMsgType.getMsgTypeSummary().getAfosid(),
                language.getLanguage());
        /*
         * Does an associated broadcast message exist. And, if one does exist,
         * is it complete?
         */
        boolean complete = existingMsg != null && existingMsg.isSuccess();
        /*
         * technically, this flag should really be set during the content
         * verification instead of during the test for completeness; however, we
         * do not want to have to iterate through the broadcast fragments a
         * second time.
         */
        boolean oneSpeechRateMatch = false;
        boolean oneVolumeMatch = false;
        if (complete) {
            /*
             * so, that we will not need to extract the numeric speech rate from
             * the prosody speech rate String.
             */
            final String formattedSpeechRate = SpeechRateFormatter
                    .formatSpeechRate(language.getSpeechRate());
            for (BroadcastFragment fragment : existingMsg
                    .getLatestBroadcastContents().getFragments()) {
                String output = fragment.getOutputName();
                complete &= output != null && !output.isEmpty()
                        && Files.exists(Paths.get(output));

                if (complete
                        && (oneSpeechRateMatch == false || oneVolumeMatch == false)
                        && fragment.getSsml() != null
                        && fragment.getSsml().isEmpty() == false) {
                    /*
                     * based on the currently limited usage of fragments, we are
                     * always guaranteed to have at least one fragment that
                     * should match the speech rate at a given point in time.
                     */
                    final SSMLDocument ssmlDocument;
                    try {
                        ssmlDocument = SSMLDocument
                                .fromSSML(fragment.getSsml());
                    } catch (SSMLConversionException e) {
                        // ignore for now; this JAXB transformation is not
                        // critical.
                        continue;
                    }

                    if (ssmlDocument == null
                            || ssmlDocument.getRootTag().getContent().isEmpty()) {
                        continue;
                    }

                    /*
                     * check for a prosody tag as the first child of the root
                     * speak tag.
                     */
                    JAXBElement<?> element = (JAXBElement<?>) ssmlDocument
                            .getRootTag().getContent().get(0);
                    if (element.getValue() instanceof Prosody) {
                        /*
                         * compare the speech rate
                         */
                        Prosody prosody = (Prosody) element.getValue();
                        oneSpeechRateMatch = formattedSpeechRate.equals(prosody
                                .getRate());
                        /*
                         * compare the volume
                         */
                        oneVolumeMatch = NeoSpeechConstants.getVolume().equals(
                                prosody.getVolume());
                    }
                }
            }
        }

        if (complete == false) {
            /*
             * an associated broadcast message does not exist or is incomplete.
             */
            return this.createOrupdate(language, staticMsgType, text, groupSet,
                    traceable);
        }

        /*
         * If we reached, this point we know that there is already an existing
         * broadcast message. So, it is just a question of whether or not an
         * update to the message will need to be generated.
         */
        // compare the message text, periodicity, and voice.
        final String mtPeriodicity = staticMsgType.getPeriodicity();
        final String msgPeriodicity = existingMsg.getInputMessage()
                .getPeriodicity();
        boolean equivalentPeriodicity = (mtPeriodicity == null && msgPeriodicity == null)
                || (mtPeriodicity != null && msgPeriodicity != null && mtPeriodicity
                        .equals(msgPeriodicity));

        final boolean messageActivationChanged = Boolean.TRUE
                .equals(existingMsg.getInputMessage().getActive()) == false;
        existingMsg.getInputMessage().setActive(Boolean.TRUE);

        /*
         * In addition to message text, if the potential regeneration was
         * triggered by a dictionary change, we will also need to determine if
         * the message contains any of the dictionary words that were altered.
         */
        boolean containsUpdatedDictionaryWord = false;
        if (traceable instanceof AbstractDictionaryWordChangeNotification) {
            AbstractDictionaryWordChangeNotification updatedWordsNotification = (AbstractDictionaryWordChangeNotification) traceable;
            if (CollectionUtils.isEmpty(updatedWordsNotification
                    .getUpdatedWords()) == false) {
                String compareContent = text.toLowerCase();
                for (String wordToFind : updatedWordsNotification
                        .getUpdatedWords()) {
                    if (compareContent.contains(wordToFind.toLowerCase())) {
                        containsUpdatedDictionaryWord = true;
                        break;
                    }
                }
            }
        }

        if (text.equals(existingMsg.getInputMessage().getContent()) == false
                || containsUpdatedDictionaryWord
                || equivalentPeriodicity == false
                || oneSpeechRateMatch == false || oneVolumeMatch == false) {
            /*
             * update the original message when one or more of the following
             * conditions apply: the content has been altered, the rate of
             * speech has been altered, and/or the periodicity has been altered.
             */
            StringBuilder logMsg = new StringBuilder(
                    "Updating existing static message for transmitter group ");
            logMsg.append(group.getId());
            logMsg.append(" with afos id ");
            logMsg.append(staticMsgType.getMsgTypeSummary().getAfosid());
            logMsg.append(" and language ");
            logMsg.append(language.getLanguage().toString());
            logMsg.append(".");
            statusHandler.info(logMsg.toString());
            return this.update(existingMsg, staticMsgType, text, traceable);
        }

        /*
         * Determine if the message had to be re-activated.
         */
        if (messageActivationChanged) {
            StringBuilder logMsg = new StringBuilder(
                    "Activating existing static message for transmitter group ");
            logMsg.append(group.getId());
            logMsg.append(" with afos id ");
            logMsg.append(staticMsgType.getMsgTypeSummary().getAfosid());
            logMsg.append(" and language ");
            logMsg.append(language.getLanguage().toString());
            logMsg.append(".");
            statusHandler.info(logMsg.toString());

            this.inputMessageDao.persist(existingMsg.getInputMessage());

            List<InputMessage> activatedMessageList = new ArrayList<>(1);
            activatedMessageList.add(existingMsg.getInputMessage());
            try {
                BmhMessageProducer.sendConfigMessage(
                        new MessageActivationNotification(activatedMessageList,
                                true, traceable), this.operational);
            } catch (Exception e) {
                statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR,
                        "Failed to notify subscribers that message: "
                                + existingMsg.getInputMessage().toString()
                                + " has been activated.", e);
            }
        }

        /*
         * No messages will need to be generated.
         */

        StringBuilder logMsg = new StringBuilder(
                "Found existing static message for transmitter group ");
        logMsg.append(group.getId());
        logMsg.append(" with afos id ");
        logMsg.append(staticMsgType.getMsgTypeSummary().getAfosid());
        logMsg.append(" and language ");
        logMsg.append(language.getLanguage().toString());
        logMsg.append(".");
        statusHandler.info(logMsg.toString());
        return null;
    }

    private String getStaticMsgName(final Set<TransmitterGroup> groupSet,
            StaticMessageType staticMsgType) {
        StringBuilder sb = new StringBuilder("StaticMsg-");
        /**
         * get the transmitter mnemonic. the {@link Set} is guaranteed to only
         * include one {@link TransmitterGroup} as of 02/11/2015.
         */
        sb.append(groupSet.iterator().next().getName());
        sb.append("-").append(staticMsgType.getMsgTypeSummary().getAfosid());

        return sb.toString();
    }

    private TraceableId createOrupdate(TransmitterLanguage language,
            StaticMessageType staticMsgType, final String text,
            final Set<TransmitterGroup> groupSet, ITraceable traceable) {
        Calendar now = TimeUtil.newCalendar();

        String inputMsgName = this.getStaticMsgName(groupSet, staticMsgType);
        // TODO: use annotation scanning to get the max field length
        if (inputMsgName.length() > 40) {
            inputMsgName = inputMsgName.substring(0, 39);
        }

        String logText = StringUtils.EMPTY;
        InputMessage inputMsg = null;
        ValidatedMessage validMsg = null;

        List<InputMessage> existingInputMessages = this.inputMessageDao
                .getAllWithAfosIdAndName(staticMsgType.getMsgTypeSummary()
                        .getAfosid(), inputMsgName);
        if (existingInputMessages.isEmpty()) {
            inputMsg = new InputMessage();
            validMsg = new ValidatedMessage();
            logText = "Creating ";
        } else {
            /*
             * With the implementation of inline message replace, there should
             * always only be one when there is an existing input message.
             */
            inputMsg = existingInputMessages.iterator().next();
            validMsg = this.validatedMessageDao
                    .getValidatedMsgByInputMsg(inputMsg);
            if (validMsg == null) {
                validMsg = new ValidatedMessage();
            }
            logText = "Updating ";
        }
        inputMsg.setName(inputMsgName);

        inputMsg.setLanguage(language.getLanguage());
        inputMsg.setAfosid(staticMsgType.getMsgTypeSummary().getAfosid());
        inputMsg.setCreationTime(now);
        inputMsg.setEffectiveTime(now);
        inputMsg.setPeriodicity(staticMsgType.getPeriodicity());
        inputMsg.setInterrupt(false);
        inputMsg.setAlertTone(false);
        inputMsg.setNwrsameTone(false);
        inputMsg.setExpirationTime(null);
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
        validMsg.setInputMessage(inputMsg);
        validMsg.setLdadStatus(LdadStatus.ERROR);
        validMsg.setTransmissionStatus(TransmissionStatus.ACCEPTED);
        validMsg.setTransmitterGroups(groupSet);

        StringBuilder logMsg = new StringBuilder(logText
                + " static message for transmitter group ");
        logMsg.append(groupSet.iterator().next().getName());
        logMsg.append(" with afos id ");
        logMsg.append(staticMsgType.getMsgTypeSummary().getAfosid());
        logMsg.append(" and language ");
        logMsg.append(language.getLanguage().toString());
        logMsg.append(".");
        statusHandler.info(logMsg.toString());

        this.validatedMessageDao.persistCascade(validMsg);

        return new TraceableId(validMsg.getId(), traceable);
    }

    private TraceableId update(BroadcastMsg existingMsg,
            StaticMessageType staticMsgType, final String text,
            ITraceable traceable) {
        InputMessage inputMsg = existingMsg.getInputMessage();
        ValidatedMessage validMsg = this.validatedMessageDao
                .getValidatedMsgByInputMsg(inputMsg);
        if (validMsg == null) {
            StringBuilder sb = new StringBuilder(
                    "Failed to update existing Broadcast Message: ");
            sb.append(existingMsg.getId()).append(
                    ". Unable to find the associated Validated Message.");

            statusHandler.error(BMH_CATEGORY.STATIC_MSG_ERROR, sb.toString());
            return null;
        }

        /*
         * In the case of a static message, the only fields that can be changed
         * are the text and the periodicity.
         */
        inputMsg.setPeriodicity(staticMsgType.getPeriodicity());
        inputMsg.setContent(text);

        /*
         * Ensure that the message has not been disabled.
         */
        inputMsg.setActive(Boolean.TRUE);
        validMsg.setInputMessage(inputMsg);

        this.validatedMessageDao.persistCascade(validMsg);

        return new TraceableId(validMsg.getId(), traceable);
    }

    /**
     * @return the programDao
     */
    public ProgramDao getProgramDao() {
        return programDao;
    }

    /**
     * @param programDao
     *            the programDao to set
     */
    public void setProgramDao(ProgramDao programDao) {
        this.programDao = programDao;
    }

    public ValidatedMessageDao getValidatedMessageDao() {
        return validatedMessageDao;
    }

    public void setValidatedMessageDao(ValidatedMessageDao validatedMessageDao) {
        this.validatedMessageDao = validatedMessageDao;
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

    /**
     * @return the inputMessageDao
     */
    public InputMessageDao getInputMessageDao() {
        return inputMessageDao;
    }

    /**
     * @param inputMessageDao
     *            the inputMessageDao to set
     */
    public void setInputMessageDao(InputMessageDao inputMessageDao) {
        this.inputMessageDao = inputMessageDao;
    }

    /**
     * @return the staticMessageTypeDao
     */
    public StaticMessageTypeDao getStaticMessageTypeDao() {
        return staticMessageTypeDao;
    }

    /**
     * @param staticMessageTypeDao
     *            the staticMessageTypeDao to set
     */
    public void setStaticMessageTypeDao(
            StaticMessageTypeDao staticMessageTypeDao) {
        this.staticMessageTypeDao = staticMessageTypeDao;
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
        } else if (this.inputMessageDao == null) {
            throw new IllegalStateException(
                    "InputMessageDao has not been set on the StaticMessageGenerator");
        } else if (this.staticMessageTypeDao == null) {
            throw new IllegalStateException(
                    "StaticMessageTypeDao has not been set on the StaticMessageGenerator");
        } else if (this.programDao == null) {
            throw new IllegalStateException(
                    "ProgramDao has not been set on the StaticMessageGenerator");
        } else if (this.validatedMessageDao == null) {
            throw new IllegalStateException(
                    "ValidatedMessageDao has not been set on the StaticMessageGenerator");
        }
    }

    private void initialize() {
        this.validateDaos();

        try {
            this.alignmentTestGenerator.process();
        } catch (StaticGenerationException | BMHConfigurationException e) {
            statusHandler.fatal(BMH_CATEGORY.STATIC_MSG_ERROR,
                    "Maintenance message generation has failed!", e);
            /* Halt the context startup. */
            throw new RuntimeException(
                    "Maintenance message generation has failed!");
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