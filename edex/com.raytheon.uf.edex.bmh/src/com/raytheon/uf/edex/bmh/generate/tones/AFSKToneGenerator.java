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
package com.raytheon.uf.edex.bmh.generate.tones;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.LinkedList;
import java.util.BitSet;

import com.raytheon.uf.edex.bmh.generate.tones.data.AFSKTone;

/**
 * Used to encode a Specific Area Message into sound based on {@link AFSKTone}
 * by utilizing the {@link ToneGenerator}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AFSKToneGenerator {
    private static final double DEFAULT_AMPLITUDE = 8192.0;

    private static final String BYTES_CHARSET = "US-ASCII";

    /*
     * The following values are defined on page 6 of the SAME Specification
     * (http://www.nws.noaa.gov/directives/sym/pd01017012curr.pdf)
     */
    private static final double SAME_LOGIC_ZERO = 1562.5; // Hz

    private static final double SAME_LOGIC_ONE = 2083.3; // Hz

    private ToneGenerator toneGenerator;

    private AFSKTone toneZero;

    private AFSKTone toneOne;

    /**
     * Constructor
     */
    public AFSKToneGenerator() {
        this.toneGenerator = new ToneGenerator();
        this.initializeAFSKTones();
    }

    /**
     * Constructor
     * 
     * @param toneGenerator
     *            the tone generator to use
     */
    public AFSKToneGenerator(ToneGenerator toneGenerator) {
        this.toneGenerator = toneGenerator;
        this.initializeAFSKTones();
    }

    /**
     * Create the tones in PCM format. Based on FskGenerator::execute(short
     * **soundBytes) in fskEncoder.C in the original BMH source code.
     * 
     * @param sameMessage
     *            the SAME message to encode
     * @return the encoded data.
     */
    public short[] execute(final String sameMessage)
            throws UnsupportedEncodingException {

        List<short[]> outputList = new LinkedList<short[]>();
        BitSet sameMessageBits = BitSet.valueOf((sameMessage.trim()
                .getBytes(BYTES_CHARSET)));
        for (int i = 0; i < sameMessageBits.length(); i++) {
            if (sameMessageBits.get(i)) {
                outputList.add(this.toneGenerator.encode(this.toneOne));
            } else {
                outputList.add(this.toneGenerator.encode(this.toneZero));
            }
        }

        return TonesManager.mergeShortData(outputList);
    }

    /**
     * Create the tone data structures that will be used to generate the audio
     * data.
     */
    private void initializeAFSKTones() {
        this.toneZero = new AFSKTone();
        this.toneZero.setFrequency(SAME_LOGIC_ZERO);
        this.toneZero.setAmplitude(DEFAULT_AMPLITUDE);

        this.toneOne = new AFSKTone();
        this.toneOne.setFrequency(SAME_LOGIC_ONE);
        this.toneOne.setAmplitude(DEFAULT_AMPLITUDE);
    }

    public AFSKTone getToneZero() {
        return toneZero;
    }

    public void setToneZero(AFSKTone toneZero) {
        this.toneZero = toneZero;
    }

    public AFSKTone getToneOne() {
        return toneOne;
    }

    public void setToneOne(AFSKTone toneOne) {
        this.toneOne = toneOne;
    }
}