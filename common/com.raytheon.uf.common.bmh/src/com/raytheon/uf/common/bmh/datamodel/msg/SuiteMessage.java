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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Join object between a Suite and a MessageType. Also contains whether the
 * message type is a trigger for the suite.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "suite_message", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "suiteName", "suitePosition" }) })
public class SuiteMessage {

    @EmbeddedId
    private SuiteMessagePK id;

    @Column(nullable = false)
    private boolean trigger;

    // position within the suite
    @Column(nullable = false, insertable = false, updatable = false)
    private int suitePosition;

    @ManyToOne
    @JoinColumn(name = "suiteName", insertable = false, updatable = false)
    private Suite suite;

    // @ManyToOne
    // @JoinColumn(name = "afosid", insertable = false, updatable = false)
    // private MessageType msgType;

    public SuiteMessagePK getId() {
        return id;
    }

    public void setId(SuiteMessagePK id) {
        this.id = id;
    }

    public boolean isTrigger() {
        return trigger;
    }

    public void setTrigger(boolean trigger) {
        this.trigger = trigger;
    }

    public int getPosition() {
        return suitePosition;
    }

    public void setPosition(int position) {
        this.suitePosition = position;
    }

    public Suite getSuite() {
        return suite;
    }

    public void setSuite(Suite suite) {
        this.suite = suite;
    }
}
