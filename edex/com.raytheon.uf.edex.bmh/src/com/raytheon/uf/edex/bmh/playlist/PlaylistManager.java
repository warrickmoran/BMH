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

import java.io.File;
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
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.same.SAMEOriginatorMapper;
import com.raytheon.uf.common.bmh.same.SAMEStateCodes;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;
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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PlaylistManager {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(PlaylistManager.class);

    private final File playlistDir;

    private PlaylistDao playlistDao = new PlaylistDao();

    private ZoneDao zoneDao = new ZoneDao();

    private ProgramDao programDao = new ProgramDao();

    private BroadcastMsgDao messageDao = new BroadcastMsgDao();

    private SAMEStateCodes stateCodes = new SAMEStateCodes();

    private SAMEOriginatorMapper originatorMapping = new SAMEOriginatorMapper();

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

    public void newMessage(BroadcastMsg msg) {
        TransmitterGroup group = msg.getTransmitterGroup();
        String programName = group.getProgramName();
        Program program = programDao.getByID(programName);
        if (msg.getInputMessage().getInterrupt()) {
            Suite suite = new Suite();
            suite.setSuiteName("Interrupt" + msg.getId());
            suite.setSuiteType(SuiteType.INTERRUPT);
            suite.setProgramPosition(program.getSuites().size());
            Playlist playlist = new Playlist();
            playlist.setTransmitterGroup(group);
            playlist.setSuite(suite);
            playlist.setMessages(Arrays.asList(msg));
            playlist.setModTime(TimeUtil.newGmtCalendar());
            playlist.setStartTime(msg.getInputMessage().getEffectiveTime());
            playlist.setEndTime(msg.getInputMessage().getExpirationTime());
            writePlaylistFile(playlist);
        }
        for (Suite suite : program.getSuites()) {
            for (SuiteMessage smessage : suite.getSuiteMessages()) {
                if (smessage.getId().getAfosid().equals(msg.getAfosid())) {
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
                    + suite.getSuiteName(), 30000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
        try {
            Playlist playlist = playlistDao.getBySuiteAndGroupName(
                    suite.getSuiteName(), group.getName());
            if (playlist == null) {
                playlist = new Playlist();
                if (suite.getSuiteType() == SuiteType.GENERAL) {
                    playlist.setMessages(Collections.<BroadcastMsg> emptyList());
                } else if (trigger) {
                    playlist.setMessages(loadExistingMessages(suite));
                } else {
                    return;
                }
                playlist.setSuite(suite);
                playlist.setTransmitterGroup(group);
            }
            playlist.setModTime(TimeUtil.newGmtCalendar());
            List<BroadcastMsg> messages = mergeMessage(msg,
                    playlist.getMessages());
            /*
             * Sort the messages by afosid so they can be added back in the
             * order specified by the suite, this skips any expired messages so
             * they are removed from the list.
             */
            Calendar expireTime = playlist.getModTime();
            Map<String, List<BroadcastMsg>> afosMapping = new HashMap<>();
            for (BroadcastMsg message : messages) {
                if (expireTime.after(message.getInputMessage()
                        .getExpirationTime())) {
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
             * messages, also calculate the start and end time of the playlist
             * by looking at the effective and expire times of any trigger
             * messages.
             */
            Calendar startTime = null;
            Calendar endTime = null;
            messages.clear();
            for (SuiteMessage smessage : suite.getSuiteMessages()) {
                List<BroadcastMsg> afosMessages = afosMapping.remove(smessage
                        .getId().getAfosid());
                if (afosMessages != null) {
                    messages.addAll(afosMessages);
                    if (smessage.isTrigger()
                            || suite.getSuiteType() == SuiteType.GENERAL) {
                        for (BroadcastMsg bmessage : afosMessages) {
                            Calendar messageStart = bmessage.getInputMessage()
                                    .getEffectiveTime();
                            Calendar messageEnd = bmessage.getInputMessage()
                                    .getExpirationTime();
                            if (startTime == null
                                    || startTime.after(messageStart)) {
                                startTime = messageStart;
                            }
                            if (endTime == null || endTime.before(messageEnd)) {
                                endTime = messageEnd;
                            }
                        }
                    }
                }
            }
            if (startTime != null) {
                playlist.setStartTime(startTime);
                playlist.setEndTime(endTime);
                playlist.setMessages(messages);
                playlistDao.persist(playlist);
                writePlaylistFile(playlist);
            }
        } finally {
            ClusterLockUtils.deleteLock(ct.getId().getName(), ct.getId()
                    .getDetails());
        }
    }

    /**
     * Get any messages which should be in the list but aren't triggers.
     * 
     * @param suite
     * @return all non-trigger messages for that suite.
     */
    private List<BroadcastMsg> loadExistingMessages(Suite suite) {
        List<BroadcastMsg> messages = new ArrayList<>();
        for (SuiteMessage smessage : suite.getSuiteMessages()) {
            if (!smessage.isTrigger()) {
                // TODO filter by expiration date in dao.
                messages.addAll(messageDao.getMessagesByAfosid(smessage.getId()
                        .getAfosid()));
            }
        }
        Collections.sort(messages, new Comparator<BroadcastMsg>() {

            @Override
            public int compare(BroadcastMsg m1, BroadcastMsg m2) {
                return m1.getCreationDate().compareTo(m2.getCreationDate());
            }

        });
        List<BroadcastMsg> result = new ArrayList<>(messages.size());
        for (BroadcastMsg message : messages) {
            mergeMessage(message, result);
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
        if (!added) {
            list.add(msg);
        }
        return list;

    }

    private void writePlaylistFile(Playlist playlist) {
        DacPlaylist dacList = convertPlaylistForDAC(playlist);
        PlaylistUpdateNotification notif = new PlaylistUpdateNotification(
                dacList);
        File playlistFile = new File(playlistDir, notif.getPlaylistPath());
        File playlistDir = playlistFile.getParentFile();
        if (!playlistDir.exists()) {
            if (!playlistDir.mkdirs()) {
                statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                        "Unable to write playlist xml, cannot create directory:"
                                + playlistDir.getAbsolutePath());
                return;
            }
        }
        try {
            JAXB.marshal(dacList, playlistFile);
        } catch (DataBindingException e) {
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
        dac.setPriority(suite.getSuiteType().ordinal());
        dac.setTransmitterGroup(db.getTransmitterGroup().getName());
        dac.setSuite(suite.getSuiteType().toString());
        dac.setCreationTime(db.getModTime());
        dac.setStart(db.getStartTime());
        dac.setExpired(db.getEndTime());
        dac.setInterrupt(suite.getSuiteType() == SuiteType.INTERRUPT);
        for (BroadcastMsg message : db.getMessages()) {
            dac.addMessage(convertMessageForDAC(message));
        }
        return dac;
    }

    private DacPlaylistMessageId convertMessageForDAC(BroadcastMsg broadcast) {
        long id = broadcast.getId();
        File playlistDir = new File(this.playlistDir, broadcast
                .getTransmitterGroup().getName());
        File messageDir = new File(playlistDir, "messages");
        File messageFile = new File(messageDir, id + ".xml");
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
            SAMEToneTextBuilder builder = new SAMEToneTextBuilder();
            builder.setOriginatorMapper(originatorMapping);
            builder.setStateCodes(stateCodes);
            builder.setEventFromAfosid(broadcast.getAfosid());
            for (String ugc : input.getAreaCodeList()) {
                try {
                    if (ugc.charAt(2) == 'Z') {
                        Zone z = zoneDao.getByID(ugc);
                        if (z != null) {
                            for (String area : z.getAreas().keySet()) {
                                builder.addAreaFromUGC(area);
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
                    statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                            "Cannot add area to SAME tone, same tone will not include this areas("
                                    + ugc + ").", e);
                }
            }
            builder.setEffectiveTime(input.getEffectiveTime());
            builder.setExpireTime(input.getExpirationTime());
            // TODO this needs to be read from configuration.
            builder.setNwsIcao("K" + SiteUtil.getSite());
            dac.setSAMEtone(builder.build().toString());
            if (!messageDir.exists()) {
                messageDir.mkdirs();
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

}
