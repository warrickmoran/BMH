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
package com.raytheon.uf.edex.bmh.handler;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.FilePermissionUtils;
import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastContents;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.common.util.file.IOPermissionsHelper;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastContentsDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.dao.WordDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EdexException;

/**
 * 
 * Copies the operational BMH database to the practice BMH database. The
 * existing practice configuration is completely removed except for the dacs,
 * and all tables are copied from operational database except dacs and
 * playlists. Objects of this class are intended to be used only once and hold
 * of the state necessary for the copy internally
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 08, 2014  3687     bsteffen    Initial creation.
 * Oct 13, 2014  3654     rjpeter     Updated to use MessageTypeSummary.
 * Oct 08, 2014  3687     bsteffen    Null out some fields during copy.
 * Oct 29, 2014  3746     rjpeter     Reorder clearAllPracticeTables.
 *                                    Auto assignment of transmitter to dac.
 * Nov 18, 2014  3746     rjpeter     Refactored MessageTypeReplacement.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Jan 26, 2015  3928     bsteffen    Copy audio files.
 * Mar 26, 2015  4213     bkowal      Fixed copying of static message types.
 * APr 02, 2015  4248     rjpeter     Use PositionComparator.
 * Apr 07, 2015  4293     bkowal      Copy broadcast message contents and fragments.
 * Apr 16, 2015  4395     rferrel     No longer copy expired {@link InputMessage}s.
 * Apr 16, 2015  4350     rferrel     Fix selected transmitters in {@link #copyInputMessages(Calendar)}.
 * May 12, 2015  4248     rjpeter     Updated copying of static message type.
 * Jul 23, 2015  4676     bkowal      Disseminate a {@link TransmitterGroupConfigNotification} as
 *                                    the practice database tables are truncated.
 * Aug 03, 2015  4350     bkowal      Only assign a practice dac to a practice transmitter group if a
 *                                    dac was originally assigned to the operational group.
 * Dec 09, 2015  5175     bkowal      Practice LDAD Configuration will be removed prior to an
 *                                    operational database copy.
 * Jun 01, 2017  6259     bkowal      Apply required permissions to files and directories as they
 *                                    are copied.
 * Jun 14, 2017  6259     bkowal      Check the source path when determining if a path is a directory
 *                                    while copying audio files.
 * 
 * </pre>
 * 
 * @author bsteffen
 */
public class BmhDatabaseCopier {

    protected static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(BmhDatabaseCopier.class);

    private Map<Integer, Transmitter> transmitterMap;

    private Map<Integer, TransmitterGroup> transmitterGroupMap;

    private Map<Integer, Area> areaMap;

    private Map<Integer, Zone> zoneMap;

    private Map<Integer, MessageTypeSummary> messageTypeMap;

    private Map<Integer, Suite> suiteMap;

    private Map<Integer, InputMessage> inputMessageMap;

    private final IMessageLogger opMessageLogger;

    private final IMessageLogger pracMessageLogger;

    public BmhDatabaseCopier(final IMessageLogger opMessageLogger,
            final IMessageLogger pracMessageLogger) {
        this.opMessageLogger = opMessageLogger;
        this.pracMessageLogger = pracMessageLogger;
    }

    public void copyAll(ITraceable traceable)
            throws EdexException, SerializationException, IOException {
        Calendar currentTime = TimeUtil.newGmtCalendar();
        clearAllPracticeTables(traceable);
        copyDictionaries();
        copyTtsVoices();
        copyTransmitterGroups();
        copyAreas();
        copyZones();
        copyMessageTypes();
        copyTransmitterLanguage();
        copySuites();
        copyPrograms();
        copyInputMessages(currentTime);
        this.copyValidatedMessages(currentTime);
        copyBroadcastMsgs(currentTime);
        BmhMessageProducer.sendConfigMessage(new ResetNotification(), false);
    }

