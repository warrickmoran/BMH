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

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramTrigger;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
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
 * 
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
    public List<TransmitterGroup> getGroupsForMsgType(final String msgType) {
        List<TransmitterGroup> groups = txTemplate
                .execute(new TransactionCallback<List<TransmitterGroup>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<TransmitterGroup> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                Program.GET_GROUPS_FOR_MSG_TYPE,
                                new String[] { "afosid" },
                                new Object[] { msgType });
                    }
                });
        return groups;
    }

    /**
     * Returns the program for a given message group.
     * 
     * @param transmitterGroup
     * @return
     */
    public Program getProgramForTransmitterGroup(
            final TransmitterGroup transmitterGroup) {
        List<Program> programs = txTemplate
                .execute(new TransactionCallback<List<Program>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<Program> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                Program.GET_PROGRAM_FOR_TRANSMITTER_GROUP,
                                new String[] { "group" },
                                new Object[] { transmitterGroup });
                    }
                });

        if ((programs != null) && (programs.size() > 0)) {
            // should only be one entry
            return programs.get(0);
        }

        return null;
    }

    public ProgramSuite getSuiteByIDForTransmitterGroup(
            final TransmitterGroup transmitterGroup, final int suiteId) {
        List<?> results = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                Program.GET_SUITE_BY_ID_FOR_TRANSMITTER_GROUP,
                                new String[] { "group", "suiteId" },
                                new Object[] { transmitterGroup, suiteId });
                    }
                });

        if (results == null || results.isEmpty()) {
            return null;
        }

        if (results.get(0) instanceof ProgramSuite) {
            return (ProgramSuite) results.get(0);
        }

        return null;
    }

    /**
     * Get all of the programs and associated data.
     * 
     * @return List of programs.
     */
    public List<Program> getPrograms() {
        List<Object> allObjects = this.loadAll();
        if (allObjects == null) {
            return Collections.emptyList();
        }

        List<Program> programList = new ArrayList<Program>(allObjects.size());
        for (Object obj : allObjects) {
            Program prog = (Program) obj;
            programList.add(prog);
        }

        return programList;
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
     * Get a list of programs populated with program name and IDs.
     * 
     * @return A list of programs populated with program name and IDs.
     */
    public List<Program> getProgramNameIDs() {
        List<Object[]> objectList = getProgramByQuery(Program.GET_PROGRAM_NAMES_IDS);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<Program> programList = createProgramNameIDs(objectList);

        return programList;
    }

    /**
     * Get a list of objects associated with the query passed in.
     * 
     * @return A list of objects.
     */
    private List<Object[]> getProgramByQuery(final String programQuery) {

        List<Object[]> objectList = txTemplate
                .execute(new TransactionCallback<List<Object[]>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<Object[]> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQuery(programQuery);
                    }
                });

        return objectList;
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

    public Set<ProgramTrigger> getProgramTriggersForSuite(final int programId,
            final int suiteId) {
        List<?> triggers = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                Program.GET_TRIGGERS_FOR_PROGRAM_AND_SUITE,
                                new String[] { "programId", "suiteId" },
                                new Object[] { programId, suiteId });
                    }
                });

        if (triggers == null || triggers.isEmpty()) {
            return null;
        }

        Set<ProgramTrigger> programTriggers = new HashSet<>(triggers.size());
        for (Object trigger : triggers) {
            if (trigger instanceof ProgramTrigger) {
                programTriggers.add((ProgramTrigger) trigger);
            }
        }

        return programTriggers;
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
        List<?> programs = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(namedQuery,
                                queryParams, paramValues);
                    }
                });

        if (programs == null || programs.isEmpty()) {
            return null;
        }

        List<Program> triggeredPrograms = new ArrayList<>(programs.size());
        for (Object object : programs) {
            if (object instanceof Object[] == false) {
                continue;
            }

            Object[] objects = (Object[]) object;
            if (objects.length != 2) {
                continue;
            }

            if (objects[0] instanceof Integer == false
                    || objects[1] instanceof String == false) {
                continue;
            }

            Program program = new Program();
            program.setId((Integer) objects[0]);
            program.setName((String) objects[1]);
            triggeredPrograms.add(program);
        }

        return triggeredPrograms;
    }

    /**
     * Get a list of programs populated with program name and IDs.
     * 
     * @param objectList
     *            Object list.
     * @return A list of programs populated with program name and IDs.
     */
    private List<Program> createProgramNameIDs(List<Object[]> objectList) {
        List<Program> programList = new ArrayList<Program>(objectList.size());
        for (Object[] objArray : objectList) {
            Program p = new Program();
            p.setName((String) objArray[0]);
            p.setId((Integer) objArray[1]);
            programList.add(p);
        }

        return programList;
    }

    @Override
    public void persistAll(final Collection<? extends Object> objs) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                for (Object obj : objs) {
                    if (obj instanceof Program) {
                        saveOrUpdate((Program) obj);
                    } else {
                        ht.saveOrUpdate(obj);
                    }
                }
            }
        });
    }

    public void saveOrUpdate(final Program program) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                saveOrUpdate(ht, program);
            }
        });
    }

    private void saveOrUpdate(HibernateTemplate ht, final Program program) {
        final int programId = program.getId();
        // work around to orphanRemoval not working correctly in
        // bidirectional relationship
        if (programId != 0) {
            ht.bulkUpdate("DELETE ProgramSuite WHERE program_id = ?", programId);
            ht.bulkUpdate("DELETE ProgramTrigger WHERE program_id = ?",
                    programId);
            ht.update(program);
        } else {
            ht.save(program);
        }

        if (program.getProgramSuites() != null) {
            for (ProgramSuite programSuite : program.getProgramSuites()) {
                ht.save(programSuite);
                if (programSuite.getTriggers() != null) {
                    for (ProgramTrigger trigger : programSuite.getTriggers()) {
                        if (trigger.getProgram() == null) {
                            trigger.setProgram(program);
                        }
                        ht.save(trigger);
                    }
                }
            }
        }
    }
}