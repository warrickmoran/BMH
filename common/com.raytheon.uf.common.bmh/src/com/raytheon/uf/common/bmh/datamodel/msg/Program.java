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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Program object. Has little to no data to itself, merely a collection of
 * Suites.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jul 10, 2014  3283     bsteffen    Eagerly fetch suites.
 * Jul 17, 2014  3175     rjpeter     Added surrogate key.
 * Aug 06, 2014 #3490     lvenable    Updated to add name/query.
 * Aug 12, 2014 #3490     lvenable    Updated to add name/query for getting Programs/Suites.
 * Aug 15, 2014 #3490     lvenable    Removed cascade type all.
 * Sep 16, 2014 #3587     bkowal      Updated to use the new {@link ProgramSuite}. Created
 *                                    named queries for retrieving programs associated with a
 *                                    trigger.
 * Oct 01, 2014 #3589     dgilling    Add getProgramSuite().
 * Oct 08, 2014 #3687     bsteffen    Remove ProgramTrigger.
 * Oct 13, 2014  3654     rjpeter     Updated to use MessageTypeSummary and ProgramSummary.
 * Oct 15, 2014  3715     bkowal      Support adding / editing program triggers for
 *                                    completely new {@link Suite}(s).
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Program.GET_PROGRAM_FOR_TRANSMITTER_GROUP, query = Program.GET_PROGRAMS_FOR_TRANSMITTER_GROUP_QUERY),
        @NamedQuery(name = Program.GET_GROUPS_FOR_MSG_TYPE, query = Program.GET_GROUPS_FOR_MSG_TYPE_QUERY),
        @NamedQuery(name = Program.GET_PROGRAM_NAMES_IDS, query = Program.GET_PROGRAM_NAMES_IDS_QUERY),
        @NamedQuery(name = Program.GET_PROGRAM_SUITES, query = Program.GET_PROGRAM_SUITES_QUERY),
        @NamedQuery(name = Program.GET_SUITE_BY_ID_FOR_TRANSMITTER_GROUP, query = Program.GET_SUITE_BY_ID_FOR_TRANSMITTER_GROUP_QUERY),
        @NamedQuery(name = Program.GET_PROGRAMS_WITH_TRIGGER_BY_SUITE_AND_MSGTYPE, query = Program.GET_PROGRAMS_WITH_TRIGGER_BY_SUITE_AND_MSGTYPE_QUERY),
        @NamedQuery(name = Program.GET_PROGRAMS_WITH_TRIGGER_BY_MSG_TYPE, query = Program.GET_PROGRAMS_WITH_TRIGGER_BY_MSG_TYPE_QUERY) })
@Entity
@Table(name = "program", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = Program.GEN, sequenceName = "program_seq")
@DynamicSerialize
public class Program {
    static final String GEN = "Program Id Generator";

    public static final String GET_PROGRAM_NAMES_IDS = "getProgramNamesAndIDs";

    protected static final String GET_PROGRAM_NAMES_IDS_QUERY = "select name, id FROM Program p";

    public static final String GET_PROGRAM_FOR_TRANSMITTER_GROUP = "getProgramsForTransmitterGroups";

    protected static final String GET_PROGRAMS_FOR_TRANSMITTER_GROUP_QUERY = "select p FROM Program p inner join p.transmitterGroups tg WHERE tg = :group";

    public static final String GET_SUITE_BY_ID_FOR_TRANSMITTER_GROUP = "getSuiteByIDForTransmitterGroup";

    protected static final String GET_SUITE_BY_ID_FOR_TRANSMITTER_GROUP_QUERY = "select ps FROM Program p inner join p.programSuites ps inner join p.transmitterGroups tg WHERE tg = :group AND ps.id.suiteId = :suiteId";

    public static final String GET_GROUPS_FOR_MSG_TYPE = "getGroupsForMsgType";

    protected static final String GET_GROUPS_FOR_MSG_TYPE_QUERY = "SELECT tg FROM Program p inner join p.transmitterGroups tg inner join p.programSuites ps inner join ps.suite s inner join s.suiteMessages sm inner join sm.msgTypeSummary mt WHERE mt.afosid = :afosid";

    public static final String GET_PROGRAM_SUITES = "getProgramsAndSuites";

