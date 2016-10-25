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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import com.raytheon.bmh.dactransmit.dacsession.AbstractDacConfig;
import com.raytheon.bmh.dactransmit.dacsession.DacCommonConfig;
import com.raytheon.bmh.dactransmit.dacsession.DacMaintenanceConfig;
import com.raytheon.uf.common.bmh.audio.SAMEPaddingConfiguration;

/**
 * Defines and parses the arguments that should be present in a command when
 * starting a dac session in maintenance mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * Apr 09, 2015 4364       bkowal      Defined the {@link #MAINT_EXEC_TIMEOUT} argument.
 * Apr 24, 2015 4394       bkowal      Updated argument descriptions based on new usage.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * Sep 30, 2016 5912       bkowal      Construction now requires {@link SAMEPaddingConfiguration}.
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class DacMaintenanceArgParser extends AbstractDacArgParser {

    public static final char INPUT_AUDIO_OPTION_KEY = 'i';

    public static final char MAINT_AUDIO_LENGTH_KEY = 'l';

    public static final char MAINT_EXEC_TIMEOUT = 'o';

    /*
     * This amplitude target, when specified, will be applied to the 2400 Hz
     * portion of the transfer tone audio.
     */
    public static final String MAINT_TRANSFER_AMPLITUDE_TARGET = TRANSMISSION_AMPLITUDE_TARGET_KEY
            + "t";

    public DacMaintenanceArgParser() {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.dactransmit.AbstractDacArgParser#getOptions()
     */
    @SuppressWarnings("static-access")
    @Override
    protected List<Option> getOptions() {
        Option inputAudio = OptionBuilder
                .withDescription(
                        "Message file containing metadata that will be used to prepare the maintenance audio.")
                .hasArg().withArgName("message").create(INPUT_AUDIO_OPTION_KEY);
        inputAudio.setRequired(true);

        Option audioDuration = OptionBuilder
                .withDescription(
                        "The duration (in seconds) of the alignment audio that will be streamed to the dac. The input audio will be replicated or cut as needed to meet the duration requirement.")
                .hasArg().withArgName("duration").withType(Integer.class)
                .create(MAINT_AUDIO_LENGTH_KEY);
        audioDuration.setRequired(false);

        Option executionTimeout = OptionBuilder
                .withDescription(
                        "The maximum amount of time (in minutes) the session can run before it will be automatically terminated. An attempt will be made to allow all tones to finish broadcasting (when applicable).")
                .hasArg().withArgName("timeout").withType(Integer.class)
                .create(MAINT_EXEC_TIMEOUT);
        executionTimeout.setRequired(true);

        Option transferDbTarget = OptionBuilder
                .withDescription(
                        "Amplitude target applied to the 2400 Hz portion of a transfer tone.")
                .hasArg().withArgName("transfer db range")
                .withType(Short.class).create(MAINT_TRANSFER_AMPLITUDE_TARGET);
        transferDbTarget.setRequired(false);

        List<Option> options = new ArrayList<>();
        options.add(inputAudio);
        options.add(audioDuration);
        options.add(executionTimeout);
        options.add(transferDbTarget);

        return options;
    }

    @Override
    protected AbstractDacConfig parseCommandLineInternal(final CommandLine cmd,
            final DacCommonConfig commonConfig,
            final SAMEPaddingConfiguration samePaddingConfiguration)
            throws ParseException {

        Path messageFilePath = Paths.get(cmd
                .getOptionValue(INPUT_AUDIO_OPTION_KEY));

        Integer duration = null;
        if (cmd.getOptionValue(MAINT_AUDIO_LENGTH_KEY, null) != null) {
            duration = Integer.parseInt(cmd
                    .getOptionValue(MAINT_AUDIO_LENGTH_KEY));
        }

        Integer timeout = Integer.parseInt(cmd
                .getOptionValue(MAINT_EXEC_TIMEOUT));

        short transferAmplitude = -999;
        if (cmd.hasOption(MAINT_TRANSFER_AMPLITUDE_TARGET)) {
            transferAmplitude = Short.parseShort(cmd
                    .getOptionValue(MAINT_TRANSFER_AMPLITUDE_TARGET));
        }

        return new DacMaintenanceConfig(commonConfig, messageFilePath,
                duration, timeout, transferAmplitude, samePaddingConfiguration);
    }
}