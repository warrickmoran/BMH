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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to indicate that a {@link MessageType} is a trigger message type when
 * associated with a certain {@link Program} and {@link Suite}. A
 * {@link ProgramTrigger} will only exist when a {@link MessageType} is actually
 * a trigger; {@link Boolean} values are no longer used to indicate the
 * existence of a trigger.
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
@DynamicSerialize
@Table(name = "program_trigger", schema = "bmh")
@BatchSize(size = 100)
public class ProgramTrigger {
    @EmbeddedId
    @DynamicSerializeElement
    private ProgramTriggerPK id;

    @ManyToOne(optional = false)
    @MapsId("suiteId")
    private Suite suite;

    @ManyToOne(optional = false)
    @MapsId("programId")
    private Program program;

    @ManyToOne(optional = false)
    @MapsId("msgTypeId")
    @DynamicSerializeElement
    private MessageType msgType;

    private void checkId() {
        if (this.id != null) {
            return;
        }
        this.id = new ProgramTriggerPK();
    }

    /**
     * @return the id
     */
    public ProgramTriggerPK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(ProgramTriggerPK id) {
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
        this.id.setSuiteId(this.suite == null ? 0 : this.suite.getId());
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
     * @return the msgType
     */
    public MessageType getMsgType() {
        return msgType;
    }

    /**
     * @param msgType
     *            the msgType to set
     */
    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
        this.checkId();
        this.id.setMsgTypeId(this.msgType == null ? 0 : this.msgType.getId());
    }
}