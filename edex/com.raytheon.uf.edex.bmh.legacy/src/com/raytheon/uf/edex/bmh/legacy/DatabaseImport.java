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
import java.util.List;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.legacy.ascii.AsciiFileTranslator;
import com.raytheon.uf.common.bmh.legacy.ascii.BmhData;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

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
                                // TODO: IGNORE DICTIONARIES
                                // DictionaryDao dictDao = new DictionaryDao();
                                // dictDao.persistAll(data.getDictionaries()
                                // .values());

                                TransmitterGroupDao tgDao = new TransmitterGroupDao();
                                tgDao.persistAll(data.getTransmitters()
                                        .values());
                                TransmitterLanguageDao langDao = new TransmitterLanguageDao();
                                langDao.persistAll(data
                                        .getTransmitterLanguages());
                                AreaDao areaDao = new AreaDao();
                                areaDao.persistAll(data.getAreas().values());
                                ZoneDao zoneDao = new ZoneDao();
                                zoneDao.persistAll(data.getZones().values());
                                MessageTypeDao msgTypeDao = new MessageTypeDao();
                                msgTypeDao.persistAll(data.getMsgTypes()
                                        .values());
                                SuiteDao suiteDao = new SuiteDao();
                                suiteDao.persistAll(data.getSuites().values());
                                ProgramDao programDao = new ProgramDao();
                                programDao.persistAll(data.getPrograms()
                                        .values());

                                if (!file.renameTo(new File(file
                                        .getAbsolutePath() + ".processed"))) {
                                    statusHandler
                                            .error(BMH_CATEGORY.LEGACY_DATABASE_IMPORT,
                                                    "Could not rename Legacy Database ["
                                                            + file.getAbsolutePath()
                                                            + "]");
                                }
                            } catch (Exception e) {
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
}
