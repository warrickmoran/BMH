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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * 
 * Contains validation information about an {@link InputMessage}, any message
 * which has been accepted for further processing will have a status of
 * {@value #ACCEPTED}, any other status indicates a failure.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 16, 2014  3283     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Entity
@Table(name = "validated_msg", schema = "bmh")
@SequenceGenerator(initialValue = 1, name = ValidatedMessage.GEN, sequenceName = "validated_msg_seq")
public class ValidatedMessage {

    public static enum TransmissionStatus {
        /** This status must be set for a message to continue processing. */
        ACCEPTED,
        /** Message has expired and will not be transmitted */
        EXPIRED,
        /** The areas for the message cannot be mapped to a transmitter */
        UNPLAYABLE,
        /** The Message type is not in the configuration */
        UNDEFINED,
        /** The Message type is not assigned to any suite. */
        UNASSIGNED,
        /** An identical message has already been received */
        DUPLICATE,
        /** Validation did not complete successfully */
        ERROR;
    }

    public static enum LdadStatus {
        /** This status must be set for a message to continue processing. */
        ACCEPTED,
        /** Validation did not complete successfully */
        ERROR;
    }

    protected static final String GEN = "Validated Messsage Id Generator";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    private int id;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private InputMessage inputMessage;

    @ManyToMany
    @JoinTable(schema = "bmh", name = "validated_msg_transmitter_groups", joinColumns = @JoinColumn(name = "validated_msg_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "transmitter_group_name", referencedColumnName = "name"))
    private Set<TransmitterGroup> transmitterGroups;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransmissionStatus transmissionStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LdadStatus ldadStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InputMessage getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    public Set<TransmitterGroup> getTransmitterGroups() {
        return transmitterGroups;
    }

    public void setTransmitterGroups(Set<TransmitterGroup> transmitterGroups) {
        this.transmitterGroups = transmitterGroups;
    }

    public TransmissionStatus getTransmissionStatus() {
        return transmissionStatus;
    }

    public void setTransmissionStatus(TransmissionStatus transmissionStatus) {
        this.transmissionStatus = transmissionStatus;
    }

    public LdadStatus getLdadStatus() {
        return ldadStatus;
    }

    public void setLdadStatus(LdadStatus ldadStatus) {
        this.ldadStatus = ldadStatus;
    }

    public boolean isAccepted() {
        return LdadStatus.ACCEPTED.equals(ldadStatus)
                || TransmissionStatus.ACCEPTED.equals(transmissionStatus);
    }

    @Override
    public String toString() {
        return "ValidatedMessage [id=" + id + ", transmissionStatus="
                + transmissionStatus + ", ldadStatus=" + ldadStatus + "]";
    }

}