    protected static final String GET_PROGRAM_SUITES_QUERY = "SELECT p.id, p.name, s.name, s.type, s.id FROM Program p inner join p.programSuites ps inner join ps.suite s";

    public static final String GET_PROGRAMS_WITH_TRIGGER_BY_SUITE_AND_MSGTYPE = "getProgramsWithTriggerBySuiteAndMsgType";

    protected static final String GET_PROGRAMS_WITH_TRIGGER_BY_SUITE_AND_MSGTYPE_QUERY = "SELECT p.id, p.name FROM Program p INNER JOIN p.programSuites ps INNER JOIN ps.triggers trig WHERE ps.id.suiteId = :suiteId AND trig.id = :msgTypeId";

    public static final String GET_PROGRAMS_WITH_TRIGGER_BY_MSG_TYPE = "getProgramsWithTriggerByMsgType";

    protected static final String GET_PROGRAMS_WITH_TRIGGER_BY_MSG_TYPE_QUERY = "SELECT p.id, p.name FROM Program p INNER JOIN p.programSuites ps INNER JOIN ps.triggers trig WHERE trig.id = :msgTypeId";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    @Column(length = 20, unique = true, nullable = false)
    @DynamicSerializeElement
    private String name;

    @OneToMany(mappedBy = "program", fetch = FetchType.EAGER)
    @OrderColumn(name = "position", nullable = false)
    @DynamicSerializeElement
    private List<ProgramSuite> programSuites;

    /*
     * Convenience mapping for working with triggers.
     */
    @Transient
    private Map<Suite, ProgramSuite> suiteToProgramSuiteMap;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id")
    @DynamicSerializeElement
    private Set<TransmitterGroup> transmitterGroups;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void checkSuiteLookupMap(final Suite suite) {
        if (this.suiteToProgramSuiteMap == null) {
            this.suiteToProgramSuiteMap = new HashMap<>();
        }

        /*
         * if it is a new suite, add it to the map. This map is transient so
         * differences between it and the actual data will have no impact.
         */
        if (suite.getId() <= 0) {
            if (this.suiteToProgramSuiteMap.containsKey(suite) == false) {
                ProgramSuite newProgramSuite = new ProgramSuite();
                newProgramSuite.setProgram(this);
                newProgramSuite.setSuite(suite);
                this.suiteToProgramSuiteMap.put(suite, newProgramSuite);
            }
            return;
        }

        /*
         * Look for changes that were made directly to the program suites list
         * via pass-by-reference.
         */
        if ((this.suiteToProgramSuiteMap.size() != this.programSuites.size())
                || (this.suiteToProgramSuiteMap.containsKey(suite) == false)) {
            this.suiteToProgramSuiteMap.clear();
            for (ProgramSuite pg : this.programSuites) {
                this.suiteToProgramSuiteMap.put(pg.getSuite(), pg);
            }

            /*
             * Now the lookup map should contain the specified suite.
             */
            if (this.suiteToProgramSuiteMap.containsKey(suite) == false) {
                throw new IllegalStateException(
                        "Unable to find any references to suite: "
                                + suite.getId()
                                + "! Too many changes have been made to the internal data structures by reference.");
            }
        }
    }

    public List<Suite> getSuites() {
        if (this.programSuites == null) {
            return Collections.emptyList();
        }

        List<Suite> suites = new ArrayList<>(this.programSuites.size());
        for (ProgramSuite programSuite : this.programSuites) {
            suites.add(programSuite.getSuite());
        }

        return suites;
    }

    public void cancelNewSuite(Suite suite) {
        if (this.suiteToProgramSuiteMap == null) {
            return;
        }
        this.suiteToProgramSuiteMap.remove(suite);
    }

    /*
     * BEGIN: Convenience methods for interacting with triggers
     */
    public boolean isTriggerMsgType(Suite suite, MessageTypeSummary messageType) {
        this.checkSuiteLookupMap(suite);
        if (suite.getType() == SuiteType.GENERAL) {
            /*
             * General suites will never have triggers.
             */
            return false;
        }

        return this.suiteToProgramSuiteMap.get(suite).isTrigger(messageType);
    }

