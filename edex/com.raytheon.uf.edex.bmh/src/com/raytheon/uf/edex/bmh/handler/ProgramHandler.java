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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;

/**
 * BMH Program related request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 5, 2014   #3490     lvenable     Initial creation
 * Aug 12, 2014 #3490     lvenable     Refactored to make a query convenience method.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class ProgramHandler implements IRequestHandler<ProgramRequest> {

    @Override
    public Object handleRequest(ProgramRequest request) throws Exception {

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
        default:
            break;
        }

        return programResponse;
    }

    /**
     * Get a list of Program name and IDs.
     * 
     * @param programQuery
     *            Query string.
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
     * @param programQuery
     *            Query string.
     * @return List of Program and Suites.
     */
    private ProgramResponse getProgramSuites() {
        ProgramDao dao = new ProgramDao();
        ProgramResponse response = new ProgramResponse();

        List<Program> programList = dao.getProgramSuites();
        response.setProgramList(programList);

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
}
