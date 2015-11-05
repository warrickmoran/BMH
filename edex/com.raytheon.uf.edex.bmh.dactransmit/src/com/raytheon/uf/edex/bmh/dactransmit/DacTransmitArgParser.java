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
package com.raytheon.uf.edex.bmh.dactransmit;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.AbstractDacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacCommonConfig;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSessionConfig;

/**
 * Parses the command line to determine the configuration for the DacSession
 * that will be created. Utilizes the Apache commons CLI library to handle the
 * parsing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Add transmitter group argument.
 * Jul 17, 2014  #3399     bsteffen     Add comms manager port argument.
 * Aug 12, 2014  #3486     bsteffen     Remove group argument
 * Aug 18, 2014  #3532     bkowal       Added transmitter decibel range argument
 * Aug 26, 2014  #3286     dgilling     Allow for initially empty or missing
 *                                      playlist directory.
 * Sep 4, 2014   #3532     bkowal       Use a decibel target instead of a range.
 * Oct 2, 2014   #3642     bkowal       Added transmitter timezone argument.
 * Oct 22, 2014  #3687     bsteffen    keep original dac hostname
 * Nov 7, 2014   #3630     bkowal      Refactor for maintenance mode.
 * Apr 29, 2015  #4394     bkowal      No longer manages the management port argument.
 * Jul 08, 2015  #4636     bkowal      Support same and alert decibel levels.
 * Nov 04, 2015  #5068     rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacTransmitArgParser extends AbstractDacArgParser {

    public static final char INPUT_DIR_OPTION_KEY = 'i';

    public static final char TIMEZONE_KEY = 'z';

    public static final String SAME_AMPLITUDE_TARGET_KEY = TRANSMISSION_AMPLITUDE_TARGET_KEY
            + "s";

    public static final String ALERT_AMPLITUDE_TARGET_KEY = TRANSMISSION_AMPLITUDE_TARGET_KEY
            + "a";

    public DacTransmitArgParser() {
        super(false);
    }

    @SuppressWarnings("static-access")
    @Override
    protected List<Option> getOptions() {
        Option inputDirectory = OptionBuilder
                .withDescription(
                        "Directory containing playlist files to stream to DAC.")
                .hasArg().withArgName("directory").create(INPUT_DIR_OPTION_KEY);
        inputDirectory.setRequired(true);
        Option timezone = OptionBuilder
                .withDescription(
                        "The timezone associated with the transmitter.")
                .hasArg().withArgName("timezone").create(TIMEZONE_KEY);
        timezone.setRequired(true);
        Option sameDbTarget = OptionBuilder
                .withDescription(
                        "The target maximum amplitude of the SAME Tones allowed by the transmitter.")
                .hasArg().withArgName("same amplitude").withType(Short.class)
                .create(SAME_AMPLITUDE_TARGET_KEY);
        sameDbTarget.setRequired(true);
        Option alertDbTarget = OptionBuilder
                .withDescription(
                        "The target maximum amplitude of the Alert Tones allowed by the transmitter.")
                .hasArg().withArgName("alert amplitude").withType(Short.class)
                .create(ALERT_AMPLITUDE_TARGET_KEY);
        alertDbTarget.setRequired(true);

        List<Option> options = new ArrayList<>();
        options.add(inputDirectory);
        options.add(timezone);
        options.add(sameDbTarget);
        options.add(alertDbTarget);

        return options;
    }

    @Override
    protected AbstractDacConfig parseCommandLineInternal(CommandLine cmd,
            DacCommonConfig commonConfig) throws ParseException {

        Path inputDirectory = FileSystems.getDefault().getPath(
                cmd.getOptionValue(INPUT_DIR_OPTION_KEY));

        String timezoneStr = cmd.getOptionValue(TIMEZONE_KEY, null);
        if (timezoneStr == null) {
            throw new ParseException("Required option -z not provided.");
        }

        TimeZone timeZone = TimeZone.getTimeZone(timezoneStr);
        /*
         * If the timezone ids do not match. An invalid timezone was provided.
         * {@link TimeZone} will default to the GMT timezone if an associated
         * time zone cannot be found.
         */
        if (timeZone.getID().equals(timezoneStr) == false) {
            throw new ParseException(
                    "An invalid timezone has been specified for the -z option: "
                            + timezoneStr + ".");
        }

        short sameAmplitude = Short.parseShort(cmd
                .getOptionValue(SAME_AMPLITUDE_TARGET_KEY));

        short alertAmplitude = Short.parseShort(cmd
                .getOptionValue(ALERT_AMPLITUDE_TARGET_KEY));

        return new DacSessionConfig(commonConfig, inputDirectory, timeZone,
                sameAmplitude, alertAmplitude);
    }
}