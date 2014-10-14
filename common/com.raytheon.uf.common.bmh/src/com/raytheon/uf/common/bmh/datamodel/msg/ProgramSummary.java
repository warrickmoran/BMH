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
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Main fields of Program needed in references. Used to minimizing cascade
 * loading.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 13, 2014            rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "program", schema = "bmh")
public class ProgramSummary {

    @DynamicSerializeElement
    protected int id;

    @DynamicSerializeElement
    private String name;

    @Transient
    private final Program parent;

    public ProgramSummary() {
        parent = null;
    }

    /**
     * This summary will provide a veiw of the parent so changes to the parent
     * or the summary will be visible in the other object.
     * 
     * @param parent
     */
    public ProgramSummary(Program parent) {
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

    @Column
    public String getName() {
        return (parent == null ? name : parent.getName());
    }

    public void setName(String name) {
        if (parent == null) {
            this.name = name;
        } else {
            parent.setName(name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ProgramSUmmary [name=" + getName() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int id = getId();
        result = (prime * result) + id;
        if (id == 0) {
            String name = getName();
            result = (prime * result) + ((name == null) ? 0 : name.hashCode());
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
        ProgramSummary other = (ProgramSummary) obj;
        int id = getId();
        if (id != other.getId()) {
            return false;
        } else if (id == 0) {
            // object has not been stored, check afosid
            String myName = getName();
            String otherName = other.getName();
            if (myName == null) {
                if (otherName != null) {
                    return false;
                }
            } else if (!myName.equals(otherName)) {
                return false;
            }
        }

        return true;
    }
}
