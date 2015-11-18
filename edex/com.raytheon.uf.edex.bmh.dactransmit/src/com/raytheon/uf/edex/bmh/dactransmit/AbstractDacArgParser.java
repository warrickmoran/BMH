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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.AbstractDacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacCommonConfig;

/**
 * Defines and analyzes arguments that are required to start a dac session
 * regardless of the specified {@link DAC_MODE}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * Apr 09, 2015 4364       bkowal      Fix the usage output.
 * Apr 29, 2015 4394       bkowal      Now handles the management port argument.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractDacArgParser {

    private static final String USAGE_SEPARATOR = " ";

    private static final String OPTIONAL_BEGIN = "[";

    private static final String OPTIONAL_END = "]";

    private static final String ARG_HYPHEN = "-";

    private static final String USAGE_STATEMENT = "DacTransmit [--help]";

    public static final String HELP_OPTION_KEY = "help";

    public static final char DAC_MODE = 'e';

    public static final char DAC_HOSTNAME_OPTION_KEY = 'd';

    public static final char DATA_PORT_OPTION_KEY = 'p';

    public static final char TRANSMITTER_OPTION_KEY = 't';

    public static final char TRANSMISSION_AMPLITUDE_TARGET_KEY = 'r';

    public static final char CONTROL_PORT_OPTION_KEY = 'c';

    public static final char COMMS_MANAGER_PORT_OPTION_KEY = 'm';

    protected String usageStatement;

    private final Options programOptions;

    /**
     * 
     */
    protected AbstractDacArgParser(final boolean modeRequired) {
        StringBuilder usageStmtBuilder = new StringBuilder(USAGE_STATEMENT);

        this.programOptions = this.buildCliParser(usageStmtBuilder,
                modeRequired);
        this.usageStatement = usageStmtBuilder.toString();
    }

    public AbstractDacConfig parseCommandLine(final String[] arguments)
            throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(programOptions, arguments);

        String dacHostname = null;
        InetAddress dacAddress = null;
        try {
            dacHostname = cmd.getOptionValue(DAC_HOSTNAME_OPTION_KEY);
            dacAddress = InetAddress.getByName(dacHostname);
        } catch (UnknownHostException e) {
            throw new ParseException("Invalid DAC address specified: "
                    + e.getLocalizedMessage());
        }

        int dataPort = Integer.parseInt(cmd
                .getOptionValue(DATA_PORT_OPTION_KEY));

        int controlPort = dataPort + 1;
        if (cmd.hasOption(CONTROL_PORT_OPTION_KEY)) {
            controlPort = Integer.parseInt(cmd
                    .getOptionValue(CONTROL_PORT_OPTION_KEY));
        }

        Collection<Integer> transmitters = parseTransmitterArg(cmd
                .getOptionValue(TRANSMITTER_OPTION_KEY));

        short amplitudeTarget = Short.parseShort(cmd
                .getOptionValue(TRANSMISSION_AMPLITUDE_TARGET_KEY));

        int managerPort = Integer.parseInt(cmd
                .getOptionValue(COMMS_MANAGER_PORT_OPTION_KEY));

        DacCommonConfig commonConfig = new DacCommonConfig(dacHostname,
                dacAddress, dataPort, controlPort, transmitters,
                amplitudeTarget, managerPort);
        return this.parseCommandLineInternal(cmd, commonConfig);
    }

    /**
     * Print this program's usage statement and help information.
     */
    public void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(usageStatement, programOptions);
    }

    @SuppressWarnings("static-access")
    private Options buildCliParser(StringBuilder usageStmtBuilder,
            final boolean modeRequired) {

        Option modeOption = (Option) DacCliArgParser.getModeOption().clone();
        modeOption.setRequired(modeRequired);
        this.addUsageOption(modeOption, usageStmtBuilder);

        Option dacAddress = OptionBuilder
                .withDescription("Hostname/IPv4 address to send audio to.")
                .hasArg().withArgName("address")
                .create(DAC_HOSTNAME_OPTION_KEY);
        dacAddress.setRequired(true);
        this.addUsageOption(dacAddress, usageStmtBuilder);

        Option dataPort = OptionBuilder
                .withDescription("UDP port for the DAC's data channel.")
                .hasArg().withArgName("port").withType(Integer.class)
                .create(DATA_PORT_OPTION_KEY);
        dataPort.setRequired(true);
        this.addUsageOption(dataPort, usageStmtBuilder);

        Option controlPort = OptionBuilder
                .withDescription(
                        "UDP port for the DAC's control channel. Defaults to 1 higher than the data port.")
                .hasArg().withArgName("port").withType(Number.class)
                .create(CONTROL_PORT_OPTION_KEY);
        controlPort.setRequired(false);
        this.addUsageOption(controlPort, usageStmtBuilder);

        Option transmitter = OptionBuilder
                .withDescription(
                        "Channel number of the transmitter to broadcast with. Specify multiple transmitters by passing the channel numbers together (ex: 1234 to send the audio to all channels.")
                .hasArg().withArgName("channel").create(TRANSMITTER_OPTION_KEY);
        transmitter.setRequired(true);
        this.addUsageOption(transmitter, usageStmtBuilder);

        Option dbRange = OptionBuilder
                .withDescription(
                        "The target maximum amplitude of the audio allowed by the transmitter.")
                .hasArg().withArgName("range").withType(Short.class)
                .create(TRANSMISSION_AMPLITUDE_TARGET_KEY);
        dbRange.setRequired(true);
        this.addUsageOption(dbRange, usageStmtBuilder);

        Option managerPort = OptionBuilder
                .withDescription(
                        "TCP/IP port for communicating with the Comms Manager")
                .hasArg().withArgName("port").withType(Integer.class)
                .create(COMMS_MANAGER_PORT_OPTION_KEY);
        managerPort.setRequired(true);
        this.addUsageOption(managerPort, usageStmtBuilder);

        Options options = new Options();
        options.addOption(DacCliArgParser.getHelpOption());
        options.addOption(modeOption);
        options.addOption(dacAddress);
        options.addOption(dataPort);
        options.addOption(controlPort);
        options.addOption(transmitter);
        options.addOption(dbRange);
        options.addOption(managerPort);
        for (Option contributedOption : this.getOptions()) {
            this.addUsageOption(contributedOption, usageStmtBuilder);
            options.addOption(contributedOption);
        }

        return options;
    }

    private void addUsageOption(Option option, StringBuilder usageStmtBuilder) {
        usageStmtBuilder.append(USAGE_SEPARATOR);
        if (option.isRequired() == false) {
            usageStmtBuilder.append(OPTIONAL_BEGIN);
        }
        usageStmtBuilder.append(ARG_HYPHEN);
        usageStmtBuilder.append(option.getOpt());
        if (option.hasArg() || option.hasArgs()) {
            usageStmtBuilder.append(USAGE_SEPARATOR);
            usageStmtBuilder.append(option.getArgName());
        }
        if (option.isRequired() == false) {
            usageStmtBuilder.append(OPTIONAL_END);
        }
    }

    protected abstract AbstractDacConfig parseCommandLineInternal(
            final CommandLine cmd, final DacCommonConfig commonConfig)
            throws ParseException;

    protected abstract List<Option> getOptions();

    private static Collection<Integer> parseTransmitterArg(
            final String transmitterArg) throws ParseException {
        Collection<Integer> selectedTransmitters = new HashSet<>();

        for (int i = 0; i <= (transmitterArg.length() - 1); i++) {
            String transmitterString = transmitterArg.substring(i, i + 1);
            Integer transmitter;
            try {
                transmitter = Integer.valueOf(transmitterString);
                if ((transmitter >= 1) && (transmitter <= 4)) {
                    selectedTransmitters.add(transmitter);
                } else {
                    throw new ParseException("Invalid value specified for -t: "
                            + transmitterString + ". Must be between 1 and 4.");
                }
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid value specified for -t: "
                        + transmitterString + ". Must be between 1 and 4.");
            }
        }

        return selectedTransmitters;
    }
}