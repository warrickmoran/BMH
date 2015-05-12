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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Main fields of message type needed in cascade operations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 10, 2014            rjpeter     Initial creation
 * Oct 23, 2014  #3728     lvenable    Added Enumerated tag for designation
 * Oct 29, 2014  #3636     rferrel     Implement logging.
 * May 12, 2015  4248      rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "msg_type")
public class MessageTypeSummary {
    @DynamicSerializeElement
    protected int id;

    @DynamicSerializeElement
    @DiffString
    private String afosid;

    @DynamicSerializeElement
    private String title;

    @DynamicSerializeElement
    private Designation designation;

    @Transient
    private final MessageType parent;

    public MessageTypeSummary() {
        this.parent = null;
    }

    /**
     * This summary will provide a veiw of the parent so changes to the parent
     * or the summary will be visible in the other object.
     * 
     * @param parent
     */
    public MessageTypeSummary(MessageType parent) {
        this.parent = parent;
    }

    @Id
    public int getId() {
        return (parent == null ? id : parent.getId());
    }

    public void setId(int id) {
        if (parent == null) {
            this.id = id;
        } else {
            parent.setId(id);
        }
    }

    @Column(length = 9, unique = true)
    public String getAfosid() {
        return (parent == null ? afosid : parent.getAfosid());
    }

    public void setAfosid(String afosid) {
        if (parent == null) {
            this.afosid = afosid;
        } else {
            parent.setAfosid(afosid);
        }
    }

    @Column(length = 40, nullable = false)
    public String getTitle() {
        return (parent == null ? title : parent.getTitle());
    }

    public void setTitle(String title) {
        if (parent == null) {
            this.title = title;
        } else {
            parent.setTitle(title);
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Designation getDesignation() {
        return (parent == null ? designation : parent.getDesignation());
    }

    public void setDesignation(Designation designation) {
        if (parent == null) {
            this.designation = designation;
        } else {
            parent.setDesignation(designation);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MessageTypeSummary [afosid=" + getAfosid() + ", title="
                + getTitle() + ", designation=" + getDesignation() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int id = getId();
        result = (prime * result) + id;
        if (id == 0) {
            String afosid = getAfosid();
            result = (prime * result)
                    + ((afosid == null) ? 0 : afosid.hashCode());
        }
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
        MessageTypeSummary other = (MessageTypeSummary) obj;
        int id = getId();
        if (id != other.getId()) {
            return false;
        } else if (id == 0) {
            // object has not been stored, check afosid
            String myAfosid = getAfosid();
            String otherAfosid = other.getAfosid();
            if (myAfosid == null) {
                if (otherAfosid != null) {
                    return false;
                }
            } else if (!myAfosid.equals(otherAfosid)) {
                return false;
            }
        }
        return true;
    }

}
