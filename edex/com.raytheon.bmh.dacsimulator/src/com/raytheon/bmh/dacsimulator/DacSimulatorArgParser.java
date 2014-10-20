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
package com.raytheon.bmh.dacsimulator;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Parses the command line to determine the configuration for the simulator that
 * will be created. Utilizes the Apache commons CLI library to handle the
 * parsing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 08, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacSimulatorArgParser {

    private static final String USAGE_STATEMENT = "DacSimulator [--help] -p port -d hostname -r port -b bufferSize [-c numChannels]";

    private static final String HELP_OPTION_KEY = "help";

    private static final char FIRST_PORT_OPTION_KEY = 'p';

    private static final char REBROADCAST_HOST_OPTION_KEY = 'd';

    private static final char REBROADCAST_PORT_OPTION_KEY = 'r';

    private static final char MIN_BUFFER_SIZE_OPTION_KEY = 'b';

    private static final char CHANNELS_OPTION_KEY = 'c';

    private static final int DEFAULT_CHANNELS = 4;

    private static final int DEFAULT_MIN_BUFFER_SIZE = 5;

    private final Options programOptions;

    public DacSimulatorArgParser() {
        this.programOptions = buildCliParser();
    }

    @SuppressWarnings("static-access")
    private static Options buildCliParser() {
        Options options = new Options();

        Option help = new Option("h", HELP_OPTION_KEY, false,
                "print this message.");
        Option firstPort = OptionBuilder
                .withDescription(
                        "First port to reserve for receiving audio transmissions. Ports will be reserved in consecutive pairs starting from this port number.")
                .hasArg().withArgName("port").withType(Number.class)
                .create(FIRST_PORT_OPTION_KEY);
        Option rebroadcastAddress = OptionBuilder
                .withDescription(
                        "Hostname/IPv4 address to rebroadcast audio to.")
                .hasArg().withArgName("address")
                .create(REBROADCAST_HOST_OPTION_KEY);
        Option rebroadcastPort = OptionBuilder
                .withDescription(
                        "Destination port for the DAC's audio rebroadcast.")
                .hasArg().withArgName("port").withType(Number.class)
                .create(REBROADCAST_PORT_OPTION_KEY);
        Option minBufferSize = OptionBuilder
                .withDescription(
                        "Minimum number of packets received before beginning broadcast. Defaults to 5.")
                .hasArg().withArgName("bufferSize").withType(Number.class)
                .create(MIN_BUFFER_SIZE_OPTION_KEY);
        Option numChannels = OptionBuilder
                .withDescription(
                        "Number of channels on this simulated DAC. Defaults to 4.")
                .hasArg().withArgName("numChannels").withType(Number.class)
                .create(CHANNELS_OPTION_KEY);

        options.addOption(help);
        options.addOption(firstPort);
        options.addOption(rebroadcastAddress);
        options.addOption(rebroadcastPort);
        options.addOption(minBufferSize);
        options.addOption(numChannels);

        return options;
    }

    public DacSimulatorConfig parseCommandLine(final String[] args)
            throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(programOptions, args);

        if (cmd.hasOption(HELP_OPTION_KEY)) {
            return new DacSimulatorConfig(true);
        }

        int firstDataPort;
        if (cmd.hasOption(FIRST_PORT_OPTION_KEY)) {
            firstDataPort = Integer.parseInt(cmd
                    .getOptionValue(FIRST_PORT_OPTION_KEY));
        } else {
            throw new ParseException("Required option -p not provided.");
        }

        InetAddress address = null;
        if (cmd.hasOption(REBROADCAST_HOST_OPTION_KEY)) {
            try {
                address = InetAddress.getByName(cmd
                        .getOptionValue(REBROADCAST_HOST_OPTION_KEY));
            } catch (UnknownHostException e) {
                throw new ParseException(
                        "Invalid rebroadcast address specified: "
                                + e.getLocalizedMessage());
            }
        } else {
            throw new ParseException("Required option -d not provided.");
        }

        int rebroadcastPort;
        if (cmd.hasOption(REBROADCAST_PORT_OPTION_KEY)) {
            rebroadcastPort = Integer.parseInt(cmd
                    .getOptionValue(REBROADCAST_PORT_OPTION_KEY));
        } else {
            throw new ParseException("Required option -r not provided.");
        }

        int minBufferSize = DEFAULT_MIN_BUFFER_SIZE;
        if (cmd.hasOption(MIN_BUFFER_SIZE_OPTION_KEY)) {
            minBufferSize = Integer.parseInt(cmd
                    .getOptionValue(MIN_BUFFER_SIZE_OPTION_KEY));
        }

        int numChannels = DEFAULT_CHANNELS;
        if (cmd.hasOption(CHANNELS_OPTION_KEY)) {
            numChannels = Integer.parseInt(cmd
                    .getOptionValue(CHANNELS_OPTION_KEY));
        }

        return new DacSimulatorConfig(false, numChannels, firstDataPort,
                address, rebroadcastPort, minBufferSize);
    }

    /**
     * Print this program's usage statement and help information.
     */
    public void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(USAGE_STATEMENT, programOptions);
    }
}
