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

import java.nio.file.Files;
import java.nio.file.Path;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.bmh.request.MaintenanceMessageRequest;
import com.raytheon.uf.edex.bmh.staticmsg.AlignmentTestGenerator;
import com.raytheon.uf.edex.bmh.staticmsg.StaticGenerationException;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Handles {@link MaintenanceMessageRequest} requests. Returns the location of
 * the maintenance audio file associated with the specified
 * {@link MaintenanceMessageRequest.AUDIOTYPE}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 5, 2014  3630       bkowal      Initial creation
 * Apr 24, 2015 4394       bkowal      Updated to use {@link DacMaintenanceMessage}.
 * Apr 29, 2015 4394       bkowal      Include the Transmitter Group Name in the
 *                                     {@link DacMaintenanceMessage}.
 * Jun 11, 2015 4490       bkowal      Maintenance traceability improvements.
 * Jul 08, 2015 4636       bkowal      Support transfer tone transmitter alignment tests.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MaintenanceMessageHandler extends
        AbstractBMHServerRequestHandler<MaintenanceMessageRequest> {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(MaintenanceMessageHandler.class);

    private final AlignmentTestGenerator alignmentTestGenerator;

    private final AlignmentTestGenerator practiceAlignmentTestGenerator;

    public MaintenanceMessageHandler(
            final AlignmentTestGenerator alignmentTestGenerator,
            AlignmentTestGenerator practiceAlignmentTestGenerator) {
        this.alignmentTestGenerator = alignmentTestGenerator;
        this.practiceAlignmentTestGenerator = practiceAlignmentTestGenerator;
    }

    @Override
    public Object handleRequest(MaintenanceMessageRequest request)
            throws Exception {
        StringBuilder logMsg = new StringBuilder("traceId=");
        logMsg.append(request.getTraceId()).append(": Handling ")
                .append(request.getType().name()).append(" alignment request ");
        if (request.isOperational() == false) {
            logMsg.append("(PRACTICE) ");
        }
        logMsg.append("...");
        statusHandler.info(logMsg.toString());
        AlignmentTestGenerator generator = (request.isOperational()) ? this.alignmentTestGenerator
                : this.practiceAlignmentTestGenerator;

        DacMaintenanceMessage message = new DacMaintenanceMessage();
        StringBuilder sb = new StringBuilder(request.getType().name());
        sb.append(" Alignment Audio (Duration: ").append(request.getDuration())
                .append("s)");
        message.setName(sb.toString());
        message.setTransmitterGroup(request.getTransmitterGroup());

        Path audioPath = null;
        switch (request.getType()) {
        case ALERT:
            audioPath = generator.getMaintenanceAlertPath();
            break;
        case SAME:
            audioPath = generator.getMaintenanceSamePath();
            break;
        case TEXT:
            audioPath = generator.getMaintenanceTextPath();
            break;
        case TRANSFER_18:
            audioPath = generator.getMaintenance18TransferPath();
            break;
        case TRANSFER_24:
            audioPath = generator.getMaintenance24TransferPath();
            break;
        }

        message.setSoundFile(audioPath.toString());

        // does the file exist?
        if (Files.exists(audioPath)) {
            return MaintenanceMessageWriter.writeMaintenanceMessage(message,
                    request);
        }

        // Attempt to create the file.
        generator.process();

        // does the file exist now?
        if (Files.exists(audioPath)) {
            return MaintenanceMessageWriter.writeMaintenanceMessage(message,
                    request);
        }

        throw new StaticGenerationException("Failed to generate / access "
                + request.getType() + " maintenance audio!");
    }

    /**
     * @return the practiceAlignmentTestGenerator
     */
    public AlignmentTestGenerator getPracticeAlignmentTestGenerator() {
        return practiceAlignmentTestGenerator;
    }
}