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
package com.raytheon.uf.common.bmh.legacy.ascii;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File parser for legacy ascii database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3175       rjpeter     Initial creation.
 * Oct 13, 2014 3654       rjpeter     Fixed words running together on line break.
 * Dec 10, 2014 3824       rferrel     Constructor for reader and source.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class AsciiFileParser implements Closeable {
    private static final char QUOTE_CHAR = '\"';

    private static final String COMMENT = "#";

    private final BufferedReader reader;

    private final String sourceFile;

    private boolean inSection = false;

    private String currentLine = "";

    private int currentLineNumber = 0;

    private int currentLineIndex = 0;

    private Pattern startOfSectionPattern = null;

    private Pattern endOfSectionPattern = null;

    /**
     * Creates reader for the passed file.
     * 
     * @param file
     * @throws FileNotFoundException
     */
    public AsciiFileParser(File file) throws FileNotFoundException {
        this(new BufferedReader(new FileReader(file)), file.getAbsolutePath());
    }

    public AsciiFileParser(BufferedReader reader, String sourceFile) {
        this.reader = reader;
        this.sourceFile = sourceFile;
    }

    /**
     * Closes the underlying file reader.
     */
    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * Returns the line number of the current file.
     * 
     * @return
     */
    public int getCurrentLineNumber() {
        return currentLineNumber;
    }

    /**
     * Returns the current line string.
     * 
     * @return
     */
    public String getCurrentLine() {
        return currentLine;
    }

    /**
     * The file this reader is working with.
     * 
     * @return
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Scans through the file until the startOfSectionPattern is found. If
     * passed true, this operation will advance to the next line skipping the
     * section header, if false will stay at section header to allow for parsing
     * of any interesting information.
     * 
     * @param advanceBeyondSectionHeader
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public Matcher scanToNextSection(boolean advanceBeyondSectionHeader)
            throws IOException, ParseException {
        if (currentLine == null) {
            return null;
        }

        if (startOfSectionPattern != null) {
            do {
                if (currentLineIndex == 0) {
                    currentLine = currentLine.trim();

                    if (currentLine.length() > 0) {
                        Matcher matcher = startOfSectionPattern
                                .matcher(currentLine);
                        if (matcher.find()) {
                            inSection = true;

                            // advance to next line to skip section header
                            if (advanceBeyondSectionHeader) {
                                goToNextLine();
                            }

                            return matcher;
                        }
                    }
                }
            } while (goToNextLine());
        } else {
            throw new ParseException(
                    "Section start pattern not set.  Failed to scan to next section",
                    currentLineNumber, sourceFile);
        }

        inSection = false;
        return null;
    }

    /**
     * The pattern to use when scanning for the start of the next section.
     * 
     * @param startOfSectionPattern
     */
    public void setStartOfSectionPattern(Pattern startOfSectionPattern) {
        this.startOfSectionPattern = startOfSectionPattern;
    }

    /**
     * The pattern to use to ensure that hasNextField() stops before going to
     * the next section.
     * 
     * @param endOfSectionPattern
     */
    public void setEndOfSectionPattern(Pattern endOfSectionPattern) {
        this.endOfSectionPattern = endOfSectionPattern;
    }

    /**
     * Get next line. Returns true if a line was able to be retrieved from the
     * reader, false otherwise.
     * 
     * @return
     * @throws IOException
     */
    private boolean goToNextLine() throws IOException {
        currentLine = reader.readLine();
        currentLineNumber++;
        currentLineIndex = 0;
        return currentLine != null;
    }

    /**
     * Advances to next line in the section if not already at the end of the
     * section. Returns true if this line is a valid line. Returns false if this
     * line is the end of the section.
     * 
     * @return
     * @throws IOException
     */
    private boolean goToNextLineInSection() throws IOException {
        boolean rval = inSection;

        if (rval) {
            rval = goToNextLine();
        }

        if (rval && (endOfSectionPattern != null)) {
            Matcher matcher = endOfSectionPattern.matcher(currentLine);
            rval = !matcher.find();
            inSection = rval;
        }

        return rval;
    }

    /**
     * Returns true if ascii file has another field in this section. Note this
     * will advance the file pointer to the next available field.
     * 
     * @return
     */
    public boolean hasNextField() throws IOException {
        // initialize to already at end of section
        boolean rval = inSection;

        if (rval) {
            // skip any leading spaces, lines starting with #, etc
            boolean keepChecking = true;
            while (keepChecking) {
                if ((currentLineIndex >= currentLine.length())
                        || (((currentLineIndex == 0) && currentLine.trim()
                                .startsWith(COMMENT)))) {
                    // end of line, or line starts with comment, advance to next
                    // line
                    if (!goToNextLineInSection()) {
                        rval = false;
                        keepChecking = false;
                        break;
                    } else {
                        currentLine = currentLine.trim();
                    }
                } else if (currentLine.charAt(currentLineIndex) == ' ') {
                    currentLineIndex++;
                } else {
                    keepChecking = false;
                }
            }
        }

        return rval;
    }

    /**
     * Returns next field. Quotes are taken in to account to return the entire
     * quoted section. In the case of >"Some value", the quotes are removed and
     * >Some value is returned. Throws a ParseException when getNextField is
     * called when at the end of a block.
     * 
     * @return
     */
    public String nextField() throws IOException, ParseException {
        String rval = null;

        if (!hasNextField()) {
            throw new ParseException("End of block reached", currentLineNumber,
                    sourceFile);
        }

        char currentChar = currentLine.charAt(currentLineIndex);
        boolean quoted = (currentChar == QUOTE_CHAR)
                || ((currentLine.length() > (currentLineIndex + 1))
                        && (currentChar == '>') && (currentLine
                        .charAt(currentLineIndex + 1) == QUOTE_CHAR));
        if (!quoted) {
            // grab to next space or end of line
            int next = currentLine.indexOf(' ', currentLineIndex);
            if (next < currentLineIndex) {
                next = currentLine.length();
            }

            rval = currentLine.substring(currentLineIndex, next);
            currentLineIndex = next + 1;
        } else {
            StringBuilder field = new StringBuilder();

            if (currentChar == '>') {
                field.append(currentChar);
                currentLineIndex++;
            }

            // skip quote
            currentLineIndex++;

            boolean fieldDone = false;

            while (!fieldDone) {
                int next = currentLine.indexOf(QUOTE_CHAR, currentLineIndex);
                if (next >= 0) {
                    if (next > 0) {
                        // append text, ignore quote
                        if ((field.length() > 1)
                                || ((field.length() == 1) && (field.charAt(0) != '>'))) {
                            field.append(' ');
                        }

                        field.append(currentLine.substring(currentLineIndex,
                                next));
                    }

                    // skip quote
                    currentLineIndex = next + 1;
                    fieldDone = true;
                } else {
                    if ((field.length() > 1)
                            || ((field.length() == 1) && (field.charAt(0) != '>'))) {
                        field.append(' ');
                    }

                    field.append(currentLine.substring(currentLineIndex));
                    if (!goToNextLineInSection()) {
                        throw new ParseException(
                                "Found end of block inside quoted section",
                                currentLineNumber, sourceFile);
                    }
                }
            }
            rval = field.toString();
        }

        if ("NULL".equals(rval)) {
            rval = null;
        }

        return rval;
    }
}
