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
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

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
 * May 30, 2014 3175       rjpeter     Initial creation.
 * Aug 05, 2014 3175       rjpeter     Fixed mapping.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "suite_message", schema = "bmh")
public class SuiteMessage {
    @EmbeddedId
    @DynamicSerializeElement
    private SuiteMessagePk id;

    @ManyToOne(optional = false)
    @MapsId("suiteId")
    // No dynamic serialize due to bi-directional relationship
    private Suite suite;

    @ManyToOne(optional = false)
    @MapsId("msgTypeId")
    @DynamicSerializeElement
    private MessageType msgType;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean trigger;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int position;

    public SuiteMessagePk getId() {
        return id;
    }

    public void setId(SuiteMessagePk id) {
        this.id = id;
    }

    public boolean isTrigger() {
        return trigger;
    }

    public void setTrigger(boolean trigger) {
        this.trigger = trigger;
    }

    public Suite getSuite() {
        return suite;
    }

    public void setSuite(Suite suite) {
        this.suite = suite;
        if (id == null) {
            id = new SuiteMessagePk();
        }

        id.setSuiteId(suite != null ? suite.getId() : 0);
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
        if (id == null) {
            id = new SuiteMessagePk();
        }

        id.setMsgTypeId(msgType != null ? msgType.getId() : 0);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Convenience method to get the afosId of the msgType associated with this
     * suiteMessage.
     * 
     * @return
     */
    public String getAfosid() {
        if (msgType != null) {
            return msgType.getAfosid();
        }

        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((msgType == null) ? 0 : msgType.hashCode());
        result = (prime * result) + ((suite == null) ? 0 : suite.hashCode());
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
        SuiteMessage other = (SuiteMessage) obj;
        if (msgType == null) {
            if (other.msgType != null) {
                return false;
            }
        } else if (!msgType.equals(other.msgType)) {
            return false;
        }
        if (suite == null) {
            if (other.suite != null) {
                return false;
            }
        } else if (!suite.equals(other.suite)) {
            return false;
        }
        return true;
    }

}
