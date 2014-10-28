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
package com.raytheon.uf.edex.bmh.handler;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;

/**
 * Handles any requests to get or modify the state of {@link Program}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 05, 2014  3490     lvenable    Initial creation
 * Aug 12, 2014  3490     lvenable    Refactored to make a query convenience method.
 * Aug 15, 2014  3432     mpduff      Added getProgramForTransmitterGroup
 * Aug 17, 2014  3490     lvenable    Added save and delete.
 * Sep 05, 2014  3554     bsteffen    Send config change notification.
 * Sep 18, 2014  3587     bkowal      Added getProgramsWithTrigger
 * Oct 02, 2014  3649     rferrel     Added addGroup.
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3654     rjpeter     Updated to use ProgramSummary.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class ProgramHandler extends
        AbstractBMHServerRequestHandler<ProgramRequest> {

    @Override
    public Object handleRequest(ProgramRequest request) throws Exception {
        ProgramConfigNotification notification = null;
        ProgramResponse programResponse = new ProgramResponse();

        switch (request.getAction()) {
        case AllProgramSummaries:
            programResponse = getProgramSummaries(request);
            break;
        case ProgramSuites:
            programResponse = getProgramSuites(request);
            break;
        case AllPrograms:
            programResponse = getPrograms(request);
            break;
        case Delete:
            deleteProgram(request);
            notification = new ProgramConfigNotification(
                    ConfigChangeType.Delete, request.getProgram());
            break;
        case Save:
            programResponse = saveProgram(request);
            notification = new ProgramConfigNotification(
                    ConfigChangeType.Update, request.getProgram());
            break;
        case GetProgramForTransmitterGroup:
            programResponse = getProgramForTransmitterGroup(request);
            break;
        case GetProgramsWithTrigger:
            programResponse = getProgramsWithTrigger(request);
            break;
        case AddGroup:
            programResponse = addGroup(request);
            notification = new ProgramConfigNotification(
                    ConfigChangeType.Update, request.getProgram());
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        BmhMessageProducer.sendConfigMessage(notification,
                request.isOperational());

        return programResponse;
    }

    /**
     * Get a list of Program name and IDs.
     * 
     * @return List of Program name and IDs.
     */
    private ProgramResponse getProgramSummaries(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();

        List<ProgramSummary> programList = dao.getProgramSummaries();
        response.setProgramSummaryList(programList);

        return response;
    }

    /**
     * Get a list of Program and Suite list.
     * 
     * @return List of Program and Suites.
     */
    private ProgramResponse getProgramSuites(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();

        List<Program> programList = dao.getProgramSuites();
        response.setProgramList(programList);

        return response;
    }

    private ProgramResponse getProgramForTransmitterGroup(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();

        Program program = dao.getProgramForTransmitterGroup(request
                .getTransmitterGroup());
        List<Program> pList = new ArrayList<>(1);
        pList.add(program);
        response.setProgramList(pList);
        return response;
    }

    private ProgramResponse getProgramsWithTrigger(ProgramRequest request) {
        if (request.getMsgTypeId() <= 0) {
            throw new IllegalArgumentException(
                    "The Message Type Id is required!");
        }

        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();

        List<Program> triggeredPrograms = null;
        if (request.getSuiteId() > 0) {
            triggeredPrograms = dao.getProgramsWithTrigger(
                    request.getSuiteId(), request.getMsgTypeId());
        } else {
            triggeredPrograms = dao.getProgramsWithTrigger(request
                    .getMsgTypeId());
        }
        response.setProgramList(triggeredPrograms);
        return response;
    }

    /**
     * Get a list of programs that are fully populated.
     * 
     * @return List of fully populated programs.
     */
    private ProgramResponse getPrograms(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();

        List<Program> progList = dao.getAll();
        response.setProgramList(progList);

        return response;
    }

    /**
     * Delete the specified program.
     * 
     * @param request
     *            Program request.
     */
    private void deleteProgram(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        if (request.getProgram() != null) {
            dao.delete(request.getProgram());
        }
    }

    /**
     * Save the specified program.
     * 
     * @param request
     *            Program request.
     */
    private ProgramResponse saveProgram(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();
        if (request.getProgram() != null) {
            dao.persist(request.getProgram());
            response.addProgram(request.getProgram());
        }

        return response;
    }

    private ProgramResponse addGroup(ProgramRequest request) {
        ProgramDao dao = new ProgramDao();
        int programId = request.getProgram().getId();
        TransmitterGroup group = request.getTransmitterGroup();
        ProgramResponse response = new ProgramResponse();
        Program program = dao.addGroup(programId, group);
        response.addProgram(program);

        return response;
    }
}
