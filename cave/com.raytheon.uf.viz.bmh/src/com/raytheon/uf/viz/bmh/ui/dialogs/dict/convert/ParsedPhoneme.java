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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert;

/**
 * Object to hold a parsed phoneme part.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2014    3355    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ParsedPhoneme {
    /**
     * The type of parse
     */
    public enum ParseType {
        Text, Phoneme, Break, Dynamic;
    }

    /** The word */
    private String word;

    /** The type */
    private ParseType type;

    /** The parsed value */
    private String parsedValue;

    /**
     * Default constructor
     */
    public ParsedPhoneme() {

    }

    /**
     * Constructor
     * 
     * @param parsedValue
     *            The parsed value
     * @param type
     *            The type
     */
    public ParsedPhoneme(String parsedValue, ParseType type) {
        this.parsedValue = parsedValue;
        this.type = type;
    }

    /**
     * @return the word
     */
    public String getWord() {
        return word;
    }

    /**
     * @param word
     *            the word to set
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * @return the type
     */
    public ParseType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(ParseType type) {
        this.type = type;
    }

    /**
     * @return the parsedValue
     */
    public String getParsedValue() {
        return parsedValue;
    }

    /**
     * @param parsedValue
     *            the parsedValue to set
     */
    public void setParsedValue(String parsedValue) {
        this.parsedValue = parsedValue;
    }
}
