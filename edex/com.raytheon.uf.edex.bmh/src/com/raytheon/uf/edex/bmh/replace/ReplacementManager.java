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
package com.raytheon.uf.edex.bmh.replace;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage.ReplacementType;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * 
 * Find and update all the messages a new message can replace or that can
 * replace a new message.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Mar 24, 2015  4290     bsteffen    Initial creation
 * May 18, 2015  4483     bkowal      Added {@link #handleMrdIdentityReplace(InputMessage, Calendar, Set)}.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ReplacementManager {

    private static final DecimalFormat mrdFormat = new DecimalFormat("000");

    private InputMessageDao inputMessageDao;

    private MessageTypeDao messageTypeDao;

    private IMessageLogger messageLogger;

    /**
     * Handle all forms of replacement related to the provided inputMessage
     * 
     * @return All input messages which have been replaced by the provided
     *         message.
     */
    public Set<InputMessage> replace(InputMessage inputMessage) {
        validateDaos();
        Calendar currentTime = TimeUtil.newGmtCalendar();
        if (!inputMessage.getActive()
                || currentTime.after(inputMessage.getExpirationTime())) {
            return Collections.emptySet();
        }
        Calendar replaceTime = inputMessage.getEffectiveTime();
        if (currentTime.after(replaceTime)) {
            replaceTime = currentTime;
        }
        Set<InputMessage> replacements = new HashSet<>();
        if (inputMessage.getMrd() != null) {
            replacements.addAll(handleMrdReplace(inputMessage, replaceTime));
            handleReverseMrdReplace(inputMessage, replaceTime);
            handleMrdIdentityReplace(inputMessage, replaceTime, replacements);
        } else {
            replacements
                    .addAll(handleIdentityReplace(inputMessage, replaceTime));
            replacements.addAll(handleMatReplace(inputMessage, replaceTime));
            handleReverseMatReplace(inputMessage, replaceTime);
        }
        return replacements;
    }

    private Set<InputMessage> handleMrdReplace(InputMessage inputMessage,
            Calendar replaceTime) {
        /*
         * Query for all messages that expire in the future and that start with
         * an mrd that is in the list of mrds this mesage replaces.
         */
        Set<InputMessage> replacements = new HashSet<>();
        for (int mrd : inputMessage.getMrdReplacements()) {
            String mrdLike = mrdFormat.format(mrd) + "%";
            List<InputMessage> replacees = inputMessageDao
                    .getActiveWithMrdLike(mrdLike, replaceTime);
            for (InputMessage replacee : replacees) {
                replacee.setExpirationTime(replaceTime);
                inputMessageDao.saveOrUpdate(replacee);
                replacements.add(replacee);
                logReplacement(inputMessage, replacee);
            }
        }
        if (!replacements.isEmpty()) {
            inputMessage.setReplacementType(ReplacementType.MRD);
            inputMessageDao.saveOrUpdate(inputMessage);
        }
        return replacements;
    }

    private void handleReverseMrdReplace(InputMessage inputMessage,
            Calendar replaceTime) {
        /*
         * Query for all messages that expire in the future and with an mrd that
         * contains this message. When the mrd of these messages is parsed it
         * may not be an actual match so it must be checked again. The new
         * message will only be replaced by future messages that have a time
         * period overlapping this time period. If multiple messages can replace
         * the new message only the earliest is marked as a replace.
         */
        int mrd = inputMessage.getMrdId();
        String mrdLike = "%" + mrdFormat.format(mrd) + "%";
        List<InputMessage> replacers = inputMessageDao.getActiveWithMrdLike(
                mrdLike, replaceTime);
        Calendar earliestReplacementTime = inputMessage.getExpirationTime();
        InputMessage firstReplacer = null;
        for (InputMessage replacer : replacers) {
            Calendar replacerEffectiveTime = replacer.getEffectiveTime();
            if (replacerEffectiveTime.before(replaceTime)
                    || replacerEffectiveTime.after(earliestReplacementTime)) {
                continue;
            }
            for (int replaceMrd : replacer.getMrdReplacements()) {
                if (replaceMrd == mrd) {
                    firstReplacer = replacer;
                    earliestReplacementTime = replacer.getEffectiveTime();
                    break;
                }
            }
        }
        if (firstReplacer != null) {
            firstReplacer.setReplacementType(ReplacementType.MRD);
            inputMessage.setExpirationTime(firstReplacer.getEffectiveTime());
            inputMessageDao.saveOrUpdate(firstReplacer);
            inputMessageDao.saveOrUpdate(inputMessage);
            logReplacement(firstReplacer, inputMessage);
        }
    }

    private void handleMrdIdentityReplace(InputMessage inputMessage,
            Calendar replaceTime, Set<InputMessage> replacements) {
        /*
         * Find all active {@link InputMessage}s with the same afos id and mrd
         * id.
         */
        String mrdLike = mrdFormat.format(inputMessage.getMrdId());
        List<InputMessage> replacees = inputMessageDao.getActiveWithMrdLike(
                mrdLike, replaceTime);
        for (InputMessage replacee : replacees) {
            if (replacee.getId() == inputMessage.getId()) {
                continue;
            }
            if (replacee.getMrdId() != inputMessage.getMrdId()) {
                continue;
            }

            if (inputMessage.getUpdateDate().after(replacee.getUpdateDate())) {
                replacee.setExpirationTime(replaceTime);
                inputMessageDao.saveOrUpdate(replacee);
                logReplacement(inputMessage, replacee);
                replacements.add(replacee);
            }
        }
    }

    private Set<InputMessage> handleIdentityReplace(InputMessage inputMessage,
            Calendar replaceTime) {
        /*
         * Query for all messages that expire in the future and have matching
         * area codes and afosids. This query will also return the current
         * message so discard that result. Compare the time period of all
         * potential replacements with the time period of the input message. If
         * the replacement is effective in the future then it can replace the
         * new message, if the replacement was effective in the past then the
         * new message will replace it, if the replacement is too far in the
         * future so there is no overlap in the time periods for the message
         * then no replacement is performed.
         */
        Set<InputMessage> replacements = new HashSet<>();
        List<InputMessage> queryResults = inputMessageDao
                .getActiveWithAfosidAndAreaCodes(inputMessage.getAfosid(),
                        inputMessage.getAreaCodes(), replaceTime);
        for (InputMessage replacement : queryResults) {
            if (replacement.getId() == inputMessage.getId()) {
                continue;
            }
            Calendar replacementEffectiveTime = replacement.getEffectiveTime();
            if (replacementEffectiveTime.before(replaceTime)) {
                replacement.setExpirationTime(replaceTime);
                inputMessageDao.saveOrUpdate(replacement);
                replacements.add(replacement);
                logReplacement(inputMessage, replacement);
            } else if (replacementEffectiveTime.before(inputMessage
                    .getExpirationTime())) {
                inputMessage.setExpirationTime(replacementEffectiveTime);
                inputMessageDao.saveOrUpdate(inputMessage);
                logReplacement(replacement, inputMessage);
            }
        }
        return replacements;
    }

    private Set<InputMessage> handleMatReplace(InputMessage inputMessage,
            Calendar replaceTime) {
        /*
         * Query for all messages that expire in the future and have matching
         * area codes and afosids that are replaced by the new message. The new
         * message will only replace messages that have a time period
         * overlapping this time period, future message with no overlap are not
         * replaced.
         */
        Collection<String> replacementAfosids = messageTypeDao
                .getReplacementAfosIdsForAfosId(inputMessage.getAfosid());
        Set<InputMessage> replacements = new HashSet<>();
        for (String afosid : replacementAfosids) {
            List<InputMessage> replacees = inputMessageDao
                    .getActiveWithAfosidAndAreaCodes(afosid,
                            inputMessage.getAreaCodes(), replaceTime);
            for (InputMessage replacee : replacees) {
                if (replacee.getEffectiveTime().before(
                        inputMessage.getExpirationTime())) {
                    replacee.setExpirationTime(replaceTime);
                    inputMessageDao.saveOrUpdate(replacee);
                    replacements.add(replacee);
                    logReplacement(inputMessage, replacee);
                }
            }
        }
        if (!replacements.isEmpty()) {
            inputMessage.setReplacementType(ReplacementType.MAT);
            inputMessageDao.saveOrUpdate(inputMessage);
        }
        return replacements;

    }

    private void handleReverseMatReplace(InputMessage inputMessage,
            Calendar replaceTime) {
        /*
         * Query for all messages that expire in the future and have matching
         * area codes and afosids that are replaced by this message. The new
         * message will only be replaced future messages that have a time period
         * overlapping this time period. If multiple messages can replace the
         * new message only the earliest is marked as a replace.
         */
        Collection<String> replacementAfosids = messageTypeDao
                .getReverseReplacementAfosIdsForAfosId(inputMessage.getAfosid());
        Calendar earliestReplacementTime = inputMessage.getExpirationTime();
        InputMessage firstReplacer = null;
        for (String afosid : replacementAfosids) {
            List<InputMessage> replacers = inputMessageDao
                    .getActiveWithAfosidAndAreaCodes(afosid,
                            inputMessage.getAreaCodes(), replaceTime);
            for (InputMessage replacer : replacers) {
                Calendar replacerEffectiveTime = replacer.getEffectiveTime();
                if (replacerEffectiveTime.after(replaceTime)
                        && replacerEffectiveTime
                                .before(earliestReplacementTime)) {
                    firstReplacer = replacer;
                    earliestReplacementTime = replacer.getEffectiveTime();
                }
            }
        }
        if (firstReplacer != null) {
            firstReplacer.setReplacementType(ReplacementType.MAT);
            inputMessage.setExpirationTime(firstReplacer.getEffectiveTime());
            inputMessageDao.saveOrUpdate(firstReplacer);
            inputMessageDao.saveOrUpdate(inputMessage);
            logReplacement(firstReplacer, inputMessage);
        }

    }

    protected void logReplacement(InputMessage replacer, InputMessage replacee) {
        if (messageLogger != null) {
            messageLogger.logReplacementActivity(replacer, replacee);
        }
    }

    public void setInputMessageDao(InputMessageDao inputMessageDao) {
        this.inputMessageDao = inputMessageDao;
    }

    public void setMessageTypeDao(MessageTypeDao messageTypeDao) {
        this.messageTypeDao = messageTypeDao;
    }

    public void setMessageLogger(IMessageLogger messageLogger) {
        this.messageLogger = messageLogger;
    }

    private void validateDaos() throws IllegalStateException {
        if (inputMessageDao == null) {
            throw new IllegalStateException(
                    "InputMessageDao has not been set on the ReplacementManager");
        } else if (messageTypeDao == null) {
            throw new IllegalStateException(
                    "MessageTypeDao has not been set on the ReplacementManager");
        }
    }

}
