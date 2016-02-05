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
package com.raytheon.bmh.dactransmit;

import java.util.Arrays;
import java.util.ListIterator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Extends {@link BasicParser} to parse special case arguments that the default
 * implementation of apache commons cli does not provide capabilities to support
 * (especially in the case of a common option like --help). Defines the standard
 * mode and helpOption.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacCliArgParser extends BasicParser {

    private static final DacCliArgParser instance = new DacCliArgParser();

    private static Option modeOption;

    private static Option helpOption;

    /**
     * 
     */
    @SuppressWarnings("static-access")
    protected DacCliArgParser() {
        Options options = new Options();

        modeOption = OptionBuilder
                .withDescription(
                        "The dac mode. Must be set to OP for operational mode or MA for maintenance mode. Operational mode (OP) will be the default when not specified.")
                .hasArg().withArgName("mode")
                .create(AbstractDacArgParser.DAC_MODE);

        helpOption = new Option("h", AbstractDacArgParser.HELP_OPTION_KEY,
                false, "print this message.");

        options.addOption(modeOption);
        options.addOption(helpOption);
        super.setOptions(options);
    }

    protected Option getOption(final String[] arguments, Option optionToRetrieve)
            throws ParseException {
        ListIterator<?> tokenIterator = Arrays.asList(
                flatten(getOptions(), arguments, false)).listIterator();
        Option option = null;

        while (tokenIterator.hasNext()) {
            String str = (String) tokenIterator.next();

            // found an Option, not an argument
            if (getOptions().hasOption(str) == false
                    || getOptions().getOption(str).equals(optionToRetrieve) == false
                    || str.startsWith("-") == false) {
                continue;
            }

            option = (Option) optionToRetrieve.clone();
            if (option.hasArg() || option.hasArgs()) {
                super.processArgs(option, tokenIterator);
            }
            break;
        }

        return option;
    }

    public static AbstractDacArgParser getDacArgParser(final String[] arguments)
            throws ParseException {
        DAC_MODE dacMode = determineMode(arguments);

        if (dacMode == DAC_MODE.MAINTENANCE) {
            return new DacMaintenanceArgParser();
        } else {
            return new DacTransmitArgParser();
        }
    }

    private static DAC_MODE determineMode(final String[] arguments)
            throws ParseException {
        Option option = instance.getOption(arguments, modeOption);

        if (option == null) {
            /*
             * return default operational mode.
             */
            return DAC_MODE.OPERATIONAL;
        }

        try {
            return DAC_MODE.lookupMode(option.getValue());
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }

    public static boolean isHelp(final String[] arguments)
            throws ParseException {
        return instance.getOption(arguments, helpOption) != null;
    }

    public static Option getModeOption() {
        return modeOption;
    }

    public static Option getHelpOption() {
        return helpOption;
    }
}