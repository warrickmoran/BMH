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
package com.raytheon.uf.edex.bmh.msg.validator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * 
 * Filter which checks a {@link CharSequence} for unacceptable words in a
 * specified {@link Language}. Unacceptable words are loaded from files in
 * ${BMH_HOME}/conf/unacceptableWords.${LANGAUGE}.txt. The format of this file
 * is one word or phrase per line, the '#' character is used for comment lines.
 * 
 * Since there is a relatively small number of languages, a filter for each
 * language is statically cached and available from the
 * {@link #getFilter(Language)} method. For convenience of checking
 * {@link InputMessage}s a static method is provided which performs the check
 * using the correct filter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Dec 02, 2014  3614     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class UnacceptableWordFilter {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(UnacceptableWordFilter.class);

    /**
     * When the file does not exist or does not contain any unnacceptable words.
     */
    protected static final Pattern UNMATCHABLE_PATTERN = Pattern.compile("$^");

    /**
     * The file containing unnacceptable words in the specified langauge.
     */
    private final Path file;

    /**
     * A pattern which will match any words from the {@link #file}.
     */
    private Pattern pattern;

    /**
     * The mod time of the {@link #file} when {@link #pattern} was loaded.
     */
    private long modTime;

    /**
     * filters should be retrieved from {@link #getFilter(Language)}. On
     * construction the file is not loaded so {@link #refresh()} should be
     * called before using the filter.
     */
    protected UnacceptableWordFilter(Language language) {
        String fileName = "unacceptableWords."
                + language.getIdentifier().toLowerCase() + ".txt";
        this.file = Paths.get(BMHConstants.getBmhHomeDirectory())
                .resolve("conf").resolve(fileName);
        this.pattern = UNMATCHABLE_PATTERN;
        this.modTime = 0;
    }

    /**
     * Check the files mod time and if it has changed reload.
     */
    private synchronized void refresh() {
        if (Files.exists(file)) {
            long modTime = this.modTime + 1;
            try {
                modTime = Files.getLastModifiedTime(file).toMillis();
            } catch (IOException e) {
                statusHandler
                        .error(BMH_CATEGORY.MESSAGE_VALIDATION_ERROR,
                                "Unable to check unacceptable word file modification time.",
                                e);
            }
            if (modTime > this.modTime) {
                this.modTime = modTime;
                load();
            }
        } else {
            this.pattern = UNMATCHABLE_PATTERN;
            this.modTime = 0;
        }
    }

    /**
     * Load the contents of {@link #file} into {@link #pattern}.
     */
    private void load() {
        List<String> lines = Collections.emptyList();
        try {
            lines = Files.readAllLines(file, Charset.defaultCharset());
        } catch (IOException e) {
            statusHandler
                    .error(BMH_CATEGORY.MESSAGE_VALIDATION_ERROR,
                            "Unable to load unacceptable words. All words will be accepted.",
                            e);
            return;
        }

        StringBuilder patternBuilder = new StringBuilder(lines.size() * 8);
        patternBuilder.append("\\b(");
        /*
         * Length is used to determine if there are any words added to the
         * pattern.
         */
        int emptyLength = patternBuilder.length();
        for (String line : lines) {
            int endIndex = line.indexOf("#");
            if (endIndex >= 0) {
                line = line.substring(0, endIndex);
            }
            line = line.trim();
            if (!line.isEmpty()) {
                line = line.replaceAll("\\s+", "\\\\s+");
                if (patternBuilder.length() > emptyLength) {
                    patternBuilder.append("|");
                }
                patternBuilder.append(line);

            }
        }
        if (patternBuilder.length() == emptyLength) {
            this.pattern = UNMATCHABLE_PATTERN;
        } else {
            patternBuilder.append(")\\b");
            this.pattern = Pattern.compile(patternBuilder.toString(),
                    Pattern.CASE_INSENSITIVE);
        }

    }

    /**
     * 
     * @param message
     *            a {@link CharSequence} to check for unacceptable words
     * @return a {@link List} of all the unnaceptable words found in message
     */
    public synchronized List<String> check(CharSequence message) {
        Matcher matcher = this.pattern.matcher(message);
        List<String> results = new ArrayList<String>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    private static Map<Language, UnacceptableWordFilter> filters = createInitialMap();

    /**
     * Retrieve a {@link UnacceptableWordFilter} for the provided language.
     */
    public static UnacceptableWordFilter getFilter(Language language) {
        UnacceptableWordFilter filter = filters.get(language);
        filter.refresh();
        return filter;
    }

    /**
     * Convenience method to get a filter and check the contents of an
     * {@link InputMessage}
     * 
     * @param message
     *            a {@link InputMessage} to check for unacceptable words
     * @return a {@link List} of all the unnaceptable words found in message
     */
    public static List<String> check(InputMessage message) {
        return getFilter(message.getLanguage()).check(message.getContent());
    }

    /**
     * Always populate the map when the class is loaded to avoid any concurrency
     * issues modifying the map.
     */
    private static Map<Language, UnacceptableWordFilter> createInitialMap() {
        Map<Language, UnacceptableWordFilter> result = new EnumMap<>(
                Language.class);
        for (Language language : Language.values()) {
            result.put(language, new UnacceptableWordFilter(language));
        }
        return result;
    }

}
