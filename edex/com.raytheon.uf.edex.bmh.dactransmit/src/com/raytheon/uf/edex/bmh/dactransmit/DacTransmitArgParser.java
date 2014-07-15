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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacTransmitArgParser {

    private static final String USAGE_STATEMENT = "DacTransmit [--help] -d hostname -p port -c port -t channel -g groupname -i directory";

    private static final String HELP_OPTION_KEY = "help";

    private static final char DAC_OPTION_KEY = 'd';

    private static final char DATA_PORT_OPTION_KEY = 'p';

    private static final char CONTROL_PORT_OPTION_KEY = 'c';

    private static final char TRANSMITTER_OPTION_KEY = 't';

    private static final char TRANSMITTER_GROUP_OPTION_KEY = 'g';

    private static final char INPUT_DIR_OPTION_KEY = 'i';

    private final Options programOptions;

    public DacTransmitArgParser() {
        this.programOptions = buildCliParser();
    }

    /**
     * Parse and validate the passed arguments to this program.
     * 
     * @param args
     *            The arguments passed in via the command line.
     * @return The {@code DacSessionConfig} that corresponds to the command line
     *         arguments.
     * @throws ParseException
     *             If any required arguments or missing or have invalid values.
     */
    public DacSessionConfig parseCommandLine(final String[] args)
            throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(programOptions, args);

        if (cmd.hasOption(HELP_OPTION_KEY)) {
            return new DacSessionConfig(true);
        }

        InetAddress dacAddress = null;
        if (cmd.hasOption(DAC_OPTION_KEY)) {
            try {
                dacAddress = InetAddress.getByName(cmd
                        .getOptionValue(DAC_OPTION_KEY));
            } catch (UnknownHostException e) {
                throw new ParseException("Invalid DAC address specified: "
                        + e.getLocalizedMessage());
            }
        } else {
            throw new ParseException("Required option -d not provided.");
        }

        int dataPort = -1;
        if (cmd.hasOption(DATA_PORT_OPTION_KEY)) {
            dataPort = Integer.parseInt(cmd
                    .getOptionValue(DATA_PORT_OPTION_KEY));
        } else {
            throw new ParseException("Required option -p not provided.");
        }

        int controlPort = dataPort + 1;
        if (cmd.hasOption(CONTROL_PORT_OPTION_KEY)) {
            controlPort = Integer.parseInt(cmd
                    .getOptionValue(CONTROL_PORT_OPTION_KEY));
        }

        Collection<Integer> transmitters = Collections.emptySet();
        if (cmd.hasOption(TRANSMITTER_OPTION_KEY)) {
            transmitters = parseTransmitterArg(cmd
                    .getOptionValue(TRANSMITTER_OPTION_KEY));
        } else {
            throw new ParseException("Required option -t not provided.");
        }

        String transmitterGroup;
        if (cmd.hasOption(TRANSMITTER_GROUP_OPTION_KEY)) {
            transmitterGroup = cmd.getOptionValue(TRANSMITTER_GROUP_OPTION_KEY);
        } else {
            throw new ParseException("Required option -g not provided.");
        }

        Path inputDirectory = null;
        if (cmd.hasOption(INPUT_DIR_OPTION_KEY)) {
            inputDirectory = FileSystems.getDefault().getPath(
                    cmd.getOptionValue(INPUT_DIR_OPTION_KEY));
        } else {
            throw new ParseException("Required option -i not provided.");
        }
        if (!Files.isDirectory(inputDirectory)) {
            throw new ParseException(
                    "Path specified by -i must point to a directory.");
        }

        DacSessionConfig config = new DacSessionConfig(false, dacAddress,
                dataPort, controlPort, transmitters, transmitterGroup,
                inputDirectory);
        return config;
    }

    @SuppressWarnings("static-access")
    private static Options buildCliParser() {
        Options options = new Options();

        Option help = new Option("h", HELP_OPTION_KEY, false,
                "print this message.");
        Option dacAddress = OptionBuilder
                .withDescription("Hostname/IPv4 address to send audio to.")
                .hasArg().withArgName("address").create(DAC_OPTION_KEY);
        Option dataPort = OptionBuilder
                .withDescription("UDP port for the DAC's data channel.")
                .hasArg().withArgName("port").withType(Number.class)
                .create(DATA_PORT_OPTION_KEY);
        Option controlPort = OptionBuilder
                .withDescription(
                        "UDP port for the DAC's control channel. Defaults to 1 higher than the data port.")
                .hasArg().withArgName("port").withType(Number.class)
                .create(CONTROL_PORT_OPTION_KEY);
        Option transmitter = OptionBuilder
                .withDescription(
                        "Channel number of the transmitter to broadcast with. Specify multiple transmitters by passing the channel numbers together (ex: 1234 to send the audio to all channels.")
                .hasArg().withArgName("channel").create(TRANSMITTER_OPTION_KEY);
        Option transmitterGroup = OptionBuilder
                .withDescription(
                        "Name of the transmitter group to use to broadcast this session.")
                .hasArg().withArgName("groupname")
                .create(TRANSMITTER_GROUP_OPTION_KEY);
        Option inputDirectory = OptionBuilder
                .withDescription(
                        "Directory containing playlist files to stream to DAC.")
                .hasArg().withArgName("directory").create(INPUT_DIR_OPTION_KEY);

        options.addOption(help);
        options.addOption(dacAddress);
        options.addOption(dataPort);
        options.addOption(controlPort);
        options.addOption(transmitter);
        options.addOption(transmitterGroup);
        options.addOption(inputDirectory);
        return options;
    }

    /**
     * Print this program's usage statement and help information.
     */
    public void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(USAGE_STATEMENT, programOptions);
    }

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