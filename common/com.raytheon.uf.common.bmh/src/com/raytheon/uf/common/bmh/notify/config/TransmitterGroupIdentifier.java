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
package com.raytheon.uf.common.bmh.notify.config;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * Provides identifying information about a {@link TransmitterGroup}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 21, 2015 4017       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterGroupIdentifier {

    /**
     * The id of the {@link TransmitterGroup}. Used to retrieve an existing
     * {@link TransmitterGroup}.
     */
    @DynamicSerializeElement
    private int id;

    /**
     * The name of the {@link TransmitterGroup}. Used to reference a
     * {@link TransmitterGroup} that no longer exists.
     */
    @DynamicSerializeElement
    private String name;

    /**
     * Constructor.
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public TransmitterGroupIdentifier() {
    }

    /**
     * Constructor. Use this constructor.
     * 
     * @param id
     *            the id of a {@link TransmitterGroup}.
     * @param name
     *            the name of a {@link TransmitterGroup}.
     */
    public TransmitterGroupIdentifier(int id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid transmitter group id specified - does not correspond to a record; id must be > 0.");
        }
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TransmitterGroupIdentifier [id=");
        sb.append(this.id).append(", name=").append(this.name).append("]");

        return sb.toString();
    }
}