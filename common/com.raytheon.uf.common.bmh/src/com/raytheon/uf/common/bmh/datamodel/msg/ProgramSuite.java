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
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

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
    private ProgramSuitePK id;

    @ManyToOne(optional = false)
    @MapsId("programId")
    // No dynamic serialize due to bi-directional relationship
    private Program program;

    @ManyToOne(optional = false)
    @MapsId("suiteId")
    @DynamicSerializeElement
    private Suite suite;

    @DynamicSerializeElement
    private int position;

    @OneToMany(fetch = FetchType.EAGER)
    @DynamicSerializeElement
    @JoinColumns({
            @JoinColumn(name = "program_id", referencedColumnName = "program_id"),
            @JoinColumn(name = "suite_id", referencedColumnName = "suite_id") })
    private Set<ProgramTrigger> triggers;

    @Transient
    private List<MessageType> triggerMsgTypes;

    private void checkId() {
        if (this.id != null) {
            return;
        }
        this.id = new ProgramSuitePK();
    }

    public void clearTriggers() {
        this.triggers.clear();
        this.triggerMsgTypes.clear();
    }

    public void addTrigger(ProgramTrigger trigger) {
        if (this.triggers == null) {
            this.triggers = new HashSet<>();
            this.triggerMsgTypes = new ArrayList<>();
        }
        trigger.setProgram(this.program);
        trigger.setSuite(this.suite);
        this.triggers.add(trigger);
        this.triggerMsgTypes.add(trigger.getMsgType());
    }

    public void removeTrigger(MessageType msgType) {
        ProgramTrigger trigger = new ProgramTrigger();
        trigger.setProgram(this.program);
        trigger.setSuite(this.suite);
        trigger.setMsgType(msgType);

        this.triggers.remove(trigger);
        this.triggerMsgTypes.remove(msgType);
    }

    public boolean isTrigger(MessageType msgType) {
        if (this.triggers == null || this.triggers.isEmpty()) {
            return false;
        }
        if (this.triggerMsgTypes == null) {
            /*
             * If the record came directly from Hibernate, the transient list
             * would never have been initialized as part of a set method.
             */
            this.triggerMsgTypes = new ArrayList<>(this.triggers.size());
            for (ProgramTrigger trigger : this.triggers) {
                this.triggerMsgTypes.add(trigger.getMsgType());
            }
        }
        return this.triggerMsgTypes.contains(msgType);
    }

    public boolean triggersExist() {
        return this.triggers != null && this.triggers.isEmpty() == false;
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
    public Set<ProgramTrigger> getTriggers() {
        return triggers;
    }

    /**
     * @param triggers
     *            the triggers to set
     */
    public void setTriggers(Set<ProgramTrigger> triggers) {
        this.triggers = triggers;
        this.triggerMsgTypes = new ArrayList<>(triggers.size());
        for (ProgramTrigger trigger : this.triggers) {
            trigger.setProgram(this.program);
            trigger.setSuite(this.suite);
            this.triggerMsgTypes.add(trigger.getMsgType());
        }
    }
}