    private void clearAllPracticeTables(ITraceable traceable) {
        clearTable(new PlaylistDao(false, this.pracMessageLogger));
        clearTable(new BroadcastMsgDao(false, this.pracMessageLogger));
        clearTable(new ValidatedMessageDao(false, this.pracMessageLogger));
        clearTable(new InputMessageDao(false, this.pracMessageLogger));
        clearTable(new ProgramDao(false));
        clearTable(new SuiteDao(false));
        clearTable(new MessageTypeDao(false));
        clearTable(new ZoneDao(false));
        clearTable(new AreaDao(false));
        clearTable(new TransmitterLanguageDao(false));
        clearTable(new LdadConfigDao(false));
        clearTable(new TtsVoiceDao(false));
        List<?> objects = clearTable(new TransmitterGroupDao(false));
        if (!CollectionUtils.isEmpty(objects)) {
            List<TransmitterGroup> groups = new ArrayList<>(objects.size());
            for (Object object : objects) {
                groups.add((TransmitterGroup) object);
            }

            try {
                BmhMessageProducer.sendConfigMessage(
                        new TransmitterGroupConfigNotification(
                                ConfigChangeType.Delete, groups, traceable),
                        false);
            } catch (Exception e) {
                statusHandler.error(BMH_CATEGORY.UNKNOWN,
                        "Error occurred sending transmitter group config delete notification for operational database copy",
                        e);
            }
        }
        clearTable(new DictionaryDao(false));
        clearTable(new WordDao(false));
    }

    private List<?> clearTable(AbstractBMHDao<?, ?> dao) {
        List<?> all = dao.getAll();
        if ((all != null) && !all.isEmpty()) {
            dao.deleteAll(all);
        }
        return all;
    }

    private void copyTtsVoices() {
        TtsVoiceDao opDao = new TtsVoiceDao(true);
        TtsVoiceDao prDao = new TtsVoiceDao(false);
        prDao.persistAll(opDao.getAll());
    }

    private void copyTransmitterGroups() {
        TransmitterGroupDao opDao = new TransmitterGroupDao(true);
        TransmitterGroupDao prDao = new TransmitterGroupDao(false);

        List<TransmitterGroup> groups = opDao.getAll();
        Collections.sort(groups, new PositionComparator());
        Map<Integer, TransmitterGroup> transmitterGroupMap = new HashMap<>(
                groups.size(), 1.0f);
        Map<Integer, Transmitter> transmitterMap = new HashMap<>(
                groups.size() * 4, 1.0f);
        Map<Integer, Dac> dacMap = new HashMap<>();
        DacDao prDacDao = new DacDao(false);
        List<Dac> prDacs = prDacDao.getAll();
        Iterator<Dac> dacIter = prDacs.iterator();

        for (TransmitterGroup group : groups) {
            transmitterGroupMap.put(group.getId(), group);
            group.setId(0);
            group.setProgramSummary(null);
            Dac dac = dacMap.get(group.getDac());
            if (dac == null) {
                /*
                 * Only find a dac for the current transmitter group if one had
                 * originally been assigned to it.
                 */
                if (group.getDac() != null && dacIter.hasNext()) {
                    dac = dacIter.next();
                    dacMap.put(group.getDac(), dac);
                    group.setDac(dac.getId());
                } else {
                    group.setDac(null);
                }
            } else {
                group.setDac(dac.getId());
            }

            for (Transmitter transmitter : group.getTransmitters()) {
                transmitterMap.put(transmitter.getId(), transmitter);
                transmitter.setId(0);

                // disable transmitter if no Dac
                if (dac == null) {
                    transmitter.setTxStatus(TxStatus.DISABLED);
                }
            }
        }
        prDao.persistAll(groups);
        this.transmitterMap = transmitterMap;
        this.transmitterGroupMap = transmitterGroupMap;
    }

    private void copyTransmitterLanguage() {
        TransmitterLanguageDao opDao = new TransmitterLanguageDao(true);
        TransmitterLanguageDao prDao = new TransmitterLanguageDao(false);
        List<TransmitterLanguage> langs = opDao.getAll();
        for (TransmitterLanguage lang : langs) {
            lang.setTransmitterGroup(transmitterGroupMap
                    .get(lang.getTransmitterGroup().getId()));
            if ((lang.getStaticMessageTypes() != null)
                    && (!lang.getStaticMessageTypes().isEmpty())) {
                // update the message type references.
                for (StaticMessageType stm : lang.getStaticMessageTypes()) {
                    MessageTypeSummary stmMsg = stm.getMsgTypeSummary();
                    int opMsgTypeId = stmMsg.getId();
                    stm.setMsgTypeSummary(this.messageTypeMap.get(opMsgTypeId));
                    stm.setId(0);
                }
            }
        }
        prDao.persistAll(langs);
    }

