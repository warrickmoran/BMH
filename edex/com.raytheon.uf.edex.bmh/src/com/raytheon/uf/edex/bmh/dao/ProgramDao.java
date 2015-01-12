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
package com.raytheon.uf.edex.bmh.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * 
 * DAO for {@link Program} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 25, 2014  3283     bsteffen    Initial creation
 * Jul 17, 2014  3175     rjpeter     Added get methods.
 * Aug 06, 2014  3490     lvenable    Updated to get Program information.
 * Aug 12, 2014  3490     lvenable    Refactored to make a getProgramByQuery() method that
 *                                    will used the query passed it to retrieve the data.
 * Sep 16, 2014  3587     bkowal      Overrode persistAll. Added getProgramTriggersForSuite.
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Oct 02, 2014  3649     rferrel     Added addGroup method.
 * Oct 08, 2014  3687     bsteffen    Remove ProgramTrigger.
 * Oct 13, 2014  3654     rjpeter     Updated to use ProgramSummary.
 * Nov 20, 2014  3698     rferrel     Added methods getSuitePrograms and getSuiteEnabledGroups.
 * Dec 02, 2014  3838     rferrel     Added getProgramGeneralSuite.
 * Jan 07, 2015  3958     bkowal      Added {@link #getTransmittersForMsgType(MessageType)}.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ProgramDao extends AbstractBMHDao<Program, Integer> {

    public ProgramDao() {
        super(Program.class);
    }

    public ProgramDao(boolean operational) {
        super(operational, Program.class);
    }

    /**
     * Gets all transmitter groups that have a program that links to a given
     * message type.
     * 
     * @param msgType
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TransmitterGroup> getGroupsForMsgType(final String msgType) {
        return (List<TransmitterGroup>) findByNamedQueryAndNamedParam(
                Program.GET_GROUPS_FOR_MSG_TYPE, "afosid", msgType);
    }

    /**
     * Retrieves the {@link Transmitter}s that are associated with the specified
     * {@link MessageType} based on {@link Program} and {@link Suite}
     * associations.
     * 
     * @param msgType
     *            the specified {@link MessageType}
     * @return the retrieved {@link List} of {@link Transmitter}s.
     */
    public List<Transmitter> getTransmittersForMsgType(final MessageType msgType) {
        List<?> results = this.findByNamedQueryAndNamedParam(
                Program.GET_TRANSMITTERS_FOR_MSG_TYPE, "afosid",
                msgType.getAfosid());
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Transmitter> transmitters = new HashSet<>(results.size(), 1.0f);
        for (Object object : results) {
            transmitters.add((Transmitter) object);
        }

        return new ArrayList<Transmitter>(transmitters);
    }

    /**
     * Returns the program for a given message group.
     * 
     * @param transmitterGroup
     * @return
     */
    public Program getProgramForTransmitterGroup(
            final TransmitterGroup transmitterGroup) {
        @SuppressWarnings("unchecked")
        List<Program> programs = (List<Program>) findByNamedQueryAndNamedParam(
                Program.GET_PROGRAM_FOR_TRANSMITTER_GROUP, "group",
                transmitterGroup);

        if ((programs != null) && (programs.size() > 0)) {
            // should only be one entry
            return programs.get(0);
        }

        return null;
    }

    public ProgramSuite getSuiteByIDForTransmitterGroup(
            final TransmitterGroup transmitterGroup, final int suiteId) {
        List<?> results = findByNamedQueryAndNamedParam(
                Program.GET_SUITE_BY_ID_FOR_TRANSMITTER_GROUP, new String[] {
                        "group", "suiteId" }, new Object[] { transmitterGroup,
                        suiteId });

        if ((results == null) || results.isEmpty()) {
            return null;
        }

        if (results.get(0) instanceof ProgramSuite) {
            return (ProgramSuite) results.get(0);
        }

        return null;
    }

    /**
     * Get a list of programs populated with program name and a list of suites
     * that contain suite name, type, and ID.
     * 
     * @return A list of programs.
     */
    public List<Program> getProgramSuites() {
        List<Object[]> objectList = getProgramByQuery(Program.GET_PROGRAM_SUITES);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<Program> programList = createProgramSuites(objectList);

        return programList;
    }

    /**
     * Get the programs that use the suite.
     * 
     * @param suiteId
     * @return suitePrograms
     */
    public List<Program> getSuitePrograms(int suiteId) {
        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) findByNamedQueryAndNamedParam(
                Program.GET_SUITE_PROGRAMS, "suiteId", suiteId);
        if ((values == null) || values.isEmpty()) {
            return Collections.emptyList();
        }
        return createProgramList(values);
    }

    /**
     * Get transmitter groups whose programs use the suite and the groups
     * contain ENABLED transmitters.
     * 
     * @param suiteId
     * @return enabledGroups
     */
    public List<TransmitterGroup> getSuiteEnabledGroups(int suiteId) {
        @SuppressWarnings("unchecked")
        List<TransmitterGroup> groups = (List<TransmitterGroup>) findByNamedQueryAndNamedParam(
                Program.GET_SUITE_ENABLED_TRANSMITTER_GROUPS, "suiteId",
                suiteId);
        if ((groups == null) || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups;
    }

    /**
     * Get list with the program's suite that is of type GENERAL. List should
     * only have 0 or 1 entry.
     * 
     * @param programId
     * @return suite
     */
    public Suite getProgramGeneralSuite(int programId) {
        @SuppressWarnings("unchecked")
        List<Suite> suites = (List<Suite>) findByNamedQueryAndNamedParam(
                Program.GET_PROGRAM_GENERAL_SUITE, "programId", programId);
        if ((suites == null) || suites.isEmpty()) {
            return null;
        }
        return suites.get(0);

    }

    /**
     * Get a list of program summaries.
     * 
     * @return A list of program summaries.
     */
    @SuppressWarnings("unchecked")
    public List<ProgramSummary> getProgramSummaries() {
        return (List<ProgramSummary>) loadAll(ProgramSummary.class);
    }

    /**
     * Get a list of objects associated with the query passed in.
     * 
     * @return A list of objects.
     */
    @SuppressWarnings("unchecked")
    private List<Object[]> getProgramByQuery(final String programQuery) {
        return (List<Object[]>) findByNamedQuery(programQuery);
    }

    /**
     * Get a list of programs populated with program name and a list of suites
     * that contain suite name, type, and ID.
     * 
     * @param objectList
     *            Object list.
     * @return A list of programs.
     */
    private List<Program> createProgramSuites(List<Object[]> objectList) {

        Map<String, Program> existingProgram = new TreeMap<String, Program>();
        Program program = null;

        for (Object[] objArray : objectList) {
            int programID = (Integer) objArray[0];
            String programName = (String) objArray[1];
            String suiteName = (String) objArray[2];
            SuiteType suiteType = (SuiteType) objArray[3];
            int suiteID = (Integer) objArray[4];

            program = existingProgram.get(programName);

            if (program == null) {
                program = new Program();
                program.setId(programID);
                program.setName(programName);
                existingProgram.put(programName, program);
            }

            Suite suite = new Suite();
            suite.setName(suiteName);
            suite.setType(suiteType);
            suite.setId(suiteID);
            ProgramSuite programSuite = new ProgramSuite();
            programSuite.setSuite(suite);

            program.addProgramSuite(programSuite);
        }

        List<Program> programList = new ArrayList<Program>(
                existingProgram.values());

        return programList;
    }

    public List<Program> getProgramsWithTrigger(final int suiteId,
            final int msgTypeId) {
        return this.getProgramsWithTrigger(
                Program.GET_PROGRAMS_WITH_TRIGGER_BY_SUITE_AND_MSGTYPE,
                new String[] { "suiteId", "msgTypeId" }, new Object[] {
                        suiteId, msgTypeId });
    }

    public List<Program> getProgramsWithTrigger(final int msgTypeId) {
        return this.getProgramsWithTrigger(
                Program.GET_PROGRAMS_WITH_TRIGGER_BY_MSG_TYPE,
                new String[] { "msgTypeId" }, new Object[] { msgTypeId });
    }

    public List<Program> getProgramsWithTrigger(final String namedQuery,
            final String[] queryParams, final Object[] paramValues) {
        List<?> programs = findByNamedQueryAndNamedParam(namedQuery,
                queryParams, paramValues);
        return createProgramList(programs);
    }

    /**
     * Convert a list of Object arrays that contain a program's id and name.
     * 
     * @param values
     * @return programs
     */
    private List<Program> createProgramList(List<?> values) {

        if ((values == null) || values.isEmpty()) {
            return null;
        }

        List<Program> programs = new ArrayList<>(values.size());
        for (Object object : values) {
            if ((object instanceof Object[]) == false) {
                continue;
            }

            Object[] objects = (Object[]) object;
            if (objects.length != 2) {
                continue;
            }

            if (((objects[0] instanceof Integer) == false)
                    || ((objects[1] instanceof String) == false)) {
                continue;
            }

            Program program = new Program();
            program.setId((Integer) objects[0]);
            program.setName((String) objects[1]);
            programs.add(program);
        }

        return programs;
    }

    @Override
    public void persistAll(final Collection<?> objs) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                for (Object obj : objs) {
                    if (obj instanceof Program) {
                        persist((Program) obj);
                    } else {
                        persist(obj);
                    }
                }
            }
        });
    }

    public void persist(final Program program) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final int programId = program.getId();
                Session session = getCurrentSession();
                // work around to orphanRemoval not working correctly in
                // bidirectional relationship
                if (programId != 0) {
                    Query query = session
                            .createQuery("DELETE ProgramSuite WHERE program_id = ?");
                    query.setParameter(0, programId);
                    query.executeUpdate();
                    session.update(program);
                } else {
                    session.save(program);
                }
            }
        });
    }

    public Program addGroup(final int programId, final TransmitterGroup group) {
        Program xProgram = txTemplate
                .execute(new TransactionCallback<Program>() {

                    @Override
                    public Program doInTransaction(TransactionStatus status) {
                        Program program = getByID(programId);
                        program.getTransmitterGroups().add(group);
                        persist(program);
                        return program;
                    }
                });
        return xProgram;
    }
}