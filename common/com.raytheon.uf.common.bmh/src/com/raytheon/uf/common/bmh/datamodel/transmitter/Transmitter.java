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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;

/**
 * Transmitter information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jun 30, 2014  3283     bsteffen    Add some getter/setters.
 * Jul 17, 2014  3406     mpduff      Added id pk column, named query, removed cascade
 * Aug 04, 2014  3173     mpduff      Added DAC and Fips, changed to use serialization adapter
 * Oct 06, 2014  3649     rferrel     Methods hashCode and equals now use id.
 * Oct 16, 2014  3636     rferrel     Added logEntry.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
// @NamedQueries({
// @NamedQuery(name = "getTransmitters", query = "SELECT t FROM Transmitter"),
// @NamedQuery(name = "getTransmitterByName", query =
// "SELECT t from Transmitter t where t.mnemonic = :mnemonic"),
// @NamedQuery(name = "getTransmitterNames", query =
// "SELECT t.mnemonic FROM Transmitter") })
@Entity
@Table(name = "transmitter", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "mnemonic" }) })
@SequenceGenerator(initialValue = 1, name = Transmitter.GEN, sequenceName = "transmitter_seq")
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = TransmitterAdapter.class)
public class Transmitter {
    static final String GEN = "Transmitter Generator";

    public enum TxMode {
        PRIMARY, SECONDARY
    };

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    protected int id;

    @Column(length = 5)
    private String mnemonic;

    @Column(length = 40, nullable = false)
    private String name;

    @Column(precision = 3, nullable = false)
    private float frequency;

    @Column(length = 10, nullable = false)
    private String callSign;

    @Column(length = 40, nullable = false)
    private String location;

    @Column(length = 40, nullable = false)
    private String serviceArea;

    @Column(length = 9)
    private String fipsCode;

    /**
     * Bi-directional relationship. Always serialized from the group side.
     */
    @ManyToOne(optional = false)
    private TransmitterGroup transmitterGroup;

    @Column
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(length = 8, nullable = false)
    private TxStatus txStatus = TxStatus.ENABLED;

    @Enumerated(EnumType.STRING)
    @Column(length = 9, nullable = false)
    private TxMode txMode = TxMode.PRIMARY;

    @Column
    private Integer dacPort;

    public Transmitter() {

    }

    /**
     * Copy Constructor.
     * 
     * @param t
     */
    public Transmitter(Transmitter t) {
        this.setCallSign(t.getCallSign());
        this.setDacPort(t.getDacPort());
        this.setFipsCode(t.getFipsCode());
        this.setFrequency(t.getFrequency());
        this.setId(t.getId());
        this.setLocation(t.getLocation());
        this.setMnemonic(t.getMnemonic());
        this.setName(t.getName());
        this.setPosition(t.getPosition());
        this.setServiceArea(t.getServiceArea());
        this.setTransmitterGroup(t.getTransmitterGroup());
        this.setTxMode(t.getTxMode());
        this.setTxStatus(t.getTxStatus());
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
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

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        if (callSign != null) {
            this.callSign = callSign;
        } else {
            this.callSign = "";
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location != null) {
            this.location = location;
        } else {
            this.location = "";
        }
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        if (serviceArea != null) {
            this.serviceArea = serviceArea;
        } else {
            this.serviceArea = "";
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public TxStatus getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(TxStatus txStatus) {
        this.txStatus = txStatus;
    }

    public TxMode getTxMode() {
        return txMode;
    }

    public void setTxMode(TxMode txMode) {
        this.txMode = txMode;
    }

    /**
     * @return the fipsCode
     */
    public String getFipsCode() {
        return fipsCode;
    }

    /**
     * @param fipsCode
     *            the fipsCode to set
     */
    public void setFipsCode(String fipsCode) {
        this.fipsCode = fipsCode;
    }

    /**
     * @return the dacPort
     */
    public Integer getDacPort() {
        return dacPort;
    }

    /**
     * @param dacPort
     *            the dacPort to set
     */
    public void setDacPort(Integer dacPort) {
        this.dacPort = dacPort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        if (id == 0) {
            result = prime * result
                    + ((mnemonic == null) ? 0 : mnemonic.hashCode());
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
        Transmitter other = (Transmitter) obj;
        if (id != other.id) {
            return false;
        }
        // Comparing new transmitters not in database.
        if (id == 0) {
            if (mnemonic == null) {
                if (other.mnemonic != null) {
                    return false;
                }
            } else if (!mnemonic.equals(other.mnemonic)) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Transmitter [mnemonic=" + mnemonic + ", name=" + name
                + ", frequency=" + frequency + ", callSign=" + callSign
                + ", location=" + location + ", serviceArea=" + serviceArea
                + ", fipsCode=" + fipsCode + ", transmitterGroup="
                + transmitterGroup + ", position=" + position + ", txStatus="
                + txStatus + ", txMode=" + txMode + ", dacPort=" + dacPort
                + "]";
    }

    /**
     * Get log entry.
     * 
     * @param oldTrans
     *            - When null assume this is a new transmitter.
     * @param user
     *            - Who is making the change
     * @return entry - empty string when no differences.
     */
    public String logEntry(Transmitter oldTrans, String user) {
        boolean logChanges = false;
        StringBuilder sb = new StringBuilder();
        sb.append("User ").append(user);
        if (oldTrans == null) {
            sb.append(" New Transmitter id: ").append(getId()).append("; ")
                    .append(toString());
        } else {
            Object oldValue = null;
            Object newValue = null;
            sb.append(" Update fields Transmitter mnemonic/id: \"")
                    .append(getMnemonic()).append("\"/").append(getId())
                    .append(" [");
            if (!oldTrans.getMnemonic().equals(getMnemonic())) {
                BMHLoggerUtils.logFieldChange(sb, "mnemonic",
                        oldTrans.getMnemonic(), getMnemonic());
                logChanges = true;
            }

            oldValue = oldTrans.getDacPort();
            if (oldValue == null) {
                oldValue = "None";
            }

            newValue = getDacPort();
            if (newValue == null) {
                newValue = "None";
            }
            if (!oldValue.equals(newValue)) {
                BMHLoggerUtils.logFieldChange(sb, "DAC Port", oldValue,
                        newValue);
                logChanges = true;
            }

            if (!oldTrans.getName().equals(getName())) {
                BMHLoggerUtils.logFieldChange(sb, "name", oldTrans.getName(),
                        getName());
                logChanges = true;
            }
            if (oldTrans.getFrequency() != getFrequency()) {
                BMHLoggerUtils.logFieldChange(sb, "frequency", new Float(
                        oldTrans.getFrequency()), new Float(getFrequency()));
                logChanges = true;
            }
            if (!oldTrans.getCallSign().equals(getCallSign())) {
                BMHLoggerUtils.logFieldChange(sb, "callSign",
                        oldTrans.getCallSign(), getCallSign());
                logChanges = true;
            }
            if (!oldTrans.getLocation().equals(getLocation())) {
                BMHLoggerUtils.logFieldChange(sb, "location",
                        oldTrans.getLocation(), getLocation());
                logChanges = true;
            }
            if (!oldTrans.getServiceArea().equals(getServiceArea())) {
                BMHLoggerUtils.logFieldChange(sb, "serviceArea",
                        oldTrans.getServiceArea(), getServiceArea());
                logChanges = true;
            }
            oldValue = oldTrans.getFipsCode();
            if (oldValue == null) {
                oldValue = "";
            }
            newValue = getFipsCode();
            if (newValue == null) {
                newValue = "";
            }
            if (!oldValue.equals(newValue)) {
                BMHLoggerUtils.logFieldChange(sb, "fipsCode", oldValue,
                        newValue);
                logChanges = true;
            }
            if (!oldTrans.getTransmitterGroup().equals(getTransmitterGroup())) {
                BMHLoggerUtils.logFieldChange(sb, "transmitterGroup", oldTrans
                        .getTransmitterGroup().getName(), getTransmitterGroup()
                        .getName());
                logChanges = true;
            }
            oldValue = oldTrans.getTxStatus().name();
            newValue = getTxStatus().name();
            if (!oldValue.equals(newValue)) {
                BMHLoggerUtils.logFieldChange(sb, "txStatus", oldValue,
                        newValue);
                logChanges = true;
            }

            oldValue = oldTrans.getTxMode().name();
            newValue = getTxMode().name();
            if (!oldValue.equals(newValue)) {
                BMHLoggerUtils.logFieldChange(sb, "txMode", oldValue, newValue);
                logChanges = true;
            }

            // No changes made
            if (!logChanges) {
                return "";
            }

            sb.setCharAt(sb.length() - 2, ']');
        }
        return sb.toString();
    }

}
