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
package com.raytheon.uf.edex.bmh.msg.validator;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * 
 * Validate {@link InputMessage}s to verify they are able to be transmitted.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation.
 * Jul 17, 2014  3406     mpduff      Area object changed.
 * Jul 17, 2014  3175     rjpeter     Updated transmitter group lookup.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Dec 03, 2015  5158     bkowal      Validate that the message is associated with a
 *                                    recognized {@link Language}.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class TransmissionValidator {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TransmissionValidator.class);

    private final InputMessageDao inputMessageDao;

    private final MessageTypeDao messageTypeDao = new MessageTypeDao();

    private final AreaDao areaDao = new AreaDao();

    private final ZoneDao zoneDao = new ZoneDao();

    private final ProgramDao programDao = new ProgramDao();

    private final TtsVoiceDao ttsVoiceDao = new TtsVoiceDao();

    public TransmissionValidator(final IMessageLogger messageLogger) {
        inputMessageDao = new InputMessageDao(messageLogger);
    }

    public void validate(ValidatedMessage message) {
        InputMessage input = message.getInputMessage();
        try {
            if (isExpired(input)) {
                message.setTransmissionStatus(TransmissionStatus.EXPIRED);
            } else if (inputMessageDao.checkDuplicate(input)) {
                message.setTransmissionStatus(TransmissionStatus.DUPLICATE);
            } else if (checkConfiguration(input)) {
                message.setTransmissionStatus(TransmissionStatus.UNDEFINED);
            } else {
                Set<TransmitterGroup> groups = getTransmissionGroups(input);
                if (groups.isEmpty()) {
                    message.setTransmissionStatus(TransmissionStatus.UNPLAYABLE);
                } else {
                    groups = checkSuite(input, groups);
                    if (groups.isEmpty()) {
                        message.setTransmissionStatus(TransmissionStatus.UNASSIGNED);
                    } else {
                        boolean languageNotSupported = false;
                        try {
                            /*
                             * This information can optionally be cached
                             * provided that the required listeners are setup to
                             * listen to changes to the configured {@link
                             * TtsVoice}s.
                             */
                            languageNotSupported = (this.ttsVoiceDao
                                    .getDefaultVoiceForLanguage(input
                                            .getLanguage()) == null);
                        } catch (IllegalStateException e) {
                            /*
                             * An unlikely case. The {@link Language} is
                             * supported. But, it is not possible to determine
                             * the default voice because more than one exists in
                             * the database for the specified {@link Language}.
                             */
                        }
                        if (languageNotSupported) {
                            message.setTransmissionStatus(TransmissionStatus.NOLANG);
                        } else {
                            message.setTransmitterGroups(groups);
                            message.setTransmissionStatus(TransmissionStatus.ACCEPTED);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            message.setTransmissionStatus(TransmissionStatus.ERROR);
            statusHandler.error(BMH_CATEGORY.MESSAGE_VALIDATION_ERROR,
                    "Error Parsing InputMessage", e);
        }
    }

    /**
     * Check if a message has expired by comparing the expiration time to the
     * current {@link SimulatedTime}.
     * 
     * @param message
     *            the message to validate.
     * @return true if the message is expired.
     */
    protected static boolean isExpired(InputMessage message) {
        Calendar expirationTime = message.getExpirationTime();
        if (expirationTime == null) {
            return false;
        } else {
            return TimeUtil.newGmtCalendar().after(message.getExpirationTime());
        }
    }

    /**
     * Check if a message type is defined in the BMH configuration
     * 
     * @param message
     *            the message to validate.
     * @return true if the message type is not defined
     */
    protected boolean checkConfiguration(InputMessage message) {
        return messageTypeDao.getByAfosId(message.getAfosid()) == null;
    }

    /**
     * Get any transmitter groups that contain transmitters covering the
     * geographical area of the message.
     * 
     * 
     * @param message
     *            the message to validate.
     * @return any transmitter groups matching the geographical area of this
     *         message.
     */
    protected Set<TransmitterGroup> getTransmissionGroups(InputMessage message) {
        List<String> ugcList = message.getAreaCodeList();
        Set<TransmitterGroup> transmitterGroups = new HashSet<>(
                ugcList.size() * 2);
        for (String ugc : ugcList) {
            if (ugc.charAt(2) == 'Z') {
                Zone zone = zoneDao.getByZoneCode(ugc);
                if (zone == null) {
                    statusHandler.warn(BMH_CATEGORY.MESSAGE_AREA_UNCONFIGURED,
                            "Message zone is not configured: " + ugc);
                } else {
                    for (Area area : zone.getAreas()) {
                        for (Transmitter t : area.getTransmitters()) {
                            transmitterGroups.add(t.getTransmitterGroup());
                        }
                    }
                }
            } else {
                Area area = areaDao.getByAreaCode(ugc);
                if (area == null) {
                    statusHandler.warn(BMH_CATEGORY.MESSAGE_AREA_UNCONFIGURED,
                            "Message area is not configured: " + ugc);
                } else {
                    for (Transmitter t : area.getTransmitters()) {
                        transmitterGroups.add(t.getTransmitterGroup());
                    }
                }
            }
        }

        return transmitterGroups;
    }

    /**
     * Check if a message type is assigned to any suite.
     * 
     * @param message
     *            the message to validate.
     * @param groups
     *            groups that the message might be sent too
     * @return any transmitter groups from groups whose program contains a suite
     *         for this message type.
     */
    protected Set<TransmitterGroup> checkSuite(InputMessage message,
            Set<TransmitterGroup> groups) {
        // TODO: Optimize
        List<TransmitterGroup> programGroups = programDao
                .getGroupsForMsgType(message.getAfosid());
        Set<TransmitterGroup> result = new HashSet<>(groups);
        result.retainAll(programGroups);
        return result;
    }

}
