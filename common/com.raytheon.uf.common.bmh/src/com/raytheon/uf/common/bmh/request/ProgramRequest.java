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

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request object for Program queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 05, 2014  3490     lvenable    Initial creation
 * Aug 12, 2014  3490     lvenable    Added ProgramSuites action.
 * Aug 15, 2014  3490     lvenable    Added Program with getters & setters.
 * Aug 15, 2014  3432     mpduff      Added GetProgramForTransmitterGroup
 * Sep 18, 2014  3587     bkowal      Added GetProgramsWithTrigger
 * Oct 02, 2014  3649     rferrel     Added AddGroup.
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Oct 13, 2014  3654     rjpeter     Updated to use ProgramSummary.
 * Nov 20, 2014  3698     rferrel     Added SuitePrograms and SuiteEnabledGroups.
 * Dec 02, 2014  3838     rferrel     Added ProgramGeneralSuite.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class ProgramRequest extends AbstractBMHServerRequest {

    public enum ProgramAction {
        Save, AllProgramSummaries, AllPrograms,

        ProgramSuites, Delete, GetProgramForTransmitterGroup,

        GetProgramsWithTrigger, AddGroup, SuitePrograms, SuiteEnabledGroups, ProgramGeneralSuite;
    }

    @DynamicSerializeElement
    private ProgramAction action;

    @DynamicSerializeElement
    private Program program;

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    /*
     * At this point in time, it is not necessary to transfer the entire Suite
     * and Message Types to the request handler.
     */
    @DynamicSerializeElement
    private int suiteId;

    @DynamicSerializeElement
    private int msgTypeId;

    /**
     * Get the program action.
     * 
     * @return The program action.
     */
    public ProgramAction getAction() {
        return action;
    }

    /**
     * Set the program action.
     * 
     * @param action
     *            The program action.
     */
    public void setAction(ProgramAction action) {
        this.action = action;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the suiteId
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * @param suiteId
     *            the suiteId to set
     */
    public void setSuiteId(int suiteId) {
        this.suiteId = suiteId;
    }

    /**
     * @return the msgTypeId
     */
    public int getMsgTypeId() {
        return msgTypeId;
    }

    /**
     * @param msgTypeId
     *            the msgTypeId to set
     */
    public void setMsgTypeId(int msgTypeId) {
        this.msgTypeId = msgTypeId;
    }
}