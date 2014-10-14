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
package com.raytheon.uf.common.bmh.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object for {@link Program} queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 5, 2014  #3490      lvenable    Initial creation
 * Aug 17, 2014 #3490      lvenable    Added addProgram method.
 * Oct 13, 2014 3654       rjpeter     Updated to use ProgramSummary.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class ProgramResponse {

    @DynamicSerializeElement
    private List<Program> programList;

    @DynamicSerializeElement
    private List<ProgramSummary> programSummaryList;

    /**
     * Set the list of programs.
     * 
     * @param programList
     */
    public void setProgramList(List<Program> programList) {
        this.programList = programList;
    }

    /**
     * Get the list of programs.
     * 
     * @return The list of programs.
     */
    public List<Program> getProgramList() {
        return this.programList;
    }

    /**
     * Add a program to the list.
     * 
     * @param p
     *            Program.
     */
    public void addProgram(Program p) {
        if (programList == null) {
            programList = new ArrayList<Program>(1);
        }
        programList.add(p);
    }

    public List<ProgramSummary> getProgramSummaryList() {
        return programSummaryList;
    }

    public void setProgramSummaryList(List<ProgramSummary> programSummaryList) {
        this.programSummaryList = programSummaryList;
    }

}