    private void copyAreas() {
        AreaDao opDao = new AreaDao(true);
        AreaDao prDao = new AreaDao(false);
        List<Area> areas = opDao.getAll();
        Map<Integer, Area> areaMap = new HashMap<>(areas.size(), 1.0f);
        for (Area area : areas) {
            Set<Transmitter> transmitters = new HashSet<>(
                    area.getTransmitters().size(), 1.0f);
            for (Transmitter transmitter : area.getTransmitters()) {
                transmitters.add(transmitterMap.get(transmitter.getId()));
            }
            area.setTransmitters(transmitters);
            areaMap.put(area.getId(), area);
            area.setId(0);
        }
        prDao.persistAll(areas);
        this.areaMap = areaMap;
    }

    private void copyZones() {
        ZoneDao opDao = new ZoneDao(true);
        ZoneDao prDao = new ZoneDao(false);
        List<Zone> zones = opDao.getAll();
        Map<Integer, Zone> zoneMap = new HashMap<>(zones.size(), 1.0f);
        for (Zone zone : zones) {
            Set<Area> areas = new HashSet<>(zone.getAreas().size(), 1.0f);
            for (Area area : zone.getAreas()) {
                areas.add(areaMap.get(area.getId()));
            }
            zone.setAreas(areas);
            zoneMap.put(zone.getId(), zone);
            zone.setId(0);
        }
        prDao.persistAll(zones);
        this.zoneMap = zoneMap;
    }

    private void copyMessageTypes() {
        MessageTypeDao opDao = new MessageTypeDao(true);
        MessageTypeDao prDao = new MessageTypeDao(false);
        List<MessageType> messageTypes = opDao.getAll();
        Map<Integer, MessageTypeSummary> messageTypeMap = new HashMap<>(
                messageTypes.size(), 1.0f);
        List<Pair<MessageType, Set<MessageTypeSummary>>> replaceMsgs = new LinkedList<>();

        for (MessageType messageType : messageTypes) {
            Set<Transmitter> transmitters = new HashSet<>(
                    messageType.getSameTransmitters().size(), 1.0f);
            for (Transmitter transmitter : messageType.getSameTransmitters()) {
                transmitters.add(transmitterMap.get(transmitter.getId()));
            }
            messageType.setSameTransmitters(transmitters);
            Set<Area> areas = new HashSet<>(
                    messageType.getDefaultAreas().size(), 1.0f);
            for (Area area : messageType.getDefaultAreas()) {
                areas.add(areaMap.get(area.getId()));
            }
            messageType.setDefaultAreas(areas);
            Set<Zone> zones = new HashSet<>(
                    messageType.getDefaultZones().size(), 1.0f);
            for (Zone zone : messageType.getDefaultZones()) {
                zones.add(zoneMap.get(zone.getId()));
            }
            messageType.setDefaultZones(zones);
            Set<TransmitterGroup> transmitterGroups = new HashSet<>(
                    messageType.getDefaultTransmitterGroups().size(), 1.0f);
            for (TransmitterGroup transmitterGroup : messageType
                    .getDefaultTransmitterGroups()) {
                transmitterGroups
                        .add(transmitterGroupMap.get(transmitterGroup.getId()));
            }
            messageType.setDefaultTransmitterGroups(transmitterGroups);
            Set<MessageTypeSummary> replaceMsgSet = messageType
                    .getReplacementMsgs();
            if (!CollectionUtil.isNullOrEmpty(replaceMsgSet)) {
                replaceMsgs.add(new Pair<>(messageType, replaceMsgSet));
            }
            messageType.setReplacementMsgs(null);
            messageTypeMap.put(messageType.getId(), messageType.getSummary());
            messageType.setId(0);
        }
        prDao.persistAll(messageTypes);

        // save any replace messages
        for (Pair<MessageType, Set<MessageTypeSummary>> replaceMsg : replaceMsgs) {
            MessageType mt = replaceMsg.getFirst();
            for (MessageTypeSummary oldReplacedMessage : replaceMsg
                    .getSecond()) {
                mt.addReplacementMsg(
                        messageTypeMap.get(oldReplacedMessage.getId()));
            }
            prDao.persist(mt);
        }

        this.messageTypeMap = messageTypeMap;
    }

