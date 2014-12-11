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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;

/**
 * An implementation of {@link AbstractAudioFileBuffer} that supports
 * dynamically changing audio segments on-demand. Can be used to construct an
 * {@link AudioFileBuffer} based on the current state of the audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 1, 2014  3642       bkowal      Initial creation
 * Dec 11, 2014 3651       bkowal      Added {@link DacPlaylistMessage} to
 *                                     the constructor.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class DynamicTimeAudioFileBuffer extends AbstractAudioFileBuffer {

    /*
     * The estimated capacity below is based on the total size of the audio
     * associated with a combination of the following: two digit hour, a two
     * digit minute, 'PM', and a timezone generated using Paul's voice rounded
     * up to a multiple of 5.
     */
    private static final int ESTIMATED_TIME_ELEMENT_CAPACITY = 22600;

    private final ByteBuffer tonesAudio;

    private List<byte[]> audioSegments;

    private final Map<Integer, TIME_MSG_TOKENS> dynamicAudioPositionMap;

    private int currentMessageCapacity;

    private final ByteBuffer endOfMessageAudio;

    private final TimeMsgCache timeCache;

    public DynamicTimeAudioFileBuffer(final DacPlaylistMessage dacMsg,
            ByteBuffer tonesAudio, List<byte[]> audioSegments,
            final Map<Integer, TIME_MSG_TOKENS> dynamicAudioPositionMap,
            ByteBuffer endOfMessageAudio, final TimeMsgCache timeCache) {
        super(dacMsg);
        this.tonesAudio = tonesAudio;
        this.audioSegments = audioSegments;
        this.dynamicAudioPositionMap = dynamicAudioPositionMap;
        this.endOfMessageAudio = endOfMessageAudio;
        this.timeCache = timeCache;

        this.currentMessageCapacity = 0;
        for (byte[] segment : this.audioSegments) {
            if (segment == null) {
                continue;
            }
            this.currentMessageCapacity += segment.length;
        }
    }

    public void updateDynamic(byte[] audioSegment, int index) {
        this.audioSegments.remove(index);
        this.audioSegments.add(index, audioSegment);
    }

    /*
     * Constructs a {@link AudioFileBuffer} using the current state of the
     * managed audio.
     */
    public AudioFileBuffer finalizeFileBuffer(Calendar calendar)
            throws NotTimeCachedException {
        /*
         * Fill in / replace the dynamic content based on the specified {@link
         * Calendar}.
         */
        Iterator<Integer> dynamicIndexIterator = this.dynamicAudioPositionMap
                .keySet().iterator();
        while (dynamicIndexIterator.hasNext()) {
            int index = dynamicIndexIterator.next();
            TIME_MSG_TOKENS token = this.dynamicAudioPositionMap.get(index);
            if (token == null) {
                // not a dynamic audio segment.
                continue;
            }

            final String tokenValue = token.getTokenValue(calendar);
            if (token.isSkipped(tokenValue)) {
                /*
                 * No audio should be retrieved for this token at this time
                 * based on its current value.
                 */
                continue;
            }
            byte[] audioData = this.timeCache
                    .lookupTimeCache(token, tokenValue);
            if (audioData == null) {
                throw new NotTimeCachedException(token, tokenValue);
            }

            this.audioSegments.remove(index);
            this.audioSegments.add(index, audioData);
        }

        int dynamicContentSize = 0;
        for (byte[] segment : this.audioSegments) {
            if (segment == null) {
                continue;
            }
            dynamicContentSize += segment.length;
        }

        ByteBuffer finalMessageBuffer = ByteBuffer.allocate(dynamicContentSize);
        for (int i = 0; i < this.audioSegments.size(); i++) {
            if (this.audioSegments.get(i) == null) {
                continue;
            }
            finalMessageBuffer.put(this.audioSegments.get(i));
        }

        AudioFileBuffer buffer = new AudioFileBuffer(this.message,
                finalMessageBuffer.array(), this.tonesAudio,
                this.endOfMessageAudio);
        buffer.rewind();
        return buffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dactransmit.playlist.IAudioFileBuffer#capacity
     * (boolean)
     */
    @Override
    public int capacity(boolean includeTones) {
        int capacity = this.currentMessageCapacity
                + ESTIMATED_TIME_ELEMENT_CAPACITY;
        if (includeTones) {
            if (this.tonesAudio != null) {
                capacity += this.tonesAudio.capacity();
            }
            if (this.endOfMessageAudio != null) {
                capacity += this.endOfMessageAudio.capacity();
            }
        }

        return capacity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dactransmit.playlist.AbstractAudioFileBuffer
     * #isDynamic()
     */
    @Override
    public boolean isDynamic() {
        return true;
    }
}