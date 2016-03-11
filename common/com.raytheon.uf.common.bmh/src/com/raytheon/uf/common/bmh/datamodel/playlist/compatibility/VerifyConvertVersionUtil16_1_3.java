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
package com.raytheon.uf.common.bmh.datamodel.playlist.compatibility;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

/**
 * Used to convert message files based on the 16.1.3 specification to the
 * current specification. Note: this method is specific to converting 16.1.3
 * files. All 16.1.3 compatibility classes can be removed sixty (60) days after
 * this version of BMH has been installed at all sites - this window is subject
 * to change if the message archival requirements change before that time.
 * 
 * Note: this class will most likely be completely replaced the next time the
 * message file schema is updated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 7, 2016  5382       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public final class VerifyConvertVersionUtil16_1_3 {

    private final static Logger logger = LoggerFactory
            .getLogger(VerifyConvertVersionUtil16_1_3.class);

    protected VerifyConvertVersionUtil16_1_3() {
    }

    public static void upsertMessageData(DacPlaylistMessage message,
            DacPlaylistMessageMetadata messageMetadata) throws Exception {
        if (DacPlaylistMessageId.CURRENT_VERSION.equals(messageMetadata
                .getVersion())) {
            /*
             * Nothing to do. The metadata has the expected format.
             */
            return;
        }
        /*
         * the only change to recover will be if the message file is also using
         * an older format. Note: this is the first time that message files will
         * be versioned. So,all previous files will not have a version set.
         */
        if (message.getVersion() != null) {
            throw new Exception("Unable to upconvert message metadata: "
                    + messageMetadata.getPath().toString()
                    + ". A previous version of message: "
                    + message.getPath().toString() + " is not available.");
        }

        logger.info("Attempting to upconvert message metadata: "
                + messageMetadata.getPath().toString() + " ...");
        DacPlaylistMessage16_1_3 compatMessage = JAXB.unmarshal(message
                .getPath().toFile(), DacPlaylistMessage16_1_3.class);
        /*
         * Use the data in the previous version of the message file (file that
         * we manage) to update the message metadata file (file managed by
         * EDEX). There will be no impact if we write this file because other
         * versions of the file that EDEX writes would have a completely
         * different timestamp.
         */
        messageMetadata.setVersion(DacPlaylistMessageId.CURRENT_VERSION);
        messageMetadata.setName(compatMessage.getName());
        messageMetadata.setStart(compatMessage.getStart());
        messageMetadata.setMessageType(compatMessage.getMessageType());
        messageMetadata.setSAMEtone(compatMessage.getSAMEtone());
        messageMetadata.setAlertTone(compatMessage.isAlertTone());
        messageMetadata.setToneBlackoutEnabled(compatMessage
                .isToneBlackoutEnabled());
        messageMetadata.setToneBlackoutStart(compatMessage
                .getToneBlackoutStart());
        messageMetadata.setToneBlackoutEnd(compatMessage.getToneBlackoutEnd());
        messageMetadata.setConfirm(compatMessage.isConfirm());
        messageMetadata.setWatch(compatMessage.isWatch());
        messageMetadata.setWarning(compatMessage.isWarning());
        /*
         * Write the updated metadata file. There is no need to worry about
         * conflicting with EDEX because newer metadata files that EDEX writes
         * will have a newer timestamp. There is no need to worry about writing
         * an updated message file because additional information over what is
         * expected will be ignored and the message file will automatically be
         * rewritten during the next broadcast of the associated message.
         */
        JAXB.marshal(messageMetadata, messageMetadata.getPath().toFile());

        message.setVersion(DacPlaylistMessageId.CURRENT_VERSION);

        logger.error("Successfully upconverted message metadata: "
                + messageMetadata.getPath().toString() + ".");
    }
}