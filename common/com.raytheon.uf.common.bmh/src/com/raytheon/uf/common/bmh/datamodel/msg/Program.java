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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Program.GET_PROGRAM_FOR_TRANSMITTER_GROUP, query = Program.GET_PROGRAMS_FOR_TRANSMITTER_GROUP_QUERY),
        @NamedQuery(name = Program.GET_GROUPS_FOR_MSG_TYPE, query = Program.GET_GROUPS_FOR_MSG_TYPE_QUERY),
        @NamedQuery(name = Program.GET_PROGRAM_NAMES_IDS, query = Program.GET_PROGRAM_NAMES_IDS_QUERY) })
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

    public static final String GET_GROUPS_FOR_MSG_TYPE = "getGroupsForMsgType";

    protected static final String GET_GROUPS_FOR_MSG_TYPE_QUERY = "SELECT tg FROM Program p inner join p.transmitterGroups tg inner join p.suites s inner join s.suiteMessages sm inner join sm.msgType mt WHERE mt.afosid = :afosid";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    @Column(length = 20, unique = true, nullable = false)
    @DynamicSerializeElement
    private String name;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "program_suites", schema = "bmh", joinColumns = { @JoinColumn(name = "program_id") }, inverseJoinColumns = { @JoinColumn(name = "suite_id") })
    @OrderColumn(name = "position", nullable = false)
    @DynamicSerializeElement
    private List<Suite> suites;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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

    public List<Suite> getSuites() {
        return suites;
    }

    public void setSuites(List<Suite> suites) {
        this.suites = suites;
    }

    public void addSuite(Suite suite) {
        if (suite != null) {
            if (suites == null) {
                suites = new ArrayList<>();
            }

            suites.add(suite);
        }
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

}