    private void copySuites() {
        SuiteDao opDao = new SuiteDao(true);
        SuiteDao prDao = new SuiteDao(false);
        List<Suite> suites = opDao.getAll();
        Map<Integer, Suite> suiteMap = new HashMap<>(suites.size(), 1.0f);
        for (Suite suite : suites) {
            for (SuiteMessage suiteMessage : suite.getSuiteMessages()) {
                suiteMessage.setMsgTypeSummary(messageTypeMap
                        .get(suiteMessage.getMsgTypeSummary().getId()));
            }
            suiteMap.put(suite.getId(), suite);
            suite.setId(0);
        }
        prDao.persistAll(suites);
        this.suiteMap = suiteMap;
    }

    private void copyPrograms() {
        ProgramDao opDao = new ProgramDao(true);
        ProgramDao prDao = new ProgramDao(false);
        List<Program> programs = opDao.getAll();
        for (Program program : programs) {
            for (ProgramSuite programSuite : program.getProgramSuites()) {
                programSuite.setSuite(
                        suiteMap.get(programSuite.getSuite().getId()));
                Set<MessageTypeSummary> messageTypes = new HashSet<>(
                        programSuite.getTriggers().size(), 1.0f);
                for (MessageTypeSummary messageType : programSuite
                        .getTriggers()) {
                    messageTypes.add(messageTypeMap.get(messageType.getId()));
                }
                programSuite.setTriggers(messageTypes);
            }
            Set<TransmitterGroup> transmitterGroups = new HashSet<>(
                    program.getTransmitterGroups().size(), 1.0f);
            for (TransmitterGroup transmitterGroup : program
                    .getTransmitterGroups()) {
                transmitterGroups
                        .add(transmitterGroupMap.get(transmitterGroup.getId()));
            }
            program.setTransmitterGroups(transmitterGroups);
            program.setId(0);
        }
        prDao.persistAll(programs);
    }

    private void copyDictionaries() {
        DictionaryDao opDao = new DictionaryDao(true);
        DictionaryDao prDao = new DictionaryDao(false);
        List<Dictionary> dictionaries = opDao.getAll();
        for (Dictionary dictionary : dictionaries) {
            for (Word word : dictionary.getWords()) {
                word.setId(0);
            }
        }
        prDao.persistAll(dictionaries);
    }

    private void copyInputMessages(Calendar currentTime) {
        InputMessageDao opDao = new InputMessageDao(true, this.opMessageLogger);
        InputMessageDao prDao = new InputMessageDao(false,
                this.pracMessageLogger);
        List<InputMessage> inputMessages = opDao
                .getAllUnexpiredInputMessages(currentTime);

        Map<Integer, InputMessage> inputMessageMap = new HashMap<>(
                inputMessages.size(), 1.0f);
        for (InputMessage inputMessage : inputMessages) {
            inputMessageMap.put(inputMessage.getId(), inputMessage);
            inputMessage.setId(0);
            if (!CollectionUtil
                    .isNullOrEmpty(inputMessage.getSelectedTransmitters())) {
                Set<Transmitter> opSelected = inputMessage
                        .getSelectedTransmitters();
                Set<Transmitter> prSelected = new HashSet<>(opSelected.size(),
                        1.0f);
                for (Transmitter t : opSelected) {
                    prSelected.add(transmitterMap.get(t.getId()));
                }
                inputMessage.setSelectedTransmitters(prSelected);
            }
        }
        prDao.persistAll(inputMessages);
        this.inputMessageMap = inputMessageMap;
    }

    private void copyValidatedMessages(Calendar currentTime) {
        ValidatedMessageDao opDao = new ValidatedMessageDao(true,
                this.opMessageLogger);
        ValidatedMessageDao prDao = new ValidatedMessageDao(false,
                this.pracMessageLogger);
        List<ValidatedMessage> validatedMsgs = opDao
                .getAllUnexpiredMessages(currentTime);
        for (ValidatedMessage validatedMsg : validatedMsgs) {
            validatedMsg.setInputMessage(this.inputMessageMap
                    .get(validatedMsg.getInputMessage().getId()));
            validatedMsg.setId(0);
            if (validatedMsg.getTransmitterGroups().isEmpty()) {
                continue;
            }
            Set<TransmitterGroup> validMsgTransmitterGroups = new HashSet<>(
                    validatedMsg.getTransmitterGroups().size(), 1.0f);
            for (TransmitterGroup tg : validatedMsg.getTransmitterGroups()) {
                validMsgTransmitterGroups
                        .add(this.transmitterGroupMap.get(tg.getId()));
            }
            validatedMsg.setTransmitterGroups(validMsgTransmitterGroups);
        }

        prDao.persistAll(validatedMsgs);
    }

