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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.AbstractDacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacCommonConfig;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacMaintenanceConfig;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacMaintenanceArgParser extends AbstractDacArgParser {

    public static final char INPUT_AUDIO_OPTION_KEY = 'i';

    public static final char MAINT_AUDIO_LENGTH_KEY = 'l';

    public DacMaintenanceArgParser() {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dactransmit.AbstractDacArgParser#getOptions()
     */
    @SuppressWarnings("static-access")
    @Override
    protected List<Option> getOptions() {
        Option inputAudio = OptionBuilder
                .withDescription(
                        "File containing the audio that will be streamed to the DAC.")
                .hasArg().withArgName("audio").create(INPUT_AUDIO_OPTION_KEY);
        inputAudio.setRequired(true);

        Option audioDuration = OptionBuilder
                .withDescription(
                        "The duration of the audio that will be streamed to the dac in seconds. The input audio will be replicated or cut as needed to meet the duration requirement.")
                .hasArg().withArgName("audio").withType(Integer.class)
                .create(MAINT_AUDIO_LENGTH_KEY);
        audioDuration.setRequired(true);

        List<Option> options = new ArrayList<>();
        options.add(inputAudio);
        options.add(audioDuration);

        return options;
    }

    @Override
    protected AbstractDacConfig parseCommandLineInternal(final CommandLine cmd,
            final DacCommonConfig commonConfig) throws ParseException {

        Path inputAudio = Paths.get(cmd.getOptionValue(INPUT_AUDIO_OPTION_KEY));

        Integer duration = Integer.parseInt(cmd
                .getOptionValue(MAINT_AUDIO_LENGTH_KEY));

        return new DacMaintenanceConfig(commonConfig, inputAudio, duration);
    }
}