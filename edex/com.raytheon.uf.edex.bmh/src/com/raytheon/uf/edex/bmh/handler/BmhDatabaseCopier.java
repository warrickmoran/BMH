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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeReplacement;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeReplacementDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.dao.WordDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BmhDatabaseCopier {

    private Map<Integer, Transmitter> transmitterMap;

    private Map<Integer, TransmitterGroup> transmitterGroupMap;

    private Map<Integer, Area> areaMap;

    private Map<Integer, Zone> zoneMap;

    private Map<Integer, MessageTypeSummary> messageTypeMap;

    private Map<Integer, Suite> suiteMap;

    private Map<Integer, InputMessage> inputMessageMap;

    public void copyAll() throws EdexException, SerializationException {
        clearAllPracticeTables();
        copyDictionaries();
        copyTtsVoices();
        copyTransmitterGroups();
        copyTransmitterLanguage();
        copyAreas();
        copyZones();
        copyMessageTypes();
        copyMessageTypeReplacements();
        copySuites();
        copyPrograms();
        copyInputMessages();
        copyBroadcastMsgs();
        BmhMessageProducer.sendConfigMessage(new ResetNotification(), false);
    }

    private void clearAllPracticeTables() {
        clearTable(new PlaylistDao(false));
        clearTable(new BroadcastMsgDao(false));
        clearTable(new ValidatedMessageDao(false));
        clearTable(new InputMessageDao(false));
        clearTable(new ProgramDao(false));
        clearTable(new SuiteDao(false));
        clearTable(new MessageTypeReplacementDao(false));
        clearTable(new MessageTypeDao(false));
        clearTable(new ZoneDao(false));
        clearTable(new AreaDao(false));
        clearTable(new TransmitterLanguageDao(false));
        clearTable(new TtsVoiceDao(false));
        clearTable(new TransmitterGroupDao(false));
        clearTable(new DictionaryDao(false));
        clearTable(new WordDao(false));
    }

    private void clearTable(AbstractBMHDao<?, ?> dao) {
        List<?> all = dao.getAll();
        if ((all != null) && !all.isEmpty()) {
            dao.deleteAll(all);
        }
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
        Collections.sort(groups, new TransmitterGroupPositionComparator());
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
                if (dacIter.hasNext()) {
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
            lang.setTransmitterGroup(transmitterGroupMap.get(lang
                    .getTransmitterGroup().getId()));
        }
        prDao.persistAll(langs);
    }

    private void copyAreas() {
        AreaDao opDao = new AreaDao(true);
        AreaDao prDao = new AreaDao(false);
        List<Area> areas = opDao.getAll();
        Map<Integer, Area> areaMap = new HashMap<>(areas.size(), 1.0f);
        for (Area area : areas) {
            Set<Transmitter> transmitters = new HashSet<>(area
                    .getTransmitters().size(), 1.0f);
            for (Transmitter transmitter : area.getTransmitters()) {
                transmitters.add(transmitterMap.get(transmitter.getId()));
            }
            area.setTransmitters(transmitters);
            areaMap.put(area.getAreaId(), area);
            area.setAreaId(0);
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
                areas.add(areaMap.get(area.getAreaId()));
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
        for (MessageType messageType : messageTypes) {
            Set<Transmitter> transmitters = new HashSet<>(messageType
                    .getSameTransmitters().size(), 1.0f);
            for (Transmitter transmitter : messageType.getSameTransmitters()) {
                transmitters.add(transmitterMap.get(transmitter.getId()));
            }
            messageType.setSameTransmitters(transmitters);
            Set<Area> areas = new HashSet<>(messageType.getDefaultAreas()
                    .size(), 1.0f);
            for (Area area : messageType.getDefaultAreas()) {
                areas.add(areaMap.get(area.getAreaId()));
            }
            messageType.setDefaultAreas(areas);
            Set<Zone> zones = new HashSet<>(messageType.getDefaultZones()
                    .size(), 1.0f);
            for (Zone zone : messageType.getDefaultZones()) {
                zones.add(zoneMap.get(zone.getId()));
            }
            messageType.setDefaultZones(zones);
            Set<TransmitterGroup> transmitterGroups = new HashSet<>(messageType
                    .getDefaultTransmitterGroups().size(), 1.0f);
            for (TransmitterGroup transmitterGroup : messageType
                    .getDefaultTransmitterGroups()) {
                transmitterGroups.add(transmitterGroupMap.get(transmitterGroup
                        .getId()));
            }
            messageType.setDefaultTransmitterGroups(transmitterGroups);
            messageType.setReplacementMsgs(null);
            messageTypeMap.put(messageType.getId(), messageType.getSummary());
            messageType.setId(0);
        }
        prDao.persistAll(messageTypes);
        this.messageTypeMap = messageTypeMap;
    }

    private void copyMessageTypeReplacements() {
        MessageTypeReplacementDao opDao = new MessageTypeReplacementDao(true);
        MessageTypeReplacementDao prDao = new MessageTypeReplacementDao(false);
        List<MessageTypeReplacement> messageTypeReplacements = opDao.getAll();
        for (MessageTypeReplacement messageTypeReplacement : messageTypeReplacements) {
            messageTypeReplacement.setMsgType(messageTypeMap
                    .get(messageTypeReplacement.getId().getMsgId()));
            messageTypeReplacement.setReplaceMsgType(messageTypeMap
                    .get(messageTypeReplacement.getId().getReplaceId()));
        }
        prDao.persistAll(messageTypeReplacements);
    }

    private void copySuites() {
        SuiteDao opDao = new SuiteDao(true);
        SuiteDao prDao = new SuiteDao(false);
        List<Suite> suites = opDao.getAll();
        Map<Integer, Suite> suiteMap = new HashMap<>(suites.size(), 1.0f);
        for (Suite suite : suites) {
            for (SuiteMessage suiteMessage : suite.getSuiteMessages()) {
                suiteMessage.setMsgTypeSummary(messageTypeMap.get(suiteMessage
                        .getMsgTypeSummary().getId()));
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
                programSuite.setSuite(suiteMap.get(programSuite.getSuite()
                        .getId()));
                Set<MessageTypeSummary> messageTypes = new HashSet<>(
                        programSuite.getTriggers().size(), 1.0f);
                for (MessageTypeSummary messageType : programSuite
                        .getTriggers()) {
                    messageTypes.add(messageTypeMap.get(messageType.getId()));
                }
                programSuite.setTriggers(messageTypes);
            }
            Set<TransmitterGroup> transmitterGroups = new HashSet<>(program
                    .getTransmitterGroups().size(), 1.0f);
            for (TransmitterGroup transmitterGroup : program
                    .getTransmitterGroups()) {
                transmitterGroups.add(transmitterGroupMap.get(transmitterGroup
                        .getId()));
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

    private void copyInputMessages() {
        InputMessageDao opDao = new InputMessageDao(true);
        InputMessageDao prDao = new InputMessageDao(false);
        List<InputMessage> inputMessages = opDao.getAll();
        Map<Integer, InputMessage> inputMessageMap = new HashMap<>(
                inputMessages.size(), 1.0f);
        for (InputMessage inputMessage : inputMessages) {
            inputMessageMap.put(inputMessage.getId(), inputMessage);
            inputMessage.setId(0);
        }
        prDao.persistAll(inputMessages);
        this.inputMessageMap = inputMessageMap;
    }

    private void copyBroadcastMsgs() {
        BroadcastMsgDao opDao = new BroadcastMsgDao(true);
        BroadcastMsgDao prDao = new BroadcastMsgDao(false);
        List<BroadcastMsg> broadcastMsgs = opDao.getAll();
        for (BroadcastMsg broadcastMsg : broadcastMsgs) {
            broadcastMsg.setTransmitterGroup(transmitterGroupMap
                    .get(broadcastMsg.getTransmitterGroup().getId()));
            broadcastMsg.setInputMessage(inputMessageMap.get(broadcastMsg
                    .getInputMessage().getId()));
            for (BroadcastFragment broadcastFragment : broadcastMsg
                    .getFragments()) {
                broadcastFragment.setId(0);
            }
            broadcastMsg.setId(0);
        }
        prDao.persistAll(broadcastMsgs);
    }

}