    private void copyBroadcastMsgs(Calendar currentTime) throws IOException {
        BroadcastMsgDao opDao = new BroadcastMsgDao(true, this.opMessageLogger);
        BroadcastMsgDao prDao = new BroadcastMsgDao(false,
                this.pracMessageLogger);
        BroadcastContentsDao prContentsDao = new BroadcastContentsDao(false);

        Path opAudioDir = BMHConstants.getBmhDataDirectory(true)
                .resolve(BMHConstants.AUDIO_DATA_DIRECTORY);
        Path prAudioDir = BMHConstants.getBmhDataDirectory(false)
                .resolve(BMHConstants.AUDIO_DATA_DIRECTORY);

        List<BroadcastMsg> broadcastMsgs = opDao
                .getAllUnexpiredMessages(currentTime);
        Map<Long, BroadcastMsg> broadcastMsgMap = new HashMap<>(
                broadcastMsgs.size(), 1.0f);
        List<BroadcastContents> contentsToCopy = new ArrayList<>();

        for (BroadcastMsg broadcastMsg : broadcastMsgs) {
            broadcastMsg.setTransmitterGroup(transmitterGroupMap
                    .get(broadcastMsg.getTransmitterGroup().getId()));
            broadcastMsg.setInputMessage(inputMessageMap
                    .get(broadcastMsg.getInputMessage().getId()));
            broadcastMsgMap.put(broadcastMsg.getId(), broadcastMsg);
            broadcastMsg.setId(0);
            if (broadcastMsg.getContents() == null
                    || broadcastMsg.getContents().isEmpty()) {
                continue;
            }

            /*
             * Only the latest Broadcast Contents are copied to the practice
             * database instead of all broadcast contents.
             */
            contentsToCopy.add(broadcastMsg.getLatestBroadcastContents());
            /*
             * Initially save the message without any contents.
             */
            broadcastMsg.getContents().clear();
        }
        prDao.persistAll(broadcastMsgs);

        for (BroadcastContents contents : contentsToCopy) {
            if (contents.getFragments() == null
                    || contents.getFragments().isEmpty()) {
                continue;
            }

            for (BroadcastFragment broadcastFragment : contents
                    .getFragments()) {
                Path output = Paths.get(broadcastFragment.getOutputName());
                if (output.startsWith(opAudioDir)) {
                    Path newOutput = prAudioDir
                            .resolve(opAudioDir.relativize(output));
                    if (Files.isDirectory(output)) {
                        Files.walkFileTree(output,
                                new DirectoryCopier(output, newOutput));
                    } else {
                        com.raytheon.uf.common.util.file.Files
                                .createDirectories(newOutput.getParent(),
                                        FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
                        Files.copy(output, newOutput,
                                StandardCopyOption.REPLACE_EXISTING);
                        IOPermissionsHelper.applyFilePermissions(newOutput,
                                FilePermissionUtils.FILE_PERMISSIONS_SET);
                    }
                    broadcastFragment.setOutputName(newOutput.toString());
                }
                broadcastFragment.setId(0);
            }

            /*
             * No reason to alter the timestamp because we do not alter the
             * timestamped file path or name.
             */
            contents.setBroadcastMsg(
                    broadcastMsgMap.get(contents.getId().getBroadcastId()));
        }
        prContentsDao.persistAll(contentsToCopy);
    }

    /**
     * Utility visitor for recursive directory copying.
     */
    private static final class DirectoryCopier extends SimpleFileVisitor<Path> {

        private final Path oldRoot;

        private final Path newRoot;

        public DirectoryCopier(Path oldRoot, Path newRoot) {
            this.oldRoot = oldRoot;
            this.newRoot = newRoot;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {
            Path newDir = newRoot.resolve(oldRoot.relativize(dir));
            com.raytheon.uf.common.util.file.Files.createDirectories(newDir,
                    FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Path newFile = newRoot.resolve(oldRoot.relativize(file));
            Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
            IOPermissionsHelper.applyFilePermissions(newFile,
                    FilePermissionUtils.FILE_PERMISSIONS_SET);
            return FileVisitResult.CONTINUE;
        }
    }

}
