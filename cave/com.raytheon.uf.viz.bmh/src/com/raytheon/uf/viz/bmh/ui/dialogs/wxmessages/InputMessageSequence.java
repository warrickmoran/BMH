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
package com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages;

/**
 * Basic POJO used to track a sequence of input messages as well as the index of the currently
 * selected message.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 12, 2015 4113       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class InputMessageSequence {
    
    public static enum SEQUENCE_DIRECTION {
        LEFT, RIGHT;
    }

    private int currentSequenceIndex;

    private final int[] messageSequence;
    
    private final int maxSequence;

    public InputMessageSequence(int initialSequence, int[] messageSequence) {
        this.currentSequenceIndex = initialSequence;
        this.messageSequence = messageSequence;
        this.maxSequence = this.messageSequence.length;
    }

    public int getCurrentSequence() {
        if (maxSequence == 0) {
            return -1;
        }

        return this.messageSequence[this.currentSequenceIndex];
    }

    public void advanceSequence(SEQUENCE_DIRECTION direction) {
        switch (direction) {
        case LEFT:
            --this.currentSequenceIndex;
            break;
        case RIGHT:
            ++this.currentSequenceIndex;
            break;
        }

        if (this.currentSequenceIndex < 0) {
            // set to the final sequence
            this.currentSequenceIndex = maxSequence - 1;
        }
        if (this.currentSequenceIndex == maxSequence) {
            // set to the first sequence
            this.currentSequenceIndex = 0;
        }
    }
    
    public int getCurrentIndex() {
        return this.currentSequenceIndex;
    }

    /**
     * @return the maxSequence
     */
    public int getMaxSequence() {
        return maxSequence;
    }
}