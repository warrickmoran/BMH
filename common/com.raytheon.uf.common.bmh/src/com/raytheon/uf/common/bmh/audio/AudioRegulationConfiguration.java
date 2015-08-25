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
package com.raytheon.uf.common.bmh.audio;

import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Configurable audio regulation settings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 24, 2015 4770       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
@XmlRootElement(name = AudioRegulationConfiguration.ROOT_NAME)
public class AudioRegulationConfiguration {

    protected static final String ROOT_NAME = "regulationConfiguration";

    public static final String XML_NAME = ROOT_NAME + ".xml";

    /*
     * Just a placeholder for now.
     */
    public static enum ALGORITHM {
        LINEAR_PCM;
    }

    @DynamicSerializeElement
    private double dbSilenceLimit;

    @DynamicSerializeElement
    private ALGORITHM regulationAlgorithm;

    public AudioRegulationConfiguration() {
    }

    /**
     * @return the dbSilenceLimit
     */
    public double getDbSilenceLimit() {
        return dbSilenceLimit;
    }

    /**
     * @param dbSilenceLimit
     *            the dbSilenceLimit to set
     */
    public void setDbSilenceLimit(double dbSilenceLimit) {
        this.dbSilenceLimit = dbSilenceLimit;
    }

    /**
     * @return the regulationAlgorithm
     */
    public ALGORITHM getRegulationAlgorithm() {
        return regulationAlgorithm;
    }

    /**
     * @param regulationAlgorithm
     *            the regulationAlgorithm to set
     */
    public void setRegulationAlgorithm(ALGORITHM regulationAlgorithm) {
        this.regulationAlgorithm = regulationAlgorithm;
    }
}