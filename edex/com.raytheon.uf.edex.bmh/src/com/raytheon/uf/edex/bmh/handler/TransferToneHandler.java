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
package com.raytheon.uf.edex.bmh.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter.TxMode;
import com.raytheon.uf.common.bmh.request.TransferToneRequest;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;
import com.raytheon.uf.common.bmh.tones.TonesManager.TRANSFER_TYPE;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;

/**
 * 
 * Generates a transfer tone file in response to a {@link TransferToneRequest}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Dec 10, 2014  3603     bsteffen     Initial creation
 * Jan 26, 2015  3359     bsteffen     Use site id for same tones.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class TransferToneHandler extends
        AbstractBMHServerRequestHandler<TransferToneRequest> {

    private static final String TRANSMITTER_CARRIER_OFF = "TXF";

    private static final String TRANSMITTER_CARRIER_ON = "TXO";

    private static final String TRANSMITTER_PRIMARY_ON = "TXP";

    private static final String TRANSMITTER_BACKUP_ON = "TXB";

    @Override
    public Object handleRequest(TransferToneRequest request)
            throws ToneGenerationException, IOException {
        TransmitterDao transmitterDao = new TransmitterDao(
                request.isOperational());
        Transmitter transmitter = transmitterDao.getByID(request
                .getTransmitterId());
        if (transmitter.getTxMode() != request.getTxMode()) {
            transmitter.setTxMode(request.getTxMode());
            transmitterDao.saveOrUpdate(transmitter);
        }
        Integer port = transmitter.getDacPort();
        boolean daisychain = false;
        for (Transmitter other : transmitter.getTransmitterGroup()
                .getTransmitters()) {
            if (!other.equals(transmitter) && other.getDacPort().equals(port)) {
                daisychain = true;
                break;
            }
        }
        StringBuilder fileName = new StringBuilder(48);
        fileName.append("transferTone-");
        fileName.append(transmitter.getMnemonic());
        fileName.append("-");
        fileName.append(request.getTxMode());
        fileName.append(BMHAudioFormat.ULAW.getExtension());
        Path output = BMHConstants.getBmhDataDirectory(request.isOperational())
                .resolve(BMHConstants.AUDIO_DATA_DIRECTORY)
                .resolve(BMHConstants.MAINTENANCE_DATA_DIRECTORY)
                .resolve(fileName.toString());

        if (daisychain) {
            SAMEToneTextBuilder sameBuilder = new SAMEToneTextBuilder();
            sameBuilder.setOriginator(SAMEToneTextBuilder.NWS_ORIGINATOR);
            sameBuilder.addArea(transmitter.getFipsCode());
            sameBuilder.setPurgeTime(0, 30);
            sameBuilder.setEffectiveTime(TimeUtil.newGmtCalendar());
            sameBuilder.setNwsSiteId(SiteUtil.getSite());

            try (OutputStream os = Files.newOutputStream(output)) {
                String onEvent = TRANSMITTER_PRIMARY_ON;
                if (request.getTxMode() == TxMode.SECONDARY) {
                    onEvent = TRANSMITTER_BACKUP_ON;
                }
                /*
                 * Each command is encoded as an independent message, so there
                 * are three messages. Each message repeats the SAME tones 3
                 * times, so the total audio is fairly large for such simple
                 * commands.
                 */
                String[] eventSequence = { TRANSMITTER_CARRIER_OFF, onEvent,
                        TRANSMITTER_CARRIER_ON };
                /* No transition silence first time through the loop. */
                byte[] silence = null;
                for (String event : eventSequence) {
                    if (silence == null) {
                        /*
                         * 2 seconds is the typical silence between messages,
                         * since this isn't a real message its not clear if its
                         * ok to make this silence shorter.
                         */
                        silence = new byte[2 * 8000];
                        Arrays.fill(silence, (byte) 0xFF);
                    } else {
                        os.write(silence);
                    }
                    sameBuilder.setEvent(event);
                    ByteBuffer same = TonesGenerator.getSAMEAlertTones(
                            sameBuilder.build().toString(), false, false);
                    os.write(same.array());
                    os.write(TonesGenerator.getEndOfMessageTones().array());
                }
            }
        } else {
            TRANSFER_TYPE transferType = null;
            if (request.getTxMode() == TxMode.PRIMARY) {
                transferType = TRANSFER_TYPE.SECONDARY_TO_PRIMARY;
            } else {
                transferType = TRANSFER_TYPE.PRIMARY_TO_SECONDARY;
            }
            byte[] audio = TonesManager.generateTransferTone(transferType);
            Files.write(output, audio);
        }
        return output.toAbsolutePath().toString();
    }

}
