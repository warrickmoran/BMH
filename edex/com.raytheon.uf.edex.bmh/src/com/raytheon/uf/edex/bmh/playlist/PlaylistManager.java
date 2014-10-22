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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import com.google.common.primitives.Ints;
import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
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
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
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

    public PlaylistManager() throws IOException {
        this(true);
    }

    public PlaylistManager(boolean operational) throws IOException {
        playlistDir = BMHConstants.getBmhDataDirectory(operational).resolve(
                "playlist");
        locker = new ClusterLocker(AbstractBMHDao.getDatabaseName(operational));
        this.operational = operational;
        Files.createDirectories(playlistDir);
    }

    /**
     * Check and regenerate playlists for the suite that was changed.
     * 
     */
    public void processSuiteChange(SuiteConfigNotification notification) {
        /*
         * TODO this has the potential for optimization by performing a single
         * request to get all groups using a particular suite.
         */
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            if (group.getEnabledTransmitters().isEmpty()) {
                continue;
            }
            ProgramSuite programSuite = this.programDao
                    .getSuiteByIDForTransmitterGroup(group,
                            notification.getId());
            if (programSuite == null) {
                continue;
            }
            refreshPlaylist(group, programSuite);
        }
    }

    public void processProgramChange(ProgramConfigNotification notification) {
        Program program = programDao.getByID(notification.getId());
        for (TransmitterGroup group : program.getTransmitterGroups()) {
            refreshTransmitterGroup(group, program);
        }
    }

    public void processTransmitterGroupChange(
            TransmitterGroupConfigNotification notification) {
        for (int groupId : notification.getIds()) {
            TransmitterGroup group = transmitterGroupDao.getByID(groupId);
            refreshTransmitterGroup(group, null);
        }
    }

    public void processForceSuiteSwitch(final TransmitterGroup group,
            final Suite suite) {
        statusHandler.info("User requested transmitter group ["
                + group.getName() + "] switch to suite [" + suite.getName()
                + "].");

        Program program = programDao.getProgramForTransmitterGroup(group);
        for (ProgramSuite programSuite : program.getProgramSuites()) {
            boolean shouldBeForce = programSuite.getSuite().equals(suite);
            if (shouldBeForce != programSuite.isForced()) {
                programSuite.setForced(shouldBeForce);
                programDao.persist(programSuite);
            }
        }
        refreshTransmitterGroup(group, program);
    }

    public void processResetNotification() {
        refreshAll();
    }

    private void refreshAll() {
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            refreshTransmitterGroup(group, null);
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
     */
    protected void refreshTransmitterGroup(TransmitterGroup group,
            Program program) {
        /*
         * Start with the assumption that all lists should be deleted. Analyze
         * the programs and find lists that should exist, refresh only those
         * lists and remove them from the delete list.
         */
        List<Playlist> listsToDelete = playlistDao.getByGroupName(group
                .getName());
        if (!group.getEnabledTransmitters().isEmpty()) {
            if (program == null) {
                program = programDao.getProgramForTransmitterGroup(group);
            }
            SuiteType forcedType = null;
            for (ProgramSuite programSuite : program.getProgramSuites()) {
                if (programSuite.isForced()) {
                    forcedType = programSuite.getSuite().getType();
                }
            }
            for (ProgramSuite programSuite : program.getProgramSuites()) {
                boolean shouldRefresh = (forcedType == null)
                        || programSuite.isForced();
                if ((shouldRefresh == false)
                        && (programSuite.getSuite().getType()
                                .compareTo(forcedType) < 0)) {
                    shouldRefresh = true;
                }
                if (shouldRefresh) {
                    refreshPlaylist(group, programSuite);
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
    }

    protected void deletePlaylist(TransmitterGroup group,
            ProgramSuite programSuite) {
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
            if (playlist != null) {
                playlist.setModTime(TimeUtil.newGmtCalendar());
                sortAndPersistPlaylist(playlist,
                        Collections.<BroadcastMsg> emptyList(), programSuite);
            }
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
            timer.stop();
            statusHandler.info("Spent " + timer.getElapsedTime()
                    + "ms refreshing playlist for " + group.getName() + "("
                    + programSuite.getSuite().getName() + ")");
        }
    }

    /**
     * Check and regenerate the playlist files for a specific group/suite
     * combination.
     */
    protected void refreshPlaylist(TransmitterGroup group,
            ProgramSuite programSuite) {
        ClusterTask ct = null;
        do {
            ct = locker.lock("playlist", group.getName() + "-"
                    + programSuite.getSuite().getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        ITimer timer = TimeUtil.getTimer();
        timer.start();
        try {
            Calendar currentTime = TimeUtil.newGmtCalendar();
            Playlist playlist = playlistDao.getBySuiteAndGroupName(programSuite
                    .getSuite().getName(), group.getName());
            List<BroadcastMsg> messages = loadExistingMessages(programSuite,
                    group, currentTime, true);
            if (!messages.isEmpty()) {
                if (playlist == null) {
                    playlist = new Playlist();
                    playlist.setSuite(programSuite.getSuite());
                    playlist.setTransmitterGroup(group);
                }
            }
            if (playlist != null) {
                playlist.setModTime(currentTime);
                sortAndPersistPlaylist(playlist, messages, programSuite);
            }
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId()
                    .getDetails());
            timer.stop();
            statusHandler.info("Spent " + timer.getElapsedTime()
                    + "ms refreshing playlist for " + group.getName() + "("
                    + programSuite.getSuite().getName() + ")");
        }
    }

    public void newMessage(BroadcastMsg msg) {
        TransmitterGroup group = msg.getTransmitterGroup();
        if (group.getEnabledTransmitters().isEmpty()) {
            return;
        }
        Program program = programDao.getProgramForTransmitterGroup(group);
        boolean disableForcedSuite = false;

        if (msg.getInputMessage().getInterrupt()) {
            Suite suite = new Suite();
            suite.setName("Interrupt" + msg.getId());
            suite.setType(SuiteType.INTERRUPT);
            Playlist playlist = new Playlist();
            playlist.setTransmitterGroup(group);
            playlist.setSuite(suite);
            playlist.setMessages(Arrays.asList(msg));
            playlist.setModTime(TimeUtil.newGmtCalendar());
            playlist.setStartTime(msg.getInputMessage().getEffectiveTime());
            playlist.setEndTime(msg.getInputMessage().getExpirationTime());
            writePlaylistFile(playlist, playlist.getStartTime());

            disableForcedSuite = true;
        }

        /*
         * Determine if we have a administratively forced GENERAL suite. If so
         * we must find it and ensure no message updates are processed for any
         * other GENERAL suites, if they exist. GENERAL suite changes can only
         * be made via user intervention.
         * 
         * FIXME: When we remove the ability to have multiple GENERAL suites in
         * a Program, remove special case for GENERAL.
         * 
         * Secondly, we need to determine if we have a forced suite that's not
         * GENERAL. If so, we might have to reset the forced flag on the
         * ProgramSuite if this new message triggers any suites.
         */
        ProgramSuite forcedGeneral = null;
        ProgramSuite forcedHigher = null;
        for (ProgramSuite programSuite : program.getProgramSuites()) {
            if (programSuite.isForced()
                    && (programSuite.getSuite().getType() == SuiteType.GENERAL)) {
                forcedGeneral = programSuite;
            } else if (programSuite.isForced()
                    && (programSuite.getSuite().getType() != SuiteType.GENERAL)) {
                forcedHigher = programSuite;
            }
        }

        // TODO: optimize.
        for (ProgramSuite programSuite : program.getProgramSuites()) {
            /*
             * Do not process any updates for GENERAL suites except the forced
             * GENERAL suite (if it exists). If the user wants a different
             * GENERAL suite to play, they must manually force that suite into
             * effect.
             * 
             * FIXME: When we remove the ability to have multiple GENERAL suites
             * in a Program, remove special case for GENERAL.
             */
            if ((forcedGeneral != null)
                    && (programSuite.getSuite().getType() == SuiteType.GENERAL)
                    && (!programSuite.getId().equals(forcedGeneral.getId()))) {
                continue;
            }
            Map<String, Set<String>> matReplacementMap = new HashMap<>();

            for (SuiteMessage smessage : programSuite.getSuite()
                    .getSuiteMessages()) {
                if (smessage.getAfosid().equals(msg.getAfosid())) {
                    boolean isTrigger = programSuite.isTrigger(smessage
                            .getMsgTypeSummary());
                    addMessageToPlaylist(msg, group, programSuite, isTrigger,
                            matReplacementMap);

                    if ((isTrigger)
                            && (forcedHigher != null)
                            && (programSuite.getId().equals(forcedHigher
                                    .getId()))) {
                        disableForcedSuite = true;
                    }
                }
            }
        }

        if ((forcedHigher != null) && (disableForcedSuite)) {
            statusHandler
                    .debug("Incoming message has triggered a suite, removing forced flag from suite "
                            + forcedHigher.getSuite().getName());
            forcedHigher.setForced(false);
            programDao.persist(forcedHigher);
        }
    }

    private void addMessageToPlaylist(BroadcastMsg msg, TransmitterGroup group,
            ProgramSuite programSuite, boolean trigger,
            Map<String, Set<String>> matReplacementMap) {
        ClusterTask ct = null;
        do {
            ct = locker.lock("playlist", group.getName() + "-"
                    + programSuite.getSuite().getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(programSuite
                    .getSuite().getName(), group.getName());
            Calendar currentTime = TimeUtil.newGmtCalendar();
            if (playlist == null) {
                playlist = new Playlist();
                if (programSuite.getSuite().getType() == SuiteType.GENERAL) {
                    playlist.setMessages(Collections.<BroadcastMsg> emptyList());
                } else if (trigger) {
                    playlist.setMessages(loadExistingMessages(programSuite,
                            group, currentTime, false));
                } else {
                    return;
                }
                playlist.setSuite(programSuite.getSuite());
                playlist.setTransmitterGroup(group);
            }
            playlist.setModTime(currentTime);
            List<BroadcastMsg> messages = mergeMessage(msg,
                    playlist.getMessages(), matReplacementMap);
            sortAndPersistPlaylist(playlist, messages, programSuite);
        } finally {
            locker.deleteLock(ct.getId().getName(), ct.getId().getDetails());
        }
    }

    private void sortAndPersistPlaylist(Playlist playlist,
            List<BroadcastMsg> messages, final ProgramSuite programSuite) {
        Calendar currentTime = playlist.getModTime();
        Suite suite = playlist.getSuite();
        /*
         * Sort the messages by afosid so they can be added back in the order
         * specified by the suite, this skips any expired messages so they are
         * removed from the list.
         */
        Map<String, List<BroadcastMsg>> afosMapping = new HashMap<>();
        for (BroadcastMsg message : messages) {
            if (currentTime
                    .after(message.getInputMessage().getExpirationTime())) {
                continue;
            }
            List<BroadcastMsg> afosMessages = afosMapping.get(message
                    .getAfosid());
            if (afosMessages == null) {
                afosMessages = new ArrayList<>(1);
                afosMapping.put(message.getAfosid(), afosMessages);
            }
            afosMessages.add(message);
        }

        /*
         * Add messages back into the list in the same order as the suite
         * messages, also calculate the start and end time of the playlist by
         * looking at the effective and expire times of any trigger messages.
         */
        Calendar startTime = null;
        Calendar latestTrigger = null;
        List<Calendar> futureTriggers = new ArrayList<>(1);
        Calendar endTime = null;
        messages.clear();
        for (SuiteMessage smessage : suite.getSuiteMessages()) {
            List<BroadcastMsg> afosMessages = afosMapping.remove(smessage
                    .getAfosid());
            if (afosMessages != null) {
                messages.addAll(afosMessages);

                Designation msgType = smessage.getMsgTypeSummary()
                        .getDesignation();

                /*
                 * We need to exert control over how the start time and expire
                 * time of the playlist is calculated. If the message is a
                 * trigger or we're creating a playlist for a GENERAL suite,
                 * then we use the message's start and expire times in
                 * calculating the same for the playlist. Additionally, if we're
                 * updating a playlist for a forced suite, we need to ignore the
                 * routine messages--station ID and time of day--so that the
                 * playlist expires when all messages but those routine messages
                 * have expired.
                 */
                if ((programSuite.isTrigger(smessage.getMsgTypeSummary()))
                        || (suite.getType() == SuiteType.GENERAL)
                        || (programSuite.isForced() && ((msgType != Designation.TimeAnnouncement) && (msgType != Designation.StationID)))) {
                    for (BroadcastMsg bmessage : afosMessages) {
                        Calendar messageStart = bmessage.getInputMessage()
                                .getEffectiveTime();
                        Calendar messageEnd = bmessage.getInputMessage()
                                .getExpirationTime();
                        if ((startTime == null)
                                || startTime.after(messageStart)) {
                            startTime = messageStart;
                        }
                        if (messageStart.before(currentTime)) {
                            if ((latestTrigger == null)
                                    || latestTrigger.before(messageStart)) {
                                latestTrigger = messageStart;
                            }
                        } else {
                            futureTriggers.add(messageStart);
                        }
                        if ((endTime == null) || endTime.before(messageEnd)) {
                            endTime = messageEnd;
                        }
                    }
                }

            }
        }
        if (startTime == null) {
            startTime = endTime = latestTrigger = playlist.getModTime();
        } else if ((latestTrigger == null) && !futureTriggers.isEmpty()) {
            latestTrigger = startTime;
            futureTriggers.remove(latestTrigger);
        }
        playlist.setStartTime(startTime);
        playlist.setEndTime(endTime);
        playlist.setMessages(messages);
        if (startTime == endTime) {
            playlistDao.delete(playlist);
        } else {
            playlistDao.persist(playlist);
        }
        if (futureTriggers.isEmpty() || (suite.getType() == SuiteType.GENERAL)
                || (programSuite.isForced())) {
            writePlaylistFile(playlist, latestTrigger);
        } else {
            /*
             * If there are multiple triggers, need to write one file per
             * trigger.
             */
            Collections.sort(futureTriggers);
            playlist.setEndTime(futureTriggers.get(0));
            writePlaylistFile(playlist, latestTrigger);
            for (int i = 0; i < (futureTriggers.size() - 1); i += 1) {
                playlist.setStartTime(futureTriggers.get(i));
                playlist.setEndTime(futureTriggers.get(i + 1));
                writePlaylistFile(playlist, futureTriggers.get(i));
            }
            playlist.setStartTime(futureTriggers.get(futureTriggers.size() - 1));
            playlist.setEndTime(endTime);
            writePlaylistFile(playlist, playlist.getStartTime());
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
     * @return all the messages from the database for a playlist (optionally
     *         excluding triggers).
     */
    private List<BroadcastMsg> loadExistingMessages(
            final ProgramSuite programSuite, TransmitterGroup group,
            Calendar expirationTime, boolean checkTrigger) {
        List<BroadcastMsg> messages = new ArrayList<>();
        if (checkTrigger) {
            for (MessageTypeSummary programTrigger : programSuite.getTriggers()) {
                messages.addAll(broadcastMsgDao
                        .getMessagesByAfosid(programTrigger.getAfosid()));
            }
            if (messages.isEmpty()
                    && (programSuite.getSuite().getType() != SuiteType.GENERAL)
                    && (!programSuite.isForced())) {
                return Collections.emptyList();
            }
        }
        for (SuiteMessage smessage : programSuite.getSuite().getSuiteMessages()) {
            if (programSuite.isTrigger(smessage.getMsgTypeSummary()) == false) {
                messages.addAll(broadcastMsgDao
                        .getUnexpiredMessagesByAfosidAndGroup(
                                smessage.getAfosid(), expirationTime, group));
            }
        }
        Collections.sort(messages, new Comparator<BroadcastMsg>() {

            @Override
            public int compare(BroadcastMsg m1, BroadcastMsg m2) {
                return m1.getCreationDate().compareTo(m2.getCreationDate());
            }

        });
        List<BroadcastMsg> result = Collections.emptyList();

        /*
         * TODO: potential enhancment would be to load all replacement afos ides
         * for current set of broadcast messages in one go
         */
        Map<String, Set<String>> matReplacementMap = new HashMap<>();

        for (BroadcastMsg message : messages) {
            result = mergeMessage(message, result, matReplacementMap);
        }

        return result;
    }

    /**
     * Add the message to the list of message, if there are any replacements,
     * add it by replacing the first message that it is a replacement for,
     * otherwise it is just added to the list.
     * 
     * @param msg
     * @param list
     * @param matReplacements
     * @return
     */
    private List<BroadcastMsg> mergeMessage(BroadcastMsg msg,
            List<BroadcastMsg> list, Map<String, Set<String>> matReplacementMap) {
        list = new ArrayList<>(list);
        boolean added = false;
        String afosid = msg.getAfosid();

        List<Integer> mrdReplacements = Ints.asList(msg.getInputMessage()
                .getMrdReplacements());
        Set<String> matReplacements = matReplacementMap.get(afosid);
        if (matReplacements == null) {
            matReplacements = messageTypeDao
                    .getReplacementAfosIdsForAfosId(afosid);
            matReplacementMap.put(afosid, matReplacements);
        }
        matReplacements.add(afosid);

        ListIterator<BroadcastMsg> messageIterator = list.listIterator();
        while (messageIterator.hasNext()) {
            BroadcastMsg potentialReplacee = messageIterator.next();
            int mrd = potentialReplacee.getInputMessage().getMrdId();
            String msgCodes = msg.getInputMessage().getAreaCodes();
            boolean areaCodesEqual = (msgCodes != null)
                    && msgCodes.equals(potentialReplacee.getInputMessage()
                            .getAreaCodes());
            boolean mrdReplacement = mrdReplacements.contains(mrd);
            boolean matReplacement = (mrd == -1)
                    && matReplacements.contains(potentialReplacee.getAfosid());
            matReplacement = matReplacement && areaCodesEqual;
            if (mrdReplacement || matReplacement) {
                if (added) {
                    messageIterator.remove();
                } else {
                    messageIterator.set(msg);
                    added = true;
                }
            }
        }

        if (!added) {
            list.add(msg);
        }
        return list;

    }

    private void writePlaylistFile(Playlist playlist, Calendar latestTriggerTime) {
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
            return;
        }
        try (BufferedOutputStream os = new BufferedOutputStream(
                Files.newOutputStream(playlistPath))) {
            JAXB.marshal(dacList, os);
        } catch (Exception e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write playlist file.", e);
            return;
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
        for (BroadcastMsg message : db.getMessages()) {
            if (message.isSuccess()) {
                dac.addMessage(convertMessageForDAC(message));
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
        try {
            Path messageFile = this.determineMessageFile(broadcast);
            if (!Files.exists(messageFile)) {
                DacPlaylistMessage dac = new DacPlaylistMessage();
                dac.setBroadcastId(id);
                for (BroadcastFragment fragment : broadcast.getFragments()) {
                    dac.addSoundFile(fragment.getOutputName());
                }
                InputMessage input = broadcast.getInputMessage();
                dac.setMessageType(input.getAfosid());
                dac.setStart(input.getEffectiveTime());
                dac.setExpire(input.getExpirationTime());
                dac.setPeriodicity(input.getPeriodicity());
                dac.setMessageText(input.getContent());
                dac.setAlertTone(input.getAlertTone());
                if (input.getAreaCodes() != null
                        && Boolean.TRUE.equals(input.getNwrsameTone())) {
                    Set<Transmitter> transmitters = broadcast
                            .getTransmitterGroup().getTransmitters();

                    SAMEToneTextBuilder builder = new SAMEToneTextBuilder();
                    builder.setOriginatorMapper(originatorMapping);
                    builder.setStateCodes(stateCodes);
                    builder.setEventFromAfosid(broadcast.getAfosid());
                    for (String ugc : input.getAreaCodeList()) {
                        try {
                            if (ugc.charAt(2) == 'Z') {
                                Zone z = zoneDao.getByZoneCode(ugc);
                                if (z != null) {
                                    for (Area area : z.getAreas()) {
                                        if (!Collections.disjoint(
                                                area.getTransmitters(),
                                                transmitters)) {
                                            builder.addAreaFromUGC(area
                                                    .getAreaCode());
                                        }
                                    }
                                }
                            } else {
                                Area area = areaDao.getByAreaCode(ugc);
                                if (!Collections.disjoint(
                                        area.getTransmitters(), transmitters)) {
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
                JAXB.marshal(
                        dac,
                        Files.newBufferedWriter(messageFile,
                                Charset.defaultCharset()));

            }
        } catch (DataBindingException | IOException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write message file.", e);
        }
        return new DacPlaylistMessageId(id);
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

    public MessageTypeDao getMessageTypeDao() {
        return messageTypeDao;
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

}
