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

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.legacy.ascii.AsciiFileTranslator;
import com.raytheon.uf.common.bmh.legacy.ascii.BmhData;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeReplacementDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.IMessageProducer;

/**
 * Imports legacy database and stores it. Will only run on start up. Moves
 * legacy file to .processed after running.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3175       rjpeter     Initial creation
 * Aug 19, 2014 3411       mpduff      Add handling for MessageTypeReplacement
 * Aug 25, 2014 3486       bsteffen    Send config change notification.
 * Aug 25, 2014 3558       rjpeter     Updated DAC population.
 * Sep 05, 2014 3554       bsteffen    Send more specific config change notification.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class DatabaseImport {
    // Default voice information if there is not one currently in the database
    protected static final int DEFAULT_VOICE_NUMBER = 101;

    protected static final String DEFAULT_VOICE_NAME = "Paul";

    protected static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(DatabaseImport.class);

    private boolean runImport;

    private String databaseDir;

    public boolean isRunImport() {
        return runImport;
    }

    public void setRunImport(boolean runImport) {
        this.runImport = runImport;
    }

    public String getDatabaseDir() {
        return databaseDir;
    }

    public void setDatabaseDir(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    public void checkImport() throws Exception {
        if (runImport) {
            File dir = new File(databaseDir);

            if (!dir.exists() && !dir.mkdirs()) {
                statusHandler.error(BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                        "Failed to create directory [" + dir.getAbsolutePath()
                                + "].  Cannot import legacy database");
                return;
            }

            if (!dir.isDirectory()) {
                statusHandler.error(
                        BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                        "Legacy import directory is not a directory ["
                                + dir.getAbsolutePath()
                                + "].  Cannot import legacy database");
                return;
            }

            List<Pair<Integer, Integer>> availableDacPorts = getAvailableDacs();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                    dir.toPath(), "*.ASC")) {
                for (Path path : stream) {
                    File file = path.toFile();
                    if (file.isFile()) {
                        // only process first one
                        statusHandler.info("Importing Legacy Database ["
                                + file.getAbsolutePath() + "]");

                        BmhData data = null;
                        try {
                            // Scan for TtsVoices
                            TtsVoiceDao voiceDao = new TtsVoiceDao();
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

                            AsciiFileTranslator asciiFile = new AsciiFileTranslator(
                                    file, false, voices);
                            data = asciiFile.getTranslatedData();
                            List<String> msgs = asciiFile
                                    .getValidationMessages();
                            for (String msg : msgs) {
                                statusHandler.warn(
                                        BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                                        msg);
                            }
                        } catch (Exception e) {
                            statusHandler.error(
                                    BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                                    "Error occurred parsing legacy database ["
                                            + file.getAbsolutePath() + "]", e);
                            throw e;
                        }

                        if (data != null) {
                            try {
                                Iterator<Pair<Integer, Integer>> portIter = availableDacPorts
                                        .iterator();
                                Iterator<TransmitterGroup> tgIter = data
                                        .getTransmitters().values().iterator();
                                while (tgIter.hasNext()) {
                                    TransmitterGroup tg = tgIter.next();
                                    if (portIter.hasNext()) {
                                        Pair<Integer, Integer> dacPort = portIter
                                                .next();
                                        tg.setDac(dacPort.getFirst());
                                        tg.getTransmitterList()
                                                .get(0)
                                                .setDacPort(dacPort.getSecond());
                                    } else {
                                        tg.getTransmitterList().get(0)
                                                .setTxStatus(TxStatus.DISABLED);
                                    }
                                }

                                // validate data stores and can be retrieved
                                TransmitterGroupDao tgDao = new TransmitterGroupDao();
                                tgDao.persistAll(data.getTransmitters()
                                        .values());
                                statusHandler.info("Saved "
                                        + data.getTransmitters().values()
                                                .size() + " transmitters");
                                tgDao.loadAll();

                                TransmitterLanguageDao langDao = new TransmitterLanguageDao();
                                langDao.persistAll(data
                                        .getTransmitterLanguages());
                                statusHandler.info("Saved "
                                        + data.getTransmitterLanguages().size()
                                        + " transmitter languages");
                                langDao.loadAll();
                                AreaDao areaDao = new AreaDao();
                                areaDao.persistAll(data.getAreas().values());
                                statusHandler.info("Saved "
                                        + data.getAreas().values().size()
                                        + " areas");
                                areaDao.loadAll();
                                ZoneDao zoneDao = new ZoneDao();
                                zoneDao.persistAll(data.getZones().values());
                                statusHandler.info("Saved "
                                        + data.getZones().values().size()
                                        + " zones");
                                zoneDao.loadAll();
                                MessageTypeDao msgTypeDao = new MessageTypeDao();
                                msgTypeDao.persistAll(data.getMsgTypes()
                                        .values());
                                statusHandler.info("Saved "
                                        + data.getMsgTypes().values().size()
                                        + " message types");
                                msgTypeDao.loadAll();
                                MessageTypeReplacementDao msgTypeReplacementDao = new MessageTypeReplacementDao();
                                msgTypeReplacementDao.persistAll(data
                                        .getReplaceList());
                                statusHandler.info("Saved "
                                        + data.getReplaceList().size()
                                        + " replacement message types");
                                msgTypeReplacementDao.loadAll();
                                SuiteDao suiteDao = new SuiteDao();
                                suiteDao.persistAll(data.getSuites().values());
                                statusHandler.info("Saved "
                                        + data.getSuites().values().size()
                                        + " suites");
                                suiteDao.loadAll();
                                ProgramDao programDao = new ProgramDao();
                                programDao.persistAll(data.getPrograms()
                                        .values());
                                statusHandler.info("Saved "
                                        + data.getPrograms().values().size()
                                        + " programs");
                                programDao.loadAll();

                                if (!file.renameTo(new File(file
                                        .getAbsolutePath() + ".processed"))) {
                                    statusHandler
                                            .error(BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                                                    "Could not rename Legacy Database ["
                                                            + file.getAbsolutePath()
                                                            + "]");
                                }
                                IMessageProducer producer = EDEXUtil
                                        .getMessageProducer();
                                ConfigNotification notification = new TransmitterGroupConfigNotification(
                                        ConfigChangeType.Update,
                                        new ArrayList<>(data.getTransmitters()
                                                .values()));
                                producer.sendAsyncUri(
                                        "jms-durable:topic:BMH.Config",
                                        SerializationUtil
                                                .transformToThrift(notification));
                                for (Suite suite : data.getSuites().values()) {
                                    notification = new SuiteConfigNotification(
                                            ConfigChangeType.Update, suite);
                                    producer.sendAsyncUri(
                                            "jms-durable:topic:BMH.Config",
                                            SerializationUtil
                                                    .transformToThrift(notification));
                                }
                            } catch (Throwable e) {
                                statusHandler
                                        .error(BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                                                "Error occurred saving legacy data to database",
                                                e);
                                throw e;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the available Dac ports. TODO: This should just use what is in db
     * and not load from properties.
     * 
     * @return
     */
    protected List<Pair<Integer, Integer>> getAvailableDacs() {
        List<Pair<Integer, Integer>> availableDacPorts = null;
        String availablePortsProp = System.getProperty("bmh.dac.ports");

        DacDao dacDao = new DacDao();
        List<Object> rows = dacDao.loadAll();
        if ((rows == null) || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Dac> dacs = new HashMap<>(rows.size());
        for (Object row : rows) {
            Dac dac = (Dac) row;
            dac.setDataPorts(new HashSet<Integer>());
            dacs.put(dac.getId(), dac);
        }

        if (availablePortsProp != null) {
            try {
                String[] tokens = availablePortsProp.split(",");

                availableDacPorts = new ArrayList<>(tokens.length / 3);
                for (String token : tokens) {
                    String[] dacPort = token.split(":");
                    if (dacPort.length == 3) {
                        int dacId = Integer.parseInt(dacPort[0]);
                        Dac dac = dacs.get(dacId);
                        if (dac != null) {
                            int dacOutputLine = Integer.parseInt(dacPort[1]);
                            int dacChannelPort = Integer.parseInt(dacPort[2]);
                            dac.getDataPorts().add(dacChannelPort);
                            availableDacPorts.add(new Pair<Integer, Integer>(
                                    dacId, dacOutputLine));
                        }
                    }
                }
            } catch (Exception e) {
                statusHandler.error(BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                        "Unable to parse bmh.dac.ports property: "
                                + availablePortsProp, e);
            }
        } else {
            availableDacPorts = Collections.emptyList();
        }

        dacDao.persistAll(dacs.values());
        return availableDacPorts;
    }
}
