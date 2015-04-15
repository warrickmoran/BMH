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
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastContents;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
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
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.MessageActivationNotification;
import com.raytheon.uf.common.bmh.notify.config.MessageForcedExpirationNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupIdentifier;
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
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.replace.ReplacementManager;
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
 * Jan 21, 2015  4017     bkowal      Do not attempt to retrieve {@link TransmitterGroup}s
 *                                    that have just been deleted. Purge playlist directories
 *                                    associated with delete groups if the directories exist.
 * Jan 26, 2015  3359     bsteffen    Use site id for same tones.
 * Jan 27, 2015  4036     bsteffen    Fix creation of general playlists.
 * Feb 03, 2015  4081     bkowal      Eliminate unused isStatic dac playlist message setting.
 * Feb 06, 2015  4036     bsteffen    Ensure message writer is closed.
 * Feb 10, 2015  4093     bsteffen    Leave tones in UTC
 * Mar 12, 2015  4207     bsteffen    Pass triggers to playlist when refreshing.
 * Mar 13, 2015  4193     bsteffen    Do not convert replaced messages to xml.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Mar 31, 2015  4339     bkowal      Distinguish between areas that cannot be added because
 *                                    they are invalid and areas that are over the allowed limit.
 * Apr 07, 2015  4293     bkowal      Include a timestamp based on the broadcast message last update date/time
 *                                    in the message xml file.
 * Apr 02, 2015  4248     rjpeter     Get ordered messages when needed.
 * Apr 07, 2015  4339     bkowal      Only notify the user when invalid areas or over the limit areas
 *                                    are NOT empty Strings.
 * Apr 15, 2015  4293     bkowal      Handle {@link MessageForcedExpirationNotification}.
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

    private final ReplacementManager replacementManager;

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
        replacementManager = new ReplacementManager();
        replacementManager.setMessageLogger(messageLogger);
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
        for (TransmitterGroupIdentifier identifier : notification
                .getIdentifiers()) {
            TransmitterGroup group = transmitterGroupDao.getByID(identifier
                    .getId());
            if (group == null) {
                /**
                 * The group no longer exists. Forcefully purge the playlist
                 * filesystem associated with the Transmitter Group.
                 */
                if ((identifier.getName() == null)
                        || identifier.getName().isEmpty()) {
                    statusHandler
                            .warn(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                    "No name is associated with the non-existent or recently deleted Transmitter Group with id: "
                                            + identifier.getId()
                                            + ". Skipping playlist purge.");
                    continue;
                }
                Path groupPlaylistPath = playlistDir.resolve(identifier
                        .getName());
                if (Files.exists(groupPlaylistPath) == false) {
                    statusHandler
                            .info("No playlist file(s) exist for recently deleted Transmitter Group: "
                                    + identifier.toString() + ".");
                    continue;
                }

                try {
                    FileUtils.deleteDirectory(groupPlaylistPath.toFile());

                    StringBuilder sb = new StringBuilder(
                            "Successfully removed the playlist file(s): ");
                    sb.append(groupPlaylistPath.toString());
                    sb.append(" for recently deleted Transmitter Group: ");
                    sb.append(identifier.toString()).append(".");
                    statusHandler.info(sb.toString());
                } catch (IOException e) {
                    StringBuilder sb = new StringBuilder(
                            "Failed to remove playlist file(s): ");
                    sb.append(groupPlaylistPath.toString());
                    sb.append(" for recently deleted Transmitter Group: ");
                    sb.append(identifier.toString()).append(".");
                    statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                            sb.toString(), e);
                }
            } else {
                refreshTransmitterGroup(group, null, null);
            }
        }
    }

    public void processMessageActivationChange(
            MessageActivationNotification notification) {
        Set<InputMessage> needsRefresh = new HashSet<>();
        for (int id : notification.getInputMessageIds()) {
            List<BroadcastMsg> messages = broadcastMsgDao
                    .getMessagesByInputMsgId(id);
            if (!messages.isEmpty()) {
                needsRefresh.addAll(replacementManager.replace(messages.get(0)
                        .getInputMessage()));
            }
            this.checkRefreshForBroadcastMsgs(messages);
        }
        refreshReplaced(needsRefresh);
    }

    public void processMessageForcedExpiration(
            MessageForcedExpirationNotification notification) {
        if (notification.getBroadcastIds().isEmpty()) {
            return;
        }

        List<BroadcastMsg> messages = new ArrayList<>(notification
                .getBroadcastIds().size());
        for (Long id : notification.getBroadcastIds()) {
            BroadcastMsg broadcastMsg = this.broadcastMsgDao.getByID(id);
            if (broadcastMsg == null) {
                continue;
            }
            messages.add(broadcastMsg);
        }
        this.checkRefreshForBroadcastMsgs(messages);
    }

    private void checkRefreshForBroadcastMsgs(List<BroadcastMsg> messages) {
        for (BroadcastMsg message : messages) {
            TransmitterGroup group = message.getTransmitterGroup();
            if (!group.isEnabled()) {
                continue;
            }
            Program program = programDao.getProgramForTransmitterGroup(group);
            if (program == null) {
                statusHandler
                        .info("Skipping playlist refresh: No program assigned to transmitter group ["
                                + group.getName() + "]");
                continue;
            }
            if (Boolean.TRUE.equals(message.getInputMessage().getActive())
                    && message.getForcedExpiration() == false) {
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

            Collection<ProgramSuite> programSuites = program.getProgramSuites();
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
            playlist.refresh(programSuite.getTriggers());
            loadExistingMessages(playlist);
            if ((playlist.getId() != 0) || !playlist.getMessages().isEmpty()) {
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

    protected void refreshReplaced(Set<InputMessage> needsRefresh) {
        for (InputMessage inputMessage : needsRefresh) {
            List<BroadcastMsg> messages = broadcastMsgDao
                    .getMessagesByInputMsgId(inputMessage.getId());
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

    public void newMessage(BroadcastMsgGroup group) {
        Set<InputMessage> needsRefresh = replacementManager.replace(group
                .getMessages().get(0).getInputMessage());
        for (BroadcastMsg message : group.getMessages()) {
            newMessage(message);
        }
        refreshReplaced(needsRefresh);
    }

    public void newMessage(BroadcastMsg msg) {
        TransmitterGroup group = msg.getTransmitterGroup();
        if (!group.isEnabled()) {
            return;
        }
        if (Boolean.FALSE.equals(msg.getInputMessage().getActive())) {
            return;
        }
        if (msg.getForcedExpiration()) {
            return;
        }
        Program program = programDao.getProgramForTransmitterGroup(group);
        if (program == null) {
            statusHandler
                    .info("No program is currently assigned to Transmitter Group: "
                            + group.getName()
                            + ". Broadcast Message: "
                            + msg.getId()
                            + " will not be scheduled for broadcast.");
            return;
        }

        if (msg.getInputMessage().getInterrupt()) {
            Suite suite = new Suite();
            suite.setName("Interrupt" + msg.getId());
            suite.setType(SuiteType.INTERRUPT);
            MessageTypeSummary summary = new MessageTypeSummary();
            summary.setAfosid(msg.getAfosid());
            SuiteMessage suiteMessage = new SuiteMessage();
            suiteMessage.setMsgTypeSummary(summary);
            suite.addSuiteMessage(suiteMessage);
            Playlist playlist = new Playlist();
            playlist.setTransmitterGroup(group);
            playlist.setSuite(suite);
            playlist.addBroadcastMessage(msg);
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
                playlist.refresh(programSuite.getTriggers());
                if (programSuite.getSuite().getType() != SuiteType.GENERAL) {
                    if (programSuite.isTrigger(msg.getAfosid())) {
                        loadExistingMessages(playlist);
                        isTrigger = true;
                    } else {
                        return;
                    }
                } else {
                    mergeMessage(playlist, msg);
                }
            } else {
                playlist.refresh(programSuite.getTriggers());
                mergeMessage(playlist, msg);
            }
            DacPlaylist dacPlaylist = persistPlaylist(playlist, programSuite,
                    false);
            if ((dacPlaylist != null) && isTrigger) {
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
            playlistDao.saveOrUpdate(playlist);
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
                .getOrderedSuiteMessages();
        List<String> afosids = new ArrayList<>(suiteMessages.size());
        for (SuiteMessage suiteMessage : suiteMessages) {
            afosids.add(suiteMessage.getAfosid());
        }
        List<BroadcastMsg> messages = broadcastMsgDao
                .getUnexpiredMessagesByAfosidsAndGroup(afosids,
                        playlist.getModTime(), playlist.getTransmitterGroup());
        for (BroadcastMsg message : messages) {
            mergeMessage(playlist, message);
        }
    }

    private void mergeMessage(Playlist playlist, BroadcastMsg msg) {
        if (Boolean.FALSE.equals(msg.getInputMessage().getActive())) {
            return;
        }
        if (msg.getForcedExpiration()) {
            return;
        }
        playlist.addBroadcastMessage(msg);
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
        String topic = PlaylistUpdateNotification.getTopicName(playlist
                .getTransmitterGroup().getName(), operational);
        try {
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-generic:topic:" + topic,
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
        for (BroadcastMsg message : db.getSortedMessages()) {
            if (message.isSuccess()
                    && ((message.getExpirationTime() == null) || message
                            .getExpirationTime().after(db.getModTime()))) {
                DacPlaylistMessageId dacMessage = convertMessageForDAC(message);
                dacMessage.setExpire(message.getExpirationTime());
                dac.addMessage(dacMessage);
            }
        }
        return dac;
    }

    private Path determineMessageFile(BroadcastMsg broadcast,
            long metadataTimestamp) throws IOException {
        Path playlistDir = this.playlistDir.resolve(broadcast
                .getTransmitterGroup().getName());
        Path messageDir = playlistDir.resolve("messages");
        Files.createDirectories(messageDir);
        return messageDir.resolve(broadcast.getId() + "_" + metadataTimestamp
                + ".xml");
    }

    private DacPlaylistMessageId convertMessageForDAC(BroadcastMsg broadcast) {
        long id = broadcast.getId();
        final long metadataTimestamp = broadcast.getUpdateDate()
                .getTimeInMillis();
        DacPlaylistMessage dac = new DacPlaylistMessage();
        try {
            final BroadcastContents contents = broadcast
                    .getLatestBroadcastContents();
            Path messageFile = this.determineMessageFile(broadcast,
                    metadataTimestamp);
            if (!Files.exists(messageFile)) {
                dac.setBroadcastId(id);
                InputMessage input = broadcast.getInputMessage();
                String afosid = input.getAfosid();
                dac.setName(input.getName());
                for (BroadcastFragment fragment : contents
                        .getOrderedFragments()) {
                    dac.addSoundFile(fragment.getOutputName());
                }
                dac.setMessageType(afosid);
                MessageType messageType = messageTypeDao.getByAfosId(afosid);
                if (messageType != null) {
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
                    dac.setToneBlackoutStart(messageType.getToneBlackOutStart());
                    dac.setToneBlackoutEnd(messageType.getToneBlackOutEnd());
                }

                dac.setStart(broadcast.getEffectiveTime());
                dac.setPeriodicity(broadcast.getInputMessage().getPeriodicity());
                dac.setMessageText(broadcast.getInputMessage().getContent());
                dac.setExpire(input.getExpirationTime());
                dac.setAlertTone(input.getAlertTone());
                if (((input.getAreaCodes() != null) || (input
                        .getSelectedTransmitters() != null))
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

                        List<String> ugcs = new ArrayList<>();
                        for (String ugc : areaCodeSet) {
                            if (ugc.charAt(2) == 'Z') {
                                Zone z = zoneDao.getByZoneCode(ugc);
                                if (z != null) {
                                    for (Area area : z.getAreas()) {
                                        if (!Collections.disjoint(
                                                area.getTransmitters(),
                                                sameTransmitters)) {
                                            ugcs.add(area.getAreaCode());
                                        }
                                    }
                                }
                            } else {
                                Area area = areaDao.getByAreaCode(ugc);
                                if (!Collections.disjoint(
                                        area.getTransmitters(),
                                        sameTransmitters)) {
                                    ugcs.add(area.getAreaCode());
                                }
                            }
                        }
                        builder.addAreasFromUGC(ugcs);
                        String invalidAreas = builder.summarizeInvalidAreas();
                        String overLimitAreas = builder
                                .summarizeOverLimitAreas();
                        if (overLimitAreas.isEmpty() == false) {
                            statusHandler.error(BMH_CATEGORY.SAME_TRUNCATION,
                                    "Failed to all all areas to the SAME Message. "
                                            + overLimitAreas);
                        }
                        if (invalidAreas.isEmpty() == false) {
                            statusHandler.error(
                                    BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                    "Failed to all all areas to the SAME Message. "
                                            + invalidAreas);
                        }

                        builder.setEffectiveTime(input.getEffectiveTime());
                        builder.setExpireTime(input.getExpirationTime());
                        builder.setNwsSiteId(SiteUtil.getSite());
                        dac.setSAMEtone(builder.build().toString());
                    }
                }
                try (Writer writer = Files.newBufferedWriter(messageFile,
                        Charset.defaultCharset())) {
                    JAXB.marshal(dac, writer);
                }
                this.messageLogger.logCreationActivity(dac,
                        broadcast.getTransmitterGroup());
            }
        } catch (DataBindingException | IOException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write message file.", e);
            this.messageLogger.logError(BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, dac);
        }
        DacPlaylistMessageId playlistId = new DacPlaylistMessageId(id);
        playlistId.setTimestamp(metadataTimestamp);
        return playlistId;
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
        replacementManager.setMessageTypeDao(messageTypeDao);
    }

    public void setInputMessageDao(InputMessageDao inputMessageDao) {
        replacementManager.setInputMessageDao(inputMessageDao);
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
        if ((forcedSuite == null) || forcedSuite.equals(testSuite)) {
            return false;
        } else {
            SuiteType forcedType = forcedSuite.getType();
            SuiteType testType = testSuite.getType();
            return forcedType.compareTo(testType) <= 0;
        }
    }

}
