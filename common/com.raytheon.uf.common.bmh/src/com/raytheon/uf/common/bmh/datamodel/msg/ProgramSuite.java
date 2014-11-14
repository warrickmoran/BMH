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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Manifestation of the relation between {@link Program} and {@link Suite}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 11, 2014 3587       bkowal      Initial creation
 * Sep 29, 2014 3589       dgilling    Add forced field.
 * Oct 08, 2014 3687       bsteffen    Remove ProgramTrigger.
 * Oct 13, 2014 3654       rjpeter     Updated to use MessageTypeSummary.
 * Oct 15, 2014 3715       bkowal      Prevent potential Null Pointer Exception for triggers.
 * Oct 21, 2014 3746       rjpeter     Hibernate upgrade.
 * Oct 28, 2014 3636       rferrel     Implement Logging
 * Nov 13, 2014 3717       bsteffen    Remove forced field.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Entity
@Table(name = "program_suite", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "program_id", "suite_id", "position" }) })
@DynamicSerialize
public class ProgramSuite implements Serializable {
    private static final long serialVersionUID = -4911273921891786116L;

    @EmbeddedId
    @DynamicSerializeElement
    @DiffTitle(position = 3)
    private ProgramSuitePK id;

    @ManyToOne(optional = false)
    @MapsId("programId")
    @ForeignKey(name = "program_suite_to_program")
    // No dynamic serialize due to bi-directional relationship
    @DiffTitle(position = 2)
    private Program program;

    @ManyToOne(optional = false)
    @MapsId("suiteId")
    @ForeignKey(name = "program_suite_to_suite")
    @DynamicSerializeElement
    @DiffString
    @DiffTitle(position = 1)
    private Suite suite;

    @DynamicSerializeElement
    private int position;

    @ManyToMany(fetch = FetchType.EAGER)
    @DynamicSerializeElement
    @JoinTable(schema = "bmh", name = "program_trigger", joinColumns = {
            @JoinColumn(name = "program_id", referencedColumnName = "program_id"),
            @JoinColumn(name = "suite_id", referencedColumnName = "suite_id") }, inverseJoinColumns = @JoinColumn(name = "msgtype_id", referencedColumnName = "id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "program_id", "suite_id", "msgtype_id" }))
    @Fetch(FetchMode.SUBSELECT)
    @ForeignKey(name = "program_trigger_to_program_suite", inverseName = "program_trigger_to_message_type")
    private Set<MessageTypeSummary> triggers;

    private void checkId() {
        if (this.id != null) {
            return;
        }
        this.id = new ProgramSuitePK();
    }

    public void clearTriggers() {
        if (this.triggers == null) {
            return;
        }
        this.triggers.clear();
    }

    public void addTrigger(MessageTypeSummary trigger) {
        if (this.triggers == null) {
            this.triggers = new HashSet<>();
        }
        this.triggers.add(trigger);
    }

    public void removeTrigger(MessageTypeSummary trigger) {
        this.triggers.remove(trigger);
    }

    public boolean isTrigger(MessageTypeSummary msgType) {
        if ((this.triggers == null) || this.triggers.isEmpty()) {
            return false;
        }
        return this.triggers.contains(msgType);
    }

    public boolean isTrigger(String afosid) {
        if ((this.triggers != null) && this.triggers.isEmpty() == false) {
        for (MessageTypeSummary trigger : this.triggers) {
            if (trigger.getAfosid().equals(afosid)) {
                return true;
            }
        }
        }
        return false;

    }

    public boolean triggersExist() {
        return (this.triggers != null) && (this.triggers.isEmpty() == false);
    }

    /**
     * @return the id
     */
    public ProgramSuitePK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(ProgramSuitePK id) {
        this.id = id;
    }

    /**
     * @return the suite
     */
    public Suite getSuite() {
        return suite;
    }

    /**
     * @param suite
     *            the suite to set
     */
    public void setSuite(Suite suite) {
        this.suite = suite;
        this.checkId();
        this.id.setSuiteId(suite.getId());
    }

    /**
     * @return the program
     */
    public Program getProgram() {
        return program;
    }

    /**
     * @param program
     *            the program to set
     */
    public void setProgram(Program program) {
        this.program = program;
        this.checkId();
        this.id.setProgramId(this.program == null ? 0 : this.program.getId());
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the triggers
     */
    public Set<MessageTypeSummary> getTriggers() {
        return triggers;
    }

    /**
     * @param triggers
     *            the triggers to set
     */
    public void setTriggers(Set<MessageTypeSummary> triggers) {
        this.triggers = triggers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProgramSuite other = (ProgramSuite) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}