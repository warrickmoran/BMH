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

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
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
     * Get a list of Program objects that have the program name and IDs
     * populated.
     * 
     * @return A list of Program objects.
     */
    public List<Program> getProgramNameIds() {

        List<Program> programList;

        List<Object[]> namesIDs = txTemplate
                .execute(new TransactionCallback<List<Object[]>>() {
                    @Override
                    public List<Object[]> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht
                                .findByNamedQuery(Program.GET_PROGRAM_NAMES_IDS);
                    }
                });

        if (namesIDs == null) {
            programList = Collections.emptyList();
        } else {
            programList = new ArrayList<Program>(namesIDs.size());
            for (Object[] objArray : namesIDs) {
                Program p = new Program();
                p.setName((String) objArray[0]);
                p.setId((Integer) objArray[1]);
                programList.add(p);
            }
        }

        return programList;
    }
}
