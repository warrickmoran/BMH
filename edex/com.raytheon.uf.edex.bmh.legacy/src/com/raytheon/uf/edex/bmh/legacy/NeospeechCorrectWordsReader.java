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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.BMHConstants;

/**
 * Reads the list of words in the neospeech correct text files. Will
 * automatically reload the words whenever a file is updated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 9, 2015  5112       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class NeospeechCorrectWordsReader {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NeospeechCorrectWordsReader.class);

    private static final String CORRECT_TXT_FORMAT = "neospeechCorrect.%s.txt";

    private final Language language;

    private final Path wordsPath;

    private final Set<String> words = new HashSet<>();

    private Long lastReadTime = null;

    public NeospeechCorrectWordsReader(Language language) {
        this.language = language;
        this.wordsPath = Paths.get(BMHConstants.getBmhConfDirectory(), String
                .format(CORRECT_TXT_FORMAT, language.getIdentifier()
                        .toLowerCase()));
    }

    public synchronized Set<String> getCorrectWords() throws Exception {
        if (Files.exists(wordsPath) == false) {
            statusHandler
                    .warn("Unable to find the correct words file for language: "
                            + language.toString()
                            + " at the expected location: "
                            + wordsPath.toString() + ".");
            return Collections.emptySet();
        }

        if (this.readUpdates()) {
            this.words.clear();
            this.lastReadTime = Files.getLastModifiedTime(wordsPath).toMillis();
            statusHandler.info("Reading neospeech correct file: "
                    + this.wordsPath.toString() + " ...");
            this.words.addAll(Files.readAllLines(wordsPath,
                    Charset.defaultCharset()));
        }

        return this.words;
    }

    private boolean readUpdates() throws Exception {
        if (this.lastReadTime == null) {
            return true;
        }

        return (this.lastReadTime.longValue() < Files.getLastModifiedTime(
                wordsPath).toMillis());
    }
}