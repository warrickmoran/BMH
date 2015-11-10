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
package com.raytheon.uf.common.bmh.datamodel.dac;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Entity representing a configured dac broadcast channel.
 * 
 * The {@link #port} is unique because a port should only be used by one channel
 * on one dac to prevent broadcast crossover. We have further made port unique
 * across all dacs to ensure that our port setting convention is followed in
 * which the port number increased by 1000 for each additional dac.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 3, 2015  5092       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Entity
@Table(name = "dac_channel", uniqueConstraints = @UniqueConstraint(name = "uk_port", columnNames = { "port" }))
@DynamicSerialize
public class DacChannel {

    public static final double DEFAULT_LEVEL = 6.0;

    @DynamicSerializeElement
    @EmbeddedId
    private DacChannelPK id;

    @ManyToOne(optional = false)
    @MapsId("dac_id")
    @ForeignKey(name = "fk_dac_channel_to_dac")
    // No dynamic serialize due to bi-directional relationship
    private Dac dac;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int port;

    @Column(nullable = false, columnDefinition = "Decimal (3,1)")
    @DynamicSerializeElement
    private double level = DEFAULT_LEVEL;

    public DacChannel() {
    }

    public DacChannel(int port) {
        this.port = port;
    }

    private void checkId() {
        if (id == null) {
            id = new DacChannelPK();
        }
    }

    /**
     * @return the id
     */
    public DacChannelPK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(DacChannelPK id) {
        this.id = id;
    }

    /**
     * @return the dac
     */
    public Dac getDac() {
        return dac;
    }

    /**
     * @param dac
     *            the dac to set
     */
    public void setDac(Dac dac) {
        this.dac = dac;
        this.checkId();
        this.id.setDac_id(dac.getId());
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the level
     */
    public double getLevel() {
        return level;
    }

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(double level) {
        if (level < -16.0 || level > 6.0) {
            throw new IllegalArgumentException(
                    "The specified level must be in the range: -16.0 to 6.0!");
        }

        this.level = level;
    }
}