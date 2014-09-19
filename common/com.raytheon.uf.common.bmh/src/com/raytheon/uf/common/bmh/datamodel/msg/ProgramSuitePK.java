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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.io.Serializable;

import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Primary key for {@link ProgramSuite}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 11, 2014 3587       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Embeddable
@DynamicSerialize
public class ProgramSuitePK implements Serializable {

    private static final long serialVersionUID = -3333221321096465284L;

    @DynamicSerializeElement
    private int programId;
    
    @DynamicSerializeElement
    private int suiteId;

    /**
     * @return the programId
     */
    public int getProgramId() {
        return programId;
    }

    /**
     * @param programId
     *            the programId to set
     */
    public void setProgramId(int programId) {
        this.programId = programId;
    }

    /**
     * @return the suiteId
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * @param suiteId the suiteId to set
     */
    public void setSuiteId(int suiteId) {
        this.suiteId = suiteId;
    }
}