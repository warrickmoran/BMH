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
package com.raytheon.uf.edex.bmh.playlist;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.MessageActivationNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.same.SAMEOriginatorMapper;
import com.raytheon.uf.common.bmh.same.SAMEStateCodes;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.edex.core.IContextStateProcessor;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterLocker;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

/**
 * 
 * Manages playlists.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 07, 2014  3285     bsteffen    Initial creation
 * Aug 26, 2014  3554     bsteffen    Add more logging, change suite field for dac
 * Sep 03, 2014  3554     bsteffen    Respond to config notifications.
 * Sep 2, 2014   3568     bkowal      Handle static messages. Handle the
 *                                    case when no ugc codes are specified.
 * Sep 10, 2014  3391     bsteffen    Generate SAME tones only for applicable
 *                                    transmitter areas.
 * Sep 09, 2014  2585     bsteffen    Implement MAT
 * Sep 12, 2014  3588     bsteffen    Support audio fragments.
 * Sep 16, 2014  3587     bkowal      No longer check the {@link SuiteMessage} for triggers.
 *                                    Updated to use the {@link ProgramSuite}.
 * Sep 23, 2014  3485     bsteffen    Move queue naming logic to PlaylistUpdateNotification
 * Sep 26, 2014  3589     dgilling    Implement administrative suite change.
 * Oct 03, 2014  3485     bsteffen    Handle InputMessage with no area codes.
 * Oct 07, 2014  3589     dgilling    Fix refresh of GENERAL suites on startup.
 * Oct 08, 2014  3687     bsteffen    Remove ProgramTrigger.
 * Oct 10, 2014  3666     bsteffen    Do not persist disabled transmitters.
 * Oct 13, 2014  3654     rjpeter     Updated to use MessageTypeSummary.
 * Oct 28, 2014  3617     dgilling    Support tone blackout period.
 * Oct 31, 2014  3778     bsteffen    Do not play inactive messages.
 * Nov 04, 2014  3781     dgilling    Use MessageType configured SAME transmitters 
 *                                    when generating SAME tones.
 * Nov 13, 2014  3717     bsteffen    Do not persist forced.
 * Nov 17, 2014  3793     bsteffen    Add same transmitters to input message.
 * Nov 17, 2014  3827     bsteffen    Fix merging of arealess messages
 * Dec 08, 2014  3878     bkowal      Set the static flag on the {@link DacPlaylistMessage}.
 * Dec 08, 2014  3651     bkowal      Message logging preparation
 * Dec 08, 2014  3864     bsteffen    Shift some logic into the playlist.
 * Dec 11, 2014  3651     bkowal      Use {@link IMessageLogger} to log message activity.
 * Dec 16, 2014  3753     bsteffen    Report failure when suites won't force.
 * Jan 05, 2015  3913     bsteffen    Handle future replacements.
 * Jan 05, 2015  3651     bkowal      Use {@link IMessageLogger} to log message errors.
 * Jan 12, 2015  3968     bkowal      Include the confirmation flag in the {@link DacPlaylistMessage}.
 * Jan 14, 2015  3969     bkowal      Indicate whether a message is a watch or a warning
 *                                    in the {@link DacPlaylistMessage}.
 * Jan 19, 2015  4011     bkowal      Use the updated version of
 *                                    {@link MessageActivationNotification}.
 * Jan 20, 2015  4010     bkowal      Use areas associated with selected transmitters
 *                                    when building SAME tones.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PlaylistManager implements IContextStateProcessor {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(PlaylistManager.class);

    private final boolean operational;

    private final Path playlistDir;

    private final ClusterLocker locker;

    private PlaylistDao playlistDao;

    private ZoneDao zoneDao;

    private AreaDao areaDao;

    private ProgramDao programDao;

    private BroadcastMsgDao broadcastMsgDao;

    private TransmitterGroupDao transmitterGroupDao;

    private MessageTypeDao messageTypeDao;

    private final SAMEStateCodes stateCodes = new SAMEStateCodes();

    private final SAMEOriginatorMapper originatorMapping = new SAMEOriginatorMapper();

    private final IMessageLogger messageLogger;

    public PlaylistManager(final IMessageLogger messageLogger)
            throws IOException {
        this(true, messageLogger);
    }

    public PlaylistManager(boolean operational,
            final IMessageLogger messageLogger) throws IOException {
        playlistDir = BMHConstants.getBmhDataDirectory(operational).resolve(
                "playlist");
        locker = new ClusterLocker(AbstractBMHDao.getDatabaseName(operational));
        this.operational = operational;
        this.messageLogger = messageLogger;
        Files.createDirectories(playlistDir);
    }

    /**
     * Check and regenerate playlists for the suite that was changed.
     * 
     */
    public void processSuiteChange(SuiteConfigNotification notification) {
        for (TransmitterGroup group : transmitterGroupDao
                .getEnabledTransmitterGroups()) {
            ProgramSuite programSuite = this.programDao
                    .getSuiteByIDForTransmitterGroup(group,
                            notification.getId());
            if (programSuite == null) {
                continue;
            }
            refreshPlaylist(group, programSuite, false);
        }
    }

    public void processProgramChange(ProgramConfigNotification notification) {
        Program program = programDao.getByID(notification.getId());
        for (TransmitterGroup group : program.getTransmitterGroups()) {
            refreshTransmitterGroup(group, program, null);
        }
    }

    public void processTransmitterGroupChange(
            TransmitterGroupConfigNotification notification) {
        for (int groupId : notification.getIds()) {
            TransmitterGroup group = transmitterGroupDao.getByID(groupId);
            refreshTransmitterGroup(group, null, null);
        }
    }

    public void processMessageActivationChange(
            MessageActivationNotification notification) {
        for (int id : notification.getInputMessageIds()) {
            List<BroadcastMsg> messages = broadcastMsgDao
                    .getMessagesByInputMsgId(id);
            for (BroadcastMsg message : messages) {
                TransmitterGroup group = message.getTransmitterGroup();
                if (!group.isEnabled()) {
                    continue;
                }
                Program program = programDao
                        .getProgramForTransmitterGroup(group);
                if (program == null) {
                    statusHandler
                            .info("Skipping playlist refresh: No program assigned to transmitter group ["
                                    + group.getName() + "]");
                    continue;
                }
                if (Boolean.TRUE.equals(message.getInputMessage().getActive())) {
                    this.messageLogger.logActivationActivity(message);
                }
                for (ProgramSuite programSuite : program.getProgramSuites()) {
                    if (programSuite.getSuite().containsSuiteMessage(
                            message.getAfosid())) {
                        refreshPlaylist(group, programSuite, false);
                        break;
                    }
                }
            }
        }
    }

    public boolean processForceSuiteSwitch(final TransmitterGroup group,
            final Suite suite) {
        statusHandler.info("User requested transmitter group ["
                + group.getName() + "] switch to suite [" + suite.getName()
                + "].");
        return refreshTransmitterGroup(group, null, suite);
    }

    public void processResetNotification() {
        refreshAll();
    }

    private void refreshAll() {
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            refreshTransmitterGroup(group, null, null);
        }
    }

    /**
     * Refresh all the playlists for a given group.
     * 
     * @param group
     *            The Group to refresh
     * @param program
     *            Optional, the program for this group. This is intended only as
     *            an optimization if null is provided it will be retrieved if
     *            needed.
     * @param forcedSuite
     *            the forced suite if any, null is acceptable in which case all
     *            triggered or general suites will be written.
     */
    protected boolean refreshTransmitterGroup(TransmitterGroup group,
            Program program, Suite forcedSuite) {
        /*
         * Start with the assumption that all lists should be deleted. Analyze
         * the programs and find lists that should exist, refresh only those
         * lists and remove them from the delete list.
         */
        List<Playlist> listsToDelete = playlistDao.getByGroupName(group
                .getName());
        if (group.isEnabled()) {
            if (program == null) {
                program = programDao.getProgramForTransmitterGroup(group);
                if (program == null) {
                    statusHandler
                            .info("Skipping playlist refresh: No program assigned to transmitter group ["
                                    + group.getName() + "]");
                    return false;
                }
            }
            List<ProgramSuite> programSuites = program.getProgramSuites();
            if (!CollectionUtil.isNullOrEmpty(programSuites)) {
                if (forcedSuite != null) {
                    ProgramSuite forcedProgramSuite = program
                            .getProgramSuite(forcedSuite);
                    if (forcedProgramSuite != null) {
                        if (!refreshPlaylist(group, forcedProgramSuite, true)) {
                            return false;
                        }
                    }
                }

                for (ProgramSuite programSuite : programSuites) {
                    if (isPreemptedByForced(forcedSuite,
                            programSuite.getSuite())) {
                        continue;
                    }
                    if (!programSuite.getSuite().equals(forcedSuite)) {
                        refreshPlaylist(group, programSuite, false);
                    }
                    Iterator<Playlist> listIterator = listsToDelete.iterator();
                    while (listIterator.hasNext()) {
                        if (listIterator.next().getSuite()
                                .equals(programSuite.getSuite())) {
                            listIterator.remove();
                        }
                    }
                }
            }
        }
        for (Playlist playlist : listsToDelete) {
            ProgramSuite programSuite = new ProgramSuite();
            programSuite.setSuite(playlist.getSuite());
            deletePlaylist(group, programSuite);
        }
        return true;
    }

    protected void deletePlaylist(TransmitterGroup group,
            ProgramSuite programSuite) {
        String groupName = group.getName();
        String suiteName = programSuite.getSuite().getName();
        ClusterTask ct = null;
        do {
            ct = locker.lock("playlist", groupName + "-" + suiteName, 30000,
                    true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        ITimer timer = TimeUtil.getTimer();
        timer.start();
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(suiteName,
                    groupName);
            if (playlist != null) {
                playlist.setModTime(TimeUtil.newGmtCalendar());
                playlist.setStartTime(null);
                playlist.setEndTime(null);
                playlist.getMessages().clear();
                persistPlaylist(playlist, programSuite, false);
            }
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
            timer.stop();
            statusHandler.info("Spent " + timer.getElapsedTime()
                    + "ms refreshing playlist for " + groupName + "("
                    + suiteName + ")");
        }
    }

    /**
     * Check and regenerate the playlist files for a specific group/suite
     * combination.
     */
    protected boolean refreshPlaylist(TransmitterGroup group,
            ProgramSuite programSuite, boolean forced) {
        ClusterTask ct = null;
        do {
            ct = locker.lock("playlist", group.getName() + "-"
                    + programSuite.getSuite().getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        ITimer timer = TimeUtil.getTimer();
        timer.start();
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(programSuite
                    .getSuite().getName(), group.getName());
            if (playlist == null) {
                playlist = new Playlist();
                playlist.setSuite(programSuite.getSuite());
                playlist.setTransmitterGroup(group);
            }
            playlist.refresh();
            loadExistingMessages(playlist);
            if (playlist.getId() != 0 || !playlist.getMessages().isEmpty()) {
                playlist.setStartTime(null);
                playlist.setEndTime(null);
                DacPlaylist dacPlaylist = persistPlaylist(playlist,
                        programSuite, forced);
                return !dacPlaylist.isEmpty();
            } else {
                return false;
            }
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
            timer.stop();
            statusHandler.info("Spent " + timer.getElapsedTime()
                    + "ms refreshing playlist for " + group.getName() + "("
                    + programSuite.getSuite().getName() + ")");
        }
    }

    public void newMessage(BroadcastMsg msg) {
        TransmitterGroup group = msg.getTransmitterGroup();
        if (!group.isEnabled()) {
            return;
        }
        if (Boolean.FALSE.equals(msg.getInputMessage().getActive())) {
            return;
        }
        Program program = programDao.getProgramForTransmitterGroup(group);

        if (msg.getInputMessage().getInterrupt()) {
            Suite suite = new Suite();
            suite.setName("Interrupt" + msg.getId());
            suite.setType(SuiteType.INTERRUPT);
            MessageTypeSummary summary = new MessageTypeSummary();
            summary.setAfosid(msg.getAfosid());
            SuiteMessage suiteMessage = new SuiteMessage();
            suiteMessage.setMsgTypeSummary(summary);
            suite.addSuiteMessage(suiteMessage);
            suite.addSuiteMessage(new SuiteMessage());
            Playlist playlist = new Playlist();
            playlist.setTransmitterGroup(group);
            playlist.setSuite(suite);
            // to fulfill the message replacement logging requirement.
            List<BroadcastMsg> replacedMsgs = new ArrayList<>();
            playlist.addBroadcastMessage(msg, null, replacedMsgs);
            this.logMsgReplacement(msg, replacedMsgs, playlist);
            playlist.setModTime(TimeUtil.newGmtCalendar());
            playlist.setStartTime(msg.getInputMessage().getEffectiveTime());
            playlist.setEndTime(msg.getInputMessage().getExpirationTime());
            writePlaylistFile(playlist, playlist.getStartTime());
        }

        for (ProgramSuite programSuite : program.getProgramSuites()) {
            if (programSuite.getSuite().containsSuiteMessage(msg.getAfosid())) {
                addMessageToPlaylist(msg, group, programSuite);
            }
        }
    }

    private void addMessageToPlaylist(BroadcastMsg msg, TransmitterGroup group,
            ProgramSuite programSuite) {
        ClusterTask ct = null;
        do {
            ct = locker.lock("playlist", group.getName() + "-"
                    + programSuite.getSuite().getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(programSuite
                    .getSuite().getName(), group.getName());
            boolean isTrigger = false; // used for logging purposes
            if (playlist == null) {
                playlist = new Playlist();
                playlist.setSuite(programSuite.getSuite());
                playlist.setTransmitterGroup(group);
                playlist.refresh();
                if (programSuite.getSuite().getType() != SuiteType.GENERAL) {
                    if (programSuite.isTrigger(msg.getAfosid())) {
                        loadExistingMessages(playlist);
                        isTrigger = true;
                    } else {
                        return;
                    }
                }
            } else {
                playlist.refresh();
                mergeMessage(playlist, msg, new HashMap<String, Set<String>>());
            }
            DacPlaylist dacPlaylist = persistPlaylist(playlist, programSuite,
                    false);
            if (dacPlaylist != null && isTrigger) {
                this.messageLogger.logTriggerActivity(msg, dacPlaylist);
            }
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
        }
    }

    private DacPlaylist persistPlaylist(Playlist playlist,
            ProgramSuite programSuite, boolean forced) {
        List<Calendar> triggerTimes = playlist.setTimes(
                programSuite.getTriggers(), forced);
        if (triggerTimes.isEmpty()) {
            /*
             * setTimes only returns no triggers if the list should not be
             * played.
             */
            playlistDao.delete(playlist);
        } else {
            playlistDao.persist(playlist);
        }
        if (triggerTimes.isEmpty()) {
            return writePlaylistFile(playlist, playlist.getModTime());
        } else if (triggerTimes.size() == 1) {
            return writePlaylistFile(playlist, triggerTimes.get(0));
        } else {
            /*
             * If there are multiple triggers, need to write one file per
             * trigger so that the dac transmit process knows enough about
             * triggers to always play the list which triggered most recently.
             */
            Calendar endTime = playlist.getEndTime();
            for (int i = 0; i < (triggerTimes.size() - 1); i += 1) {
                playlist.setEndTime(triggerTimes.get(0));
                writePlaylistFile(playlist, triggerTimes.get(i));
                playlist.setStartTime(triggerTimes.get(i));
            }
            playlist.setEndTime(endTime);
            return writePlaylistFile(playlist, playlist.getStartTime());
        }
    }

    /**
     * Load any {@link BroadcastMsg}s from the database which should be included
     * in the playlist for the given suite/group.
     * 
     * @param programSuite
     *            suite containing message types that should be loaded
     * @param group
     *            the group for which messages are loaded
     * @param expirationTime
     *            messages that expire before this time are not included in the
     *            list.
     * @param checkTrigger
     *            If true trigger messages are loaded and non-trigger messages
     *            are loaded only if trigger messages are present. If false only
     *            non trigger messages will be loaded.
     * @param forced
     *            if this list is forced and so non trigger messages should load
     *            even if there are no triggers.
     * @return all the messages from the database for a playlist (optionally
     *         excluding triggers).
     */
    private void loadExistingMessages(Playlist playlist) {
        List<SuiteMessage> suiteMessages = playlist.getSuite()
                .getSuiteMessages();
        List<String> afosids = new ArrayList<>(suiteMessages.size());
        for (SuiteMessage suiteMessage : suiteMessages) {
            afosids.add(suiteMessage.getAfosid());
        }
        List<BroadcastMsg> messages = broadcastMsgDao
                .getUnexpiredMessagesByAfosidsAndGroup(afosids,
                        playlist.getModTime(), playlist.getTransmitterGroup());
        Map<String, Set<String>> matReplacementMap = new HashMap<>();
        for (BroadcastMsg message : messages) {
            mergeMessage(playlist, message, matReplacementMap);
        }
    }

    private void mergeMessage(Playlist playlist, BroadcastMsg msg,
            Map<String, Set<String>> matReplacementMap) {
        if (Boolean.FALSE.equals(msg.getInputMessage().getActive())) {
            return;
        }
        String afosid = msg.getAfosid();
        Set<String> matReplacements = matReplacementMap.get(afosid);
        if (matReplacements == null) {
            matReplacements = messageTypeDao
                    .getReplacementAfosIdsForAfosId(afosid);
            matReplacementMap.put(afosid, matReplacements);
        }
        // to fulfill the message replacement logging requirement.
        List<BroadcastMsg> replacedMsgs = new ArrayList<>();
        playlist.addBroadcastMessage(msg, matReplacements, replacedMsgs);
        this.logMsgReplacement(msg, replacedMsgs, playlist);
    }

    private DacPlaylist writePlaylistFile(Playlist playlist,
            Calendar latestTriggerTime) {
        DacPlaylist dacList = convertPlaylistForDAC(playlist);
        dacList.setLatestTrigger(latestTriggerTime);
        PlaylistUpdateNotification notif = new PlaylistUpdateNotification(
                dacList);
        statusHandler.info("PlaylistManager writing new playlist to "
                + notif.getPlaylistPath());
        Path playlistPath = playlistDir.resolve(notif.getPlaylistPath());
        if (Files.exists(playlistPath)) {
            statusHandler.warn(
                    BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Overwriting an existing playlist file at "
                            + playlistPath.toAbsolutePath());
        }
        Path playlistDir = playlistPath.getParent();
        try {
            Files.createDirectories(playlistDir);
        } catch (IOException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write playlist xml, cannot create directory:"
                            + playlistDir.toAbsolutePath(), e);
            this.messageLogger.logError(BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, playlist, e);
            return null;
        }
        try (BufferedOutputStream os = new BufferedOutputStream(
                Files.newOutputStream(playlistPath))) {
            JAXB.marshal(dacList, os);
            this.messageLogger.logPlaylistActivity(dacList);
        } catch (Exception e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write playlist file.", e);
            this.messageLogger.logError(BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, playlist, e);
            return null;
        }
        String queue = PlaylistUpdateNotification.getQueueName(playlist
                .getTransmitterGroup().getName(), operational);
        try {
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-durable:queue:" + queue,
                    SerializationUtil.transformToThrift(notif));
        } catch (EdexException | SerializationException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to send playlist notification.", e);
        }
        return dacList;
    }

    private DacPlaylist convertPlaylistForDAC(Playlist db) {
        DacPlaylist dac = new DacPlaylist();
        Suite suite = db.getSuite();
        dac.setPriority(suite.getType().ordinal());
        dac.setTransmitterGroup(db.getTransmitterGroup().getName());
        dac.setSuite(suite.getName());
        dac.setCreationTime(db.getModTime());
        dac.setStart(db.getStartTime());
        dac.setExpired(db.getEndTime());
        dac.setInterrupt(suite.getType() == SuiteType.INTERRUPT);
        for (PlaylistMessage message : db.getSortedMessages()) {
            BroadcastMsg broadcast = message.getBroadcastMsg();
            if (broadcast.isSuccess()) {
                DacPlaylistMessageId dacMessage = convertMessageForDAC(broadcast);
                dacMessage.setReplaceTime(message.getReplacementTime());
                dac.addMessage(dacMessage);
            }
        }
        return dac;
    }

    private Path determineMessageFile(BroadcastMsg broadcast)
            throws IOException {
        Path playlistDir = this.playlistDir.resolve(broadcast
                .getTransmitterGroup().getName());
        Path messageDir = playlistDir.resolve("messages");
        Files.createDirectories(messageDir);
        return messageDir.resolve(broadcast.getId() + ".xml");
    }

    private DacPlaylistMessageId convertMessageForDAC(BroadcastMsg broadcast) {
        long id = broadcast.getId();
        DacPlaylistMessage dac = new DacPlaylistMessage();
        try {
            Path messageFile = this.determineMessageFile(broadcast);
            if (!Files.exists(messageFile)) {
                dac.setBroadcastId(id);
                for (BroadcastFragment fragment : broadcast.getFragments()) {
                    dac.addSoundFile(fragment.getOutputName());
                }
                InputMessage input = broadcast.getInputMessage();
                String afosid = input.getAfosid();
                dac.setName(input.getName());
                dac.setMessageType(afosid);
                MessageType messageType = messageTypeDao.getByAfosId(afosid);
                if (messageType != null) {
                    /*
                     * determine if the {@link MessageType} is static.
                     */
                    dac.setStatic(messageType.getDesignation().isStatic());
                    /*
                     * determine if we need to notify the user when the message
                     * expires before it is broadcast at least once.
                     */
                    dac.setWatch(messageType.getDesignation() == Designation.Watch);
                    dac.setWarning(messageType.getDesignation() == Designation.Warning);
                }
                if (broadcast.getInputMessage().getConfirm() != null) {
                    /*
                     * determine if the initial broadcast of the message should
                     * be confirmed.
                     */
                    dac.setConfirm(broadcast.getInputMessage().getConfirm());
                }
                dac.setToneBlackoutEnabled(messageType.isToneBlackoutEnabled());
                if (dac.isToneBlackoutEnabled()) {
                    TimeZone sourceTZ = TimeZone.getTimeZone(broadcast
                            .getTransmitterGroup().getTimeZone());
                    try {
                        String blackoutStart = convertToUTCTime(
                                messageType.getToneBlackOutStart(), sourceTZ);
                        dac.setToneBlackoutStart(blackoutStart);
                    } catch (NumberFormatException e) {
                        String errorMsg = String
                                .format("Invalid value for tone blackout start time: %s. This message will ignore the configured tone blackout period.",
                                        messageType.getToneBlackOutStart());
                        statusHandler.error(
                                BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, errorMsg,
                                e);
                        dac.setToneBlackoutEnabled(false);
                        dac.setToneBlackoutStart(null);
                        dac.setToneBlackoutEnd(null);
                    }

                    try {
                        /*
                         * only try to parse blackout end time, if parsing start
                         * time was successful.
                         */
                        if (dac.isToneBlackoutEnabled()) {
                            String blackoutEnd = convertToUTCTime(
                                    messageType.getToneBlackOutEnd(), sourceTZ);
                            dac.setToneBlackoutEnd(blackoutEnd);
                        }
                    } catch (NumberFormatException e) {
                        String errorMsg = String
                                .format("Invalid value for tone blackout end time: %s. This message will ignore the configured tone blackout period.",
                                        messageType.getToneBlackOutStart());
                        statusHandler.error(
                                BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, errorMsg,
                                e);
                        dac.setToneBlackoutEnabled(false);
                        dac.setToneBlackoutStart(null);
                        dac.setToneBlackoutEnd(null);
                    }
                }

                dac.setStart(input.getEffectiveTime());
                dac.setExpire(input.getExpirationTime());
                dac.setPeriodicity(input.getPeriodicity());
                dac.setMessageText(input.getContent());
                dac.setAlertTone(input.getAlertTone());
                if (((input.getAreaCodes() != null) || input
                        .getSelectedTransmitters() != null)
                        && Boolean.TRUE.equals(input.getNwrsameTone())) {
                    Set<String> sameTransmittersNames = input
                            .getSameTransmitterSet();
                    Set<Transmitter> sameTransmitters = new HashSet<>();
                    Set<Transmitter> groupTransmitters = broadcast
                            .getTransmitterGroup().getTransmitters();
                    for (Transmitter transmitter : groupTransmitters) {
                        if (sameTransmittersNames.contains(transmitter
                                .getMnemonic())) {
                            sameTransmitters.add(transmitter);
                        }
                    }

                    if (!sameTransmitters.isEmpty()) {
                        SAMEToneTextBuilder builder = new SAMEToneTextBuilder();
                        builder.setOriginatorMapper(originatorMapping);
                        builder.setStateCodes(stateCodes);
                        builder.setEventFromAfosid(broadcast.getAfosid());
                        Set<String> areaCodeSet = new HashSet<>();
                        if (input.getAreaCodes() != null) {
                            areaCodeSet.addAll(input.getAreaCodeList());
                        }
                        if (input.getSelectedTransmitters() != null) {
                            for (Transmitter t : input
                                    .getSelectedTransmitters()) {
                                /*
                                 * in this case, we only actually care about the
                                 * area(s) if the selected Transmitter is a SAME
                                 * Transmitter.
                                 */
                                if (sameTransmitters.contains(t) == false) {
                                    /*
                                     * not a SAME transmitter, will not need the
                                     * areas.
                                     */
                                    continue;
                                }
                                List<Area> transmitterAreas = this.areaDao
                                        .getAreasForTransmitter(t.getId());
                                for (Area a : transmitterAreas) {
                                    areaCodeSet.add(a.getAreaCode());
                                }
                            }
                        }

                        for (String ugc : areaCodeSet) {
                            try {
                                if (ugc.charAt(2) == 'Z') {
                                    Zone z = zoneDao.getByZoneCode(ugc);
                                    if (z != null) {
                                        for (Area area : z.getAreas()) {
                                            if (!Collections.disjoint(
                                                    area.getTransmitters(),
                                                    sameTransmitters)) {
                                                builder.addAreaFromUGC(area
                                                        .getAreaCode());
                                            }
                                        }
                                    }
                                } else {
                                    Area area = areaDao.getByAreaCode(ugc);
                                    if (!Collections.disjoint(
                                            area.getTransmitters(),
                                            sameTransmitters)) {
                                        builder.addAreaFromUGC(ugc);
                                    }
                                }
                            } catch (IllegalStateException e) {
                                statusHandler
                                        .error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                                "Cannot add area to SAME tone, same tone will not include all areas.",
                                                e);
                                break;
                            } catch (IllegalArgumentException e) {
                                statusHandler.error(
                                        BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                        "Cannot add area to SAME tone, same tone will not include this areas("
                                                + ugc + ").", e);
                            }
                        }
                        builder.setEffectiveTime(input.getEffectiveTime());
                        builder.setExpireTime(input.getExpirationTime());
                        // TODO this needs to be read from configuration.
                        builder.setNwsIcao("K" + SiteUtil.getSite());
                        dac.setSAMEtone(builder.build().toString());
                    }
                }
                JAXB.marshal(
                        dac,
                        Files.newBufferedWriter(messageFile,
                                Charset.defaultCharset()));
                this.messageLogger.logCreationActivity(dac,
                        broadcast.getTransmitterGroup());
            }
        } catch (DataBindingException | IOException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write message file.", e);
            this.messageLogger.logError(BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, dac);
        }
        return new DacPlaylistMessageId(id);
    }

    private static String convertToUTCTime(String localTime, TimeZone sourceTZ)
            throws NumberFormatException {
        int hours = Integer.valueOf(localTime.substring(0, 2));
        int minutes = Integer.valueOf(localTime.substring(2));

        Calendar localTimeCal = Calendar.getInstance(sourceTZ);
        localTimeCal.set(Calendar.HOUR_OF_DAY, hours);
        localTimeCal.set(Calendar.MINUTE, minutes);

        DateFormat outputFormat = new SimpleDateFormat("HHmm");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return outputFormat.format(localTimeCal.getTime());
    }

    public void setPlaylistDao(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    public void setZoneDao(ZoneDao zoneDao) {
        this.zoneDao = zoneDao;
    }

    public void setAreaDao(AreaDao areaDao) {
        this.areaDao = areaDao;
    }

    public void setProgramDao(ProgramDao programDao) {
        this.programDao = programDao;
    }

    public void setBroadcastMsgDao(BroadcastMsgDao broadcastMsgDao) {
        this.broadcastMsgDao = broadcastMsgDao;
    }

    public void setTransmitterGroupDao(TransmitterGroupDao transmitterGroupDao) {
        this.transmitterGroupDao = transmitterGroupDao;
    }

    public void setMessageTypeDao(MessageTypeDao messageTypeDao) {
        this.messageTypeDao = messageTypeDao;
    }

    /**
     * Validate all DAOs are set correctly and throw an exception if any are not
     * set.
     * 
     * @throws IllegalStateException
     */
    private void validateDaos() throws IllegalStateException {
        if (playlistDao == null) {
            throw new IllegalStateException(
                    "PlaylistDao has not been set on the PlaylistManager");
        } else if (zoneDao == null) {
            throw new IllegalStateException(
                    "ZoneDao has not been set on the PlaylistManager");
        } else if (areaDao == null) {
            throw new IllegalStateException(
                    "AreaDao has not been set on the PlaylistManager");
        } else if (programDao == null) {
            throw new IllegalStateException(
                    "ProgramDao has not been set on the PlaylistManager");
        } else if (broadcastMsgDao == null) {
            throw new IllegalStateException(
                    "BroadcastMsgDao has not been set on the PlaylistManager");
        } else if (transmitterGroupDao == null) {
            throw new IllegalStateException(
                    "TransmitterGroupDao has not been set on the PlaylistManager");
        } else if (messageTypeDao == null) {
            throw new IllegalStateException(
                    "MessageTypeDao has not been set on the PlaylistManager");
        }
    }

    private void logMsgReplacement(BroadcastMsg newMsg,
            List<BroadcastMsg> replacedMsgs, Playlist playlist) {
        for (BroadcastMsg msg : replacedMsgs) {
            this.messageLogger.logReplacementActivity(newMsg, msg);
        }
    }

    @Override
    public void preStart() {
        validateDaos();
        refreshAll();
    }

    @Override
    public void postStart() {
        /* Required to implement IContextStateProcessor but not used. */
    }

    @Override
    public void preStop() {
        /* Required to implement IContextStateProcessor but not used. */
    }

    @Override
    public void postStop() {
        /* Required to implement IContextStateProcessor but not used. */
    }

    private static boolean isPreemptedByForced(Suite forcedSuite,
            Suite testSuite) {
        if (forcedSuite == null || forcedSuite.equals(testSuite)) {
            return false;
        } else {
            SuiteType forcedType = forcedSuite.getType();
            SuiteType testType = testSuite.getType();
            return forcedType.compareTo(testType) <= 0;
        }
    }

}
