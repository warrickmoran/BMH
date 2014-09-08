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
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.ProgramConfigNotification;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * BMH Program related request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 05, 2014   #3490    lvenable    Initial creation
 * Aug 12, 2014   #3490    lvenable    Refactored to make a query convenience method.
 * Aug 15, 2014    3432    mpduff      Added getProgramForTransmitterGroup
 * Aug 17, 2014 #3490      lvenable     Added save and delete.
 * Sep 05, 2014 3554       bsteffen    Send config change notification.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class ProgramHandler implements IRequestHandler<ProgramRequest> {

    @Override
    public Object handleRequest(ProgramRequest request) throws Exception {
        ProgramConfigNotification notification = null;
        ProgramResponse programResponse = new ProgramResponse();

        switch (request.getAction()) {
        case ListNamesIDs:
            programResponse = getProgramNameIDs();
            break;
        case ProgramSuites:
            programResponse = getProgramSuites();
            break;
        case AllPrograms:
            programResponse = getPrograms();
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
        default:
            break;
        }

        if (notification != null) {
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-durable:topic:BMH.Config",
                    SerializationUtil.transformToThrift(notification));
        }

        return programResponse;
    }

    /**
     * Get a list of Program name and IDs.
     * 
     * @return List of Program name and IDs.
     */
    private ProgramResponse getProgramNameIDs() {
        ProgramDao dao = new ProgramDao();
        ProgramResponse response = new ProgramResponse();

        List<Program> programList = dao.getProgramNameIDs();
        response.setProgramList(programList);

        return response;
    }

    /**
     * Get a list of Program and Suite list.
     * 
     * @return List of Program and Suites.
     */
    private ProgramResponse getProgramSuites() {
        ProgramDao dao = new ProgramDao();
        ProgramResponse response = new ProgramResponse();

        List<Program> programList = dao.getProgramSuites();
        response.setProgramList(programList);

        return response;
    }

    private ProgramResponse getProgramForTransmitterGroup(ProgramRequest req) {
        ProgramDao dao = new ProgramDao();
        ProgramResponse response = new ProgramResponse();

        Program program = dao.getProgramForTransmitterGroup(req
                .getTransmitterGroup());
        List<Program> pList = new ArrayList<>();
        pList.add(program);
        response.setProgramList(pList);
        return response;
    }

    /**
     * Get a list of programs that are fully populated.
     * 
     * @return List of fully populated programs.
     */
    private ProgramResponse getPrograms() {
        ProgramDao dao = new ProgramDao();
        ProgramResponse response = new ProgramResponse();

        List<Program> progList = dao.getPrograms();
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
        ProgramDao dao = new ProgramDao();
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
        ProgramDao dao = new ProgramDao();
        ProgramResponse response = new ProgramResponse();
        if (request.getProgram() != null) {
            dao.saveOrUpdate(request.getProgram());
            response.addProgram(request.getProgram());
        }

        return response;
    }
}
