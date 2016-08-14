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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.xml.bind.JAXBException;

import org.springframework.util.CollectionUtils;

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
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacTriggerSpan;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.MessageActivationNotification;
import com.raytheon.uf.common.bmh.notify.config.MessageForcedExpirationNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupIdentifier;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.bmh.stats.AbstractBMHProcessingTimeEvent;
import com.raytheon.uf.common.bmh.stats.MessageExpirationProcessingEvent;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.common.event.EventBus;
import com.raytheon.uf.common.serialization.JAXBManager;
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
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
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
 * Apr 20, 2015  4397     bkowal      Generate and submit a {@link AbstractBMHProcessingTimeEvent} when
 *                                    applicable.
 * Apr 27, 2015  4397     bkowal      Add additional information to the {@link DacPlaylistMessage}
 *                                    required to calculate the stats information.
 * May 05, 2015  4463     bkowal      Attempt to use the originator associated with the specified
 *                                    {@link MessageType}.
 * May 05, 2015  4456     bkowal      Do not write an interrupt playlist for a {@link BroadcastMsg}
 *                                    that has already played as an interrupt.
 * May 06, 2015  4471     bkowal      Added support for Demo Message SAME Tones.
 * May 11, 2015  4002     bkowal      Added {@link #applyMrdFollowsSorting(SortedSet, Map, DacPlaylist)}.
 * May 12, 2015  4248     rjpeter     Fixed misspelling.
 * May 13, 2015  4429     rferrel     Changes for traceId.
 * May 19, 2015  4508     rjpeter     Set timestamp on  {@link DacPlaylistMessage}.
 * May 21, 2015  4429     rjpeter     Added additional logging methods.
 * May 28, 2015  4429     rjpeter     Add ITraceable.
 * Jun 01, 2015  4490     bkowal      Use the new {@link BMH_CATEGORY#SAME_AREA_TRUNCATION}.
 * Jun 08, 2015  4490     bkowal      Walk the file tree that will be deleted to handle the case
 *                                    when the dac transmit deletes files before we can reach them.
 * Jun 18, 2015  4490     bkowal      Refresh all potential playlists when a message is
 *                                    deactivated.
 * Jun 23, 2015  4490     bkowal      Cluster lock playlist directory deletions.
 * Jul 28, 2015  4686     bkowal      Moved statistics to common.
 * Aug 10, 2015  4723     bkowal      Added {@link #checkStatusExpired()}.
 * Aug 10, 2015  4424     bkowal      Updated to use the new playlist directory constant.
 * Aug 27, 2015  4811     bkowal      Create a server-side record of the unlikely scenario.
 * Sep 21, 2015  4901     bkowal      Verify the {@link Area} associated with a ugc actually
 *                                    exists before adding it to the dac playlist message.
 * Sep 22, 2015  4904     bkowal      Propagate replaced messages to the playlist.
 * Sep 24, 2015  4924     bkowal      Catch any error encountered when producing a
 *                                    {@link DacPlaylistMessage}.
 * Jan 26, 2016  5278     bkowal      Include all triggers in a playlist.
 * Feb 04, 2016  5308     bkowal      Utilize {@link DacPlaylistMessageMetadata}.
 * Mar 01, 2016  5382     bkowal      Potentially restore a message file from the archive when
 *                                    a message that was previously archived is updated.
 * Jul 01, 2016  5727     bkowal      The replaced message (when applicable) is now required to
 *                                    determine the {@link DacTriggerSpan}s.
 * Aug 04, 2016  5766     bkowal      Handle the scheduling and prediction of cycle-based
 *                                    periodic messages.
 * </pre>
 * 
 * @author bsteffen
 */
public class PlaylistManager implements IContextStateProcessor {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(PlaylistManager.class);

    private final boolean operational;

    private final Path playlistDir;

    private final ClusterLocker locker;

    private final JAXBManager jaxbManager;

    private PlaylistDao playlistDao;

    private ZoneDao zoneDao;

    private AreaDao areaDao;

    private ProgramDao programDao;

    private BroadcastMsgDao broadcastMsgDao;

    private TransmitterGroupDao transmitterGroupDao;

    private MessageTypeDao messageTypeDao;

    private ValidatedMessageDao validatedMessageDao;

    private final ReplacementManager replacementManager;

    private final IMessageLogger messageLogger;

    public PlaylistManager(final IMessageLogger messageLogger)
            throws IOException {
        this(true, messageLogger);
    }

    public PlaylistManager(boolean operational,
            final IMessageLogger messageLogger) throws IOException {
        playlistDir = BMHConstants.getBmhDataDirectory(operational).resolve(
                BMHConstants.PLAYLIST_DIRECTORY);
        locker = new ClusterLocker(AbstractBMHDao.getDatabaseName(operational));
        try {
            this.jaxbManager = new JAXBManager(DacPlaylistMessage.class,
                    DacPlaylistMessageMetadata.class, DacPlaylist.class);
        } catch (JAXBException e) {
            throw new RuntimeException(
                    "Failed to instantiate the JAXB Manager.", e);
        }
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
            refreshPlaylist(group, programSuite, false, null, notification,
                    null);
        }
    }

    public void processProgramChange(ProgramConfigNotification notification) {
        Program program = programDao.getByID(notification.getId());
        for (TransmitterGroup group : program.getTransmitterGroups()) {
            refreshTransmitterGroup(group, program, null, null, notification);
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
                ClusterTask ct = null;
                Path groupPlaylistPath = null;
                do {
                    ct = locker.lock("playlist", identifier.getName(), 30000,
                            true);
                } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
                try {
                    /*
                     * Only verify the files existence after we receive the
                     * lock.
                     */
                    groupPlaylistPath = playlistDir.resolve(identifier
                            .getName());
                    if (Files.exists(groupPlaylistPath) == false) {
                        statusHandler
                                .info("No playlist file(s) exist for recently deleted Transmitter Group: "
                                        + identifier.toString() + ".");
                        continue;
                    }

                    Files.walkFileTree(groupPlaylistPath,
                            new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file,
                                        BasicFileAttributes attrs)
                                        throws IOException {
                                    Files.deleteIfExists(file);
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult postVisitDirectory(
                                        Path dir, IOException e)
                                        throws IOException {
                                    if (e == null) {
                                        Files.deleteIfExists(dir);
                                        return FileVisitResult.CONTINUE;
                                    } else {
                                        // directory iteration failed
                                        throw e;
                                    }
                                }
                            });

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
                } finally {
                    locker.deleteLock(ct.getId().getName(), ct.getId()
                            .getDetails());
                }
            } else {
                refreshTransmitterGroup(group, null, null, null, notification);
                this.checkStatusExpired();
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
            MessageExpirationProcessingEvent event = null;
            if (notification.getExpireRequestTime() != null) {
                event = new MessageExpirationProcessingEvent(
                        notification.getExpireRequestTime());
            }
            this.checkRefreshForBroadcastMsgs(messages, event, notification);
        }
        refreshReplaced(needsRefresh, notification);
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
        this.checkRefreshForBroadcastMsgs(
                messages,
                new MessageExpirationProcessingEvent(notification
                        .getRequestTime()), notification);
    }

    private void checkRefreshForBroadcastMsgs(List<BroadcastMsg> messages,
            AbstractBMHProcessingTimeEvent event, ITraceable traceable) {
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
            final boolean activeMessage = Boolean.TRUE.equals(message
                    .getInputMessage().getActive())
                    && (message.getForcedExpiration() == false);
            if (activeMessage) {
                this.messageLogger.logActivationActivity(null, message);
            }
            for (ProgramSuite programSuite : program.getProgramSuites()) {
                if (programSuite.getSuite().containsSuiteMessage(
                        message.getAfosid())) {
                    refreshPlaylist(group, programSuite, false, event,
                            traceable, null);
                    if (activeMessage) {
                        /*
                         * if the message has been deactivated, we want to
                         * ensure that every playlist the message belongs to is
                         * updated because the change will not necessarily
                         * trigger a switch to a playlist that contains the
                         * message.
                         */
                        break;
                    }
                }
            }
        }
    }

    public boolean processForceSuiteSwitch(final TransmitterGroup group,
            final Suite suite, AbstractBMHProcessingTimeEvent event,
            ITraceable traceable) {
        statusHandler.info("User requested transmitter group ["
                + group.getName() + "] switch to suite [" + suite.getName()
                + "].");
        return refreshTransmitterGroup(group, null, suite, event, traceable);
    }

    public void processResetNotification(ResetNotification notification) {
        refreshAll(notification);
    }

    private void refreshAll(ITraceable traceable) {
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            refreshTransmitterGroup(group, null, null, null, traceable);
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
            Program program, Suite forcedSuite,
            AbstractBMHProcessingTimeEvent event, ITraceable traceable) {
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
                    ProgramSuite forcedProgramSuite = null;
                    try {
                        forcedProgramSuite = program
                                .getProgramSuite(forcedSuite);
                    } catch (IllegalStateException e) {
                        /*
                         * Log a server-side record of the unlikely scenario.
                         */
                        statusHandler
                                .error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                        "The refresh of playlists for Program: "
                                                + program.getName()
                                                + " has failed.", e);
                        throw e;
                    }
                    if (forcedProgramSuite != null) {
                        if (!refreshPlaylist(group, forcedProgramSuite, true,
                                event, traceable, null)) {
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
                        refreshPlaylist(group, programSuite, false, event,
                                traceable, null);
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
                persistPlaylist(playlist, programSuite, false, null, null, null);
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
            ProgramSuite programSuite, boolean forced,
            AbstractBMHProcessingTimeEvent event, ITraceable traceable,
            BroadcastMsg replacedMessage) {
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
                        programSuite, forced, event, traceable, replacedMessage);
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

    protected void refreshReplaced(Set<InputMessage> needsRefresh,
            ITraceable traceable) {
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
                        refreshPlaylist(group, programSuite, false, null,
                                traceable, message);
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
            newMessage(message, group);
        }
        refreshReplaced(needsRefresh, group);
    }

    private void newMessage(BroadcastMsg msg, ITraceable traceable) {
        TransmitterGroup group = msg.getTransmitterGroup();
        if (!group.isEnabled()) {
            logBroadcastMsgInfo(traceable, msg, "group " + group.getName()
                    + " not enabled for new message.");
            return;
        }
        if (Boolean.FALSE.equals(msg.getInputMessage().getActive())) {
            logBroadcastMsgInfo(traceable, msg, "new message not active.");
            return;
        }
        if (msg.getForcedExpiration()) {
            logBroadcastMsgInfo(traceable, msg,
                    "forced expiration of new message.");
            return;
        }
        Program program = programDao.getProgramForTransmitterGroup(group);
        if (program == null) {
            logBroadcastMsgInfo(
                    traceable,
                    msg,
                    "No program is currently assigned to Transmitter Group: "
                            + group.getName()
                            + ". Broadcast Message will not be scheduled for broadcast.");
            return;
        }

        if (msg.getInputMessage().getInterrupt()
                && msg.isPlayedInterrupt() == false) {
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
            writePlaylistFile(playlist, playlist.getStartTime(), null,
                    traceable, null);
        }

        for (ProgramSuite programSuite : program.getProgramSuites()) {
            if (programSuite.getSuite().containsSuiteMessage(msg.getAfosid())) {
                addMessageToPlaylist(msg, group, programSuite, traceable);
            }
        }
    }

    private void logBroadcastMsgInfo(ITraceable traceable, BroadcastMsg msg,
            String details) {
        messageLogger.logInfo(traceable, BMH_COMPONENT.PLAYLIST_MANAGER,
                BMH_ACTIVITY.PLAYLIST_WRITE, msg, details);
    }

    private void addMessageToPlaylist(BroadcastMsg msg, TransmitterGroup group,
            ProgramSuite programSuite, ITraceable traceable) {
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
            if (isTrigger) {
                playlist.setTriggerBroadcastId(msg.getId());
            }
            DacPlaylist dacPlaylist = persistPlaylist(playlist, programSuite,
                    false, null, traceable, null);
            if ((dacPlaylist != null) && isTrigger) {
                this.messageLogger.logTriggerActivity(traceable, msg,
                        dacPlaylist);
            }
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
        }
    }

    /**
     * Sets the transmission status of {@link ValidatedMessage}s associated with
     * expired {@link BroadcastMsg}s to EXPIRED instead of ACCEPTED if the
     * {@link BroadcastMsg} has not been "delievered" yet. The expiration time
     * of the {@link BroadcastMsg} is based on the expiration time of the
     * associated {@link InputMessage}. So, a {@link BroadcastMsg} that expires
     * on one {@link Transmitter} will expire on all {@link Transmitter}s.
     */
    private void checkStatusExpired() {
        List<ValidatedMessage> validatedMsgs = this.validatedMessageDao
                .getExpiredNonDeliveredMessages(TimeUtil.newGmtCalendar());
        for (ValidatedMessage validatedMsg : validatedMsgs) {
            validatedMsg.setTransmissionStatus(TransmissionStatus.EXPIRED);
            final String msgHeader = TraceableUtil
                    .createTraceMsgHeader(validatedMsg);

            StringBuilder sb = new StringBuilder(msgHeader);
            sb.append("Setting transmission status to ").append(
                    TransmissionStatus.EXPIRED.name());
            sb.append(" for non-broadcast, expired message: ")
                    .append(validatedMsg.getId()).append(".");
            statusHandler.info(sb.toString());
        }
        this.validatedMessageDao.persistAll(validatedMsgs);
    }

    private DacPlaylist persistPlaylist(Playlist playlist,
            ProgramSuite programSuite, boolean forced,
            AbstractBMHProcessingTimeEvent event, ITraceable traceable,
            BroadcastMsg replacedMessage) {
        List<DacTriggerSpan> triggerSpans = playlist.setTimes(
                programSuite.getTriggers(), forced, replacedMessage);

        if (triggerSpans.isEmpty()) {
            /*
             * setTimes only returns no triggers if the list should not be
             * played.
             */
            playlistDao.delete(playlist);
        } else {
            playlistDao.saveOrUpdate(playlist);
        }
        if (triggerSpans.isEmpty()) {
            return writePlaylistFile(playlist, playlist.getModTime(), event,
                    traceable, replacedMessage);
        } else {
            /*
             * If there are multiple triggers, all need to be written to the
             * playlist.
             */
            return writePlaylistFile(playlist, triggerSpans.get(0).getStart(),
                    event, traceable, replacedMessage, triggerSpans);
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
            Calendar latestTriggerTime, AbstractBMHProcessingTimeEvent event,
            ITraceable traceable, BroadcastMsg replacedMessage) {
        return this.writePlaylistFile(playlist, latestTriggerTime, event,
                traceable, replacedMessage, null);
    }

    private DacPlaylist writePlaylistFile(Playlist playlist,
            Calendar latestTriggerTime, AbstractBMHProcessingTimeEvent event,
            ITraceable traceable, BroadcastMsg replacedMessage,
            List<DacTriggerSpan> triggerSpans) {
        DacPlaylist dacList = convertPlaylistForDAC(playlist, traceable);
        dacList.setTriggers(triggerSpans);
        if (replacedMessage != null) {
            DacPlaylistMessageId id = new DacPlaylistMessageId(
                    replacedMessage.getId());
            /*
             * We only care about the updated expiration time for a replaced
             * message because its existence will not trigger the reload of
             * message metadata.
             */
            id.setExpire(replacedMessage.getExpirationTime());
            dacList.setReplacedMessage(id);
        }
        dacList.setLatestTrigger(latestTriggerTime);
        PlaylistUpdateNotification notif = new PlaylistUpdateNotification(
                dacList);
        String traceMsg = TraceableUtil.createTraceMsgHeader(traceable);
        statusHandler.info(traceMsg
                + "PlaylistManager writing new playlist to "
                + notif.getPlaylistPath());
        Path playlistPath = playlistDir.resolve(notif.getPlaylistPath());
        if (Files.exists(playlistPath)) {
            statusHandler.warn(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, traceMsg
                    + "Overwriting an existing playlist file at "
                    + playlistPath.toAbsolutePath());
        }
        Path playlistDir = playlistPath.getParent();
        try {
            Files.createDirectories(playlistDir);
        } catch (IOException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write playlist xml, cannot create directory:"
                            + playlistDir.toAbsolutePath(), e);
            this.messageLogger.logError(traceable,
                    BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, playlist, e);
            return null;
        }
        try {
            this.jaxbManager.marshalToXmlFile(dacList, playlistPath.toString());
            this.messageLogger.logPlaylistActivity(traceable, dacList);
        } catch (Exception e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, traceMsg
                    + "Unable to write playlist file.", e);
            this.messageLogger.logError(traceable,
                    BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, playlist, e);
            return null;
        }
        String topic = PlaylistUpdateNotification.getTopicName(operational);
        try {
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-generic:topic:" + topic,
                    SerializationUtil.transformToThrift(notif));
        } catch (EdexException | SerializationException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, traceMsg
                    + "Unable to send playlist notification.", e);
        } finally {
            if (event != null && this.operational) {
                event.finalizeEvent();
                event.setTransmitterGroup(playlist.getTransmitterGroup()
                        .getName());
                EventBus.publish(event);
            }
        }
        return dacList;
    }

    private DacPlaylist convertPlaylistForDAC(Playlist db, ITraceable traceable) {
        DacPlaylist dac = new DacPlaylist();
        Suite suite = db.getSuite();
        dac.setPriority(suite.getType().ordinal());
        dac.setTransmitterGroup(db.getTransmitterGroup().getName());
        dac.setSuite(suite.getName());
        dac.setCreationTime(db.getModTime());
        dac.setStart(db.getStartTime());
        dac.setExpired(db.getEndTime());
        dac.setInterrupt(suite.getType() == SuiteType.INTERRUPT);
        if (traceable != null) {
            dac.setTraceId(traceable.getTraceId());
        }

        SortedSet<BroadcastMsg> sorted = db.getSortedMessages();
        Map<Long, List<Long>> mrdFollowsMap = db.buildFollowsMapping();

        for (BroadcastMsg message : this.applyMrdFollowsSorting(sorted,
                mrdFollowsMap, dac)) {
            if (message.isSuccess()
                    && ((message.getExpirationTime() == null) || message
                            .getExpirationTime().after(db.getModTime()))) {
                DacPlaylistMessageId dacMessage = convertMessageForDAC(message,
                        traceable);
                dacMessage.setExpire(message.getExpirationTime());
                dac.addMessage(dacMessage);
            }
        }
        dac.setTriggerBroadcastId(db.getTriggerBroadcastId());
        return dac;
    }

    private Collection<BroadcastMsg> applyMrdFollowsSorting(
            SortedSet<BroadcastMsg> sorted,
            Map<Long, List<Long>> mrdFollowsMap, DacPlaylist playlist) {
        if (CollectionUtils.isEmpty(mrdFollowsMap)) {
            return sorted;
        }

        List<Long> sortedIdList = new LinkedList<>();
        Map<Long, BroadcastMsg> idToBroadcastMap = new HashMap<>(sorted.size(),
                1.0f);
        for (BroadcastMsg broadcastMsg : sorted) {
            sortedIdList.add(broadcastMsg.getId());
            idToBroadcastMap.put(broadcastMsg.getId(), broadcastMsg);
        }

        MRDFollowsManager mrdFollowsManager = new MRDFollowsManager(
                sortedIdList, mrdFollowsMap);
        sortedIdList = mrdFollowsManager.orderWithFollows(playlist.toString());

        List<BroadcastMsg> sortedWithFollows = new LinkedList<>();
        for (long id : sortedIdList) {
            sortedWithFollows.add(idToBroadcastMap.get(id));
        }
        return sortedWithFollows;
    }

    private Path determineMessageMetadataFile(BroadcastMsg broadcast,
            long metadataTimestamp) throws IOException {
        Path playlistDir = this.playlistDir.resolve(broadcast
                .getTransmitterGroup().getName());
        Path messageDir = playlistDir.resolve("messages");
        Files.createDirectories(messageDir);
        return messageDir.resolve(broadcast.getId() + "_" + metadataTimestamp
                + ".xml");
    }

    private DacPlaylistMessageId convertMessageForDAC(BroadcastMsg broadcast,
            ITraceable traceable) {
        long id = broadcast.getId();
        final long metadataTimestamp = broadcast.getUpdateDate()
                .getTimeInMillis();
        DacPlaylistMessage dac = new DacPlaylistMessage();
        try {
            final BroadcastContents contents = broadcast
                    .getLatestBroadcastContents();
            InputMessage input = broadcast.getInputMessage();
            Path messageMetadataFile = this.determineMessageMetadataFile(
                    broadcast, metadataTimestamp);
            if (!Files.exists(messageMetadataFile)) {
                /*
                 * Prepare new message metadata.
                 */
                DacPlaylistMessageMetadata dacMetadata = new DacPlaylistMessageMetadata(
                        dac);
                dacMetadata.setVersion(DacPlaylistMessageId.CURRENT_VERSION);
                for (BroadcastFragment fragment : contents
                        .getOrderedFragments()) {
                    dacMetadata.addSoundFile(fragment.getOutputName());
                }
                dacMetadata.setName(input.getName());
                dacMetadata.setStart(broadcast.getEffectiveTime());
                String afosid = input.getAfosid();
                MessageType messageType = messageTypeDao.getByAfosId(afosid);
                if (messageType != null) {
                    /*
                     * determine if we need to notify the user when the message
                     * expires before it is broadcast at least once.
                     */
                    dacMetadata
                            .setWatch(messageType.getDesignation() == Designation.Watch);
                    dacMetadata
                            .setWarning(messageType.getDesignation() == Designation.Warning);
                }
                dacMetadata.setMessageType(afosid);
                final String periodicity = broadcast.getInputMessage()
                        .getPeriodicity();
                dacMetadata.setPeriodicity(periodicity);
                if (periodicity == null
                        || MessageType.DEFAULT_NO_PERIODICITY
                                .equals(periodicity)) {
                    dacMetadata.setCycles(broadcast.getInputMessage()
                            .getCycles());
                }
                dacMetadata.setMessageText(broadcast.getInputMessage()
                        .getContent());

                if (Boolean.TRUE.equals(input.getNwrsameTone())) {
                    if (input.getAfosid().length() >= 6
                            && SAMEToneTextBuilder.DEMO_EVENT.equals(input
                                    .getAfosid().substring(3, 6))) {
                        // Build a Demo SAME Tone.
                        SAMEToneTextBuilder builder = new SAMEToneTextBuilder();
                        builder.setEvent(SAMEToneTextBuilder.DEMO_EVENT);
                        if (messageType != null) {
                            builder.setOriginator(messageType.getOriginator());
                        }
                        builder.setEffectiveTime(input.getEffectiveTime());
                        builder.setExpireTime(input.getExpirationTime());
                        builder.setNwsSiteId(SiteUtil.getSite());
                        dacMetadata.setSAMEtone(builder.build().toString());
                    } else if (((input.getAreaCodes() != null) || (input
                            .getSelectedTransmitters() != null))) {
                        // Build a Standard SAME Tone.
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
                            if (messageType != null) {
                                builder.setOriginator(messageType
                                        .getOriginator());
                            }
                            builder.setEventFromAfosid(broadcast.getAfosid());
                            Set<String> areaCodeSet = new HashSet<>();
                            if (input.getAreaCodes() != null) {
                                areaCodeSet.addAll(input.getAreaCodeList());
                            }
                            if (input.getSelectedTransmitters() != null) {
                                for (Transmitter t : input
                                        .getSelectedTransmitters()) {
                                    /*
                                     * in this case, we only actually care about
                                     * the area(s) if the selected Transmitter
                                     * is a SAME Transmitter.
                                     */
                                    if (sameTransmitters.contains(t) == false) {
                                        /*
                                         * not a SAME transmitter, will not need
                                         * the areas.
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
                                    if (area != null) {
                                        if (!Collections.disjoint(
                                                area.getTransmitters(),
                                                sameTransmitters)) {
                                            ugcs.add(area.getAreaCode());
                                        }
                                    }
                                }
                            }
                            builder.addAreasFromUGC(ugcs);
                            String invalidAreas = builder
                                    .summarizeInvalidAreas();
                            String overLimitAreas = builder
                                    .summarizeOverLimitAreas();
                            if (overLimitAreas.isEmpty() == false) {
                                statusHandler
                                        .error(BMH_CATEGORY.SAME_AREA_TRUNCATION,
                                                TraceableUtil
                                                        .createTraceMsgHeader(traceable)
                                                        + "Failed to add all areas to the SAME Message. "
                                                        + overLimitAreas);
                            }
                            if (invalidAreas.isEmpty() == false) {
                                statusHandler.error(
                                        BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                        "Failed to add all areas to the SAME Message. "
                                                + invalidAreas);
                            }

                            builder.setEffectiveTime(input.getEffectiveTime());
                            builder.setExpireTime(input.getExpirationTime());
                            builder.setNwsSiteId(SiteUtil.getSite());
                            dacMetadata.setSAMEtone(builder.build().toString());
                        }
                    }
                }
                dacMetadata.setAlertTone(input.getAlertTone());
                dacMetadata.setToneBlackoutEnabled(messageType
                        .isToneBlackoutEnabled());
                if (dacMetadata.isToneBlackoutEnabled()) {
                    dacMetadata.setToneBlackoutStart(messageType
                            .getToneBlackOutStart());
                    dacMetadata.setToneBlackoutEnd(messageType
                            .getToneBlackOutEnd());
                }
                if (broadcast.getInputMessage().getConfirm() != null) {
                    /*
                     * determine if the initial broadcast of the message should
                     * be confirmed.
                     */
                    dacMetadata.setConfirm(broadcast.getInputMessage()
                            .getConfirm());
                }
                dacMetadata.setInitialRecognitionTime(input.getLastUpdateTime()
                        .getTime());
                dacMetadata.setExpire(input.getExpirationTime());
                this.jaxbManager.marshalToXmlFile(dacMetadata,
                        messageMetadataFile.toString());
                statusHandler.info("Wrote message metadata file: "
                        + messageMetadataFile.toString() + ".");
                dac.setMetadata(dacMetadata);
                this.messageLogger.logPlaylistMessageActivity(traceable, dac,
                        broadcast.getTransmitterGroup());
            }
        } catch (Throwable e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write message file.", e);
            this.messageLogger.logError(traceable,
                    BMH_COMPONENT.PLAYLIST_MANAGER,
                    BMH_ACTIVITY.PLAYLIST_WRITE, dac);
        }
        DacPlaylistMessageId playlistId = new DacPlaylistMessageId(id);
        playlistId.setTimestamp(metadataTimestamp);
        playlistId.setTraceId(traceable.getTraceId());
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

    public void setValidatedMessageDao(ValidatedMessageDao validatedMessageDao) {
        this.validatedMessageDao = validatedMessageDao;
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
        } else if (validatedMessageDao == null) {
            throw new IllegalStateException(
                    "ValidatedMessageDao has not been set on the PlaylistManager");
        }
    }

    @Override
    public void preStart() {
        validateDaos();
        refreshAll(TraceableUtil.createCurrentTraceId("Edex-Start"));
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
