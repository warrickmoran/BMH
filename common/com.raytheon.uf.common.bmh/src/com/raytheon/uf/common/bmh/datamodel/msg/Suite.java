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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.bmh.datamodel.PositionUtil;
import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Suite object. Contains the priority of the suite and the list of message
 * types that belong to the suite.
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
 * Aug 05, 2014  3175     rjpeter     Fixed Suite mapping.
 * Aug 06, 2014 #3490     lvenable    Updated to add name/query.
 * Aug 12, 2014 #3490     lvenable    Updated to add name/query for getting 
 *                                    message types.
 * Aug 17, 2014 #3490     lvenable    Added batch size, fixed issue in setSuiteMessages().
 * Aug 21, 2014 #3490     lvenable    Remove cascade all.
 * Sep 18, 2014 #3587     bkowal      Added a transient to track messages types associated
 *                                    with triggers that are no longer associated with the suite.
 * Oct 13, 2014 3654      rjpeter     Updated to use MessageTypeSummary.
 * Oct 15, 2014 3715      bkowal      Supporting adding program triggers to completely
 *                                    new {@link Suite}(s).
 * Oct 21, 2014 3746      rjpeter     Hibernate upgrade.
 * Oct 29, 2014 3636      rferrel     Implement Logging.
 * Nov 13, 2014 3717      bsteffen    Add containsSuiteMessage
 * Dec 07, 2014 3752      mpduff      Add getSuiteByName
 * Apr 02, 2015 4248      rjpeter     Made suiteMessages database relation a set, added ordered return methods.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Suite.GET_SUITE_NAMES_CATS_IDS, query = Suite.GET_SUITE_NAMES_CATS_IDS_QUERY),
        @NamedQuery(name = Suite.GET_SUITE_MSG_TYPES, query = Suite.GET_SUITE_MSG_TYPES_QUERY),
        @NamedQuery(name = Suite.GET_SUITE_BY_NAME, query = Suite.GET_SUITE_BY_NAME_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "suite", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = Suite.GEN, sequenceName = "suite_seq")
@BatchSize(size = 100)
public class Suite {
    public enum SuiteType {
        GENERAL, HIGH, EXCLUSIVE, INTERRUPT;
    }

    public static final String GET_SUITE_NAMES_CATS_IDS = "getSuiteNamesCatsIDs";

    protected static final String GET_SUITE_NAMES_CATS_IDS_QUERY = "select name, type, id FROM Suite s";

    public static final String GET_SUITE_MSG_TYPES = "getSuiteMessageTypes";

    protected static final String GET_SUITE_MSG_TYPES_QUERY = "select s.id, s.name, s.type, mt.afosid FROM Suite s inner join s.suiteMessages sm inner join sm.msgTypeSummary mt";

    public static final String GET_SUITE_BY_NAME = "getSuiteByName";

    protected static final String GET_SUITE_BY_NAME_QUERY = "FROM Suite s WHERE s.name = :name";

    static final String GEN = "Suite Id Generator";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    @DiffTitle(position = 2)
    protected int id;

    @Column(length = 40, unique = true, nullable = false)
    @DynamicSerializeElement
    @DiffString
    @DiffTitle(position = 1)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 9, nullable = false)
    @DynamicSerializeElement
    private SuiteType type = SuiteType.GENERAL;

    @OneToMany(mappedBy = "suite", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @DynamicSerializeElement
    @Fetch(FetchMode.SUBSELECT)
    private Set<SuiteMessage> suiteMessages;

    @Transient
    @DynamicSerializeElement
    /*
     * id values associated with suite messages that are linked to triggers that
     * have been removed from the suite. Special case due to the hibernate
     * orphanRemoval bug.
     */
    private List<SuiteMessagePk> removedTriggerSuiteMessages;

    @Transient
    /*
     * Trigger messages that were added to a completely new suite. This field is
     * only populated and used within Viz. The reason for creating this field
     * was so that the triggers would be able to cross the {@link Suite} to
     * {@link Program} boundary as objects are passed from dialog to dialog.
     */
    private List<MessageTypeSummary> newTriggerSuiteMessages;

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

    public SuiteType getType() {
        return type;
    }

    public void setType(SuiteType type) {
        this.type = type;
    }

    public List<SuiteMessage> getOrderedSuiteMessages() {
        return PositionUtil.order(this.suiteMessages);
    }

    public void setOrderedSuiteMessages(List<SuiteMessage> suiteMessages) {
        if (suiteMessages == null) {
            this.suiteMessages = null;
            return;
        }

        PositionUtil.updatePositions(suiteMessages);
        setSuiteMessages(new HashSet<>(suiteMessages));
    }

    public Set<SuiteMessage> getSuiteMessages() {
        return suiteMessages;
    }

    public void setSuiteMessages(Set<SuiteMessage> suiteMessages) {
        this.suiteMessages = suiteMessages;

        if (this.suiteMessages != null) {
            for (SuiteMessage sm : this.suiteMessages) {
                sm.setSuite(this);
            }
        }
    }

    public void addSuiteMessage(SuiteMessage suiteMessage) {
        if (suiteMessage != null) {
            if (suiteMessages == null) {
                suiteMessages = new HashSet<>();
            }

            PositionUtil.updatePositions(suiteMessages);
            suiteMessage.setPosition(suiteMessages.size());
            suiteMessage.setSuite(this);
            suiteMessages.add(suiteMessage);
        }
    }

    public boolean containsSuiteMessage(String afosid) {
        if (suiteMessages != null) {
            for (SuiteMessage suiteMessage : suiteMessages) {
                if (suiteMessage.getAfosid().equals(afosid)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the removedTriggerSuiteMessages
     */
    public List<SuiteMessagePk> getRemovedTriggerSuiteMessages() {
        return removedTriggerSuiteMessages;
    }

    /**
     * @param removedTriggerSuiteMessages
     *            the removedTriggerSuiteMessages to set
     */
    public void setRemovedTriggerSuiteMessages(
            List<SuiteMessagePk> removedTriggerSuiteMessages) {
        this.removedTriggerSuiteMessages = removedTriggerSuiteMessages;
    }

    public List<MessageTypeSummary> getNewTriggerSuiteMessages() {
        return newTriggerSuiteMessages;
    }

    public void setNewTriggerSuiteMessages(
            List<MessageTypeSummary> newTriggerSuiteMessages) {
        this.newTriggerSuiteMessages = newTriggerSuiteMessages;
    }

    /**
     * Manually sets the position filed in the SuiteMessage. Work around for
     * https://hibernate.atlassian.net/browse/HHH-5732
     */
    public void updatePositions() {
        if (suiteMessages != null) {
            int index = 0;
            for (SuiteMessage sm : suiteMessages) {
                sm.setPosition(index++);
            }
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
        Suite other = (Suite) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Suite [id=").append(id).append(", name=").append(name)
                .append(", type=").append(type).append(", suiteMessages=");
        if (suiteMessages == null) {
            sb.append(suiteMessages);
        } else {
            sb.append("[");
            if (suiteMessages.size() > 0) {
                for (SuiteMessage suiteMessage : suiteMessages) {
                    sb.append("[").append(suiteMessage.getAfosid())
                            .append("], ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append("]");
        }

        return sb.toString();
    }

}
