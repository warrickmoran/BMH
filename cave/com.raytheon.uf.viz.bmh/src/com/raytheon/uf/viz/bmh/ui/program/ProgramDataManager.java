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
package com.raytheon.uf.viz.bmh.ui.program;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramRequest.ProgramAction;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * DataManager for the Program data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 17, 2014  #3490     lvenable     Initial creation
 * Sep 18, 2014  #3587     bkowal       Added methods to retrieve programs associated
 *                                      with a trigger.
 * Oct 02, 2014  #3649     rferrel      Added methods addGroup and getProgramForTransmitterGroup.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class ProgramDataManager {

    /**
     * Let a list of fully populated programs.
     * 
     * @param comparator
     *            Comparator used for sorting, null for no sorting.
     * @return List of programs.
     * @throws Exception
     */
    public List<Program> getAllPrograms(Comparator<Program> comparator)
            throws Exception {
        List<Program> programList = null;

        ProgramRequest pr = new ProgramRequest();
        pr.setAction(ProgramAction.AllPrograms);
        ProgramResponse progResponse = null;

        progResponse = (ProgramResponse) BmhUtils.sendRequest(pr);
        programList = progResponse.getProgramList();

        if (comparator != null && programList.isEmpty() == false) {
            Collections.sort(programList, comparator);
        }

        return programList;
    }

    /**
     * Save the selected program.
     * 
     * @param selectedProgram
     *            Program to save.
     * @return The saved program.
     * @throws Exception
     */
    public ProgramResponse saveProgram(Program selectedProgram)
            throws Exception {
        ProgramRequest pr = new ProgramRequest();
        pr.setProgram(selectedProgram);
        pr.setAction(ProgramAction.Save);

        ProgramResponse response = (ProgramResponse) BmhUtils.sendRequest(pr);
        return response;
    }

    public ProgramResponse addGroup(Program program, TransmitterGroup group)
            throws Exception {
        ProgramRequest pr = new ProgramRequest();
        pr.setProgram(program);
        pr.setTransmitterGroup(group);
        pr.setAction(ProgramAction.AddGroup);

        ProgramResponse response = (ProgramResponse) BmhUtils.sendRequest(pr);
        return response;
    }

    /**
     * Delete the selected program.
     * 
     * @param selectedProgram
     *            The program to delete.
     * @return Program response.
     * @throws Exception
     */
    public ProgramResponse deleteProgram(Program selectedProgram)
            throws Exception {
        ProgramRequest pr = new ProgramRequest();
        pr.setProgram(selectedProgram);
        pr.setAction(ProgramAction.Delete);

        ProgramResponse response = (ProgramResponse) BmhUtils.sendRequest(pr);
        return response;
    }

    public List<Program> getProgramsWithTrigger(MessageType msgType)
            throws Exception {
        return this.getProgramsWithTrigger(null, msgType);
    }

    public List<Program> getProgramsWithTrigger(Suite suite, MessageType msgType)
            throws Exception {
        ProgramRequest pr = new ProgramRequest();
        pr.setAction(ProgramAction.GetProgramsWithTrigger);
        if (suite != null) {
            pr.setSuiteId(suite.getId());
        }
        pr.setMsgTypeId(msgType.getId());

        ProgramResponse response = (ProgramResponse) BmhUtils.sendRequest(pr);
        return response.getProgramList();
    }

    /**
     * Get a list of programs each with the associated suite information.
     * 
     * @return List of programs.
     * @throws Exception
     */
    public List<Program> getProgramSuites() throws Exception {
        ProgramRequest pr = new ProgramRequest();
        pr.setAction(ProgramAction.ProgramSuites);
        ProgramResponse progResponse = null;

        progResponse = (ProgramResponse) BmhUtils.sendRequest(pr);
        return progResponse.getProgramList();
    }

    /**
     * Get the fully populated {@link Program} that contains the
     * {@link TransmitterGroup} in its group list.
     * 
     * @param group
     * @return program
     * @throws Exception
     */
    public Program getProgramForTransmitterGroup(TransmitterGroup group)
            throws Exception {
        ProgramRequest request = new ProgramRequest();
        request.setTransmitterGroup(group);
        request.setAction(ProgramAction.GetProgramForTransmitterGroup);

        ProgramResponse response;
        response = (ProgramResponse) BmhUtils.sendRequest(request);
        List<Program> programs = response.getProgramList();
        if ((programs == null) || (programs.size() == 0)) {
            return null;
        }
        return programs.get(0);
    }
}
