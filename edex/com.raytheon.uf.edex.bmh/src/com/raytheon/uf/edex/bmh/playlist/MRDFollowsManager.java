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
package com.raytheon.uf.edex.bmh.playlist;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.CircularFollowsException;
import com.raytheon.uf.common.bmh.ImpossibleFollowsException;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * Reorders a playlist based on the mrd follows rules.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 13, 2015 4484       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MRDFollowsManager {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(MRDFollowsManager.class);

    /**
     * The initial suite-ordered messages.
     */
    private final List<Long> orderedMessages;

    /**
     * Keeps track of the mrd rules that the messages will be ordered based on.
     * The key {@link Long} value in the {@link Map} is the id of the message
     * that will be followed. The associated {@link List} of {@link Long}s are
     * the
     */
    private final Map<Long, List<Long>> mrdFollowsRulesMap;

    /**
     * Keeps track of the mrd rules that have currently been applied. This
     * {@link Map} will be referenced when other messages are moved to ensure
     * that the movement will not violate any of the previous rules that have
     * already been applied. Essentially just a replica of
     * {@link #mrdFollowsRulesMap} limited to the rules that have actually been
     * applied as they are applied.
     */
    private final Map<Long, List<Long>> appliedRulesMap = new LinkedHashMap<>();

    /**
     * Used to keep track of which messages have been moved to a position
     * immediately after another message. The goal is to attempt to keep
     * messages that have been moved with each other although that may not
     * always be possible based on the rules. And the movement is chained such
     * that if: message N has been relocated to a location after message T and
     * message T has been relocated to a location after message R, when message
     * R is moved message T and N will also be moved.
     * 
     * $----------RTN-----
     * 
     * (move R to 1 follows $)
     * 
     * $RTN---------------
     * 
     * However, if there is a M earlier in the sequence and there is a rule that
     * states N must follow M, R will not be moved; although, this would not
     * violate the ruling because R would still be after $.
     * 
     * $--MSFT----RTN-----
     * 
     * (move R to 1 follows $; but N must follow M X NOT POSSIBLE)
     */
    private final Map<Long, Long> mrdFollowsChainMap = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param orderedMessages
     *            a {@link List} of the suite-ordered messages
     * @param mrdReplacementRulesMap
     *            a {@link Map}
     */
    public MRDFollowsManager(List<Long> orderedMessages,
            Map<Long, List<Long>> mrdReplacementRulesMap) {
        this.orderedMessages = orderedMessages;
        this.mrdFollowsRulesMap = mrdReplacementRulesMap;
    }

    /**
     * Orders the {@link #orderedMessages} based on the
     * {@link #mrdFollowsRulesMap}.
     * 
     * @param playlist
     *            the playlist that contains the messages that are being
     *            re-ordered (only for logging purposes)
     * @throws CircularFollowsException
     * @throws ImpossibleFollowsException
     */
    public List<Long> orderWithFollows(String playlist) {
        for (Long followsId : mrdFollowsRulesMap.keySet()) {
            int followsIndex = orderedMessages.indexOf(followsId);
            if (followsIndex == -1) {
                continue;
            }

            for (Long followerId : mrdFollowsRulesMap.get(followsId)) {
                int followerIndex = orderedMessages.indexOf(followerId);
                if (followerIndex == -1) {
                    continue;
                }

                try {
                    if (followerIndex == followsIndex + 1) {
                        /*
                         * message is already at the correct location.
                         */
                        mrdFollowsChainMap.put(followsId, followerId);
                    } else if (followerIndex > followsIndex) {
                        /*
                         * the follower already follows the follows. however,
                         * the follower is not immediately after the follows.
                         * determine, if the follower can be moved immediately
                         * after the follows. if it cannot (dependent on if its
                         * chained followers can also be removed), then it will
                         * remain where it is.
                         */
                        int newIndex = followsIndex + 1;
                        if (this.validateNewPosition(followerId, newIndex)) {
                            this.applyFollowsRule(followsId, followerId,
                                    followerIndex, newIndex, true);
                        }
                    } else {
                        /*
                         * the follower is currently located before the follows.
                         * determine if the follower (and all dependent chained
                         * followers) can be relocated after the follows. throw
                         * an {@link ImpossibleFollowsException} if that is not
                         * possible because there is no way to arrange the
                         * messages in the desired order.
                         */
                        int newIndex = followsIndex;
                        if (this.validateNewPosition(followerId, newIndex)) {
                            this.applyFollowsRule(followsId, followerId,
                                    followerIndex, newIndex, false);
                        } else {
                            throw new ImpossibleFollowsException(followerId,
                                    followsId);
                        }
                    }
                } catch (Exception e) {
                    statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                            "Failed to complete mrd follows message sorting for playlist: "
                                    + playlist + ".", e);
                    continue;
                }
                if (appliedRulesMap.containsKey(followerId) == false) {
                    appliedRulesMap.put(followerId, new LinkedList<Long>());
                }
                appliedRulesMap.get(followerId).add(followsId);
            }
        }

        return this.orderedMessages;
    }

    /**
     * Ensures that the message associated with the specified id can be
     * relocated to the specified index. This function will verify that both
     * previously applied rules will not be violated and that any chained
     * followers will also be able to move with the message.
     * 
     * @param followerId
     *            the specified id
     * @param desiredPositionIndex
     *            the specified index
     * @return true, if the relocation is possible; false, otherwise
     * @throws CircularFollowsException
     */
    private boolean validateNewPosition(long followerId,
            int desiredPositionIndex) throws CircularFollowsException {
        /*
         * Verify that the follower will still follow all associated follows if
         * it were to be moved.
         */
        if (appliedRulesMap.containsKey(followerId)) {
            for (long followsId : appliedRulesMap.get(followerId)) {
                /*
                 * Verify that the follower would still be after the followed if
                 * it were moved to its new position.
                 */
                final int followedIndex = orderedMessages.indexOf(followsId);
                if (desiredPositionIndex < followedIndex) {
                    return false;
                }
            }
        }

        /*
         * will anybody be traveling with us?
         */
        if (mrdFollowsChainMap.containsKey(followerId) == false) {
            return true;
        }

        final long startingFollowerId = followerId;
        final List<Long> followsPath = new LinkedList<>();
        followsPath.add(startingFollowerId);
        while (mrdFollowsChainMap.containsKey(followerId)) {
            final long chainedFollowerId = mrdFollowsChainMap.get(followerId);
            followsPath.add(chainedFollowerId);
            if (startingFollowerId == chainedFollowerId) {
                throw new CircularFollowsException(followsPath);
            }
            ++desiredPositionIndex;
            if (validateNewPosition(chainedFollowerId, desiredPositionIndex) == false) {
                return false;
            }
            followerId = chainedFollowerId;
        }

        return true;
    }

    /**
     * Relocates the specified follower to the specified location immediately
     * after the specified followee.
     * 
     * @param followsId
     *            the specified followee
     * @param followerId
     *            the specified follower
     * @param followerIndex
     *            the current location of the follower
     * @param newIndex
     *            the new location of the follower
     * @param incrementIndex
     *            boolean indicating whether or not the new location (newIndex)
     *            will need to be incremented as subsequent chained followers
     *            are moved to a location immediately following the follower.
     *            The location generally only needs to be incremented when the
     *            follower et al are being moved backwards.
     * @throws CircularFollowsException
     */
    private void applyFollowsRule(long followsId, long followerId,
            int followerIndex, int newIndex, boolean incrementIndex)
            throws CircularFollowsException {
        this.orderedMessages.remove(followerIndex);
        this.orderedMessages.add(newIndex, followerId);

        mrdFollowsChainMap.put(followsId, followerId);

        final long startingFollowerId = followerId;
        final List<Long> followsPath = new LinkedList<>();
        followsPath.add(startingFollowerId);
        while (mrdFollowsChainMap.containsKey(followerId)) {
            if (incrementIndex) {
                ++newIndex;
            }

            long chainedFollowerId = mrdFollowsChainMap.get(followerId);
            followsPath.add(chainedFollowerId);
            int chainedFollowIndex = orderedMessages.indexOf(chainedFollowerId);
            if (startingFollowerId == chainedFollowerId) {
                throw new CircularFollowsException(followsPath);
            }

            orderedMessages.remove(chainedFollowIndex);
            orderedMessages.add(newIndex, chainedFollowerId);

            followerId = chainedFollowerId;
        }
    }
}