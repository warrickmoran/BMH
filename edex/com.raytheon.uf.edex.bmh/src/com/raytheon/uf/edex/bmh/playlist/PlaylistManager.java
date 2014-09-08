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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.same.SAMEOriginatorMapper;
import com.raytheon.uf.common.bmh.same.SAMEStateCodes;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.edex.core.IContextStateProcessor;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PlaylistManager implements IContextStateProcessor {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(PlaylistManager.class);

    private final File playlistDir;

    private PlaylistDao playlistDao;

    private ZoneDao zoneDao;

    private ProgramDao programDao;

    private BroadcastMsgDao broadcastMsgDao;

    private TransmitterGroupDao transmitterGroupDao;

    private final SAMEStateCodes stateCodes = new SAMEStateCodes();

    private final SAMEOriginatorMapper originatorMapping = new SAMEOriginatorMapper();

    public PlaylistManager() {
        playlistDir = new File(BMHConstants.getBmhDataDirectory(), "playlist");
        if (!playlistDir.exists()) {
            if (!playlistDir.mkdirs()) {
                IllegalStateException e = new IllegalStateException(
                        "Unable to create directory:"
                                + playlistDir.getAbsolutePath());
                statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                        e.getLocalizedMessage(), e);
                throw e;

            }
        }
    }

    /**
     * Check and regenerate playlists for the suite that was changed.
     * 
     */
    public void processSuiteChange(SuiteConfigNotification notification) {
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            Program program = programDao.getProgramForTransmitterGroup(group);
            for (Suite suite : program.getSuites()) {
                if (suite.getId() == notification.getId()) {
                    refreshPlaylist(group, suite);
                }
            }
        }
    }

    public void processProgramChange(ProgramConfigNotification notification) {
        Program program = programDao.getByID(notification.getId());
        for (TransmitterGroup group : program.getTransmitterGroups()) {
            List<Playlist> currentLists = playlistDao.getByGroupName(group
                    .getName());
            List<Suite> programSuites = program.getSuites();
            for (Suite suite : programSuites) {
                    refreshPlaylist(group, suite);
            }
            for (Playlist playlist : currentLists) {
                if (!programSuites.contains(playlist.getSuite())) {
                    deletePlaylist(group, playlist.getSuite());
                }
            }

        }
    }

    protected void deletePlaylist(TransmitterGroup group, Suite suite) {
        ClusterTask ct = null;
        do {
            ct = ClusterLockUtils.lock("playlist", group.getName() + "-"
                    + suite.getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        ITimer timer = TimeUtil.getTimer();
        timer.start();
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(
                    suite.getName(), group.getName());
            if (playlist != null) {
                playlist.setModTime(TimeUtil.newGmtCalendar());
                sortAndPersistPlaylist(playlist,
                        Collections.<BroadcastMsg> emptyList());
            }
        } finally {
            ClusterLockUtils.deleteLock(ct.getId().getName(), ct.getId()
                    .getDetails());
            timer.stop();
            statusHandler.info("Spent " + timer.getElapsedTime()
                    + "ms refreshing playlist for " + group.getName() + "("
                    + suite.getName() + ")");
        }
    }

    /**
     * Check and regenerate the playlist files for a specific group/suite
     * combination.
     */
    protected void refreshPlaylist(TransmitterGroup group, Suite suite) {
        ClusterTask ct = null;
        do {
            ct = ClusterLockUtils.lock("playlist", group.getName() + "-"
                    + suite.getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        ITimer timer = TimeUtil.getTimer();
        timer.start();
        try {
            Calendar currentTime = TimeUtil.newGmtCalendar();
            Playlist playlist = playlistDao.getBySuiteAndGroupName(
                    suite.getName(), group.getName());
            List<BroadcastMsg> messages = loadExistingMessages(suite, group,
                    currentTime, true);
            if (!messages.isEmpty()) {
                if (playlist == null) {
                    playlist = new Playlist();
                    playlist.setSuite(suite);
                    playlist.setTransmitterGroup(group);
                }
            }
            if (playlist != null) {
                playlist.setModTime(currentTime);
                sortAndPersistPlaylist(playlist, messages);
            }
        } finally {
            ClusterLockUtils.deleteLock(ct.getId().getName(), ct.getId()
                    .getDetails());
            timer.stop();
            statusHandler.info("Spent " + timer.getElapsedTime()
                    + "ms refreshing playlist for " + group.getName() + "("
                    + suite.getName() + ")");
        }
    }

    public void newMessage(BroadcastMsg msg) {
        TransmitterGroup group = msg.getTransmitterGroup();
        Program program = programDao.getProgramForTransmitterGroup(group);

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
        }

        for (Suite suite : program.getSuites()) {
            for (SuiteMessage smessage : suite.getSuiteMessages()) {
                if (smessage.getAfosid().equals(msg.getAfosid())) {
                    addMessageToPlaylist(msg, group, suite,
                            smessage.isTrigger());
                }
            }
        }
    }

    private void addMessageToPlaylist(BroadcastMsg msg, TransmitterGroup group,
            Suite suite, boolean trigger) {
        ClusterTask ct = null;
        do {
            ct = ClusterLockUtils.lock("playlist", group.getName() + "-"
                    + suite.getName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(
                    suite.getName(), group.getName());
            Calendar currentTime = TimeUtil.newGmtCalendar();
            if (playlist == null) {
                playlist = new Playlist();
                if (suite.getType() == SuiteType.GENERAL) {
                    playlist.setMessages(Collections.<BroadcastMsg> emptyList());
                } else if (trigger) {
                    playlist.setMessages(loadExistingMessages(suite, group,
                            currentTime, false));
                } else {
                    return;
                }
                playlist.setSuite(suite);
                playlist.setTransmitterGroup(group);
            }
            playlist.setModTime(currentTime);
            List<BroadcastMsg> messages = mergeMessage(msg,
                    playlist.getMessages());
            sortAndPersistPlaylist(playlist, messages);
        } finally {
            ClusterLockUtils.deleteLock(ct.getId().getName(), ct.getId()
                    .getDetails());
        }
    }

    private void sortAndPersistPlaylist(Playlist playlist,
            List<BroadcastMsg> messages) {
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
                if (smessage.isTrigger()
                        || (suite.getType() == SuiteType.GENERAL)) {
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
        if (futureTriggers.isEmpty() || (suite.getType() == SuiteType.GENERAL)) {
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
     * @param suite
     *            suite containing message types that should be loaded
     * @param group
     *            the group for which messages are loaded
     * @param expirationTime
     *            messages taht expire before this time are not included in the
     *            list.
     * @param checkTrigger
     *            If true trigger messages are laoded and non-trigger messages
     *            are loaded only if trigger messages are present. If false only
     *            non trigger messages will be loaded.
     * @return all the messages from the database for a playlist(optionally
     *         excluding triggers).
     */
    private List<BroadcastMsg> loadExistingMessages(Suite suite,
            TransmitterGroup group, Calendar expirationTime,
            boolean checkTrigger) {
        List<BroadcastMsg> messages = new ArrayList<>();
        if (checkTrigger) {
            for (SuiteMessage smessage : suite.getSuiteMessages()) {
                if (smessage.isTrigger()) {
                    messages.addAll(broadcastMsgDao
                            .getUnexpiredMessagesByAfosidAndGroup(
                                    smessage.getAfosid(), expirationTime, group));
                }
            }
            if (messages.isEmpty() && suite.getType() != SuiteType.GENERAL) {
                return Collections.emptyList();
            }
        }
        for (SuiteMessage smessage : suite.getSuiteMessages()) {
            if (!smessage.isTrigger()) {
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
        for (BroadcastMsg message : messages) {
            result = mergeMessage(message, result);
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
     * @return
     */
    private List<BroadcastMsg> mergeMessage(BroadcastMsg msg,
            List<BroadcastMsg> list) {
        list = new ArrayList<>(list);
        boolean added = false;
        int[] replacements = msg.getInputMessage().getMrdReplacements();
        for (int replacement : replacements) {
            ListIterator<BroadcastMsg> messageIterator = list.listIterator();
            while (messageIterator.hasNext()) {
                if (messageIterator.next().getInputMessage().getMrdId() == replacement) {
                    if (added) {
                        messageIterator.remove();
                    } else {
                        messageIterator.set(msg);
                        added = true;
                    }
                }
            }
        }

        /*
         * handle static message replacement.
         * 
         * TODO: temporary until #3585
         */
        ListIterator<BroadcastMsg> messageIterator = list.listIterator();
        while (messageIterator.hasNext()) {
            BroadcastMsg nextBroadcastMsg = messageIterator.next();
            if (msg.getInputMessage().isStaticMsg()
                    && msg.getInputMessage().getReplaceId() == nextBroadcastMsg
                            .getId()) {
                messageIterator.set(msg);
                added = true;
                break;
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
        File playlistFile = new File(playlistDir, notif.getPlaylistPath());
        if (playlistFile.exists()) {
            statusHandler.warn(
                    BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Overwriting an existing playlist file at "
                            + playlistFile.getAbsolutePath());
        }
        File playlistDir = playlistFile.getParentFile();
        if (!playlistDir.exists()) {
            if (!playlistDir.mkdirs()) {
                statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                        "Unable to write playlist xml, cannot create directory:"
                                + playlistDir.getAbsolutePath());
                return;
            }
        }
        try (BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(playlistFile))) {
            JAXB.marshal(dacList, os);
        } catch (Exception e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to write playlist file.", e);
            return;
        }
        try {
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-durable:queue:BMH.Playlist."
                            + playlist.getTransmitterGroup().getName(),
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
            dac.addMessage(convertMessageForDAC(message));
        }
        return dac;
    }

    private File determineMessageFile(BroadcastMsg broadcast) {
        File playlistDir = new File(this.playlistDir, broadcast
                .getTransmitterGroup().getName());
        File messageDir = new File(playlistDir, "messages");
        if (!messageDir.exists()) {
            messageDir.mkdirs();
        }
        File messageFile = new File(messageDir, broadcast.getId() + ".xml");

        return messageFile;
    }

    private DacPlaylistMessageId convertMessageForDAC(BroadcastMsg broadcast) {
        long id = broadcast.getId();
        File messageFile = this.determineMessageFile(broadcast);
        if (!messageFile.exists()) {
            DacPlaylistMessage dac = new DacPlaylistMessage();
            dac.setBroadcastId(id);
            dac.setSoundFile(broadcast.getOutputName());
            InputMessage input = broadcast.getInputMessage();
            dac.setMessageType(input.getAfosid());
            dac.setStart(input.getEffectiveTime());
            dac.setExpire(input.getExpirationTime());
            dac.setPeriodicity(input.getPeriodicity());
            dac.setMessageText(input.getContent());
            dac.setAlertTone(input.getAlertTone());
            if (input.getAreaCodes() != null) {
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
                                    builder.addAreaFromUGC(area.getAreaCode());
                                }
                            }
                        } else {
                            builder.addAreaFromUGC(ugc);
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
            try {
                JAXB.marshal(dac, messageFile);
            } catch (DataBindingException e) {
                statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                        "Unable to write message file.", e);
            }
        }
        return new DacPlaylistMessageId(id);
    }

    public void setPlaylistDao(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    public void setZoneDao(ZoneDao zoneDao) {
        this.zoneDao = zoneDao;
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
        } else if (programDao == null) {
            throw new IllegalStateException(
                    "ProgramDao has not been set on the PlaylistManager");
        } else if (broadcastMsgDao == null) {
            throw new IllegalStateException(
                    "BroadcastMsgDao has not been set on the PlaylistManager");
        } else if (transmitterGroupDao == null) {
            throw new IllegalStateException(
                    "TransmitterGroupDao has not been set on the PlaylistManager");
        }
    }

    @Override
    public void preStart() {
        validateDaos();
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            Program program = programDao.getProgramForTransmitterGroup(group);
            for (Suite suite : program.getSuites()) {
                refreshPlaylist(group, suite);
            }
        }
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
