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

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.AbstractTraceableSystemConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
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
 * Nov 20, 2014  3698     rferrel     Implement SuitePrograms and SuiteEnabledGroups.
 * Dec 01, 2014  3838     rferrel     Added getProgramGeneralSuite.
 * Dec 07, 2014  3846     mpduff      Added getProgramById
 * Jan 07, 2015  3958     bkowal      Added {@link #getTransmittersForMsgType(ProgramRequest)}.
 * Mar 13, 2015  4213     bkowal      Added {@link #getStaticMessageTypesForProgram(ProgramRequest)}.
 * Apr 22, 2015  4397     bkowal      Construct a {@link AbstractTraceableSystemConfigNotification}
 *                                    notification when database changes occur.
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
                    ConfigChangeType.Delete, request, request.getProgram());
            break;
        case Save:
            programResponse = saveProgram(request);
            notification = new ProgramConfigNotification(
                    ConfigChangeType.Update, request, request.getProgram());
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
                    ConfigChangeType.Update, request, request.getProgram());
            break;
        case SuitePrograms:
            programResponse = getSuitePrograms(request);
            break;
        case SuiteEnabledGroups:
            return getSuiteEnabledGroups(request);
        case ProgramGeneralSuite:
            return getProgramGeneralSuite(request);
        case GetProgramById:
            return getProgramById(request);
        case GetTransmittersForMsgType:
            return this.getTransmittersForMsgType(request);
        case GetStaticMsgTypesForProgram:
            return this.getStaticMessageTypesForProgram(request);
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

    private ProgramResponse getSuitePrograms(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();
        List<Program> programList = dao.getSuitePrograms(request.getSuiteId());
        response.setProgramList(programList);
        return response;
    }

    private List<TransmitterGroup> getSuiteEnabledGroups(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        List<TransmitterGroup> enabledGroups = dao
                .getSuiteEnabledGroups(request.getSuiteId());
        return enabledGroups;
    }

    private Suite getProgramGeneralSuite(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        Suite suite = dao.getProgramGeneralSuite(request.getProgram().getId());
        return suite;
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
        Program program = request.getProgram();
        if (program != null) {
            dao.delete(program);

            IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
            if (logger.isPriorityEnabled(Priority.INFO)) {
                String user = BMHLoggerUtils.getUser(request);
                BMHLoggerUtils.logDelete(request, user, program);
            }
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
        Program program = request.getProgram();
        if (program != null) {
            IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
            Program oldProgram = null;
            if (logger.isPriorityEnabled(Priority.INFO)) {
                oldProgram = dao.getByID(program.getId());
            }
            dao.persist(request.getProgram());
            response.addProgram(request.getProgram());
            if (logger.isPriorityEnabled(Priority.INFO)) {
                String user = BMHLoggerUtils.getUser(request);
                BMHLoggerUtils.logSave(request, user, oldProgram, program);
            }
        }

        return response;
    }

    private ProgramResponse addGroup(ProgramRequest request) {
        ProgramDao dao = new ProgramDao();
        int programId = request.getProgram().getId();
        TransmitterGroup group = request.getTransmitterGroup();
        ProgramResponse response = new ProgramResponse();
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);

        Program oldProgram = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldProgram = dao.getByID(programId);
        }
        Program program = dao.addGroup(programId, group);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logSave(request, user, oldProgram, program);
        }
        response.addProgram(program);

        return response;
    }

    /**
     * Get a Program by id.
     * 
     * @return ProgramResponse with a list containing the one Program
     */
    private ProgramResponse getProgramById(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();
        Program program = dao.getByID(request.getProgramId());

        response.addProgram(program);

        return response;
    }

    /**
     * Uses the {@link ProgramDao} to retrieve all {@link Transmitter}s
     * associated with the specified {@link MessageType}.
     * 
     * @param request
     *            Identifies the specified {@link MessageType}.
     * @return a {@link ProgramResponse} with a {@link List} of the
     *         {@link Transmitter}s that were found.
     */
    private ProgramResponse getTransmittersForMsgType(ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();
        response.setTransmitters(dao.getTransmittersForMsgType(request
                .getMessageType()));

        return response;
    }

    private ProgramResponse getStaticMessageTypesForProgram(
            ProgramRequest request) {
        ProgramDao dao = new ProgramDao(request.isOperational());
        ProgramResponse response = new ProgramResponse();
        response.setMessageTypes(dao.getStaticMsgTypesForProgram(request
                .getProgramId()));

        return response;
    }
}