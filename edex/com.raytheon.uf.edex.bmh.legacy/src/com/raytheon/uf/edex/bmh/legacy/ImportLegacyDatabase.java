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
package com.raytheon.uf.edex.bmh.legacy;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.dac.DacComparator;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.legacy.ascii.AsciiFileTranslator;
import com.raytheon.uf.common.bmh.legacy.ascii.BmhData;
import com.raytheon.uf.common.bmh.notify.config.ResetNotification;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Imports legacy BmhData to the database. Assumes data is already verified.
 * Based on the old DatabaseImport class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 10, 2014 3824       rferrel     Initial creation
 * Jan 06, 2015 3651       bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Feb 10, 2015 4058       rjpeter     Added auto assigning of DACs.
 * Feb 26, 2015 4187       rjpeter     Respect operational flag on reset notification.
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class ImportLegacyDatabase {

    protected static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(ImportLegacyDatabase.class);

    private final String input;

    private final String source;

    private final boolean operational;

    private final IMessageLogger messageLogger;

    public ImportLegacyDatabase(String input, String source,
            boolean operational, final IMessageLogger messageLogger) {
        this.input = input;
        this.source = source;
        this.operational = operational;
        this.messageLogger = messageLogger;
    }

    public void saveImport() throws Exception {

        statusHandler.info("Start Importing Legacy Database: " + source);
        BmhData data = null;
        // Scan for TtsVoices
        TtsVoiceDao voiceDao = new TtsVoiceDao(operational);
        List<TtsVoice> voices = voiceDao.getAll();
        if ((voices == null) || (voices.size() == 0)) {
            TtsVoice voice = new TtsVoice();
            voice.setVoiceNumber(101);
            voice.setVoiceName("Paul");
            voice.setLanguage(Language.ENGLISH);
            voice.setMale(true);
            voiceDao.create(voice);
            voices.add(voice);
        }

        clearTables();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        AsciiFileTranslator asciiFile = new AsciiFileTranslator(reader, source,
                false, voices);
        data = asciiFile.getTranslatedData();

        if (data != null) {
            try {
                List<Pair<Integer, Integer>> availableDacPorts = getAvailableDacs();
                Iterator<Pair<Integer, Integer>> portIter = availableDacPorts
                        .iterator();
                Iterator<TransmitterGroup> tgIter = data.getTransmitters()
                        .values().iterator();
                while (tgIter.hasNext()) {
                    TransmitterGroup tg = tgIter.next();
                    if (portIter.hasNext()) {
                        Pair<Integer, Integer> dacPort = portIter.next();
                        tg.setDac(dacPort.getFirst());
                        tg.getTransmitterList().get(0)
                                .setDacPort(dacPort.getSecond());
                    } else {
                        tg.getTransmitterList().get(0)
                                .setTxStatus(TxStatus.DISABLED);
                    }
                }

                // validate data stores and can be retrieved
                TransmitterGroupDao tgDao = new TransmitterGroupDao(operational);
                tgDao.persistAll(data.getTransmitters().values());
                statusHandler.info("Saved "
                        + data.getTransmitters().values().size()
                        + " transmitters");
                tgDao.loadAll();

                TransmitterLanguageDao langDao = new TransmitterLanguageDao(
                        operational);
                langDao.persistAll(data.getTransmitterLanguages());
                statusHandler.info("Saved "
                        + data.getTransmitterLanguages().size()
                        + " transmitter languages");
                langDao.loadAll();

                AreaDao areaDao = new AreaDao(operational);
                areaDao.persistAll(data.getAreas().values());
                statusHandler.info("Saved " + data.getAreas().values().size()
                        + " areas");
                areaDao.loadAll();

                ZoneDao zoneDao = new ZoneDao(operational);
                zoneDao.persistAll(data.getZones().values());
                statusHandler.info("Saved " + data.getZones().values().size()
                        + " zones");
                zoneDao.loadAll();

                MessageTypeDao msgTypeDao = new MessageTypeDao(operational);
                msgTypeDao.persistAll(data.getMsgTypes().values());
                statusHandler
                        .info("Saved " + data.getMsgTypes().values().size()
                                + " message types");
                msgTypeDao.loadAll();
                for (Map.Entry<MessageType, Set<MessageTypeSummary>> entry : data
                        .getReplaceMap().entrySet()) {
                    MessageType mt = entry.getKey();
                    mt.setReplacementMsgs(entry.getValue());
                }
                msgTypeDao.persistAll(data.getReplaceMap().keySet());
                statusHandler.info("Saved " + data.getReplaceMap().size()
                        + " replacement message types");

                SuiteDao suiteDao = new SuiteDao(operational);
                suiteDao.persistAll(data.getSuites().values());
                statusHandler.info("Saved " + data.getSuites().values().size()
                        + " suites");
                suiteDao.loadAll();

                ProgramDao programDao = new ProgramDao(operational);
                programDao.persistAll(data.getPrograms().values());
                statusHandler.info("Saved "
                        + data.getPrograms().values().size() + " programs");
                programDao.loadAll();

                /*
                 * update transmitter status based on program now that program
                 * link has been added
                 */
                TransmitterDao tDao = new TransmitterDao(operational);
                for (TransmitterGroup tg : tgDao.loadAll()) {
                    for (Transmitter t : tg.getTransmitterList()) {
                        TxStatus newStatus = t.getTxStatus();
                        if ((newStatus == TxStatus.ENABLED)) {
                            if ((t.getDacPort() == null)
                                    || (tg.getDac() == null)) {
                                newStatus = TxStatus.DISABLED;
                            } else {
                                // Check for GENERAL suite.
                                ProgramSummary ps = t.getTransmitterGroup()
                                        .getProgramSummary();
                                if (ps == null) {
                                    newStatus = TxStatus.DISABLED;
                                } else {
                                    Program p = data.getPrograms().get(
                                            ps.getName());
                                    newStatus = TxStatus.DISABLED;
                                    List<Suite> suites = p.getSuites();
                                    for (Suite s : suites) {
                                        if (s.getType() == SuiteType.GENERAL) {
                                            newStatus = TxStatus.ENABLED;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (newStatus != t.getTxStatus()) {
                            t.setTxStatus(newStatus);
                            tDao.saveOrUpdate(t);
                        }
                    }
                }

                BmhMessageProducer.sendConfigMessage(new ResetNotification(),
                        operational);
            } catch (Throwable e) {
                statusHandler.error(BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                        "Error occurred saving legacy data to database", e);
                throw e;
            }
        }
        statusHandler.info("Finished Importing Legacy Database: " + source);
    }

    /**
     * Clear tables except DAC, TtsVoice, Dictionary and Word.
     */
    private void clearTables() {
        clearTable(new PlaylistDao(operational, this.messageLogger));
        clearTable(new BroadcastMsgDao(operational, this.messageLogger));
        clearTable(new ValidatedMessageDao(operational, this.messageLogger));
        clearTable(new InputMessageDao(operational, this.messageLogger));
        clearTable(new ProgramDao(operational));
        clearTable(new SuiteDao(operational));
        clearTable(new MessageTypeDao(operational));
        clearTable(new ZoneDao(operational));
        clearTable(new AreaDao(operational));
        clearTable(new TransmitterLanguageDao(operational));
        clearTable(new TransmitterGroupDao(operational));
    }

    private void clearTable(AbstractBMHDao<?, ?> dao) {
        List<?> all = dao.getAll();
        if ((all != null) && (all.size() > 0)) {
            dao.deleteAll(all);
        }
    }

    /**
     * Returns the available DAC ports.
     * 
     * @return
     */
    protected List<Pair<Integer, Integer>> getAvailableDacs() {
        List<Pair<Integer, Integer>> availableDacPorts = null;
        DacDao dacDao = new DacDao(operational);

        List<Dac> rows = dacDao.loadAll();
        if ((rows == null) || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.sort(rows, new DacComparator());
        availableDacPorts = new ArrayList<>(rows.size() * 4);

        for (Dac dac : rows) {
            Set<Integer> ports = dac.getDataPorts();
            if (CollectionUtil.isNullOrEmpty(ports) == false) {
                int portVal = 1;
                for (@SuppressWarnings("unused")
                Integer port : ports) {
                    availableDacPorts.add(new Pair<>(dac.getId(), portVal++));
                }
            }
        }

        return availableDacPorts;
    }

}
