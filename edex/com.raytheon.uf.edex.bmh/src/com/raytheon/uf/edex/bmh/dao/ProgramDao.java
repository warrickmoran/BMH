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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
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
 * Aug 06, 2014 #3490     lvenable    Updated to get Program information.
 * Aug 12, 2014 #3490     lvenable    Refactored to make a getProgramByQuery() method that
 *                                    will used the query passed it to retrieve the data.
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
            String programName = (String) objArray[0];
            String suiteName = (String) objArray[1];
            SuiteType suiteType = (SuiteType) objArray[2];
            int suiteID = (Integer) objArray[3];

            program = existingProgram.get(programName);

            if (program == null) {
                program = new Program();
                program.setName(programName);
                existingProgram.put(programName, program);
            }

            Suite suite = new Suite();
            suite.setName(suiteName);
            suite.setType(suiteType);
            suite.setId(suiteID);

            program.addSuite(suite);
        }

        List<Program> programList = new ArrayList<Program>(
                existingProgram.values());

        return programList;
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
}
