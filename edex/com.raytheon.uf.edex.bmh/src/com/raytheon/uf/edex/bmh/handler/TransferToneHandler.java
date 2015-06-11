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

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter.TxMode;
import com.raytheon.uf.common.bmh.request.TransferToneRequest;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.bmh.tones.TonesManager.TransferType;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

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
 * Apr 14, 2015  4398     rjpeter      Only send TXB/TXP for mode switch.
 * Apr 24, 2015  4394     bkowal       Updated to use {@link DacMaintenanceMessage}.
 * Apr 29, 2015  4394     bkowal       Include the Transmitter Group Name in the
 *                                     {@link DacMaintenanceMessage}.
 * Jun 11, 2015  4490     bkowal       Maintenance traceability improvements.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class TransferToneHandler extends
        AbstractBMHServerRequestHandler<TransferToneRequest> {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TransferToneHandler.class);

    private static final String TRANSMITTER_PRIMARY_ON = "TXP";

    private static final String TRANSMITTER_BACKUP_ON = "TXB";

    @Override
    public Object handleRequest(TransferToneRequest request) throws Exception {
        StringBuilder logMsg = new StringBuilder("traceId=");
        logMsg.append(request.getTraceId()).append(": Handling ")
                .append(request.getTxMode().name())
                .append(" transfer tone request ");
        if (request.isOperational() == false) {
            logMsg.append("(PRACTICE) ");
        }
        logMsg.append("...");
        statusHandler.info(logMsg.toString());
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

        DacMaintenanceMessage message = new DacMaintenanceMessage();
        message.setTransmitterGroup(transmitter.getTransmitterGroup().getName());

        if (daisychain) {
            SAMEToneTextBuilder sameBuilder = new SAMEToneTextBuilder();
            sameBuilder.setOriginator(SAMEToneTextBuilder.NWS_ORIGINATOR);
            sameBuilder.addArea(transmitter.getFipsCode());
            sameBuilder.setPurgeTime(0, 30);
            sameBuilder.setEffectiveTime(TimeUtil.newGmtCalendar());
            sameBuilder.setNwsSiteId(SiteUtil.getSite());
            String onEvent = TRANSMITTER_PRIMARY_ON;
            if (request.getTxMode() == TxMode.SECONDARY) {
                onEvent = TRANSMITTER_BACKUP_ON;
            }
            sameBuilder.setEvent(onEvent);
            final String group = transmitter.getTransmitterGroup().getName();
            message.setName(group + " " + onEvent + " SAME Tones");

            message.setSAMEtone(sameBuilder.build().toString());
        } else {
            TransferType transferType = null;
            if (request.getTxMode() == TxMode.PRIMARY) {
                transferType = TransferType.SECONDARY_TO_PRIMARY;
            } else {
                transferType = TransferType.PRIMARY_TO_SECONDARY;
            }
            message.setName(transmitter.getMnemonic() + " "
                    + transferType.toString() + " Transfer Tones");

            message.setTransferToneType(transferType);
        }

        return MaintenanceMessageWriter.writeMaintenanceMessage(message,
                request);
    }

}
