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
import java.util.List;

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
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = Suite.GET_SUITE_NAMES_CATS_IDS, query = Suite.GET_SUITE_NAMES_CATS_IDS_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "suite", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = Suite.GEN, sequenceName = "suite_seq")
public class Suite {
    public enum SuiteType {
        GENERAL, HIGH, EXCLUSIVE, INTERRUPT;
    }

    public static final String GET_SUITE_NAMES_CATS_IDS = "getSuiteNamesCatsIDs";

    protected static final String GET_SUITE_NAMES_CATS_IDS_QUERY = "select name, type, id FROM Suite s";

    static final String GEN = "Suite Id Generator";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    @Column(length = 20, unique = true, nullable = false)
    @DynamicSerializeElement
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 9, nullable = false)
    @DynamicSerializeElement
    private SuiteType type = SuiteType.GENERAL;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "suite", fetch = FetchType.EAGER)
    // updating position broken https://hibernate.atlassian.net/browse/HHH-5732
    @OrderColumn(name = "position", nullable = false)
    @DynamicSerializeElement
    private List<SuiteMessage> suiteMessages;

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

    public List<SuiteMessage> getSuiteMessages() {
        return suiteMessages;
    }

    public void setSuiteMessages(List<SuiteMessage> suiteMessages) {
        this.suiteMessages = suiteMessages;
        updatePositions();
        // handle bi-directional mapping
        if (suiteMessages != null) {
            for (SuiteMessage sm : suiteMessages) {
                sm.setSuite(this);
            }
        }
    }

    public void addSuiteMessage(SuiteMessage suiteMessage) {
        if (suiteMessage != null) {
            if (suiteMessages == null) {
                suiteMessages = new ArrayList<>();
                suiteMessage.setPosition(0);
            } else {
                // Work around for
                // https://hibernate.atlassian.net/browse/HHH-5732
                suiteMessage.setPosition(suiteMessages.get(
                        suiteMessages.size() - 1).getPosition() + 1);
            }

            suiteMessages.add(suiteMessage);
            suiteMessage.setSuite(this);
        }
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

}