    public boolean doTriggersExist(Suite suite) {
        this.checkSuiteLookupMap(suite);
        if (suite.getType() == SuiteType.GENERAL) {
            /*
             * General suites will never have triggers.
             */
            return false;
        }

        return this.suiteToProgramSuiteMap.get(suite).triggersExist();
    }

    public void clearTriggerMsgTypes(Suite suite) {
        this.checkSuiteLookupMap(suite);
        this.suiteToProgramSuiteMap.get(suite).clearTriggers();
    }

    public void addTriggerMsgType(Suite suite, MessageTypeSummary messageType) {
        this.checkSuiteLookupMap(suite);
        this.suiteToProgramSuiteMap.get(suite).addTrigger(messageType);
    }

    public void removeTriggerMsgType(Suite suite, MessageTypeSummary msgType) {
        this.checkSuiteLookupMap(suite);
        if (this.isTriggerMsgType(suite, msgType) == false) {
            return;
        }
        this.suiteToProgramSuiteMap.get(suite).removeTrigger(msgType);
    }

    /*
     * END: Convenience methods for interacting with triggers
     */

    public void setSuites(List<Suite> suites) {
        if (programSuites == null) {
            programSuites = new ArrayList<>(suites.size());
        }
        for (Suite suite : suites) {
            ProgramSuite programSuite = new ProgramSuite();
            programSuite.setProgram(this);
            programSuite.setSuite(suite);
            programSuite.setPosition(this.programSuites.size());
            this.programSuites.add(programSuite);
        }
    }

    public void addSuite(Suite suite) {
        if (suite != null) {
            if (programSuites == null) {
                programSuites = new ArrayList<>();
            }

            ProgramSuite programSuite = new ProgramSuite();
            programSuite.setProgram(this);
            programSuite.setSuite(suite);
            programSuite.setPosition(this.programSuites.size());
            // check for new trigger messages for new suites.
            if (suite.getNewTriggerSuiteMessages() != null) {
                for (MessageTypeSummary msgType : suite
                        .getNewTriggerSuiteMessages()) {
                    programSuite.addTrigger(msgType);
                }
            }
            this.programSuites.add(programSuite);
        }
    }

    /**
     * @return the programSuites
     */
    public List<ProgramSuite> getProgramSuites() {
        return programSuites;
    }

    /**
     * @param programSuites
     *            the programSuites to set
     */
    public void setProgramSuites(List<ProgramSuite> programSuites) {
        if (programSuites == null) {
            /*
             * Instantiate new list instead of empty list so that items can
             * still be added to the list.
             */
            this.programSuites = new ArrayList<>();
            return;
        }
        this.programSuites = programSuites;

        Iterator<ProgramSuite> psi = programSuites.iterator();
        while (psi.hasNext()) {
            if (psi.next() == null) {
                psi.remove();
            }
        }

        this.updatePositions();

        for (ProgramSuite ps : this.programSuites) {
            ps.setProgram(this);
        }
    }

    public void updatePositions() {
        if (this.programSuites == null) {
            return;
        }

        int index = 0;
        for (ProgramSuite ps : this.programSuites) {
            ps.setPosition(index++);
        }
    }

    public void addProgramSuite(ProgramSuite programSuite) {
        if (this.programSuites == null) {
            this.programSuites = new ArrayList<>();
            programSuite.setPosition(0);
        } else {
            programSuite.setPosition(this.programSuites.get(
                    this.programSuites.size() - 1).getPosition() + 1);
        }

        programSuite.setProgram(this);
        this.programSuites.add(programSuite);
    }

    public Set<TransmitterGroup> getTransmitterGroups() {
        return transmitterGroups;
    }

    public void setTransmitterGroups(Set<TransmitterGroup> transmitterGroups) {
        this.transmitterGroups = transmitterGroups;
    }

    public void addTransmitterGroup(TransmitterGroup group) {
        if (group != null) {
            if (transmitterGroups == null) {
                transmitterGroups = new HashSet<>();
            }

            transmitterGroups.add(group);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Program other = (Program) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public ProgramSuite getProgramSuite(final Suite suite) {
        checkSuiteLookupMap(suite);
        return suiteToProgramSuiteMap.get(suite);
    }

    public ProgramSummary getProgramSummary() {
        return new ProgramSummary(this);
    }
}
